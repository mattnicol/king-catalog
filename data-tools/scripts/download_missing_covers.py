#!/usr/bin/env python3
"""
Download covers for specific titles missing cover_local_path.
Uses Open Library search API and download patterns from enrich.py.
"""

import hashlib
import json
import time
import urllib.parse
import urllib.request
from pathlib import Path

ROOT         = Path(__file__).parent.parent
PROJECT_ROOT = ROOT.parent
ENRICHED     = ROOT / "enriched"
RAW          = ROOT / "raw"
COVERS_DIR   = ENRICHED / "covers"
COVERS_DIR.mkdir(exist_ok=True)

OL_SEARCH = "https://openlibrary.org/search.json"
OL_COVER  = "https://covers.openlibrary.org/b/id/{cover_id}-L.jpg"
REQUEST_DELAY = 0.5

OL_PLACEHOLDER_HASHES = {
    "04b41f5dc246f9ed8bf27ade4999603a",
}


def safe_filename(title: str) -> str:
    cleaned = "".join(c if c.isalnum() or c in " -_" else "_" for c in title)
    return cleaned.strip().replace(" ", "_")[:80] + ".jpg"


def ol_search(title: str, year, author: str) -> dict | None:
    params_dict = {
        "title": title,
        "author": author.lower(),
        "fields": "key,title,first_publish_year,cover_i,number_of_pages_median",
        "limit": 5,
    }
    params = urllib.parse.urlencode(params_dict)
    try:
        with urllib.request.urlopen(f"{OL_SEARCH}?{params}", timeout=12) as resp:
            data = json.loads(resp.read().decode("utf-8"))
    except Exception as e:
        print(f"  OL search error: {e}")
        return None

    docs = data.get("docs") or []
    if not docs:
        # Try without author
        params_dict2 = {
            "title": title,
            "fields": "key,title,first_publish_year,cover_i,number_of_pages_median",
            "limit": 5,
        }
        params2 = urllib.parse.urlencode(params_dict2)
        try:
            with urllib.request.urlopen(f"{OL_SEARCH}?{params2}", timeout=12) as resp:
                data2 = json.loads(resp.read().decode("utf-8"))
            docs = data2.get("docs") or []
        except Exception:
            return None

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
    except Exception as e:
        print(f"  Download error: {e}")
        return False
    if len(data) < 10_000:
        print(f"  File too small ({len(data)} bytes), likely placeholder")
        return False
    if hashlib.md5(data).hexdigest() in OL_PLACEHOLDER_HASHES:
        print(f"  Placeholder hash detected")
        return False
    dest.write_bytes(data)
    return True


TARGETS = [
    {
        "file": RAW / "grady-hendrix-source.json",
        "id": 3007,
        "title": "How to Sell a Haunted House",
        "author": "Grady Hendrix",
        "year": 2023,
    },
    {
        "file": RAW / "josh-malerman-source.json",
        "id": 1011,
        "title": "The Pallbearers Club",
        "author": "Josh Malerman",
        "year": 2022,
    },
    {
        "file": RAW / "josh-malerman-source.json",
        "id": 1009,
        "title": "On This, the Day of the Pig",
        "author": "Josh Malerman",
        "year": 2021,
    },
    {
        "file": ENRICHED / "king_catalog.json",
        "id": 231,
        "title": "Witchcraft for Wayward Girls",
        "author": "Stephen King",
        "year": 2025,
    },
]


def process_target(target: dict):
    title = target["title"]
    author = target["author"]
    year = target["year"]
    entry_id = target["id"]
    json_file = target["file"]

    print(f"\nProcessing: {title} ({author}, {year})")

    fname = safe_filename(title)
    dest = COVERS_DIR / fname

    if dest.exists():
        print(f"  Cover already exists: {fname}")
        # Still update json if needed
        rel_path = f"data-tools/enriched/covers/{fname}"
        update_json_cover(json_file, entry_id, dest, fname, rel_path)
        return

    # Search OL
    doc = ol_search(title, year, author)
    time.sleep(REQUEST_DELAY)

    if not doc:
        print(f"  No OL results found")
        return

    cover_id = doc.get("cover_i")
    if not cover_id:
        print(f"  No cover_i in OL result: {doc.get('title', 'unknown')}")
        return

    print(f"  Found OL doc: {doc.get('title', '?')} (cover_id={cover_id})")
    ok = download_cover(cover_id, dest)
    time.sleep(REQUEST_DELAY)

    if ok:
        print(f"  Downloaded: {fname}")
        rel_path = f"data-tools/enriched/covers/{fname}"
        update_json_cover(json_file, entry_id, dest, fname, rel_path, cover_id=cover_id)
    else:
        print(f"  Download failed for cover_id={cover_id}")


def update_json_cover(json_file: Path, entry_id: int, dest: Path, fname: str, rel_path: str, cover_id: int = None):
    with open(json_file, encoding="utf-8") as f:
        data = json.load(f)

    changed = False
    for entry in data:
        if entry.get("id") == entry_id:
            if not entry.get("cover_local_path") and dest.exists():
                entry["cover_local_path"] = rel_path
                if cover_id:
                    entry["cover_candidate_url"] = f"https://covers.openlibrary.org/b/id/{cover_id}-L.jpg"
                    entry["cover_source"] = "open_library"
                entry["cover_verified"] = False
                changed = True
                print(f"  Updated JSON: cover_local_path = {rel_path}")
            else:
                print(f"  JSON already has cover_local_path: {entry.get('cover_local_path')}")
            break
    else:
        print(f"  Entry id={entry_id} not found in {json_file}")

    if changed:
        with open(json_file, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
        print(f"  Saved {json_file}")


if __name__ == "__main__":
    for target in TARGETS:
        process_target(target)

    print("\nDone.")
