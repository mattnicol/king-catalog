Patch the data pipeline only.

Goal:
Improve Goodreads rating retrieval significantly.

Requirements:
1. Strengthen title matching and normalization:
   - strip series prefixes/suffixes where needed
   - handle Richard Bachman / Stephen King alias cases
   - handle punctuation, subtitles, and colon variants
2. Parse Goodreads page JSON-LD when available.
3. Add fallback HTML extraction for rating/count if JSON-LD is absent.
4. Log why matches fail.
5. Refresh:
   - data-tools/enriched/king_catalog.json
   - data-tools/enriched/king_catalog_review.csv
6. Do not invent values.
