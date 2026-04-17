Patch the existing data-tools/scripts/enrich.py and any supporting code.

We need to harden Goodreads matching and then ensure the reseeded data is imported by the app.

STRICT REQUIREMENTS

A. GOODREADS AUTHOR / TITLE MATCHING
- When selecting a Goodreads result, require strong title and author matching.
- Reject non-book summary/guide/analysis editions.

Hard reject patterns in title and/or author:
- "Summary of"
- "Includes Analysis"
- "Study Guide"
- "Workbook"
- "Trivia"
- "Instaread"
- "Instaread Summaries"
- "Analysis"
- "Summary"
- "Book Review"

Behavior:
- Normalize title before matching
- Normalize author before matching
- Prefer exact or near-exact main-title match by the expected real author
- For Bachman titles, handle alias mapping carefully

B. GOODREADS MINIMUM RATINGS THRESHOLD
- Require a minimum Goodreads ratings count of 1000
- If a matched Goodreads page has fewer than 1000 ratings, reject it and leave Goodreads fields null
- Print to stdout when a candidate is rejected for low ratings count

C. OBSERVABILITY
- In verbose mode, print:
  - target title
  - expected author
  - Goodreads search candidates considered
  - why each candidate was rejected or accepted
  - final selected Goodreads URL/title/author/rating/count

D. RESEED / IMPORT
- Ensure the refreshed catalog JSON is copied into the actual app asset path(s)
- Ensure the app seed import logic can re-import refreshed data when needed
- If DB is empty, seed
- If seed file changed and reseed is requested, support safe reseed behavior
- Do not mark seed as imported if import fails

E. VERIFICATION
After patching, run and print output for these sample titles:
- Finders Keepers
- Carrie
- Horrorstör

The verification output must show:
- selected Goodreads match title
- selected Goodreads match author
- goodreads_rating
- goodreads_ratings_count

Do not claim success unless the selected Goodreads match for Finders Keepers is the real Stephen King title, not a summary/analysis edition.
