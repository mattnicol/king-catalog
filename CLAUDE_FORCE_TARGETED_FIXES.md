You are patching an existing repository.

We have a repeated failure pattern:
- Claude claims targeted catalog updates succeeded
- but the actual dataset still shows the same wrong values

Therefore this task is NOT complete unless you:
1. make the requested targeted data changes
2. print verification output to the terminal
3. fail loudly if verification still shows the wrong state

STRICT SCOPE
Only fix these exact catalog issues:
- Witchcraft for Wayward Girls:
  - must exist
  - must be author = "Grady Hendrix"
  - should have as much metadata as can be confidently retrieved
  - should not be associated with Stephen King
- Never Flinch:
  - must exist
  - must be author = "Stephen King"
  - should have as much metadata as can be confidently retrieved
- The Plant:
  - must be removed from the final app seed/catalog outputs

DO NOT do a full-catalog enrichment pass.
DO NOT claim success without printing proof.

REQUIRED DATA OUTPUTS / PROOF
After making changes, you must print to stdout:
1. A BEFORE snapshot for these titles from:
   - data-tools/enriched/king_catalog.json
2. The exact code/script path used to make the update
3. An AFTER snapshot for these titles from:
   - data-tools/enriched/king_catalog.json
4. A FINAL snapshot from the actual app seed file used by the app:
   - app/app/src/main/assets/king_catalog.json if that exists
   - otherwise app/src/main/assets/king_catalog.json
5. A git diff summary for changed files
6. A final assertion block that checks:
   - WFWG exists and author == Grady Hendrix
   - Never Flinch exists and author == Stephen King
   - The Plant is NOT PRESENT in the final app seed

REQUIRED IMPLEMENTATION BEHAVIOR
- Patch scripts or data files as needed
- If targeted script support does not exist, add it
- If the cleanest fix is direct targeted patching of the catalog JSON plus rebuild of the app seed, do that
- Do not rely on broad re-enrichment
- Do not mark success if assertions fail

METADATA EXPECTATIONS
For Witchcraft for Wayward Girls and Never Flinch, populate where confidently available:
- author
- year / release_date
- decade
- cover
- genres
- keywords
- goodreads_rating / goodreads_ratings_count if confidently retrievable
- word_count if reasonably available
- audible length / audible_minutes if reasonably available

VERIFICATION SCRIPT
Create or update a small verification script that prints targeted rows and validates the assertions above.
Run it before and after the fix.

IMPORTANT
The output to screen is a required deliverable. The goal is to make the data generation/update process observable so prompts and CLI can be tuned effectively.
