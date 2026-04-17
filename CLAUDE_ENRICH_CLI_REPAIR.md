Patch the existing data-tools/scripts/enrich.py and any supporting scripts.

We have a concrete failure:
- Running:
  python data-tools/scripts/enrich.py --author "Stephen King" --fields descriptions,keywords --source wikipedia,goodreads,openlibrary --verbose --force
  prints repeated:
  [WP] no Wikipedia title found
  -> no changes
- This proves the current descriptions/keywords implementation is not doing the required fallback behavior.

GOALS
1. Fix the enrich.py CLI so description/keyword enrichment actually works.
2. Make it observable: print which source was tried, which source succeeded, and what fields changed.
3. Make it runnable outside Claude with a proper --help.
4. Use the Reddit thread URL below as a seed source for series/connections ingestion support:
   https://www.reddit.com/r/stephenking/comments/1753bsl/an_ordered_sorted_and_categorized_list_of/

STRICT REQUIREMENTS

A. CLI / HELP
- Ensure enrich.py supports and documents via --help:
  --title (repeatable)
  --author (repeatable)
  --all-authors
  --fields
  --blanks-only
  --force
  --dry-run
  --verbose
  --source
  --rebuild-seed / --no-rebuild-seed
  --once-only-curation
  --include-spoilers
  --reddit-thread-url (repeatable)
- Print example commands in --help output.

B. FIELD NAME NORMALIZATION
- Accept both:
  description
  descriptions
- Treat them equivalently.
- Accept both:
  keyword
  keywords
- Treat them equivalently.
- Normalize internally to canonical singular/plural names as needed.

C. TITLE NORMALIZATION / MATCHING
Implement robust title normalization before lookup:
- strip leading/trailing quotes
- normalize apostrophes
- lowercase
- strip parenthetical aliases like:
  Rage (Bachman)
- normalize punctuation and colon variants
- support known title aliases where needed

Examples that must not fail due to naive matching:
- Carrie
- 'Salem's Lot
- The Shining
- Rage (Bachman)

D. DESCRIPTION / KEYWORD ENRICHMENT CASCADE
For description/keyword updates, do NOT stop at Wikipedia failure.
Use a cascade:
1. Wikipedia
2. Goodreads
3. Open Library
4. existing notes/comments if present and useful
5. leave unchanged + flag for review if still unresolved

Behavior:
- If Wikipedia lookup fails, continue to Goodreads / Open Library instead of "no changes".
- For each title, print to stdout:
  - normalized lookup title
  - source attempts
  - source success/failure
  - changed fields

E. GOODREADS / OPEN LIBRARY FALLBACKS
- Goodreads:
  implement search-result lookup + fetch detail page + parse JSON-LD / HTML fallback
- Open Library:
  use search + bibliographic fields as a fallback source
- Descriptions should prefer concise, neutral summaries
- Keywords should be structured and additive, not a messy sentence blob

F. BLANKS ONLY / FORCE / ONCE-ONLY
- --blanks-only should only fill empty/null fields
- --force should overwrite existing fields in scope
- --once-only-curation should only populate curated description/keywords if blank, even if force is not set

G. OUTPUT TO SCREEN
This is REQUIRED.
Print:
1. Parsed CLI config
2. Files targeted
3. Titles targeted
4. For each title:
   - normalized title
   - source attempts in order
   - fields changed
5. Final summary:
   - titles processed
   - titles changed
   - descriptions updated
   - keywords updated
   - unresolved titles
6. Show where the rebuilt seed file was written

H. SERIES / CONNECTIONS INGESTION SUPPORT
- Add support for:
  --fields connections
  --reddit-thread-url <url>
- Implement ingestion support for the provided Reddit thread as a seed source.
- It does not need to fully solve every connection today, but it must:
  - parse/store candidate groups
  - support spoiler-hidden defaults
  - persist structured connection data fields
- The Reddit thread clearly includes categories for Dark Tower lead-ins, Dark Tower core, tie-ins, duologies, trilogies, genre groupings, and Bachman books, so use those categories as seed group names. Do not invent unsupported groupings.

I. VERIFICATION
After patching, run and print results for:
python data-tools/scripts/enrich.py \
  --author "Stephen King" \
  --fields description,keywords \
  --source wikipedia,goodreads,openlibrary \
  --verbose \
  --force \
  --dry-run

Then run the same command without --dry-run on a small sample only:
python data-tools/scripts/enrich.py \
  --title "Carrie" \
  --title "'Salem's Lot" \
  --title "Rage (Bachman)" \
  --fields description,keywords \
  --source wikipedia,goodreads,openlibrary \
  --verbose \
  --force

Do not claim success unless the terminal output clearly shows:
- fallback source attempts after Wikipedia misses
- changed fields for at least some of the sample titles
- final summary counts
