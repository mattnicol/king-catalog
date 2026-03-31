Patch the existing Android app and data pipeline in this repository.

You must implement the following v3 requirements.

================================
UI / UX CHANGES
================================

1. Splash page
- Remove all text from the splash page.
- Use the splash image only.
- Remove any whitespace/padding/margins around the splash image.
- The splash image should fill cleanly and feel intentional.

2. Stephen King section
- Put all Stephen King / Richard Bachman books under a single collapsible full-width button titled "Stephen King".
- This should behave like an expandable section/group in the relevant browsing UI.
- Bachman titles remain under Stephen King, but still clearly flagged as "as Richard Bachman".

3. Other Authors in Library
- Titles by non-King authors should also appear in the library when tagged/owned appropriately.
- Library behavior should be consistent regardless of author.

4. Other Authors page
- Add full-width collapsible author buttons for:
  - Josh Malerman
  - Joe Hill
  - Grady Hendrix
- Each author should expand/collapse independently.
- Press / long-press behavior must match the Books menu actions.

================================
DATA EXPANSION
================================

5. Add databases for:
- Josh Malerman
- Joe Hill
- Grady Hendrix

Requirements for these authors:
- Build and populate a database using the same fields/parameters as the King database where reasonably possible.
- Source title/catalog data by cross-referencing Goodreads and Wikipedia.
- Goodreads API is not available; use scraping/parsing if needed.
- Cross-check between Goodreads and Wikipedia to improve confidence.
- Never invent data.
- If a field cannot be confidently populated, leave it null and mark for review.
- Covers should be attempted where possible for long-form works.
- Keep output app-compatible with the current import model.

6. App data model
- Continue supporting Stephen King / Richard Bachman plus these other authors.
- Keep all existing fields where possible:
  - title
  - author
  - author alias / Bachman flag
  - year / release date
  - decade
  - story type
  - genres
  - keywords
  - word count
  - audible length / audible_minutes
  - adaptation flag / IMDb URL
  - collection data
  - notes
  - cover paths / URLs
  - ownership / reading state

7. Library + interactions
- All press / long press behavior in Other Authors must match Books.
- Other-author titles must support add/remove library, reading now, mark read, add/remove reading list, etc.

================================
IMPLEMENTATION
================================

8. Data outputs
- Update the enriched catalog output so all authors are included in the main app seed dataset.
- Preserve app compatibility and offline-first behavior.
- Refresh any local covers/assets as needed.

9. Android app
- Update grouping and UI logic to support collapsible author sections.
- Ensure Library includes owned/tagged titles from all authors.
- Ensure the app still builds.

10. Safety / quality
- Prefer patching the existing codebase over creating disconnected alternatives.
- Add concise logging or comments where helpful.
