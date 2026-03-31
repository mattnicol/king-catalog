#!/usr/bin/env python3
"""
build_catalog.py — Merge all author source files into a single catalog JSON.

Reads:
  data-tools/enriched/king_catalog.json  (Stephen King, already enriched)
  data-tools/raw/josh-malerman-source.json
  data-tools/raw/joe-hill-source.json
  data-tools/raw/grady-hendrix-source.json

Writes:
  data-tools/enriched/all_catalog.json   (combined, app-compatible)
  app/app/src/main/assets/king_catalog.json  (same file, copied to assets)
"""

import json
import os
import shutil

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

KING_JSON      = os.path.join(REPO_ROOT, "data-tools", "enriched", "king_catalog.json")
MALERMAN_JSON  = os.path.join(REPO_ROOT, "data-tools", "raw", "josh-malerman-source.json")
HILL_JSON      = os.path.join(REPO_ROOT, "data-tools", "raw", "joe-hill-source.json")
HENDRIX_JSON   = os.path.join(REPO_ROOT, "data-tools", "raw", "grady-hendrix-source.json")

OUT_ENRICHED   = os.path.join(REPO_ROOT, "data-tools", "enriched", "all_catalog.json")
OUT_ASSETS     = os.path.join(REPO_ROOT, "app", "app", "src", "main", "assets", "king_catalog.json")


def load(path: str) -> list:
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    # Ensure every SK entry has an explicit author field (older file may omit it)
    if "king_catalog" in path:
        for entry in data:
            entry.setdefault("author", "Stephen King")
    return data


def check_id_collisions(combined: list) -> None:
    ids = [e["id"] for e in combined]
    dupes = [i for i in ids if ids.count(i) > 1]
    if dupes:
        raise ValueError(f"Duplicate IDs detected: {sorted(set(dupes))}")


def main():
    king     = load(KING_JSON)
    malerman = load(MALERMAN_JSON)
    hill     = load(HILL_JSON)
    hendrix  = load(HENDRIX_JSON)

    combined = king + malerman + hill + hendrix
    check_id_collisions(combined)

    # Sort by author then year for readability
    combined.sort(key=lambda e: (e.get("author", ""), e.get("year") or 0))

    with open(OUT_ENRICHED, "w", encoding="utf-8") as f:
        json.dump(combined, f, indent=2, ensure_ascii=False)
    print(f"Wrote {len(combined)} entries → {OUT_ENRICHED}")

    shutil.copy2(OUT_ENRICHED, OUT_ASSETS)
    print(f"Copied to assets → {OUT_ASSETS}")

    # Summary per author
    from collections import Counter
    counts = Counter(e.get("author", "Unknown") for e in combined)
    for author, n in sorted(counts.items()):
        print(f"  {author}: {n} entries")


if __name__ == "__main__":
    main()
