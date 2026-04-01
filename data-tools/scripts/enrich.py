#!/usr/bin/env python3
"""
Enrich all author catalog files with:
  - Cover image candidates from Open Library (preferred edition closest to known year)
  - Word count estimates from page count where the field is missing
  - Goodreads ratings scraped from book pages (goodreads_rating, goodreads_ratings_count)

Files enriched (all in-place):
  data-tools/enriched/king_catalog.json   (Stephen King, produced by parse.py)
  data-tools/raw/*-source.json            (other authors: Malerman, Hill, Hendrix, …)

After enrichment, rebuild_catalog.py is called to regenerate the combined
app seed file at app/app/src/main/assets/king_catalog.json.

Idempotent: entries whose cover_local_path already points to an existing file
are skipped. Safe to rerun.

Outputs per run:
  data-tools/enriched/covers/                  downloaded images (all authors)
  data-tools/enriched/all_catalog_review.csv   review flags across all authors
"""

import csv
import hashlib
import json
import re
import subprocess
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

ROOT         = Path(__file__).parent.parent       # data-tools/
PROJECT_ROOT = ROOT.parent                         # king-catalog/
ENRICHED     = ROOT / "enriched"
RAW          = ROOT / "raw"
COVERS_DIR   = ENRICHED / "covers"
CSV_PATH     = ENRICHED / "all_catalog_review.csv"

# Stephen King source (Stage 1 output from parse.py)
SK_JSON = ENRICHED / "king_catalog.json"

# Pattern for other-author raw source files
OTHER_AUTHOR_GLOB = "*-source.json"

OL_SEARCH = "https://openlibrary.org/search.json"
OL_COVER  = "https://covers.openlibrary.org/b/id/{cover_id}-L.jpg"

WORDS_PER_PAGE = 275   # conservative estimate for page-count → word-count fills
REQUEST_DELAY  = 0.5   # seconds between Open Library API calls (be polite)

# Known OL placeholder image hashes (returned when no cover is available)
# These are the "no cover" placeholder files served by Open Library.
OL_PLACEHOLDER_HASHES: set[str] = {
    "04b41f5dc246f9ed8bf27ade4999603a",  # common OL "no cover" placeholder
}

CSV_FIELDS = [
    "id", "author", "title", "year", "word_count", "audible_minutes",
    "story_type", "collection", "goodreads_rating", "goodreads_ratings_count",
    "review_flags",
]


# ---------------------------------------------------------------------------
# Open Library helpers
# ---------------------------------------------------------------------------

def ol_search(title: str, year: int | None, author: str, use_author_filter: bool = True) -> dict | None:
    """
    Search Open Library for a title by a given author.
    Returns the best-matching document dict or None.
    """
    params_dict = {
        "title": title,
        "fields": "key,title,first_publish_year,cover_i,number_of_pages_median",
        "limit": 5,
    }
    if use_author_filter:
        params_dict["author"] = author.lower()
    params = urllib.parse.urlencode(params_dict)
    try:
        with urllib.request.urlopen(f"{OL_SEARCH}?{params}", timeout=12) as resp:
            data = json.loads(resp.read().decode("utf-8"))
    except Exception:
        return None

    docs = data.get("docs") or []
    if not docs:
        return None
    if len(docs) == 1 or year is None:
        return docs[0]

    # Prefer the doc whose first_publish_year is closest to the known year
    def year_dist(doc: dict) -> int:
        y = doc.get("first_publish_year")
        return abs(y - year) if isinstance(y, int) else 9999

    return min(docs, key=year_dist)


def download_cover(cover_id: int, dest: Path) -> bool:
    """
    Download a cover image to dest.
    Returns True on success.
    Rejects files under 10 000 bytes or matching known OL placeholder hashes.
    """
    url = OL_COVER.format(cover_id=cover_id)
    try:
        with urllib.request.urlopen(url, timeout=15) as resp:
            data = resp.read()
    except Exception:
        return False
    if len(data) < 10_000:
        return False
    if hashlib.md5(data).hexdigest() in OL_PLACEHOLDER_HASHES:
        return False
    dest.write_bytes(data)
    return True


def safe_filename(title: str) -> str:
    cleaned = "".join(c if c.isalnum() or c in " -_" else "_" for c in title)
    return cleaned.strip().replace(" ", "_")[:80] + ".jpg"


# ---------------------------------------------------------------------------
# Entry helpers
# ---------------------------------------------------------------------------

def cover_already_done(entry: dict) -> bool:
    """True if cover_local_path is set and the file exists on disk."""
    lp = entry.get("cover_local_path")
    if not lp:
        return False
    return (PROJECT_ROOT / lp).exists()


def add_flag(entry: dict, flag: str) -> None:
    flags: list = entry.setdefault("review_flags", [])
    if flag not in flags:
        flags.append(flag)


def remove_flag(entry: dict, flag: str) -> None:
    entry["review_flags"] = [f for f in (entry.get("review_flags") or []) if f != flag]


def recalculate_review_status(entry: dict) -> None:
    flags = entry.get("review_flags") or []
    entry["review_status"] = "needs_review" if flags else "ok"


# ---------------------------------------------------------------------------
# Goodreads scraping
# ---------------------------------------------------------------------------

GR_REQUEST_DELAY = 2.5  # seconds between Goodreads requests; be conservative


def _gr_headers() -> dict:
    return {
        "User-Agent": (
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        ),
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
        "Accept-Language": "en-US,en;q=0.9",
        "Cache-Control": "no-cache",
        "Pragma": "no-cache",
        "Sec-Fetch-Dest": "document",
        "Sec-Fetch-Mode": "navigate",
        "Sec-Fetch-Site": "none",
        "Upgrade-Insecure-Requests": "1",
    }


def _fetch_url(url: str, timeout: int = 20) -> str | None:
    """Fetch a URL with browser-like headers. Handles gzip transparently."""
    import gzip as _gzip
    req = urllib.request.Request(url, headers=_gr_headers())
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            data = resp.read()
            encoding = resp.info().get("Content-Encoding", "")
            if encoding == "gzip":
                data = _gzip.decompress(data)
            return data.decode("utf-8", errors="replace")
    except Exception:
        return None


def _parse_gr_rating(html: str) -> tuple[float | None, int | None]:
    """
    Parse Goodreads rating + count from a book page.
    Tries JSON-LD (most reliable), then HTML microdata, then JSON blobs.
    Returns (rating, count) or (None, None).
    """
    def _valid_rating(v: float) -> bool:
        return 1.0 <= v <= 5.0

    # 1. JSON-LD blocks (GR includes these for SEO)
    for m in re.finditer(
        r'<script[^>]+type=["\']application/ld\+json["\'][^>]*>(.*?)</script>',
        html, re.DOTALL | re.IGNORECASE,
    ):
        try:
            obj = json.loads(m.group(1))
            # May be a single object or a list
            items: list = obj if isinstance(obj, list) else [obj]
            # Also unwrap @graph containers
            expanded: list = []
            for item in items:
                if "@graph" in item:
                    g = item["@graph"]
                    expanded.extend(g if isinstance(g, list) else [g])
                else:
                    expanded.append(item)
            for item in expanded:
                ar = item.get("aggregateRating") or {}
                rv = ar.get("ratingValue")
                rc = ar.get("ratingCount")
                if rv is not None:
                    try:
                        r = float(rv)
                        if _valid_rating(r):
                            return r, (int(float(rc)) if rc is not None else None)
                    except (ValueError, TypeError):
                        pass
        except Exception:
            continue

    # 2. HTML microdata: itemprop="ratingValue" (various attribute orderings)
    for pattern in [
        r'itemprop=["\']ratingValue["\'][^>]*content=["\']([0-9.]+)["\']',
        r'content=["\']([0-9.]+)["\'][^>]*itemprop=["\']ratingValue["\']',
        r'itemprop=["\']ratingValue["\'][^>]*>\s*([0-9.]+)',
    ]:
        m = re.search(pattern, html)
        if m:
            try:
                r = float(m.group(1))
                if _valid_rating(r):
                    count = None
                    for cp in [
                        r'itemprop=["\']ratingCount["\'][^>]*content=["\']([0-9,]+)["\']',
                        r'content=["\']([0-9,]+)["\'][^>]*itemprop=["\']ratingCount["\']',
                        r'itemprop=["\']ratingCount["\'][^>]*>\s*([0-9,]+)',
                    ]:
                        m2 = re.search(cp, html)
                        if m2:
                            count = int(m2.group(1).replace(",", ""))
                            break
                    return r, count
            except (ValueError, TypeError):
                pass

    # 3. JSON blobs embedded in page scripts
    m = re.search(r'"ratingValue"\s*:\s*"?([0-9.]+)"?', html)
    if m:
        try:
            r = float(m.group(1))
            if _valid_rating(r):
                m2 = re.search(r'"(?:ratingsCount|ratingCount)"\s*:\s*([0-9]+)', html)
                return r, (int(m2.group(1)) if m2 else None)
        except (ValueError, TypeError):
            pass

    # 4. Plain text pattern: "X.XX avg rating · N,NNN ratings"
    m = re.search(r'([0-9]\.[0-9]+)\s+avg\s+rating\s*[·\-]\s*([\d,]+)\s+ratings', html)
    if m:
        try:
            r = float(m.group(1))
            if _valid_rating(r):
                return r, int(m.group(2).replace(",", ""))
        except (ValueError, TypeError):
            pass

    return None, None


def _extract_book_urls(html: str) -> list[str]:
    """
    Extract candidate book-page URLs from a Goodreads search-results page.
    Returns up to 5 unique URLs in result order.
    """
    seen: set[str] = set()
    results: list[str] = []
    patterns = [
        r'href=["\']?(/book/show/[0-9]+[^"\'?\s#]*)',
        r'href=["\']?https?://(?:www\.)?goodreads\.com(/book/show/[0-9]+[^"\'?\s#]*)',
    ]
    for pat in patterns:
        for m in re.finditer(pat, html):
            path = m.group(1)
            url = "https://www.goodreads.com" + path.split("?")[0]
            if url not in seen:
                seen.add(url)
                results.append(url)
        if results:
            break
    return results[:5]


def _normalize_for_match(s: str) -> str:
    """Lowercase, strip punctuation/parens/articles for loose title matching."""
    s = re.sub(r"\s*\([^)]*\)", "", s)           # remove parentheticals
    s = re.sub(r"[^a-z0-9 ]", "", s.lower())
    # Drop leading articles for matching purposes
    s = re.sub(r"^(the|a|an) ", "", s.strip())
    return s.strip()


def _extract_subtitle(title: str) -> str | None:
    """Return the post-colon portion of a title, if any."""
    if ":" in title:
        part = title.split(":", 1)[1].strip()
        return part if part else None
    return None


def _series_query_variants(title: str, author: str, is_bachman: bool) -> list[str]:
    """
    Build an ordered list of search queries to try for a title.
    Tries the most specific query first, falling back to progressively looser ones.
    """
    # Strip Bachman annotation before building queries
    clean = re.sub(r"\s*\(Bachman\)\s*$", "", title, flags=re.IGNORECASE).strip()

    queries: list[str] = []

    # 1. Full title + author
    queries.append(f"{clean} {author}")

    # 2. If title has a series prefix ("Series: Subtitle"), try subtitle + author
    subtitle = _extract_subtitle(clean)
    if subtitle:
        queries.append(f"{subtitle} {author}")
        queries.append(subtitle)

    # 3. Bachman alias fallback: also search under "Stephen King"
    if is_bachman:
        queries.append(f"{clean} Stephen King")
        if subtitle:
            queries.append(f"{subtitle} Stephen King")

    # 4. Bare title as last resort
    queries.append(clean)

    # Deduplicate while preserving order
    seen: set[str] = set()
    unique: list[str] = []
    for q in queries:
        if q not in seen:
            seen.add(q)
            unique.append(q)
    return unique


def _title_matches(norm_title: str, page_title_norm: str) -> tuple[bool, str]:
    """
    Check if the normalised catalog title plausibly matches the Goodreads page title.
    Returns (matched: bool, reason: str) so callers can log rejections.
    """
    if not norm_title or len(norm_title) < 3:
        return True, "title too short to verify"

    # Word-overlap check: at least one significant word (≥4 chars) must appear in
    # the page title.  This handles "The Dark Tower I: The Gunslinger" → "gunslinger"
    # matching against a page whose <title> is "The Gunslinger (The Dark Tower, #1)".
    title_words = {w for w in norm_title.split() if len(w) >= 4}
    page_words  = {w for w in page_title_norm.split() if len(w) >= 4}
    overlap = title_words & page_words
    if overlap:
        return True, f"word overlap: {overlap}"

    # Substring check as safety net for very short significant words
    if norm_title in page_title_norm or page_title_norm[:len(norm_title)] == norm_title:
        return True, "substring match"

    return False, f"no overlap — title words={title_words!r} page words={page_words!r}"


def fetch_goodreads_rating(
    title: str,
    author: str,
    year: int | None = None,
    is_bachman: bool = False,
    verbose: bool = True,
) -> tuple[float | None, int | None]:
    """
    Search Goodreads for a book and return (rating, ratings_count).
    Uses multiple search strategies and several HTML parsing fallbacks.
    Returns (None, None) if no confident match is found.
    Never invents values.
    """
    clean_title = re.sub(r"\s*\(Bachman\)\s*$", "", title, flags=re.IGNORECASE).strip()

    # Build normalised title for matching — prefer the subtitle when present
    # (e.g. "The Dark Tower I: The Gunslinger" → match on "gunslinger")
    subtitle = _extract_subtitle(clean_title)
    norm_title = _normalize_for_match(subtitle if subtitle else clean_title)

    queries = _series_query_variants(title, author, is_bachman)

    for query in queries:
        search_url = (
            "https://www.goodreads.com/search?q="
            + urllib.parse.quote_plus(query)
            + "&search_type=books"
        )
        search_html = _fetch_url(search_url)
        time.sleep(GR_REQUEST_DELAY)
        if not search_html:
            if verbose:
                print(f"      [GR] fetch failed for query: {query!r}", flush=True)
            continue

        book_urls = _extract_book_urls(search_html)
        if not book_urls:
            if verbose:
                print(f"      [GR] no book URLs in search results for: {query!r}", flush=True)
            continue

        # Try top candidates from this search
        found_url_but_no_rating = False
        for book_url in book_urls[:3]:
            book_html = _fetch_url(book_url)
            time.sleep(GR_REQUEST_DELAY)
            if not book_html:
                if verbose:
                    print(f"      [GR] failed to fetch {book_url}", flush=True)
                continue

            # Title sanity-check against <title> tag
            page_title_m = re.search(r"<title[^>]*>([^<]+)</title>", book_html, re.IGNORECASE)
            if page_title_m:
                page_title_norm = _normalize_for_match(page_title_m.group(1))
                matched, reason = _title_matches(norm_title, page_title_norm)
                if not matched:
                    if verbose:
                        print(
                            f"      [GR] SKIP {book_url} — {reason}",
                            flush=True,
                        )
                    continue

            rating, count = _parse_gr_rating(book_html)
            if rating is not None:
                return rating, count

            if verbose:
                print(f"      [GR] rating not parsed from {book_url}", flush=True)
            found_url_but_no_rating = True

        if found_url_but_no_rating:
            # We found a plausible page but couldn't parse the rating — don't
            # keep trying looser queries that might return a wrong book.
            break

    return None, None


# ---------------------------------------------------------------------------
# Core enrichment logic
# ---------------------------------------------------------------------------

def enrich_entry(entry: dict) -> dict:
    """
    Attempt to enrich a single catalog entry.
    Returns a stats dict with integer counts.
    """
    stats = {"cover_resolved": 0, "cover_skipped": 0, "cover_failed": 0, "wc_filled": 0, "gr_filled": 0}

    title:  str       = entry.get("title") or ""
    year:   int | None = entry.get("year")
    author: str       = entry.get("author") or "Stephen King"
    is_parent = entry.get("is_collection_parent", False)
    is_bachman = entry.get("as_bachman", False)

    if not title:
        return stats

    if cover_already_done(entry):
        stats["cover_skipped"] = 1
        return stats

    # Bachman books: strip " (Bachman)" suffix for search, use "Richard Bachman" as author
    search_title = title
    search_author = author
    if is_bachman:
        search_title = re.sub(r"\s*\(Bachman\)\s*$", "", title, flags=re.IGNORECASE).strip()
        search_author = "Richard Bachman"

    # Collection parents: skip author filter — works better for anthologies
    doc = ol_search(search_title, year, author=search_author, use_author_filter=(not is_parent))
    time.sleep(REQUEST_DELAY)

    if not doc:
        stats["cover_failed"] = 1
        return stats

    cover_id: int | None = doc.get("cover_i")
    ol_pages: int | None = doc.get("number_of_pages_median")

    # Word count fill (only if currently missing)
    if ol_pages and entry.get("word_count") is None:
        entry["word_count"] = round(ol_pages * WORDS_PER_PAGE)
        remove_flag(entry, "missing_word_count")
        add_flag(entry, "estimated_from_page_count")
        stats["wc_filled"] = 1

    if not cover_id:
        stats["cover_failed"] = 1
        recalculate_review_status(entry)
        return stats

    cover_url = OL_COVER.format(cover_id=cover_id)
    entry["cover_candidate_url"] = cover_url
    entry["cover_source"] = "open_library"
    entry["cover_verified"] = False   # candidate only — requires human verification

    fname = safe_filename(title)
    dest  = COVERS_DIR / fname
    if download_cover(cover_id, dest):
        entry["cover_local_path"] = f"data-tools/enriched/covers/{fname}"
        stats["cover_resolved"] = 1
    else:
        stats["cover_failed"] = 1

    recalculate_review_status(entry)
    return stats


# ---------------------------------------------------------------------------
# Repair pass
# ---------------------------------------------------------------------------

def repair_missing_cover_paths(entries: list[dict]) -> int:
    """Set cover_local_path for entries with a matching file in covers/ but no path recorded."""
    repaired = 0
    for entry in entries:
        if entry.get("cover_local_path"):
            continue
        fname = safe_filename(entry.get("title") or "")
        dest  = COVERS_DIR / fname
        if dest.exists() and dest.stat().st_size >= 1000:
            entry["cover_local_path"] = f"data-tools/enriched/covers/{fname}"
            entry["cover_source"]     = entry.get("cover_source") or "local_file"
            repaired += 1
    return repaired


# ---------------------------------------------------------------------------
# CSV output (combined across all authors)
# ---------------------------------------------------------------------------

def write_csv(all_entries: list[dict]) -> None:
    needs_review = [e for e in all_entries if e.get("review_status") == "needs_review"]
    with CSV_PATH.open("w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=CSV_FIELDS, extrasaction="ignore")
        writer.writeheader()
        for e in needs_review:
            row = {k: e.get(k) for k in CSV_FIELDS}
            row["review_flags"] = "; ".join(e.get("review_flags") or [])
            writer.writerow(row)


# ---------------------------------------------------------------------------
# Per-file enrichment
# ---------------------------------------------------------------------------

def enrich_file(path: Path, label: str, fetch_goodreads: bool = False) -> tuple[list[dict], dict]:
    """Load, enrich, and write back a single JSON catalog file. Returns (entries, totals)."""
    entries: list[dict] = json.loads(path.read_text(encoding="utf-8"))
    count = len(entries)

    repaired = repair_missing_cover_paths(entries)
    if repaired:
        print(f"  Repaired {repaired} missing cover paths from existing files")

    totals = {"cover_resolved": 0, "cover_skipped": 0, "cover_failed": 0, "wc_filled": 0, "gr_filled": 0}

    for i, entry in enumerate(entries, 1):
        title  = entry.get("title", "?")
        prefix = f"  [{i:>3}/{count}]"
        print(f"{prefix} {title}", end=" ... ", flush=True)

        stats = enrich_entry(entry)
        for k, v in stats.items():
            totals[k] += v

        if stats["cover_resolved"]:
            status = "cover ok"
        elif stats["cover_skipped"]:
            status = "already done"
        else:
            status = "no cover"
        if stats["wc_filled"]:
            status += ", wc estimated"
        print(status)

    if fetch_goodreads:
        print(f"\n  [Goodreads ratings pass for {label}]")
        for i, entry in enumerate(entries, 1):
            if entry.get("goodreads_rating") is not None:
                continue  # already populated
            title      = entry.get("title", "")
            year_      = entry.get("year")
            is_bachman = bool(entry.get("as_bachman"))
            author     = "Richard Bachman" if is_bachman else (entry.get("author") or "Stephen King")
            print(f"    GR [{i:>3}/{count}] {title}", end="\n", flush=True)
            rating, count_ = fetch_goodreads_rating(
                title, author, year=year_, is_bachman=is_bachman, verbose=True,
            )
            if rating is not None:
                entry["goodreads_rating"] = rating
                entry["goodreads_ratings_count"] = count_
                totals["gr_filled"] += 1
                print(f"      => {rating} ({count_} ratings)", flush=True)
            else:
                print(f"      => not found", flush=True)

    path.write_text(json.dumps(entries, indent=2, ensure_ascii=False), encoding="utf-8")
    return entries, totals


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main() -> int:
    import argparse
    parser = argparse.ArgumentParser(description="Enrich catalog with covers and metadata.")
    parser.add_argument("--goodreads", action="store_true",
                        help="Also fetch Goodreads ratings (slow; makes many HTTP requests)")
    args = parser.parse_args()

    COVERS_DIR.mkdir(parents=True, exist_ok=True)

    # Collect all source files: SK catalog + other-author raw files
    source_files: list[tuple[Path, str]] = [(SK_JSON, "Stephen King")]
    for p in sorted(RAW.glob(OTHER_AUTHOR_GLOB)):
        # Derive a display label from filename, e.g. "josh-malerman-source.json" → "Josh Malerman"
        stem = p.stem.replace("-source", "").replace("-", " ").title()
        source_files.append((p, stem))

    all_entries: list[dict] = []
    grand_totals = {"cover_resolved": 0, "cover_skipped": 0, "cover_failed": 0, "wc_filled": 0, "gr_filled": 0}

    for path, label in source_files:
        if not path.exists():
            print(f"[SKIP] {path} not found")
            continue
        print(f"\n── {label} ({path.name}) ──")
        entries, totals = enrich_file(path, label, fetch_goodreads=args.goodreads)
        all_entries.extend(entries)
        for k, v in totals.items():
            grand_totals[k] += v

    # Combined review CSV
    print(f"\nWriting {CSV_PATH} ...")
    write_csv(all_entries)

    needs_review_count = sum(1 for e in all_entries if e.get("review_status") == "needs_review")

    print()
    print("=== Enrichment Summary (all authors) ===")
    print(f"  Entries processed:  {len(all_entries)}")
    print(f"  Covers resolved:    {grand_totals['cover_resolved']}")
    print(f"  Covers skipped:     {grand_totals['cover_skipped']}  (already done)")
    print(f"  Covers failed:      {grand_totals['cover_failed']}  (no OL result or no cover ID)")
    print(f"  Word count fills:   {grand_totals['wc_filled']}  (estimated from page count)")
    if args.goodreads:
        print(f"  Goodreads ratings:  {grand_totals['gr_filled']}  (fetched)")
    print(f"  Still needs review: {needs_review_count}")
    print()

    # Rebuild combined catalog so assets stay in sync
    build_script = Path(__file__).parent / "build_catalog.py"
    if build_script.exists():
        print("Rebuilding combined catalog ...")
        result = subprocess.run([sys.executable, str(build_script)], check=False)
        if result.returncode != 0:
            print("  WARNING: build_catalog.py exited with errors.")
    else:
        print(f"WARNING: {build_script} not found — run it manually to refresh assets.")

    return 0


if __name__ == "__main__":
    sys.exit(main())
