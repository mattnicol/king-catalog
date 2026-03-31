Build Python scripts under data-tools/scripts to transform the raw Stephen King source text into a clean normalized JSON dataset for the Android app.

Input:
- data-tools/raw/stephen-king-source.txt

Output:
- data-tools/enriched/king_catalog.json
- data-tools/enriched/king_catalog_review.csv

Requirements:
- Parse titles, year, word count, audible length, genres/keywords, movies, comments
- Ignore source Read/Heard/Want columns
- Derive story_type from genres/keywords
- Derive keywords array from remaining tokens
- Detect collections from comments like "Collection: Night Shift"
- Treat collection titles as primary browse titles
- Mark child stories with collection parent references
- Parse adaptation text into has_adaptation and adaptation_notes
- Convert audible durations to total minutes
- Convert word counts to integers where possible
- Add cover lookup fields:
  - cover_candidate_url
  - cover_verified (default false)
  - cover_source
- Add review_status field for rows needing manual verification

Important:
- Automate candidate retrieval where possible, but do not invent data
- If a value cannot be confidently sourced, leave it null and flag for review
- Prioritize safe, reviewable enrichment over aggressive guessing
