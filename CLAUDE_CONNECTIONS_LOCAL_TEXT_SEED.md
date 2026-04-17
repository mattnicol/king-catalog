Patch data-tools/scripts/enrich.py so connections enrichment can ingest a local text file instead of Reddit fetches.

Requirements:
1. Add CLI flag:
   --reddit-text-file <path>
   repeatable

2. When --fields connections is used:
   - if --reddit-text-file is provided, parse those local files
   - do not attempt live Reddit fetch for those files
   - keep existing --reddit-thread-url support as best-effort only

3. Parse the local text file into candidate connection groups and titles using the section headings in the supplied seed content.
4. Use structured connection records with fields like:
   - group
   - kind
   - role
   - order
   - spoiler
   - note

5. Good defaults:
   - The Dark Tower main books => group "The Dark Tower", kind "series", role "core"
   - Stories Leading to the Dark Tower => group "The Dark Tower", kind "connection", role "supplemental"
   - Dark Tower Tie-In Material => group "The Dark Tower", kind "tie_in", role "supplemental"
   - Duologies => each named duology becomes its own group
   - Trilogies => each named trilogy becomes its own group
   - The Bachman Books => group "The Bachman Books", kind "connection", role "core"
   - Categorized by Genre => use as connection/grouping metadata, not as spoiler text

6. Do not add spoilers by default.
7. Print to stdout in verbose mode:
   - which local files were loaded
   - section names found
   - candidate groups found
   - candidate titles found
   - titles updated

8. Verification:
Run this command and print the output:
python3 data-tools/scripts/enrich.py \
  --author "Stephen King" \
  --connections \
  --reddit-text-file data-tools/raw/king_connections_seed.txt \
  --dry-run \
  --verbose

Do not claim success unless the dry-run shows candidate groups/titles from the local file.
