#!/usr/bin/env python3
"""
deploy_catalog.py — Build catalog and bump CATALOG_VERSION so the app re-seeds.

Run this after any enrichment (enrich.py, download_missing_covers.py, manual edits).
It does two things:
  1. Runs build_catalog.py to merge all sources → app/app/src/main/assets/king_catalog.json
  2. Bumps CATALOG_VERSION in KingCatalogDatabase.kt so the app's upsert runs on next launch

User data (owned/read/reading-list/binding/notes) is NEVER touched — the app's
upsertCatalogData preserves all user fields and only updates catalog fields.
"""

import re
import subprocess
import sys
import os

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

DB_KT = os.path.join(
    REPO_ROOT,
    "app", "app", "src", "main", "java",
    "com", "mattnicol", "kingcatalog",
    "data", "db", "KingCatalogDatabase.kt",
)
BUILD_SCRIPT = os.path.join(REPO_ROOT, "data-tools", "scripts", "build_catalog.py")


def bump_catalog_version() -> tuple[int, int]:
    with open(DB_KT, encoding="utf-8") as f:
        text = f.read()

    m = re.search(r"(const val CATALOG_VERSION\s*=\s*)(\d+)", text)
    if not m:
        raise RuntimeError(f"CATALOG_VERSION not found in {DB_KT}")

    old = int(m.group(2))
    new = old + 1
    updated = text[: m.start(2)] + str(new) + text[m.end(2):]

    with open(DB_KT, "w", encoding="utf-8") as f:
        f.write(updated)

    return old, new


def main():
    print("=== Step 1: build_catalog.py ===")
    result = subprocess.run([sys.executable, BUILD_SCRIPT], check=False)
    if result.returncode != 0:
        print("build_catalog.py failed — aborting.", file=sys.stderr)
        sys.exit(1)

    print("\n=== Step 2: bump CATALOG_VERSION ===")
    old, new = bump_catalog_version()
    print(f"CATALOG_VERSION: {old} → {new}")
    print(f"Updated: {DB_KT}")

    print("\nDone. Rebuild the app and install — enriched data will appear on next launch.")
    print("User data (owned/read/list/binding) is preserved by upsertCatalogData.")


if __name__ == "__main__":
    main()
