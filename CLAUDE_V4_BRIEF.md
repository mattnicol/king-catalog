Patch the existing Android app and data pipeline in this repository.

Implement the following v4 requirements.

================================
DATA / CATALOG UPDATES
================================

1. Dark Tower cover replacement
- Replace the current covers for the full Dark Tower main sequence and the additional related volume (7 + 1 total).
- Do NOT keep the current bad matches.
- Use a different source from the current one.
- Prefer the official Stephen King / Dark Tower site for these covers where possible.
- Ensure each of these has its own correct cover asset / URL / local file:
  - The Dark Tower I: The Gunslinger
  - The Dark Tower II: The Drawing of the Three
  - The Dark Tower III: The Waste Lands
  - The Dark Tower IV: Wizard and Glass
  - The Dark Tower: The Wind Through the Keyhole
  - The Dark Tower V: Wolves of the Calla
  - The Dark Tower VI: Song of Susannah
  - The Dark Tower VII: The Dark Tower

2. Bachman cover investigation
- Investigate why Bachman titles are missing covers or mismatching.
- Likely issue: author matching between Stephen King and Richard Bachman.
- Fix cover resolution logic so Bachman titles can resolve properly.

3. Add Stephen King's Never Flinch
- Add the title with relevant metadata.
- Source from Wikipedia / Goodreads / other reliable sources as needed.
- Preserve same schema as existing titles.

4. Add Joe Hill's King Sorrow
- Add the title with relevant metadata.
- Source from Wikipedia / Goodreads / other reliable sources as needed.
- Preserve same schema as existing titles.

5. Goodreads score / rating
- Add Goodreads rating/score to each title where possible.
- Goodreads API is not available; use page scraping / parsing if required.
- Store the rating in the dataset in a consistent numeric field, for example:
  - goodreads_rating
  - goodreads_ratings_count
- Never invent values. Leave null if not confidently resolved.

6. Library-status filter in browsing
- Add a filter for library state so the user can filter:
  - In Library
  - Not In Library
- This should apply in both:
  - Books
  - Other Authors

7. Genre / data integrity
- Preserve the current schema and app compatibility.
- Do not regress existing author support.

================================
APP / UI UPDATES
================================

8. UI polish
- Improve the visual polish of the app.
- Keep the gothic / red / black theme, but make the UI feel less cheap.
- Focus on:
  - spacing
  - hierarchy
  - card/list presentation
  - filter/sort controls
  - detail screen presentation
  - section headers
- Preserve buildability.

9. App behavior
- Ensure Books and Other Authors both support the new library-state filter.
- Preserve press / long-press behavior.

================================
IMPLEMENTATION NOTES
================================

10. Covers / assets
- Download or refresh local cover assets where possible.
- Keep the app offline-first.
- Update the app seed data accordingly.

11. Outputs
- Refresh:
  - data-tools/enriched/king_catalog.json
  - data-tools/enriched/king_catalog_review.csv
  - data-tools/enriched/covers/
- Ensure app assets can be refreshed from enriched outputs.

12. Build
- Keep the app buildable with Gradle.
