Patch the existing Android app and data pipeline in this repository.

IMPORTANT:
- Keep scope narrow.
- Do NOT run full-catalog enrichment.
- Only touch the specific titles/issues listed below.

================================
TITLE FIXES
================================

1. Witchcraft for Wayward Girls
- Fix author association: this title must belong to Grady Hendrix, not Stephen King.
- Fill missing metadata as much as possible:
  - correct author
  - release date / year
  - decade
  - cover
  - word count if reasonably available
  - audible length / audible_minutes if reasonably available
  - Goodreads rating / ratings count if reasonably available
  - genres
  - keywords
- Never invent values; leave null if not confidently matched.

2. Never Flinch
- Ensure this title exists as a Stephen King title.
- Fill metadata as much as possible:
  - author
  - release date / year
  - decade
  - cover
  - word count if reasonably available
  - audible length / audible_minutes if reasonably available
  - Goodreads rating / ratings count if reasonably available
  - genres
  - keywords
- Never invent values; leave null if not confidently matched.

3. Horrorstör
- Goodreads score is missing.
- Fix Goodreads retrieval and/or matching for this title specifically.
- Ensure goodreads_rating and goodreads_ratings_count are populated if confidently retrievable.

================================
SPLASH FIX
================================

4. Splash image top white line
- Remove the thin white line at the top of the splash image.
- Investigate whether the image is being stretched, letterboxed, or padded.
- Fix the splash so it renders edge-to-edge with no visible white line and no unwanted whitespace.
- If the current image asset is too soft/blurry because of scaling, adjust rendering behavior first:
  - use correct scale/crop mode
  - remove top inset/padding/margin
  - avoid stretching distortion
- Keep splash image only, with no text.

================================
IMPLEMENTATION
================================

5. Goodreads display
- Preserve Goodreads score display in title cards.
- Keep null-safe rendering.

6. Outputs
- Refresh only what is needed for:
  - Witchcraft for Wayward Girls
  - Never Flinch
  - Horrorstör
- Refresh:
  - data-tools/enriched/king_catalog.json
  - data-tools/enriched/king_catalog_review.csv
  - any changed cover assets if needed

7. App stability
- Preserve buildability.
- Do not regress existing UI behavior.
