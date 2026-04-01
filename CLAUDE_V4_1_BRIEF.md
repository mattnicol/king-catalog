Patch the existing Android app and data pipeline in this repository.

Implement these updates:

1. Author grouping location
- Remove the collapsible author heading from Books.
- Books should just show the normal list view.
- Add the collapsible author grouping to My Library instead.
- In My Library:
  - Stephen King / Richard Bachman titles should appear under a full-width collapsible "Stephen King" section
  - Other authors should appear under their own full-width collapsible sections
- Library behavior should still respect owned/tagged state.

2. Splash screen whitespace
- Remove any whitespace, padding, or margins around the splash image.
- The splash image should fill cleanly edge-to-edge with no visible blank border.

3. Softer font
- Replace the current typography treatment with a softer, more intentional font setup.
- Keep the gothic / horror tone, but make it feel less like a default Android font.
- Prioritize readability.
- Update the theme typography centrally rather than one-off styling.

4. Goodreads data retrieval improvement
- The last pass failed to retrieve Goodreads ratings.
- Improve the Goodreads retrieval logic substantially.
- Do a better job of matching titles and retrieving:
  - goodreads_rating
  - goodreads_ratings_count
- Use Goodreads page parsing / JSON-LD / fallback parsing as needed.
- Handle Bachman titles and alternate author/title forms carefully.
- Never invent values; leave null if not confidently matched.
- Refresh the catalog output with the improved Goodreads data.

5. App integration
- Ensure the app seed data is refreshed with any updated Goodreads fields.
- Preserve build stability.

Implementation notes:
- Patch existing code, do not create disconnected alternatives.
- Keep offline-first app behavior.
- Ensure app still builds with Gradle at the end.
