Patch the existing Android app and data pipeline in this repository.

IMPORTANT:
- Do NOT re-run metadata / enrichment for the full catalog.
- Only update/enrich titles that are explicitly in scope for this patch.
- Keep the patch targeted and fast.
- Patch existing code and scripts; do not create disconnected alternatives.

================================
SCOPED DATA CHANGES ONLY
================================

Only update metadata/enrichment for these titles if needed:
1. Never Flinch
2. Witchcraft for Wayward Girls
3. King Sorrow
4. How to Sell a Haunted House
5. The Pallbearers Club
6. On This, the Day of the Pig

Data requirements for in-scope titles only:
- ensure correct author
- release date / year
- decade
- cover
- word count if reasonably available
- audible length / audible_minutes if reasonably available
- Goodreads rating / ratings count if reasonably available
- genres
- keywords
- null-safe if not confidently retrievable

Do NOT run a full-catalog refresh.

Also:
- Remove "The Plant" entirely from the app seed/catalog outputs and any UI lists.

================================
HOME SCREEN
================================

1. Add the same long-press functionality to Home page title cards that exists in Books and My Library.
- This must work for:
  - Currently Reading
  - Recently Read
  - Next Up
- Actions must match Books / My Library behavior.

2. Improve the Home UI significantly.
- The Reading / Read / Next sections are currently plain and boring.
- Make them feel more premium and polished.
- Keep horizontal side-scrolling behavior.
- Improve:
  - section headers
  - card presentation
  - cover prominence
  - spacing
  - hierarchy
  - background / surfaces
  - visual identity within the existing gothic red/black theme
- Keep it readable and not cluttered.

================================
SEARCH UX
================================

3. Add a quick clear button ("x") to the Books search field.
- Tapping it clears the search text immediately.
- If Library and Other Authors has an equivalent search field, apply the same behavior there too.

================================
GOODREADS / DISPLAY
================================

4. Keep Goodreads score visible in title preview cards.
5. Ensure Goodreads score is sortable in both Books and My Library.
6. Ensure Goodreads rendering is null-safe and does not crash the app.

================================
CRASH / STABILITY
================================

7. Harden the app against crash / null-data regressions.
- Do not mark seed import complete if import fails.
- If DB is empty, reseed safely.
- Add null-safe rendering for:
  - Home cards
  - Goodreads display
  - search field changes
  - detail screens
  - sort/filter on nullable values

================================
OUTPUTS / ASSETS
================================

8. Refresh only the necessary outputs for the in-scope titles:
- data-tools/enriched/king_catalog.json
- data-tools/enriched/king_catalog_review.csv
- data-tools/enriched/covers/ (only for in-scope changed titles if possible)

9. Ensure the Android app still builds with Gradle.

10. If scripts are updated, make them support scoped runs, e.g. only processing a passed list of titles instead of the full catalog.
