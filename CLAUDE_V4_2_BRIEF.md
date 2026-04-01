Patch the existing Android app and data pipeline in this repository.

Implement all of the following.

================================
UI / PRODUCT
================================

1. UI polish
- The UI still feels budget and generic.
- Improve the overall visual polish beyond just font changes.
- Keep the gothic red/black tone, but improve:
  - typography
  - spacing
  - hierarchy
  - cards/rows
  - chips/buttons
  - detail screens
  - section headers
  - empty states
- Use a softer, more intentional font treatment than the default-looking one.
- Keep it readable.

2. Goodreads score visibility
- Include Goodreads score directly in the title preview/card in all relevant list views.
- Ensure it is clearly visible, not hidden in detail only.
- Add Goodreads score as a sortable item in both:
  - Books
  - My Library
- Preserve existing sort options.

3. Library filter
- Preserve the library-state filter and ensure it works consistently in Books and Other Authors / My Library contexts.

================================
DATA / CATALOG
================================

4. Add / complete metadata and assets for these titles:
- King Sorrow
- How to Sell a Haunted House
- The Pallbearers Club
- On This, the Day of the Pig
- Witchcraft for Wayward Girls

For each of the above, ensure:
- cover
- word count if reasonably available
- audible length / audible_minutes if reasonably available
- Goodreads rating / ratings count if reasonably available
- genre / keywords if missing

5. Goodreads retrieval
- Goodreads scores were supposedly retrieved but are not showing correctly enough.
- Improve both retrieval and app display.
- Strengthen matching logic and fallback parsing.
- Use Goodreads page parsing / JSON-LD / HTML fallback as needed.
- Never invent values; leave null if not confidently matched.
- Update the GR score for Green Mile, as a single vote is too few for a reliable score.

================================
CRASH / NULL DATA INVESTIGATION
================================

6. Investigate the app crash and subsequent null-data behavior.
- Likely causes may include:
  - Room migration issues
  - seed import / seedImported logic issues
  - null handling in UI rendering
  - sorting/filtering on null Goodreads fields
  - detail screen assumptions
- Patch defensively:
  - null-safe rendering everywhere
  - safe default sorting behavior
  - robust first-run / reseed logic
  - graceful migration path
- Add concise logging around:
  - seed import
  - DB version / migration
  - title count after import
  - crashes or caught exceptions where appropriate

7. App behavior after crash
- Ensure the app does not end up in a broken "null data" state after a failed seed or failed migration.
- If seed import fails, recover safely and avoid marking seed as complete.
- If DB is empty, reseed.

================================
IMPLEMENTATION
================================

8. Patch existing code, do not create disconnected alternatives.
9. Refresh:
- data-tools/enriched/king_catalog.json
- data-tools/enriched/king_catalog_review.csv
- data-tools/enriched/covers/
10. Refresh app assets from enriched outputs.
11. Ensure the app builds with Gradle at the end.
