You are patching an existing Android-only Kotlin/Jetpack Compose app and its supporting data pipeline in this repository.

You must update BOTH:
1. the data enrichment pipeline and outputs
2. the Android app UI / behavior

Use the existing repository structure. Patch existing code where practical rather than creating disconnected alternatives.

====================
V2 DATA REQUIREMENTS
====================

1. Include the Richard Bachman catalog.
- These titles should still be listed under Stephen King in the app.
- They must be flagged as "as Richard Bachman".
- Add a filter for Bachman titles.

2. Redo cover retrieval work.
- All novels, novellas, and collections are expected to have a cover if at all possible.
- Re-run cover sourcing and downloading.
- Prioritize well-known/public bibliographic sources and reasonable candidates.
- We especially expect covers for:
  - collections
  - Dark Tower books
- Download covers locally where possible.
- Preserve:
  - cover_candidate_url
  - cover_local_path
  - cover_source
  - cover_verified
  - review_status

3. Collections currently missing release dates must be filled.
- Look them up and populate release date / year.
- Wikipedia is acceptable if needed for release dates.

4. Apply decade tags to all titles based on release date.

5. Split Genre from Keywords in both the dataset and app-facing model.
- Genre should be broad buckets like:
  Horror, Crime, Sci-Fi, Fantasy, Thriller, Drama, Mystery, Western, Dark Fantasy, etc.
- Keywords should be more specific concepts like:
  Alien, Creature, Murder, Apocalypse, Vampire, Telekinesis, Coming-of-age, Gunslinger, etc.
- Refactor the data model if required.
- Preserve story_type separately.

6. Collections should have covers too.

7. Output refreshed data files:
- data-tools/enriched/king_catalog.json
- data-tools/enriched/king_catalog_review.csv
- any local covers under data-tools/enriched/covers/

===================
V2 APP REQUIREMENTS
===================

1. Rename "Library" to "My Library" everywhere in the UI.

2. When selecting a book in the Books menu, open a detail view/screen.
- Show all relevant metadata we have for the title:
  - title
  - author / Bachman flag
  - year / release date
  - decade
  - story type
  - genre(s)
  - keywords
  - word count
  - audible length
  - adaptation flag / IMDb link if available
  - collection membership / parent info
  - notes/comments where suitable
  - cover
- Also present the same actions currently available on long press.

3. When a Collection is selected in detail view:
- Show a tidy catalog/list of all stories within that collection.

4. Apply the same Sort and Filter selectors to My Library that Books has.
- Keep Sort and Filter as separate controls.
- Multi-option filters should remain supported where relevant.

5. Add Bachman filter to Books and My Library.

6. Ensure decade tags are used for filtering.

7. Genre and Keyword should be separate filters.

8. Preserve collection-parent browsing behavior in Books and My Library:
- collections act as main browseable parents
- child stories should appear in collection detail view

9. Keep offline-first behavior.
- Seed data still comes from app assets.
- Prefer local cover paths first.

=========================
IMPLEMENTATION EXPECTATION
=========================

- Patch the existing Python scripts under data-tools/scripts as needed.
- Rebuild the enriched dataset.
- Patch the Android app.
- Update README/build notes only if needed.
- Keep the project buildable.

At the end, ensure the app still builds with Gradle.
