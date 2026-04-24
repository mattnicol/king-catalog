Patch the existing Android app and data pipeline.

CRITICAL PERMANENT RULE:
All app updates must preserve user metadata/state. Do not wipe, reset, overwrite, or discard:
- in-library / owned state
- read state
- reading now state
- reading list state
- binding owned/wanted
- user notes
- any future user-owned metadata

Catalog seed data and catalog metadata may be updated, but user metadata must be merged and preserved across:
- migrations
- reseeds
- app updates
- seed imports
- catalog refreshes

Do not use destructive migrations unless there is absolutely no alternative, and never if it loses user state.

================================
UI CHANGES
================================

1. Bottom navigation
- Remove "Series" from the bottom navigation bar.

2. Books screen
- Add a Series & Connections entry/button to the right-hand side of the Books search bar.
- This should open the Series & Connections screen/modal.
- Books should remain focused on catalog browsing.

3. Series & Connections screen
- Remove genre groups from Series & Connections:
  - remove Horror
  - remove Mystery / Thriller / Crime
  - remove Supernatural / Fantasy
  - remove any pure genre groupings
- Keep narrative/series/connection groups only.
- Reorder the Series & Connections menu as:
  1. The Dark Tower
  2. Bill Hodges
  3. Gwendy
  4. Bachman
  5. Duologies
  6. Castle Rock, if available
  7. Any remaining non-genre connection groups
- Remove the label/note text "Candidate from local seed" everywhere.
- If Castle Rock data is available or can be safely inferred from existing catalog metadata, add a Castle Rock category at the bottom.
- Do not add spoiler text by default.

4. Binding filter visibility
- The Binding filter is being covered by Android system navigation.
- Fix layout/insets so the filter controls are not hidden behind the system nav bar.
- Use appropriate bottom padding / navigationBarsPadding / content insets so controls remain visible.

5. Binding filter conditional display
- When In Library / Not In Library filters are toggled:
  - Show Binding filters only when "In Library" is selected or when viewing My Library.
  - Hide/remove Binding filters when "Not In Library" is selected.
  - Avoid nonsensical binding filters for books not in library.

6. Other Authors ordering
- Reorder Other Authors by author name alphabetically.

================================
DATA / ENRICH SCRIPT
================================

7. Confirm enrich.py can add/update author catalogs
- Ensure enrich.py supports adding/updating a specific author catalog without full unrelated catalog re-enrichment.
- Ensure help text explains how to run author-specific catalog updates.
- Ensure output prints what author/title files were updated.

8. Add Paul Tremblay catalog
- Add Paul Tremblay to Other Authors.
- Include only novels and collections.
- Do not include individual short stories.
- Use the same fields/schema as other author catalogs.
- Pull/fill metadata where possible:
  - title
  - author
  - year / release_date
  - decade
  - story_type = novel or collection
  - genres
  - keywords
  - description
  - cover
  - word_count if reasonably available
  - audible length / audible_minutes if reasonably available
  - Goodreads rating and ratings count if confidently matched and >= 1000 ratings
  - adaptation info if available
- Use reliable sources such as Wikipedia, Goodreads, Open Library, and author/bibliographic pages.
- Do not invent values. Leave null and flag review if not confidently matched.

Expected Paul Tremblay titles to include at minimum:
Novels:
- The Little Sleep
- No Sleep Till Wonderland
- Swallowing a Donkey's Eye
- A Head Full of Ghosts
- Disappearance at Devil's Rock
- The Cabin at the End of the World
- Survivor Song
- The Pallbearers Club
- Horror Movie
- Another
- Dead but Dreaming of Electric Sheep

Collections:
- Compositions for the Young and Old
- City Pier: Above and Below
- In the Mean Time
- Growing Things and Other Stories
- The Beast You Are: Stories

9. Seed import / merge
- Ensure Paul Tremblay records end up in the final merged app seed.
- Ensure importing the new seed preserves all user metadata as described above.

================================
TEST / VERIFY
================================

After patching, print verification output showing:
- Series removed from bottom nav
- Series button exists near Books search bar
- Series groups order
- genre groups removed from Series & Connections
- "Candidate from local seed" removed
- Other Authors sorted alphabetically
- Paul Tremblay title count
- Paul Tremblay novels count
- Paul Tremblay collections count
- final seed author counts
- confirmation that seed import preserves user metadata fields

Keep the app buildable.
