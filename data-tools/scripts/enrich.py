#!/usr/bin/env python3
"""
enrich.py — King Catalog enrichment CLI.

Enriches catalog entries with cover images, word counts, Goodreads ratings,
descriptions, keywords, and series/connections data from external sources.

IMPORTANT: This tool never invents data. If a field cannot be confidently
populated from a reliable source it leaves the field null and marks the
entry for review.

─────────────────────────────────────────────────────────────────────────────
TARGETING  (one required unless --review-only)
─────────────────────────────────────────────────────────────────────────────
  --title "The Shining"       Process entries whose title contains this string
                                (repeatable; case-insensitive substring match)
  --author "Stephen King"     Process all entries by this author (repeatable)
  --all-authors               Process every entry in all author catalog files

─────────────────────────────────────────────────────────────────────────────
FIELD SELECTION  (default: covers)
─────────────────────────────────────────────────────────────────────────────
  --fields covers,goodreads,descriptions,keywords,connections,metadata
                              Comma-separated fields to enrich
  --descriptions              Shortcut for --fields descriptions
  --keywords                  Shortcut for --fields keywords
  --connections               Shortcut for --fields connections

─────────────────────────────────────────────────────────────────────────────
BEHAVIOR FLAGS
─────────────────────────────────────────────────────────────────────────────
  --blanks-only               Only update null/empty fields; skip populated
  --once-only-curation        Same as --blanks-only; intended for one-time
                                description and keyword curation passes
  --dry-run                   Preview changes without writing to disk
  --verbose                   Show detailed HTTP and parse output
  --include-spoilers          Include spoiler-tagged connections in output
  --force                     Re-fetch and overwrite existing data

─────────────────────────────────────────────────────────────────────────────
SOURCES
─────────────────────────────────────────────────────────────────────────────
  --source wikipedia,goodreads,openlibrary,reddit
                              Comma-separated sources to use (default varies
                              by --fields)
  --reddit-thread-url <url>   Reddit thread to parse as seed for connections
                                (repeatable; requires --fields connections)

─────────────────────────────────────────────────────────────────────────────
CATALOG REBUILD
def _normalize_reddit_json_url(url: str) -> str:
    url = url.strip()
    if url.endswith(".json"):
        return url
    url = url.rstrip("/")
    return url + "/.json"

─────────────────────────────────────────────────────────────────────────────
  --no-rebuild-seed           Skip rebuild of combined catalog after enrichment
                                (default: rebuild is performed)

─────────────────────────────────────────────────────────────────────────────
REVIEW
─────────────────────────────────────────────────────────────────────────────
  --review-only               Print review flags and unresolved items only;
                                no enrichment is performed

─────────────────────────────────────────────────────────────────────────────
EXAMPLES
─────────────────────────────────────────────────────────────────────────────

  # Download missing covers for The Shining
  python enrich.py --title "The Shining"

  # Re-fetch cover + Goodreads rating for two specific titles
  python enrich.py --title "It" --title "Carrie" --fields covers,goodreads --force

  # Fill all blank descriptions from Wikipedia (one-time pass, never overwrites)
  python enrich.py --all-authors --fields descriptions --once-only-curation

  # Fill blank keywords from Wikipedia categories (one-time pass)
  python enrich.py --all-authors --fields keywords --once-only-curation

  # Full enrichment run for Stephen King titles
  python enrich.py --author "Stephen King" --fields covers,goodreads,descriptions,keywords

  # Seed connections from a Reddit thread (adds candidates marked for review)
  python enrich.py --all-authors --fields connections \\
    --reddit-thread-url "https://www.reddit.com/r/stephenking/comments/abc123/"

  # Dry run: see what would be updated without writing
  python enrich.py --all-authors --dry-run

  # Show all review flags (no changes made)
  python enrich.py --review-only
"""

import argparse
import csv
import hashlib
import json
import re
import subprocess
import sys
import time
import unicodedata
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

# ─────────────────────────────────────────────────────────────────────────────
# Path constants
# ─────────────────────────────────────────────────────────────────────────────

ROOT         = Path(__file__).parent.parent       # data-tools/
PROJECT_ROOT = ROOT.parent                         # king-catalog/
ENRICHED     = ROOT / "enriched"
RAW          = ROOT / "raw"
COVERS_DIR   = ENRICHED / "covers"
CSV_PATH     = ENRICHED / "all_catalog_review.csv"

SK_JSON = ENRICHED / "king_catalog.json"
OTHER_AUTHOR_GLOB = "*-source.json"

OL_SEARCH = "https://openlibrary.org/search.json"
OL_COVER  = "https://covers.openlibrary.org/b/id/{cover_id}-L.jpg"

WORDS_PER_PAGE   = 275
REQUEST_DELAY    = 0.5
GR_REQUEST_DELAY = 2.5
WP_REQUEST_DELAY = 0.4

GR_MIN_RATINGS = 1000

# Patterns that identify non-book summary/guide/analysis editions — reject on match
GR_REJECT_PATTERNS: list[str] = [
    "summary of",
    "includes analysis",
    "study guide",
    "workbook",
    "trivia",
    "instaread summaries",
    "instaread",
    "analysis",
    "summary",
    "book review",
]

OL_PLACEHOLDER_HASHES: set[str] = {
    "04b41f5dc246f9ed8bf27ade4999603a",
}

CSV_FIELDS = [
    "id", "author", "title", "year", "word_count", "audible_minutes",
    "story_type", "collection", "goodreads_rating", "goodreads_ratings_count",
    "review_flags",
]

VALID_FIELDS  = {"covers", "goodreads", "descriptions", "keywords", "connections", "metadata"}
VALID_SOURCES = {"wikipedia", "goodreads", "openlibrary", "reddit"}

# Singular → plural aliases accepted on the CLI
FIELD_ALIASES = {
    "description": "descriptions",
    "keyword":     "keywords",
    "connection":  "connections",
    "cover":       "covers",
}

VALID_CONNECTION_KINDS = {"series", "trilogy", "cycle", "universe", "connection", "easter_egg", "tie_in"}
VALID_CONNECTION_ROLES = {"core", "supplemental"}


# ─────────────────────────────────────────────────────────────────────────────
# Argument parsing
# ─────────────────────────────────────────────────────────────────────────────

def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="enrich.py",
        description=(
            "King Catalog enrichment CLI.\n"
            "Enriches catalog entries with covers, ratings, descriptions,\n"
            "keywords, and series/connections. Never invents data."
        ),
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )

    tgt = parser.add_argument_group("Targeting (one required unless --review-only)")
    tgt.add_argument("--title", dest="titles", action="append", metavar="TITLE",
                     help="Process entries whose title contains this string (repeatable)")
    tgt.add_argument("--author", dest="authors", action="append", metavar="AUTHOR",
                     help="Process all entries by this author (repeatable)")
    tgt.add_argument("--all-authors", action="store_true",
                     help="Process every entry in all author catalog files")

    fld = parser.add_argument_group("Field selection")
    fld.add_argument("--fields", metavar="FIELD,...",
                     help=("Comma-separated fields: "
                           + ", ".join(sorted(VALID_FIELDS))))
    fld.add_argument("--descriptions", action="store_true",
                     help="Shortcut for --fields descriptions")
    fld.add_argument("--keywords", action="store_true",
                     help="Shortcut for --fields keywords")
    fld.add_argument("--connections", action="store_true",
                     help="Shortcut for --fields connections")

    beh = parser.add_argument_group("Behavior")
    beh.add_argument("--blanks-only", action="store_true",
                     help="Only update null/empty fields; skip populated ones")
    beh.add_argument("--once-only-curation", action="store_true",
                     help="Same as --blanks-only; for one-time description/keyword passes")
    beh.add_argument("--dry-run", action="store_true",
                     help="Print what would change without writing to disk")
    beh.add_argument("--verbose", action="store_true",
                     help="Show detailed HTTP and parse output")
    beh.add_argument("--include-spoilers", action="store_true",
                     help="Include spoiler-tagged connections in output")
    beh.add_argument("--force", action="store_true",
                     help="Re-fetch and overwrite existing data for targeted entries")

    src = parser.add_argument_group("Sources")
    src.add_argument("--source", metavar="SOURCE,...",
                     help=("Comma-separated sources: "
                           + ", ".join(sorted(VALID_SOURCES))))
    src.add_argument("--reddit-thread-url", dest="reddit_urls", action="append",
                     metavar="URL",
                     help="Reddit thread URL for connections seed (repeatable)")
    src.add_argument("--reddit-text-file", dest="reddit_text_files", action="append",
                     metavar="PATH",
                     help="Local text file with connections seed (repeatable; skips live Reddit fetch)")

    parser.add_argument("--no-rebuild-seed", action="store_true",
                        help="Skip combined catalog rebuild after enrichment")
    parser.add_argument("--review-only", action="store_true",
                        help="Print review flags only; no enrichment performed")

    return parser


def resolve_fields(args: argparse.Namespace) -> set[str]:
    """Merge --fields, --descriptions, --keywords, --connections into a set."""
    fields: set[str] = set()

    if args.fields:
        for f in args.fields.split(","):
            f = f.strip().lower()
            f = FIELD_ALIASES.get(f, f)  # normalize singular aliases
            if f not in VALID_FIELDS:
                print(f"WARNING: unknown field '{f}' — ignoring", file=sys.stderr)
            else:
                fields.add(f)

    if args.descriptions:
        fields.add("descriptions")
    if args.keywords:
        fields.add("keywords")
    if args.connections:
        fields.add("connections")

    # Default to covers when nothing specified
    if not fields:
        fields.add("covers")

    return fields


def resolve_sources(args: argparse.Namespace, fields: set[str]) -> set[str]:
    """Resolve --source or derive sensible defaults from fields."""
    if args.source:
        sources: set[str] = set()
        for s in args.source.split(","):
            s = s.strip().lower()
            if s not in VALID_SOURCES:
                print(f"WARNING: unknown source '{s}' — ignoring", file=sys.stderr)
            else:
                sources.add(s)
        return sources

    # Defaults by field
    sources = set()
    if "covers" in fields:
        sources.add("openlibrary")
    if "goodreads" in fields:
        sources.add("goodreads")
    if "descriptions" in fields:
        sources.add("wikipedia")
    if "keywords" in fields:
        sources.add("wikipedia")
    if "connections" in fields:
        sources.add("reddit")
        sources.add("wikipedia")
    return sources


# ─────────────────────────────────────────────────────────────────────────────
# Open Library helpers
# ─────────────────────────────────────────────────────────────────────────────

def ol_search(
    title: str,
    year: int | None,
    author: str,
    use_author_filter: bool = True,
) -> dict | None:
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

    def year_dist(doc: dict) -> int:
        y = doc.get("first_publish_year")
        return abs(y - year) if isinstance(y, int) else 9999

    return min(docs, key=year_dist)


def download_cover(cover_id: int, dest: Path) -> bool:
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


# ─────────────────────────────────────────────────────────────────────────────
# Goodreads helpers
# ─────────────────────────────────────────────────────────────────────────────

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
    """Fetch a URL with browser-like headers; handles gzip transparently."""
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
    def _valid_rating(v: float) -> bool:
        return 1.0 <= v <= 5.0

    for m in re.finditer(
        r'<script[^>]+type=["\']application/ld\+json["\'][^>]*>(.*?)</script>',
        html, re.DOTALL | re.IGNORECASE,
    ):
        try:
            obj = json.loads(m.group(1))
            items: list = obj if isinstance(obj, list) else [obj]
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

    m = re.search(r'"ratingValue"\s*:\s*"?([0-9.]+)"?', html)
    if m:
        try:
            r = float(m.group(1))
            if _valid_rating(r):
                m2 = re.search(r'"(?:ratingsCount|ratingCount)"\s*:\s*([0-9]+)', html)
                return r, (int(m2.group(1)) if m2 else None)
        except (ValueError, TypeError):
            pass

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
    # Transliterate accented/special chars to ASCII equivalents (ö→o, é→e, etc.)
    s = unicodedata.normalize("NFKD", s)
    s = "".join(c for c in s if not unicodedata.combining(c))
    s = re.sub(r"\s*\([^)]*\)", "", s)
    s = re.sub(r"[^a-z0-9 ]", "", s.lower())
    s = re.sub(r"^(the|a|an) ", "", s.strip())
    return s.strip()


def _gr_parse_page_title(html_title: str) -> tuple[str, str]:
    """Parse a Goodreads <title> tag into (book_title_part, author_part)."""
    raw = re.sub(r'\s*[|].*$', '', html_title).strip()
    lower = raw.lower()
    idx = lower.rfind(" by ")
    if idx >= 0:
        return raw[:idx].strip(), raw[idx + 4:].strip()
    return raw, ""


def _is_rejected_gr_edition(book_title: str, page_author: str) -> tuple[bool, str]:
    """Return (True, reason) if the page looks like a summary/guide/analysis edition."""
    combined = (book_title + " " + page_author).lower()
    for pat in GR_REJECT_PATTERNS:
        if pat in combined:
            return True, f"reject pattern {pat!r} found in {combined[:80]!r}"
    return False, ""


def _extract_subtitle(title: str) -> str | None:
    if ":" in title:
        part = title.split(":", 1)[1].strip()
        return part if part else None
    return None


def _series_query_variants(title: str, author: str, is_bachman: bool) -> list[str]:
    clean = re.sub(r"\s*\(Bachman\)\s*$", "", title, flags=re.IGNORECASE).strip()
    queries: list[str] = []
    queries.append(f"{clean} {author}")
    subtitle = _extract_subtitle(clean)
    if subtitle:
        queries.append(f"{subtitle} {author}")
        queries.append(subtitle)
    if is_bachman:
        queries.append(f"{clean} Stephen King")
        if subtitle:
            queries.append(f"{subtitle} Stephen King")
    queries.append(clean)
    seen: set[str] = set()
    unique: list[str] = []
    for q in queries:
        if q not in seen:
            seen.add(q)
            unique.append(q)
    return unique


def _title_matches(norm_title: str, page_title_norm: str) -> tuple[bool, str]:
    if not norm_title or len(norm_title) < 3:
        return True, "title too short to verify"
    title_words = {w for w in norm_title.split() if len(w) >= 4}
    page_words  = {w for w in page_title_norm.split() if len(w) >= 4}
    overlap = title_words & page_words
    if overlap:
        return True, f"word overlap: {overlap}"
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
    clean_title = re.sub(r"\s*\(Bachman\)\s*$", "", title, flags=re.IGNORECASE).strip()
    subtitle = _extract_subtitle(clean_title)
    norm_title = _normalize_for_match(subtitle if subtitle else clean_title)
    queries = _series_query_variants(title, author, is_bachman)

    if verbose:
        print(f"      [GR] target title:    {title!r}", flush=True)
        print(f"      [GR] expected author: {author!r}", flush=True)

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
                print(f"      [GR] no book URLs for: {query!r}", flush=True)
            continue

        if verbose:
            print(f"      [GR] query {query!r} → {len(book_urls)} candidate(s)", flush=True)

        found_url_but_no_rating = False
        for book_url in book_urls[:3]:
            book_html = _fetch_url(book_url)
            time.sleep(GR_REQUEST_DELAY)
            if not book_html:
                if verbose:
                    print(f"      [GR]   SKIP {book_url} — fetch failed", flush=True)
                continue

            page_book_title, page_author = "", ""
            page_title_m = re.search(r"<title[^>]*>([^<]+)</title>", book_html, re.IGNORECASE)
            if page_title_m:
                page_book_title, page_author = _gr_parse_page_title(page_title_m.group(1))
                if verbose:
                    print(f"      [GR]   candidate: {book_url}", flush=True)
                    print(f"      [GR]     title:   {page_book_title!r}", flush=True)
                    print(f"      [GR]     author:  {page_author!r}", flush=True)

                # Title similarity check
                page_title_norm = _normalize_for_match(page_title_m.group(1))
                matched, reason = _title_matches(norm_title, page_title_norm)
                if not matched:
                    if verbose:
                        print(f"      [GR]     REJECT — title mismatch: {reason}", flush=True)
                    continue

                # Reject summary/guide/analysis editions
                rejected, rej_reason = _is_rejected_gr_edition(page_book_title, page_author)
                if rejected:
                    if verbose:
                        print(f"      [GR]     REJECT — {rej_reason}", flush=True)
                    continue
            elif verbose:
                print(f"      [GR]   candidate: {book_url} (no <title>)", flush=True)

            rating, count = _parse_gr_rating(book_html)

            # Minimum ratings threshold
            if rating is not None and count is not None and count < GR_MIN_RATINGS:
                print(
                    f"      [GR]     REJECT {book_url} — ratings count {count} < {GR_MIN_RATINGS}",
                    flush=True,
                )
                continue

            if rating is not None:
                if verbose:
                    print(f"      [GR]     ACCEPT — rating={rating} count={count}", flush=True)
                    print(f"      [GR]   SELECTED: {book_url}", flush=True)
                    print(f"      [GR]     title:   {page_book_title!r}", flush=True)
                    print(f"      [GR]     author:  {page_author!r}", flush=True)
                    print(f"      [GR]     rating:  {rating}  count: {count}", flush=True)
                return rating, count

            if verbose:
                print(f"      [GR]   rating not parsed from {book_url}", flush=True)
            found_url_but_no_rating = True

        if found_url_but_no_rating:
            break

    return None, None


def _parse_gr_description(html: str) -> str | None:
    """Extract book description from a Goodreads book page."""
    # JSON-LD first
    for m in re.finditer(
        r'<script[^>]+type=["\']application/ld\+json["\'][^>]*>(.*?)</script>',
        html, re.DOTALL | re.IGNORECASE,
    ):
        try:
            obj = json.loads(m.group(1))
            items = obj if isinstance(obj, list) else [obj]
            expanded: list = []
            for item in items:
                if "@graph" in item:
                    g = item["@graph"]
                    expanded.extend(g if isinstance(g, list) else [g])
                else:
                    expanded.append(item)
            for item in expanded:
                desc = item.get("description") or ""
                if isinstance(desc, str) and len(desc) > 30:
                    desc = re.sub(r"<[^>]+>", " ", desc)
                    return re.sub(r"\s+", " ", desc).strip()[:1200]
        except Exception:
            continue

    # HTML fallback — Goodreads description container patterns
    for pattern in [
        r'<div[^>]+class=["\'][^"\']*BookPageMetaData[^"\']*description[^"\']*["\'][^>]*>(.*?)</div>',
        r'<span[^>]+class=["\'][^"\']*Formatted[^"\']*["\'][^>]*>(.*?)</span>',
        r'<div[^>]+id=["\']description["\'][^>]*>.*?<span[^>]*>(.*?)</span>',
    ]:
        m = re.search(pattern, html, re.DOTALL | re.IGNORECASE)
        if m:
            text = re.sub(r"<[^>]+>", " ", m.group(1))
            text = re.sub(r"\s+", " ", text).strip()
            if len(text) > 30:
                return text[:1200]

    return None


def _parse_gr_keywords(html: str) -> list[str]:
    """Extract genre/shelf tags from a Goodreads book page."""
    keywords: list[str] = []
    seen: set[str] = set()
    # Genre links typically contain /genres/ or /shelf/show/
    for m in re.finditer(
        r'href=["\'][^"\']*(?:genres|shelf/show)[^"\']*["\'][^>]*>([^<]+)</a>',
        html, re.IGNORECASE,
    ):
        kw = m.group(1).strip().lower().replace(" ", "-")
        kw = re.sub(r"[^a-z0-9\-]", "", kw)
        if 3 <= len(kw) <= 40 and kw not in seen:
            seen.add(kw)
            keywords.append(kw)
    return keywords[:12]


def _fetch_goodreads_book_html(
    title: str, author: str, year: int | None, is_bachman: bool, verbose: bool,
) -> str | None:
    """Search Goodreads for the book and return the HTML of the best matching page."""
    queries = _series_query_variants(title, author, is_bachman)
    clean_title = re.sub(r"\s*\(Bachman\)\s*$", "", title, flags=re.IGNORECASE).strip()
    subtitle = _extract_subtitle(clean_title)
    norm_title = _normalize_for_match(subtitle if subtitle else clean_title)

    for query in queries:
        search_url = (
            "https://www.goodreads.com/search?q="
            + urllib.parse.quote_plus(query)
            + "&search_type=books"
        )
        search_html = _fetch_url(search_url)
        time.sleep(GR_REQUEST_DELAY)
        if not search_html:
            continue

        book_urls = _extract_book_urls(search_html)
        if not book_urls:
            continue

        for book_url in book_urls[:3]:
            book_html = _fetch_url(book_url)
            time.sleep(GR_REQUEST_DELAY)
            if not book_html:
                continue
            page_title_m = re.search(r"<title[^>]*>([^<]+)</title>", book_html, re.IGNORECASE)
            if page_title_m:
                page_book_title, page_author = _gr_parse_page_title(page_title_m.group(1))
                page_title_norm = _normalize_for_match(page_title_m.group(1))
                matched, reason = _title_matches(norm_title, page_title_norm)
                if not matched:
                    if verbose:
                        print(f"      [GR] SKIP {book_url} — {reason}", flush=True)
                    continue
                rejected, rej_reason = _is_rejected_gr_edition(page_book_title, page_author)
                if rejected:
                    if verbose:
                        print(f"      [GR] SKIP {book_url} — {rej_reason}", flush=True)
                    continue
            if verbose:
                print(f"      [GR] fetched {book_url}", flush=True)
            return book_html

    return None


def fetch_goodreads_description(
    title: str,
    author: str,
    year: int | None = None,
    is_bachman: bool = False,
    verbose: bool = False,
) -> str | None:
    html = _fetch_goodreads_book_html(title, author, year, is_bachman, verbose)
    if not html:
        if verbose:
            print(f"      [GR] no page found for description: {title!r}", flush=True)
        return None
    desc = _parse_gr_description(html)
    if verbose:
        if desc:
            print(f"      [GR] description found: {desc[:80]}…", flush=True)
        else:
            print(f"      [GR] description not parsed for: {title!r}", flush=True)
    return desc


def fetch_goodreads_keywords(
    title: str,
    author: str,
    year: int | None = None,
    is_bachman: bool = False,
    verbose: bool = False,
) -> list[str]:
    html = _fetch_goodreads_book_html(title, author, year, is_bachman, verbose)
    if not html:
        return []
    kws = _parse_gr_keywords(html)
    if verbose and kws:
        print(f"      [GR] keywords found: {kws[:6]}", flush=True)
    return kws


def fetch_openlibrary_description(
    title: str,
    author: str,
    year: int | None = None,
    verbose: bool = False,
) -> str | None:
    """Fetch description from Open Library works API."""
    normalized = _normalize_title_for_lookup(title)
    doc = ol_search(normalized, year, author)
    if not doc:
        if verbose:
            print(f"      [OL] no search result for: {title!r}", flush=True)
        return None

    work_key = doc.get("key")
    if not work_key:
        return None

    try:
        url = f"https://openlibrary.org{work_key}.json"
        with urllib.request.urlopen(url, timeout=12) as resp:
            work = json.loads(resp.read().decode("utf-8"))
        time.sleep(REQUEST_DELAY)
        desc = work.get("description") or ""
        if isinstance(desc, dict):
            desc = desc.get("value", "")
        if isinstance(desc, str) and len(desc) > 30:
            if verbose:
                print(f"      [OL] description found: {desc[:80]}…", flush=True)
            return desc[:1200]
    except Exception as e:
        if verbose:
            print(f"      [OL] description fetch error: {e}", flush=True)
    return None


def fetch_openlibrary_keywords(
    title: str,
    author: str,
    year: int | None = None,
    verbose: bool = False,
) -> list[str]:
    """Fetch subject-derived keywords from Open Library."""
    normalized = _normalize_title_for_lookup(title)
    params_dict = {
        "title": normalized,
        "author": author.lower(),
        "fields": "key,title,subject",
        "limit": 3,
    }
    params = urllib.parse.urlencode(params_dict)
    try:
        with urllib.request.urlopen(f"{OL_SEARCH}?{params}", timeout=12) as resp:
            data = json.loads(resp.read().decode("utf-8"))
        time.sleep(REQUEST_DELAY)
        docs = data.get("docs") or []
        if not docs:
            return []
        subjects = docs[0].get("subject") or []
        keywords: list[str] = []
        seen: set[str] = set()
        for subj in subjects[:20]:
            kw = subj.lower().strip().replace(" ", "-")
            kw = re.sub(r"[^a-z0-9\-]", "", kw)
            if 3 <= len(kw) <= 40 and kw not in seen:
                seen.add(kw)
                keywords.append(kw)
        if verbose and keywords:
            print(f"      [OL] keywords found: {keywords[:6]}", flush=True)
        return keywords[:12]
    except Exception as e:
        if verbose:
            print(f"      [OL] keywords fetch error: {e}", flush=True)
        return []


# ─────────────────────────────────────────────────────────────────────────────
# Wikipedia helpers
# ─────────────────────────────────────────────────────────────────────────────

WP_API = "https://en.wikipedia.org/api/rest_v1/page/summary/{title}"
WP_CATS_API = (
    "https://en.wikipedia.org/w/api.php"
    "?action=query&titles={title}&prop=categories"
    "&cllimit=20&format=json&redirects=1"
)

_IGNORE_WP_CATS = {
    "articles", "pages", "stubs", "wikidata", "cs1", "dmy dates", "mdy dates",
    "wikipedia", "all articles", "use", "short description",
}


def _normalize_title_for_lookup(title: str) -> str:
    """Strip quotes, parenthetical suffixes, normalize apostrophes."""
    t = title.strip("'\"")
    # Normalize smart apostrophes
    t = t.replace("\u2018", "'").replace("\u2019", "'")
    # Strip parenthetical pen-name / edition suffixes like (Bachman)
    t = re.sub(r"\s*\([^)]*\)\s*$", "", t).strip()
    return t


def _wp_search_title(title: str, author: str) -> str | None:
    """Return the Wikipedia page title for a book, or None."""
    normalized = _normalize_title_for_lookup(title)
    # Try both author variants for Bachman titles
    queries = [f"{normalized} {author} novel"]
    if normalized != title.strip():
        queries.append(f"{normalized} Stephen King novel")
    queries.append(normalized)

    for query in queries:
        params = urllib.parse.urlencode({"action": "opensearch", "search": query,
                                         "limit": 3, "format": "json"})
        try:
            with urllib.request.urlopen(
                f"https://en.wikipedia.org/w/api.php?{params}", timeout=10
            ) as resp:
                data = json.loads(resp.read().decode("utf-8"))
            results = data[1] if data and len(data) > 1 else []
            if results:
                return results[0]
        except Exception:
            pass
    return None




def fetch_wikipedia_summary(title: str, author: str, verbose: bool = False) -> str | None:
    """
    Fetch a short summary for a book from Wikipedia.
    Returns the extract (first paragraph) or None.
    """
    wp_title = _wp_search_title(title, author)
    if not wp_title:
        if verbose:
            print(f"      [WP] no Wikipedia title found for: {title!r}", flush=True)
        return None

    encoded = urllib.parse.quote(wp_title.replace(" ", "_"))
    url = WP_API.format(title=encoded)
    try:
        with urllib.request.urlopen(url, timeout=10) as resp:
            data = json.loads(resp.read().decode("utf-8"))
        extract = data.get("extract") or ""
        # Only take the first paragraph
        first_para = extract.split("\n\n")[0].strip()
        if len(first_para) < 30:
            return None
        if verbose:
            print(f"      [WP] summary for {title!r}: {first_para[:80]}…", flush=True)
        return first_para
    except Exception:
        return None


def fetch_wikipedia_keywords(title: str, author: str, verbose: bool = False) -> list[str]:
    """Fetch category-derived keywords for a book from Wikipedia."""
    wp_title = _wp_search_title(title, author)
    if not wp_title:
        return []

    encoded = urllib.parse.quote(wp_title.replace(" ", "_"))
    url = WP_CATS_API.format(title=encoded)
    try:
        with urllib.request.urlopen(url, timeout=10) as resp:
            data = json.loads(resp.read().decode("utf-8"))
        pages = data.get("query", {}).get("pages", {})
        cats: list[str] = []
        for page in pages.values():
            for cat in (page.get("categories") or []):
                raw = cat.get("title", "").replace("Category:", "").lower().strip()
                # Skip noisy maintenance / meta categories
                if any(ig in raw for ig in _IGNORE_WP_CATS):
                    continue
                # Clean to a simple keyword
                kw = re.sub(r"\s+novels?$", "", raw).strip()
                kw = re.sub(r"^[\d]{4}\s+", "", kw)  # strip leading years
                kw = kw.replace(" ", "-")
                if 3 <= len(kw) <= 40:
                    cats.append(kw)
        if verbose and cats:
            print(f"      [WP] keywords for {title!r}: {cats[:6]}", flush=True)
        return cats[:12]
    except Exception:
        return []


# ─────────────────────────────────────────────────────────────────────────────
# Reddit / Connections helpers
# ─────────────────────────────────────────────────────────────────────────────

def _reddit_json_url(url: str) -> str:
    """Convert a Reddit thread URL to its JSON API endpoint."""
    url = url.rstrip("/")
    if not url.endswith(".json"):
        url += ".json"
    return url


def fetch_reddit_comments(url: str, verbose: bool = False) -> list[str]:
    """
    Fetch post body + comment bodies from a Reddit thread JSON endpoint.
    Returns a list of plain-text strings (post body first, then comments).
    """
    json_url = _reddit_json_url(url)
    if verbose:
        print(f"      [Reddit] original URL:    {url}", flush=True)
        print(f"      [Reddit] normalized URL:  {json_url}", flush=True)

    headers = {
        "User-Agent": (
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        ),
        "Accept": "application/json,text/html,*/*;q=0.8",
        "Accept-Language": "en-US,en;q=0.9",
    }
    req = urllib.request.Request(json_url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            raw = resp.read().decode("utf-8")
        data = json.loads(raw)
        if verbose:
            print(f"      [Reddit] fetch status:   OK", flush=True)
    except Exception as e:
        if verbose:
            print(f"      [Reddit] fetch status:   FAILED — {e}", flush=True)
        else:
            print(f"      [Reddit] fetch failed for {url}: {e}", flush=True)
        return []

    texts: list[str] = []

    def _walk(node: object) -> None:
        if isinstance(node, list):
            for item in node:
                _walk(item)
        elif isinstance(node, dict):
            kind = node.get("kind")
            data_ = node.get("data") or {}
            if kind == "t3":
                selftext = data_.get("selftext") or ""
                if selftext and selftext not in ("[deleted]", "[removed]"):
                    texts.append(selftext)
            elif kind == "t1":
                body = data_.get("body") or ""
                if body and body not in ("[deleted]", "[removed]"):
                    texts.append(body)
            for child_key in ("children", "replies"):
                child = data_.get(child_key)
                if child:
                    _walk(child)

    _walk(data)
    if verbose:
        print(f"      [Reddit] texts extracted: {len(texts)} (post body + comments)", flush=True)
    return texts


def extract_connection_candidates(
    comments: list[str],
    catalog_titles: list[str],
    thread_url: str,
    verbose: bool = False,
) -> dict[str, list[dict]]:
    """
    Cross-reference Reddit comments against catalog titles to build candidate
    connections.  Returns { title_lower: [candidate_connection, ...] }.

    Candidates are marked spoiler=False and need_review=True.  They should
    not be written without human verification.
    """
    # Build a quick-lookup set of normalised titles
    norm_map: dict[str, str] = {
        _normalize_for_match(t): t for t in catalog_titles
    }

    results: dict[str, list[dict]] = {}

    for comment in comments:
        for norm, orig_title in norm_map.items():
            if len(norm) < 4:
                continue
            # Check if the normalised title appears in the normalised comment
            norm_comment = re.sub(r"[^a-z0-9 ]", "", comment.lower())
            if norm not in norm_comment:
                continue

            # Try to infer a group name from context words near the mention
            context_snip = comment[:500]
            group = _infer_group_from_context(context_snip, orig_title)

            candidate: dict = {
                "group": group,
                "kind": "connection",
                "role": "supplemental",
                "order": None,
                "spoiler": False,
                "note": f"Candidate from Reddit thread: {thread_url}",
                "review_flag": "needs_verification_connections",
            }

            bucket = results.setdefault(orig_title.lower(), [])
            # Deduplicate by group
            if not any(c["group"] == group for c in bucket):
                bucket.append(candidate)

    if verbose:
        all_groups = {c["group"] for conns in results.values() for c in conns}
        print(f"      [Connections] candidate groups found:  {len(all_groups)}", flush=True)
        print(f"      [Connections] candidate titles found:  {len(results)}", flush=True)
    return results


def _infer_group_from_context(text: str, book_title: str) -> str:
    """
    Heuristic: look for known universe / category names near the book mention.
    Group names are sourced from the Reddit thread categories.
    Falls back to 'Unknown Group (needs review)'.
    """
    # Ordered by specificity — more specific phrases checked first
    known_groups = [
        # Dark Tower categories (from Reddit thread)
        ("dark tower lead", "Dark Tower lead-ins"),
        ("dark tower core", "Dark Tower core"),
        ("dark tower tie", "Dark Tower tie-ins"),
        ("dark tower", "Dark Tower"),
        ("gunslinger", "Dark Tower"),
        # Bachman
        ("bachman book", "Bachman Books"),
        ("bachman", "Bachman Books"),
        # Multi-book groupings
        ("trilogy", "trilogies"),
        ("duology", "duologies"),
        ("genre group", "genre groupings"),
        # Named universes / locations
        ("castle rock", "Castle Rock"),
        ("derry", "Derry"),
        ("overlook", "Overlook"),
        ("holly gibney", "Holly Gibney"),
        ("bill hodges", "Bill Hodges"),
        ("the stand", "The Stand"),
    ]
    text_lower = text.lower()
    for keyword, group in known_groups:
        if keyword in text_lower:
            return group
    return "Unknown Group (needs review)"


def make_connection(
    group: str,
    kind: str = "connection",
    role: str = "supplemental",
    order: int | None = None,
    spoiler: bool = False,
    note: str | None = None,
) -> dict:
    """Build a validated connection dict."""
    if kind not in VALID_CONNECTION_KINDS:
        kind = "connection"
    if role not in VALID_CONNECTION_ROLES:
        role = "supplemental"
    return {
        "group": group,
        "kind": kind,
        "role": role,
        "order": order,
        "spoiler": spoiler,
        "note": note,
    }


# ─────────────────────────────────────────────────────────────────────────────
# Local seed file parser
# ─────────────────────────────────────────────────────────────────────────────

def _clean_section_canonical(line: str) -> str:
    """Strip emoji/non-ASCII, trailing parentheticals, normalize to uppercase."""
    s = re.sub(r"[^\x00-\x7F]", "", line)
    s = re.sub(r"\s*\(.*?\)\s*$", "", s)
    return s.strip().upper()


def _is_section_heading(line: str) -> bool:
    """True if line is naturally all-uppercase (section header heuristic).

    Lines containing a publication year like (1977) are titles, not headings.
    We check the ORIGINAL case of the stripped line, not the uppercased canonical.
    """
    if re.search(r"\(\d{4}\)", line):
        return False
    s = re.sub(r"[^\x00-\x7F]", "", line)  # strip non-ASCII / emoji
    s = re.sub(r"\s*\(.*?\)\s*$", "", s).strip()  # strip trailing parentheticals
    if not s:
        return False
    letters = re.sub(r"[^A-Za-z]", "", s)  # keep only letters, original case
    return bool(letters) and letters == letters.upper() and len(letters) >= 3


def _to_title_case(s: str) -> str:
    """Title-case an all-caps string, handling apostrophes correctly."""
    return " ".join(w[0].upper() + w[1:].lower() if w else w for w in s.split())


_LOCAL_SECTION_ATTRS: dict[str, dict] = {
    "STORIES LEADING TO THE DARK TOWER": {
        "group": "The Dark Tower", "kind": "connection", "role": "supplemental",
    },
    "THE DARK TOWER": {
        "group": "The Dark Tower", "kind": "series", "role": "core",
    },
    "DARK TOWER TIE-IN MATERIAL": {
        "group": "The Dark Tower", "kind": "tie_in", "role": "supplemental",
    },
    "THE BACHMAN BOOKS": {
        "group": "The Bachman Books", "kind": "connection", "role": "core",
    },
}

_LOCAL_SKIP_SECTIONS = {"NOVELS", "NOVELLAS", "SHORT STORIES", "MOVIES", "MINISERIES"}
_LOCAL_SUBGROUP_SECTIONS = {"DUOLOGIES", "TRILOGIES", "TRIOLOGIES"}
_LOCAL_GENRE_SECTION = "CATEGORIZED BY GENRE"

_SECTION_TYPE_SKIP     = "skip"
_SECTION_TYPE_SUBGROUP = "subgroup"
_SECTION_TYPE_GENRE    = "genre"


def parse_local_text_file(
    path: Path,
    catalog_titles: list[str],
    verbose: bool = False,
) -> dict[str, list[dict]]:
    """
    Parse a local seed text file into candidate connection groups.
    Detects section headings and maps titles to structured connection records.
    Returns {title_lower: [connection_dict, ...]}.
    """
    norm_map: dict[str, str] = {
        _normalize_for_match(t): t for t in catalog_titles if t
    }

    results: dict[str, list[dict]] = {}
    sections_found: list[str] = []
    groups_found: set[str] = set()

    current_section_type: str = _SECTION_TYPE_SKIP
    current_attrs: dict | None = None

    def _add_candidate(title_lower: str, attrs: dict) -> None:
        group = attrs["group"]
        bucket = results.setdefault(title_lower, [])
        if not any(c["group"] == group for c in bucket):
            bucket.append({
                "group": group,
                "kind": attrs["kind"],
                "role": attrs["role"],
                "order": None,
                "spoiler": False,
                "note": f"Candidate from local seed: {path.name}",
                "review_flag": "needs_verification_connections",
            })
            groups_found.add(group)

    def _match_title(line: str) -> str | None:
        m = re.match(r"^(.+?)\s*\(\d{4}\)", line)
        raw = (m.group(1) if m else line.split(" - ")[0]).strip().strip("'\"")
        if not raw or len(raw) < 2:
            return None
        norm = _normalize_for_match(raw)
        orig = norm_map.get(norm)
        if orig:
            return orig.lower()
        # Fallback: allow "The Gunslinger" to match "The Dark Tower: The Gunslinger"
        if len(norm) >= 6:
            for catalog_norm, catalog_orig in norm_map.items():
                if catalog_norm.endswith(norm):
                    return catalog_orig.lower()
        return None

    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("="):
            continue

        if not _is_section_heading(line):
            # Attempt title match in active sections
            if current_attrs is not None:
                clean = re.sub(r"[^\x00-\x7F]", "", line).strip()
                matched = _match_title(clean)
                if matched:
                    _add_candidate(matched, current_attrs)
            continue

        canonical = _clean_section_canonical(line)

        # Known major section with explicit attrs
        if canonical in _LOCAL_SECTION_ATTRS:
            current_section_type = canonical
            current_attrs = _LOCAL_SECTION_ATTRS[canonical]
            if canonical not in sections_found:
                sections_found.append(canonical)
            continue

        # Skip sections (novels, movies, etc.)
        if any(canonical == s or canonical.startswith(s) or s in canonical
               for s in _LOCAL_SKIP_SECTIONS):
            current_section_type = _SECTION_TYPE_SKIP
            current_attrs = None
            if canonical not in sections_found:
                sections_found.append(canonical)
            continue

        # Duologies / trilogies top-level section
        if any(s in canonical for s in _LOCAL_SUBGROUP_SECTIONS):
            current_section_type = _SECTION_TYPE_SUBGROUP
            current_attrs = None
            if canonical not in sections_found:
                sections_found.append(canonical)
            continue

        # Genre section
        if _LOCAL_GENRE_SECTION in canonical or canonical == _LOCAL_GENRE_SECTION:
            current_section_type = _SECTION_TYPE_GENRE
            current_attrs = None
            if canonical not in sections_found:
                sections_found.append(canonical)
            continue

        # Named sub-group within duologies/trilogies
        if current_section_type == _SECTION_TYPE_SUBGROUP:
            kind = "trilogy" if "TRILOG" in canonical else "series"
            group_name = _to_title_case(canonical)
            current_attrs = {"group": group_name, "kind": kind, "role": "core"}
            if canonical not in sections_found:
                sections_found.append(canonical)
            continue

        # Genre sub-heading (Horror, Mystery, etc.)
        if current_section_type == _SECTION_TYPE_GENRE:
            group_name = _to_title_case(canonical)
            current_attrs = {"group": group_name, "kind": "connection", "role": "supplemental"}
            if canonical not in sections_found:
                sections_found.append(canonical)
            continue

        # Unknown heading — stop collecting
        current_section_type = _SECTION_TYPE_SKIP
        current_attrs = None
        if canonical not in sections_found:
            sections_found.append(canonical)

    if verbose:
        print(f"  [LocalSeed] loaded: {path}", flush=True)
        print(f"  [LocalSeed] sections found: {sections_found}", flush=True)
        print(f"  [LocalSeed] candidate groups: {sorted(groups_found)}", flush=True)
        print(f"  [LocalSeed] candidate titles: {len(results)}", flush=True)

    return results


# ─────────────────────────────────────────────────────────────────────────────
# Entry helpers
# ─────────────────────────────────────────────────────────────────────────────

def cover_already_done(entry: dict) -> bool:
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


def repair_missing_cover_paths(entries: list[dict]) -> int:
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


def _matches_title_filter(entry: dict, title_filter: set[str] | None) -> bool:
    if title_filter is None:
        return True
    entry_title = (entry.get("title") or "").lower()
    return any(t.lower() in entry_title for t in title_filter)


def _matches_author_filter(entry: dict, author_filter: set[str] | None) -> bool:
    if author_filter is None:
        return True
    entry_author = (entry.get("author") or "Stephen King").lower()
    return any(a.lower() in entry_author for a in author_filter)


# ─────────────────────────────────────────────────────────────────────────────
# Field-level enrichment functions
# ─────────────────────────────────────────────────────────────────────────────

def enrich_covers(
    entry: dict,
    force: bool,
    blanks_only: bool,
    verbose: bool,
    dry_run: bool,
) -> dict:
    """Enrich cover image.  Returns stats dict."""
    stats = {"cover_resolved": 0, "cover_skipped": 0, "cover_failed": 0}

    if not force and not dry_run and cover_already_done(entry):
        stats["cover_skipped"] = 1
        return stats

    title  = entry.get("title") or ""
    year   = entry.get("year")
    author = entry.get("author") or "Stephen King"
    is_parent  = entry.get("is_collection_parent", False)
    is_bachman = entry.get("as_bachman", False)

    search_title  = title
    search_author = author
    if is_bachman:
        search_title  = re.sub(r"\s*\(Bachman\)\s*$", "", title, flags=re.IGNORECASE).strip()
        search_author = "Richard Bachman"

    doc = ol_search(search_title, year, author=search_author, use_author_filter=(not is_parent))
    time.sleep(REQUEST_DELAY)

    if not doc:
        stats["cover_failed"] = 1
        return stats

    cover_id: int | None = doc.get("cover_i")
    ol_pages: int | None = doc.get("number_of_pages_median")

    if ol_pages and entry.get("word_count") is None:
        if not dry_run:
            entry["word_count"] = round(ol_pages * WORDS_PER_PAGE)
            remove_flag(entry, "missing_word_count")
            add_flag(entry, "estimated_from_page_count")
        if verbose:
            print(f"        word_count estimated from {ol_pages} pages", flush=True)

    if not cover_id:
        stats["cover_failed"] = 1
        return stats

    cover_url = OL_COVER.format(cover_id=cover_id)
    if not dry_run:
        entry["cover_candidate_url"] = cover_url
        entry["cover_source"] = "open_library"
        entry["cover_verified"] = False

    fname = safe_filename(title)
    dest  = COVERS_DIR / fname
    if not dry_run and download_cover(cover_id, dest):
        entry["cover_local_path"] = f"data-tools/enriched/covers/{fname}"
        stats["cover_resolved"] = 1
    elif dry_run:
        stats["cover_resolved"] = 1  # assume would succeed
    else:
        stats["cover_failed"] = 1

    return stats


def enrich_goodreads(
    entry: dict,
    force: bool,
    blanks_only: bool,
    verbose: bool,
    dry_run: bool,
) -> dict:
    """Enrich Goodreads rating.  Returns stats dict."""
    stats = {"gr_filled": 0, "gr_skipped": 0}

    has_rating = entry.get("goodreads_rating") is not None
    if not force and (blanks_only or has_rating) and has_rating:
        stats["gr_skipped"] = 1
        return stats

    title      = entry.get("title") or ""
    year       = entry.get("year")
    is_bachman = bool(entry.get("as_bachman"))
    author     = "Richard Bachman" if is_bachman else (entry.get("author") or "Stephen King")

    if dry_run:
        print(f"        [dry-run] would fetch Goodreads rating for {title!r}", flush=True)
        return stats

    if force:
        entry.pop("goodreads_rating", None)
        entry.pop("goodreads_ratings_count", None)

    rating, count = fetch_goodreads_rating(
        title, author, year=year, is_bachman=is_bachman, verbose=verbose,
    )
    if rating is not None:
        entry["goodreads_rating"] = rating
        entry["goodreads_ratings_count"] = count
        stats["gr_filled"] = 1
        if verbose:
            print(f"        => {rating} ({count} ratings)", flush=True)
    return stats


def enrich_description(
    entry: dict,
    force: bool,
    blanks_only: bool,
    verbose: bool,
    dry_run: bool,
    sources: set[str] | None = None,
) -> dict:
    """Enrich description via cascade: Wikipedia → Goodreads → OpenLibrary."""
    stats = {"desc_filled": 0, "desc_skipped": 0}

    has_desc = bool(entry.get("description"))
    if not force and (blanks_only or has_desc) and has_desc:
        stats["desc_skipped"] = 1
        return stats

    title      = entry.get("title") or ""
    author     = entry.get("author") or "Stephen King"
    year       = entry.get("year")
    is_bachman = bool(entry.get("as_bachman"))
    if is_bachman:
        author = "Richard Bachman"

    if sources is None:
        sources = {"wikipedia", "goodreads", "openlibrary"}

    normalized = _normalize_title_for_lookup(title)
    if verbose:
        print(f"      [desc] normalized lookup title: {normalized!r}", flush=True)

    if dry_run:
        print(f"        [dry-run] would fetch description for {title!r} via {sorted(sources)}", flush=True)
        return stats

    summary: str | None = None
    used_source: str = ""

    # 1. Wikipedia
    if "wikipedia" in sources:
        if verbose:
            print(f"      [desc] trying Wikipedia …", flush=True)
        summary = fetch_wikipedia_summary(title, author, verbose=verbose)
        time.sleep(WP_REQUEST_DELAY)
        if summary:
            used_source = "wikipedia"

    # 2. Goodreads
    if not summary and "goodreads" in sources:
        if verbose:
            print(f"      [desc] Wikipedia failed — trying Goodreads …", flush=True)
        summary = fetch_goodreads_description(title, author, year=year, is_bachman=is_bachman, verbose=verbose)
        if summary:
            used_source = "goodreads"

    # 3. Open Library
    if not summary and "openlibrary" in sources:
        if verbose:
            print(f"      [desc] Goodreads failed — trying Open Library …", flush=True)
        summary = fetch_openlibrary_description(title, author, year=year, verbose=verbose)
        if summary:
            used_source = "openlibrary"

    if summary:
        entry["description"] = summary
        entry["description_source"] = used_source
        remove_flag(entry, "missing_description")
        stats["desc_filled"] = 1
        if verbose:
            print(f"      [desc] filled from {used_source}", flush=True)
    else:
        add_flag(entry, "missing_description")
        if verbose:
            print(f"      [desc] unresolved — all sources failed for {title!r}", flush=True)

    return stats


def enrich_keywords(
    entry: dict,
    force: bool,
    blanks_only: bool,
    verbose: bool,
    dry_run: bool,
    sources: set[str] | None = None,
) -> dict:
    """Enrich keywords via cascade: Wikipedia → Goodreads → OpenLibrary."""
    stats = {"kw_filled": 0, "kw_skipped": 0}

    existing = entry.get("keywords") or []
    if not force and blanks_only and existing:
        stats["kw_skipped"] = 1
        return stats

    title      = entry.get("title") or ""
    author     = entry.get("author") or "Stephen King"
    year       = entry.get("year")
    is_bachman = bool(entry.get("as_bachman"))
    if is_bachman:
        author = "Richard Bachman"

    if sources is None:
        sources = {"wikipedia", "goodreads", "openlibrary"}

    if dry_run:
        print(f"        [dry-run] would fetch keywords for {title!r} via {sorted(sources)}", flush=True)
        return stats

    new_kws: list[str] = []
    used_source: str = ""

    # 1. Wikipedia
    if "wikipedia" in sources:
        if verbose:
            print(f"      [kw] trying Wikipedia …", flush=True)
        new_kws = fetch_wikipedia_keywords(title, author, verbose=verbose)
        time.sleep(WP_REQUEST_DELAY)
        if new_kws:
            used_source = "wikipedia"

    # 2. Goodreads
    if not new_kws and "goodreads" in sources:
        if verbose:
            print(f"      [kw] Wikipedia failed — trying Goodreads …", flush=True)
        new_kws = fetch_goodreads_keywords(title, author, year=year, is_bachman=is_bachman, verbose=verbose)
        if new_kws:
            used_source = "goodreads"

    # 3. Open Library
    if not new_kws and "openlibrary" in sources:
        if verbose:
            print(f"      [kw] Goodreads failed — trying Open Library …", flush=True)
        new_kws = fetch_openlibrary_keywords(title, author, year=year, verbose=verbose)
        if new_kws:
            used_source = "openlibrary"

    if new_kws:
        merged = list(dict.fromkeys(existing + new_kws))
        entry["keywords"] = merged
        entry["keywords_source"] = used_source
        stats["kw_filled"] = 1
        if verbose:
            print(f"      [kw] filled from {used_source}: {new_kws[:6]}", flush=True)
    return stats


def enrich_connections(
    entry: dict,
    candidate_map: dict[str, list[dict]],
    force: bool,
    blanks_only: bool,
    verbose: bool,
    dry_run: bool,
    include_spoilers: bool,
) -> dict:
    """
    Merge candidate connections from Reddit into the entry.
    Only adds candidates; never removes existing connections.
    Returns stats dict.
    """
    stats = {"conn_added": 0, "conn_skipped": 0}

    title_lower = (entry.get("title") or "").lower()
    candidates = candidate_map.get(title_lower) or []

    if not candidates:
        return stats

    existing: list[dict] = entry.get("connections") or []
    existing_groups = {c.get("group", "").lower() for c in existing}

    new_conns = []
    for cand in candidates:
        if not include_spoilers and cand.get("spoiler"):
            continue
        if cand["group"].lower() in existing_groups:
            continue
        new_conns.append(cand)

    if not new_conns:
        stats["conn_skipped"] = len(candidates)
        return stats

    if dry_run:
        print(f"        [dry-run] would add {len(new_conns)} connection candidate(s)", flush=True)
        return stats

    entry["connections"] = existing + new_conns
    add_flag(entry, "needs_review_connections")
    stats["conn_added"] = len(new_conns)
    return stats


# ─────────────────────────────────────────────────────────────────────────────
# CSV output
# ─────────────────────────────────────────────────────────────────────────────

def write_csv(all_entries: list[dict]) -> None:
    needs_review = [e for e in all_entries if e.get("review_status") == "needs_review"]
    with CSV_PATH.open("w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=CSV_FIELDS, extrasaction="ignore")
        writer.writeheader()
        for e in needs_review:
            row = {k: e.get(k) for k in CSV_FIELDS}
            row["review_flags"] = "; ".join(e.get("review_flags") or [])
            writer.writerow(row)


# ─────────────────────────────────────────────────────────────────────────────
# Per-file enrichment
# ─────────────────────────────────────────────────────────────────────────────

def enrich_file(
    path: Path,
    label: str,
    fields: set[str],
    sources: set[str],
    title_filter: set[str] | None,
    author_filter: set[str] | None,
    force: bool,
    blanks_only: bool,
    verbose: bool,
    dry_run: bool,
    include_spoilers: bool,
    candidate_connections: dict[str, list[dict]],
) -> tuple[list[dict], dict]:
    """Load, enrich, and write-back a single JSON catalog file."""
    entries: list[dict] = json.loads(path.read_text(encoding="utf-8"))
    count = len(entries)

    if "covers" in fields:
        repaired = repair_missing_cover_paths(entries)
        if repaired:
            print(f"  Repaired {repaired} missing cover paths from existing files")

    totals: dict[str, int] = {
        "cover_resolved": 0, "cover_skipped": 0, "cover_failed": 0,
        "wc_filled": 0,
        "gr_filled": 0, "gr_skipped": 0,
        "desc_filled": 0, "desc_skipped": 0,
        "kw_filled": 0, "kw_skipped": 0,
        "conn_added": 0, "conn_skipped": 0,
        "processed": 0, "skipped": 0,
    }

    for i, entry in enumerate(entries, 1):
        title  = entry.get("title", "?")
        prefix = f"  [{i:>3}/{count}]"

        # Apply targeting filters
        title_match  = _matches_title_filter(entry, title_filter)
        author_match = _matches_author_filter(entry, author_filter)
        if not (title_match and author_match):
            totals["skipped"] += 1
            continue

        print(f"{prefix} {title}", flush=True)
        totals["processed"] += 1

        status_parts: list[str] = []

        if "covers" in fields:
            stats = enrich_covers(entry, force=force, blanks_only=blanks_only,
                                  verbose=verbose, dry_run=dry_run)
            for k, v in stats.items():
                totals[k] += v
            if stats["cover_resolved"]:
                status_parts.append("cover ok")
            elif stats["cover_skipped"]:
                status_parts.append("cover already done")
            else:
                status_parts.append("cover failed")

        if "goodreads" in fields:
            stats = enrich_goodreads(entry, force=force, blanks_only=blanks_only,
                                     verbose=verbose, dry_run=dry_run)
            for k, v in stats.items():
                totals[k] += v
            if stats.get("gr_filled"):
                status_parts.append("goodreads ok")

        if "descriptions" in fields:
            stats = enrich_description(entry, force=force, blanks_only=blanks_only,
                                       verbose=verbose, dry_run=dry_run, sources=sources)
            for k, v in stats.items():
                totals[k] += v
            if stats.get("desc_filled"):
                status_parts.append("description filled")

        if "keywords" in fields:
            stats = enrich_keywords(entry, force=force, blanks_only=blanks_only,
                                    verbose=verbose, dry_run=dry_run, sources=sources)
            for k, v in stats.items():
                totals[k] += v
            if stats.get("kw_filled"):
                status_parts.append("keywords filled")

        if "connections" in fields:
            stats = enrich_connections(
                entry, candidate_connections,
                force=force, blanks_only=blanks_only, verbose=verbose,
                dry_run=dry_run, include_spoilers=include_spoilers,
            )
            for k, v in stats.items():
                totals[k] += v
            if stats.get("conn_added"):
                status_parts.append(f"{stats['conn_added']} connections added")

        recalculate_review_status(entry)
        print(f"       → {', '.join(status_parts) or 'no changes'}", flush=True)

    if not dry_run:
        path.write_text(json.dumps(entries, indent=2, ensure_ascii=False), encoding="utf-8")

    return entries, totals


# ─────────────────────────────────────────────────────────────────────────────
# Review-only output
# ─────────────────────────────────────────────────────────────────────────────

def print_review(all_entries: list[dict]) -> None:
    needs_review = [e for e in all_entries if e.get("review_status") == "needs_review"]
    if not needs_review:
        print("No entries flagged for review.")
        return

    print(f"\n{'─'*60}")
    print(f"  REVIEW FLAGS  ({len(needs_review)} entries)")
    print(f"{'─'*60}")
    for e in sorted(needs_review, key=lambda x: (x.get("author") or "", x.get("title") or "")):
        flags = "; ".join(e.get("review_flags") or [])
        author = e.get("author") or "Stephen King"
        title  = e.get("title") or "?"
        print(f"  [{author}] {title}")
        print(f"    Flags: {flags}")
    print(f"{'─'*60}\n")


# ─────────────────────────────────────────────────────────────────────────────
# Entry point
# ─────────────────────────────────────────────────────────────────────────────

def main() -> int:
    parser = build_parser()
    args   = parser.parse_args()

    # ── Validate targeting ───────────────────────────────────────────────────
    has_target = bool(args.titles or args.authors or args.all_authors or args.review_only)
    if not has_target:
        parser.error(
            "Specify a target: --title <TITLE>, --author <AUTHOR>, --all-authors, "
            "or --review-only.\n\nRun with --help for examples."
        )

    fields  = resolve_fields(args)
    sources = resolve_sources(args, fields)

    blanks_only = args.blanks_only or args.once_only_curation

    # ── Validate connections flags ───────────────────────────────────────────
    has_text_files = bool(args.reddit_text_files)
    if "connections" in fields and not args.reddit_urls and not has_text_files:
        print(
            "INFO: --fields connections without --reddit-thread-url or --reddit-text-file will only "
            "review/flag existing connection fields.",
            flush=True,
        )

    # ── Print run plan ───────────────────────────────────────────────────────
    print()
    print("King Catalog Enrichment Tool")
    print("=" * 44)
    if args.dry_run:
        print("  *** DRY RUN — no files will be modified ***")
    if args.all_authors:
        print("  Target:       all authors")
    elif args.authors:
        print(f"  Target:       authors {args.authors}")
    elif args.titles:
        print(f"  Target:       titles {args.titles}")
    print(f"  Fields:       {', '.join(sorted(fields))}")
    print(f"  Sources:      {', '.join(sorted(sources)) or 'none'}")
    print(f"  Blanks only:  {blanks_only}")
    print(f"  Force:        {args.force}")
    print(f"  Verbose:      {args.verbose}")
    if args.reddit_urls:
        print(f"  Reddit URLs:  {args.reddit_urls}")
    if args.reddit_text_files:
        print(f"  Text files:   {args.reddit_text_files}")
    print()

    # ── Source file collection ───────────────────────────────────────────────
    COVERS_DIR.mkdir(parents=True, exist_ok=True)

    source_files: list[tuple[Path, str]] = [(SK_JSON, "Stephen King")]
    for p in sorted(RAW.glob(OTHER_AUTHOR_GLOB)):
        stem = p.stem.replace("-source", "").replace("-", " ").title()
        source_files.append((p, stem))

    # ── Collect all entries for review-only mode ─────────────────────────────
    if args.review_only:
        all_entries: list[dict] = []
        for path, label in source_files:
            if path.exists():
                all_entries.extend(json.loads(path.read_text(encoding="utf-8")))
        print_review(all_entries)
        print(f"Total entries loaded: {len(all_entries)}")
        return 0

    # ── Resolve targeting filters ────────────────────────────────────────────
    title_filter:  set[str] | None = set(args.titles)  if args.titles  else None
    author_filter: set[str] | None = set(args.authors) if args.authors else None
    if args.all_authors:
        title_filter  = None
        author_filter = None

    # ── Fetch/parse connection candidates ────────────────────────────────────
    candidate_connections: dict[str, list[dict]] = {}
    if "connections" in fields and (args.reddit_urls or args.reddit_text_files):
        # Build full catalog title list for cross-referencing
        all_catalog_titles: list[str] = []
        for path, _ in source_files:
            if path.exists():
                entries_tmp = json.loads(path.read_text(encoding="utf-8"))
                all_catalog_titles.extend(e.get("title", "") for e in entries_tmp)

        if args.reddit_text_files:
            print("Parsing local text file(s) for connections seed …")
            for txt_path_str in args.reddit_text_files:
                txt_path = Path(txt_path_str)
                if not txt_path.exists():
                    print(f"  WARNING: text file not found: {txt_path}")
                    continue
                print(f"  {txt_path}")
                candidates = parse_local_text_file(
                    txt_path, all_catalog_titles, verbose=args.verbose,
                )
                for title_key, conns in candidates.items():
                    candidate_connections.setdefault(title_key, []).extend(conns)
            print(f"  Candidates found for {len(candidate_connections)} title(s) from local file(s).")
            print()

        if args.reddit_urls:
            print("Fetching Reddit thread(s) for connections seed …")
            for url in args.reddit_urls:
                print(f"  {url}")
                comments = fetch_reddit_comments(url, verbose=args.verbose)
                if comments:
                    candidates = extract_connection_candidates(
                        comments, all_catalog_titles, url, verbose=args.verbose,
                    )
                    for title_key, conns in candidates.items():
                        candidate_connections.setdefault(title_key, []).extend(conns)
            print(f"  Candidates found for {len(candidate_connections)} title(s) total.")
            print()

    # ── Per-file enrichment loop ─────────────────────────────────────────────
    all_entries = []
    grand_totals: dict[str, int] = {
        "cover_resolved": 0, "cover_skipped": 0, "cover_failed": 0,
        "wc_filled": 0,
        "gr_filled": 0, "gr_skipped": 0,
        "desc_filled": 0, "desc_skipped": 0,
        "kw_filled": 0, "kw_skipped": 0,
        "conn_added": 0, "conn_skipped": 0,
        "processed": 0, "skipped": 0,
    }

    for path, label in source_files:
        if not path.exists():
            print(f"[SKIP] {path} not found")
            continue
        print(f"── {label} ({path.name}) ──")
        entries, totals = enrich_file(
            path=path,
            label=label,
            fields=fields,
            sources=sources,
            title_filter=title_filter,
            author_filter=author_filter,
            force=args.force,
            blanks_only=blanks_only,
            verbose=args.verbose,
            dry_run=args.dry_run,
            include_spoilers=args.include_spoilers,
            candidate_connections=candidate_connections,
        )
        all_entries.extend(entries)
        for k, v in totals.items():
            grand_totals[k] += v
        print()

    # ── Review CSV ───────────────────────────────────────────────────────────
    if not args.dry_run:
        print(f"Writing {CSV_PATH} …")
        write_csv(all_entries)

    # ── Summary ──────────────────────────────────────────────────────────────
    needs_review_count = sum(1 for e in all_entries if e.get("review_status") == "needs_review")

    print()
    print("═" * 44)
    print("  Enrichment Summary")
    print("═" * 44)
    if args.dry_run:
        print("  *** DRY RUN — no files modified ***")
    print(f"  Entries processed:    {grand_totals['processed']}")
    print(f"  Entries skipped:      {grand_totals['skipped']}  (not targeted)")
    if "covers" in fields:
        print(f"  Covers resolved:      {grand_totals['cover_resolved']}")
        print(f"  Covers already done:  {grand_totals['cover_skipped']}")
        print(f"  Covers failed:        {grand_totals['cover_failed']}")
        print(f"  Word counts filled:   {grand_totals['wc_filled']}  (estimated from page count)")
    if "goodreads" in fields:
        print(f"  Goodreads ratings:    {grand_totals['gr_filled']}  (fetched)")
    if "descriptions" in fields:
        print(f"  Descriptions filled:  {grand_totals['desc_filled']}")
    if "keywords" in fields:
        print(f"  Keyword sets filled:  {grand_totals['kw_filled']}")
    if "connections" in fields:
        print(f"  Connections added:    {grand_totals['conn_added']}  (needs review)")
    print(f"  Still needs review:   {needs_review_count}")
    print("═" * 44)
    print()

    # ── Print any review flags if verbose ────────────────────────────────────
    if args.verbose and needs_review_count:
        print_review(all_entries)

    # ── Rebuild combined catalog ─────────────────────────────────────────────
    if not args.no_rebuild_seed and not args.dry_run:
        build_script = Path(__file__).parent / "build_catalog.py"
        if build_script.exists():
            print("Rebuilding combined catalog …")
            result = subprocess.run([sys.executable, str(build_script)], check=False)
            if result.returncode != 0:
                print("  WARNING: build_catalog.py exited with errors.")
            print()
        else:
            print(f"WARNING: {build_script} not found — run it manually to refresh assets.")

    return 0


if __name__ == "__main__":
    sys.exit(main())
