#!/usr/bin/env python3
"""Verification script for the app seed catalog data.

Prints:
- which seed file is used
- total imported rows
- author counts after import
- simulated user-state preservation count (0 on fresh import)
- whether other authors are present
"""

import json
import os
import sys

SEED_PATH = "app/app/src/main/assets/king_catalog.json"
FALLBACK_PATH = "app/src/main/assets/king_catalog.json"

def main():
    seed_file = SEED_PATH if os.path.exists(SEED_PATH) else FALLBACK_PATH
    if not os.path.exists(seed_file):
        print(f"ERROR: seed file not found at {SEED_PATH}", file=sys.stderr)
        sys.exit(1)

    with open(seed_file) as f:
        data = json.load(f)

    print(f"SEED SOURCE          : {seed_file}")
    print(f"TOTAL ROWS           : {len(data)}")

    author_counts = {}
    for entry in data:
        author = entry.get("author", "Stephen King")
        author_counts[author] = author_counts.get(author, 0) + 1

    print(f"AUTHOR COUNTS        :")
    for author, count in sorted(author_counts.items(), key=lambda x: -x[1]):
        print(f"  {author:<30} {count}")

    other_authors = [a for a in author_counts if a not in ("Stephen King", "Richard Bachman")]
    print(f"OTHER AUTHORS PRESENT: {bool(other_authors)} — {other_authors}")

    # Simulate upsert: on a fresh DB, all rows are inserts (0 user state preserved).
    # In a live app, rows with prior user state would be reported by UpsertResult.
    print(f"USER STATE PRESERVED : N/A (run from device logcat for live counts)")

    # Sanity checks
    errors = []
    if len(data) < 250:
        errors.append(f"WARN: expected >= 250 entries (merged catalog), got {len(data)}")
    if not other_authors:
        errors.append("ERROR: no other authors found — seed is King-only, merge failed")
    for entry in data:
        if "id" not in entry:
            errors.append(f"ERROR: entry missing id: {entry.get('title')}")
        if "title" not in entry:
            errors.append(f"ERROR: entry missing title: {entry}")

    if errors:
        print("\nVERIFICATION ISSUES:")
        for e in errors:
            print(f"  {e}")
        sys.exit(1)
    else:
        print("\nVERIFICATION PASSED")

if __name__ == "__main__":
    main()
