#!/usr/bin/env python3
"""
Parse the Stephen King source TSV into:
  - data-tools/enriched/king_catalog.json
  - data-tools/enriched/king_catalog_review.csv
"""

import csv
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).parent.parent
RAW = ROOT / "raw" / "stephen-king-source.txt"
ENRICHED = ROOT / "enriched"
JSON_OUT = ENRICHED / "king_catalog.json"
CSV_OUT = ENRICHED / "king_catalog_review.csv"

ENRICHED.mkdir(exist_ok=True)

# ---------------------------------------------------------------------------
# Known story-type tokens (first match in genres/keywords → story_type)
# ---------------------------------------------------------------------------
STORY_TYPE_TOKENS = {"novel", "novella", "short-story", "collection"}

# Section / column header patterns to skip
SECTION_NAMES = {
    "Alcohol and Cocaine Period",
    "Detox and Sober Period",
    "Post Hit-By-Van Period",
}
HEADER_RE = re.compile(r"^No\.\s+Name\s+Year")


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def is_header_line(line: str) -> bool:
    s = line.strip()
    if not s:
        return True
    if s == "Count":
        return True
    for name in SECTION_NAMES:
        if s.startswith(name):
            return True
    if HEADER_RE.match(s):
        return True
    return False


def parse_word_count(raw: str) -> tuple[int | None, list[str]]:
    """Return (count_int, review_flags)."""
    if not raw or not raw.strip():
        return None, ["missing_word_count"]
    s = raw.strip()
    # "About 30,000"
    m = re.match(r"[Aa]bout\s*([\d,]+)", s)
    if m:
        return int(m.group(1).replace(",", "")), ["approximate_word_count"]
    try:
        return int(s.replace(",", "")), []
    except ValueError:
        return None, [f"unparseable_word_count:{s}"]


def audible_to_minutes(
    raw: str, word_count: int | None, story_type: str | None
) -> tuple[float | None, list[str]]:
    """
    Convert audible duration string to total minutes.

    The source mixes two formats for 3-component strings:
      - HH:MM:SS  (long works, e.g. "47:47:00" ≈ 47 h for The Stand)
      - MM:SS:00  (short works in Bazaar of Bad Dreams, e.g. "37:55:00" ≈ 38 min)

    Disambiguation: if a word count is available, compare both interpretations
    against the expected duration at 150 wpm.  If no word count, fall back to
    story_type: novels → HH:MM:SS, short-story/collection_piece → MM:SS.
    """
    if not raw or not raw.strip():
        return None, []
    raw = raw.strip()
    parts = raw.split(":")
    try:
        if len(parts) == 2:
            h, m_ = int(parts[0]), int(parts[1])
            return float(h * 60 + m_), []

        if len(parts) == 3:
            a, b, c = int(parts[0]), int(parts[1]), int(parts[2])
            as_hms = a * 60 + b + c / 60.0   # treat as HH:MM:SS
            as_mss = a + b / 60.0             # treat as MM:SS (ignore c, usually 0)

            if word_count is not None:
                expected = word_count / 150.0  # minutes at 150 wpm
                diff_hms = abs(as_hms - expected)
                diff_mss = abs(as_mss - expected)
                if diff_mss < diff_hms:
                    return as_mss, ["audible_format_inferred_mmss"]
                return as_hms, []

            # No word count: use story_type heuristic
            if story_type in ("short_story", "collection_piece"):
                if a > 9:
                    return as_mss, ["audible_format_inferred_mmss"]
            return as_hms, []

        return None, [f"unparseable_audible:{raw}"]
    except (ValueError, IndexError):
        return None, [f"unparseable_audible:{raw}"]


def parse_genres(raw: str) -> tuple[str | None, list[str]]:
    """Return (story_type, keywords)."""
    if not raw or not raw.strip():
        return None, []
    tokens = raw.strip().split()
    story_type = None
    keywords = []
    for tok in tokens:
        tl = tok.lower()
        if tl in STORY_TYPE_TOKENS:
            if story_type is None:
                story_type = tl
            # don't add to keywords
        else:
            keywords.append(tl)
    return story_type, keywords


def normalize_story_type(raw_type: str | None) -> str | None:
    if raw_type is None:
        return None
    if raw_type == "short-story":
        return "short_story"
    if raw_type == "collection":
        return "collection_piece"
    return raw_type  # "novel", "novella"


def split_movie_cell(raw: str) -> list[str]:
    """
    A single movies cell sometimes contains multiple bare years separated
    by spaces (e.g. "2015 2016 2019").  Split those into individual items.
    Other entries (with parenthetical info) are returned as-is.
    """
    raw = raw.strip()
    if not raw:
        return []
    tokens = raw.split()
    if all(re.match(r"^\d{4}$", t) for t in tokens):
        return tokens  # each bare year is its own entry
    return [raw]


def parse_adaptations(raw_items: list[str]) -> list[dict]:
    """
    Parse adaptation entries from the collected movies column values.
    Each item in raw_items corresponds to one cell (primary row or
    continuation row).
    """
    entries = []

    # Expand bare-year clusters first
    expanded: list[str] = []
    for item in raw_items:
        expanded.extend(split_movie_cell(item))

    for line in expanded:
        line = line.strip()
        if not line:
            continue

        entry: dict = {
            "year": None,
            "year_end": None,
            "title": None,
            "format": None,
            "times_seen": None,
            "status": None,
            "want": False,
            "raw": line,
        }

        ll = line.lower()

        # Dollar Babies (short-film licensing program)
        if ll.startswith("dollar babies"):
            entry["status"] = "dollar_babies"
            entries.append(entry)
            continue

        in_dev = "in development" in ll
        entry["want"] = "(want)" in ll

        if in_dev:
            entry["status"] = "in_development"

        # Year or year-range at the start
        yr_m = re.match(r"(\d{4})(?:-(\d{4}))?", line)
        if yr_m:
            entry["year"] = int(yr_m.group(1))
            if yr_m.group(2):
                entry["year_end"] = int(yr_m.group(2))

        # Extract all parenthetical groups
        FORMAT_KEYWORDS = ("tv mini-series", "tv series", "tv movie",
                           "mini-series", "youtube")
        for paren in re.findall(r"\(([^)]+)\)", line):
            pl = paren.lower()
            # seen N time(s)
            seen_m = re.match(r"seen (\d+) times?", pl)
            if seen_m:
                entry["times_seen"] = int(seen_m.group(1))
                continue
            # known format tag
            if any(kw in pl for kw in FORMAT_KEYWORDS):
                entry["format"] = paren
                continue
            # "want" tag
            if pl.strip() == "want":
                entry["want"] = True
                continue
            # Otherwise treat as title (first non-numeric, non-status paren)
            if entry["title"] is None and not re.match(r"^\d", pl):
                entry["title"] = paren

        if entry["year"] is not None or entry["status"] is not None:
            entries.append(entry)
        elif line:
            # Bare unrecognized text
            entry["status"] = "unknown"
            entries.append(entry)

    return entries


def parse_comments(raw: str) -> dict:
    """
    Extract structured fields from the comments column.
    Returns dict with keys: collection, notes.
    Boring description text is stripped and not stored.
    """
    result: dict = {
        "collection": None,
        "notes": None,
    }
    if not raw or not raw.strip():
        return result

    text = raw.strip()

    # --- Collection name -------------------------------------------------
    coll_m = re.search(
        r"Collection:\s*([^.]+?)(?:\.\s|\.$|$|(?=\s*Boring))", text
    )
    if coll_m:
        name = coll_m.group(1).strip().rstrip(".")
        # Strip trailing description separated by ": ", " - ", or ", but/also/and "
        name = re.sub(r"(:\s+|\s+-\s+|,\s+(?:but|also|and)\s).*", "", name).strip()
        result["collection"] = name

    # Locate "Boring description/title:" to strip it from notes
    boring_m = re.search(r"Boring (?:description|title):\s*.*", text, re.IGNORECASE)

    # --- Remaining notes --------------------------------------------------
    notes = text
    if result["collection"]:
        notes = re.sub(r"Collection:\s*[^.]+\.\s*", "", notes).strip()
    if boring_m:
        notes = notes[: boring_m.start()].strip().rstrip(".,")
    notes = notes.strip()
    if notes:
        result["notes"] = notes

    return result


# ---------------------------------------------------------------------------
# Source file parser
# ---------------------------------------------------------------------------

def parse_source() -> list[dict]:
    """Read the TSV and return list of raw entry dicts."""
    lines = RAW.read_text(encoding="utf-8").replace("\xa0", " ").splitlines()
    entries: list[dict] = []
    current: dict | None = None

    for line in lines:
        if is_header_line(line):
            continue

        cols = line.split("\t")
        while len(cols) < 11:
            cols.append("")

        no_col = cols[0].strip()

        if re.match(r"^\d+$", no_col):
            # Primary row
            if current is not None:
                entries.append(current)
            current = {
                "no": int(no_col),
                "name": cols[1].strip(),
                "year": cols[2].strip(),
                "word_count_raw": cols[3].strip(),
                "audible_raw": cols[4].strip(),
                "genres_raw": cols[5].strip(),
                "movies_raw": [cols[6].strip()] if cols[6].strip() else [],
                "comments_raw": cols[10].strip() if len(cols) > 10 else "",
            }
        else:
            # Continuation row — only movies column carries data
            if current is not None and cols[6].strip():
                current["movies_raw"].append(cols[6].strip())

    if current is not None:
        entries.append(current)

    return entries


# ---------------------------------------------------------------------------
# Normalization
# ---------------------------------------------------------------------------

def normalize_entries(
    raw_entries: list[dict],
) -> tuple[list[dict], dict[str, list[int]]]:
    """
    Normalize raw entries.
    Returns (normalized_list, collections_map {name: [child_ids]}).
    """
    normalized: list[dict] = []
    # ordered dict preserving first-seen collection order
    collections: dict[str, list[int]] = {}

    for raw in raw_entries:
        flags: list[str] = []

        word_count, wc_flags = parse_word_count(raw["word_count_raw"])
        flags.extend(wc_flags)

        raw_type, keywords = parse_genres(raw["genres_raw"])
        story_type = normalize_story_type(raw_type)
        if story_type is None:
            flags.append("missing_story_type")

        audible_minutes, aud_flags = audible_to_minutes(
            raw["audible_raw"], word_count, story_type
        )
        flags.extend(aud_flags)

        adaptations = parse_adaptations(raw["movies_raw"])
        has_adaptation = bool(adaptations)

        comment = parse_comments(raw["comments_raw"])

        year: int | None = None
        if raw["year"].strip():
            try:
                year = int(raw["year"].strip())
            except ValueError:
                flags.append(f"unparseable_year:{raw['year']}")

        collection_name = comment["collection"]

        entry: dict = {
            "id": raw["no"],
            "title": raw["name"],
            "year": year,
            "word_count": word_count,
            "audible_minutes": round(audible_minutes, 1) if audible_minutes is not None else None,
            "story_type": story_type,
            "keywords": keywords,
            "has_adaptation": has_adaptation,
            "adaptations": adaptations,
            "collection": collection_name,
            "collection_id": None,
            "is_collection_parent": False,
            "notes": comment["notes"],
            "cover_candidate_url": None,
            "cover_local_path": None,
            "cover_verified": False,
            "cover_source": None,
            "review_status": "needs_review" if flags else "ok",
            "review_flags": flags,
        }
        normalized.append(entry)

        if collection_name:
            collections.setdefault(collection_name, []).append(raw["no"])

    return normalized, collections


def build_collection_entries(
    collections: dict[str, list[int]], next_id: int
) -> tuple[list[dict], dict[str, int]]:
    """
    Create a synthetic top-level entry for each collection.
    Returns (entries_list, {collection_name: assigned_id}).
    """
    entries: list[dict] = []
    name_to_id: dict[str, int] = {}
    for name, child_ids in collections.items():
        entry: dict = {
            "id": next_id,
            "title": name,
            "year": None,
            "word_count": None,
            "audible_minutes": None,
            "story_type": "collection",
            "keywords": [],
            "has_adaptation": False,
            "adaptations": [],
            "collection": None,
            "collection_id": None,
            "is_collection_parent": True,
            "child_ids": child_ids,
            "boring_description": None,
            "notes": f"Collection of {len(child_ids)} stories/pieces",
            "cover_candidate_url": None,
            "cover_local_path": None,
            "cover_verified": False,
            "cover_source": None,
            "review_status": "ok",
            "review_flags": [],
        }
        entries.append(entry)
        name_to_id[name] = next_id
        next_id += 1
    return entries, name_to_id


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main() -> int:
    print(f"Reading {RAW} ...")
    raw_entries = parse_source()
    print(f"  Parsed {len(raw_entries)} entries from source")

    print("Normalizing ...")
    normalized, collections = normalize_entries(raw_entries)
    print(f"  Detected {len(collections)} unique collections")

    max_story_id = max(e["id"] for e in normalized)
    col_entries, col_name_to_id = build_collection_entries(
        collections, next_id=max_story_id + 1
    )

    # Back-link child entries to their collection's id
    for entry in normalized:
        cname = entry["collection"]
        if cname and cname in col_name_to_id:
            entry["collection_id"] = col_name_to_id[cname]

    all_entries = normalized + col_entries
    all_entries.sort(key=lambda e: e["id"])

    # --- JSON output ------------------------------------------------------
    print(f"Writing {JSON_OUT} ...")
    JSON_OUT.write_text(
        json.dumps(all_entries, indent=2, ensure_ascii=False), encoding="utf-8"
    )

    # --- CSV review output -----------------------------------------------
    print(f"Writing {CSV_OUT} ...")
    needs_review = [e for e in all_entries if e["review_status"] == "needs_review"]

    csv_fields = [
        "id",
        "title",
        "year",
        "word_count",
        "audible_minutes",
        "story_type",
        "collection",
        "review_flags",
    ]
    with CSV_OUT.open("w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=csv_fields, extrasaction="ignore")
        writer.writeheader()
        for e in needs_review:
            row = {k: e.get(k) for k in csv_fields}
            row["review_flags"] = "; ".join(e.get("review_flags", []))
            writer.writerow(row)

    # --- Summary ----------------------------------------------------------
    story_types: dict[str, int] = {}
    for e in normalized:
        t = e["story_type"] or "unknown"
        story_types[t] = story_types.get(t, 0) + 1

    adaptation_count = sum(1 for e in normalized if e["has_adaptation"])

    print()
    print("=== Summary ===")
    print(f"  Stories:          {len(normalized)}")
    print(f"  Collections:      {len(col_entries)}")
    print(f"  Total entries:    {len(all_entries)}")
    print()
    print("  Story types:")
    for t, n in sorted(story_types.items()):
        print(f"    {t:<20} {n}")
    print()
    print(f"  With adaptations: {adaptation_count}")
    print(f"  Needs review:     {len(needs_review)}")
    print()
    print(f"  Output JSON:  {JSON_OUT}")
    print(f"  Output CSV:   {CSV_OUT}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
