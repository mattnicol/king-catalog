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

GR_SEARCH = "https://www.goodreads.com/search?q={query}&search_type=books"
GR_REQUEST_DELAY = 1.5  # Goodreads rate-limits aggressively


def _gr_headers() -> dict:
    return {
        "User-Agent": (
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 "
            "(KHTML, like Gecko) Chrome/122.0 Safari/537.36"
        ),
        "Accept-Language": "en-US,en;q=0.9",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    }


def _fetch_url(url: str, timeout: int = 15) -> str | None:
    req = urllib.request.Request(url, headers=_gr_headers())
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return resp.read().decode("utf-8", errors="replace")
    except Exception:
        return None


def _parse_gr_rating(html: str) -> tuple[float | None, int | None]:
    """
    Parse Goodreads rating from JSON-LD embedded in the page.
    Returns (rating_value, ratings_count) or (None, None).
    """
    # Try JSON-LD first (most reliable)
    for m in re.finditer(r'<script[^>]+type="application/ld\+json"[^>]*>(.*?)</script>', html, re.DOTALL):
        try:
            obj = json.loads(m.group(1))
            ar = obj.get("aggregateRating") or {}
            rv = ar.get("ratingValue")
            rc = ar.get("ratingCount")
            if rv is not None:
                return float(rv), (int(rc) if rc is not None else None)
        except Exception:
            continue
    return None, None


def fetch_goodreads_rating(title: str, author: str) -> tuple[float | None, int | None]:
    """
    Search Goodreads for a book and return (rating, ratings_count).
    Returns (None, None) on failure or if not confidently resolved.
    """
    query = f"{title} {author}"
    search_url = GR_SEARCH.format(query=urllib.parse.quote_plus(query))
    html = _fetch_url(search_url)
    time.sleep(GR_REQUEST_DELAY)
    if not html:
        return None, None

    # Find first book link in search results
    m = re.search(r'href="(/book/show/[^"?]+)"', html)
    if not m:
        return None, None

    book_url = "https://www.goodreads.com" + m.group(1)
    book_html = _fetch_url(book_url)
    time.sleep(GR_REQUEST_DELAY)
    if not book_html:
        return None, None

    return _parse_gr_rating(book_html)


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
            title  = entry.get("title", "")
            author = entry.get("author") or "Stephen King"
            if entry.get("as_bachman"):
                author = "Richard Bachman"
            print(f"    GR [{i:>3}/{count}] {title}", end=" ... ", flush=True)
            rating, count_ = fetch_goodreads_rating(title, author)
            if rating is not None:
                entry["goodreads_rating"] = rating
                entry["goodreads_ratings_count"] = count_
                totals["gr_filled"] += 1
                print(f"{rating} ({count_} ratings)")
            else:
                print("not found")

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
