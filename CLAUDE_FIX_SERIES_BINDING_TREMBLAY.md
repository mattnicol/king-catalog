Patch the existing Android app and data pipeline.

CRITICAL RULE:
Do not wipe, reset, overwrite, or discard user metadata/state during any app update, migration, seed import, or reseed. Preserve:
- in library / owned
- read
- reading now
- reading list
- binding owned/wanted
- user notes
- any future user-owned metadata

Catalog metadata may be updated, but user metadata must be merged and preserved.

FIXES REQUIRED:

1. Series entry point
- On the Books screen, the Series button/icon exists near the search bar.
- Add visible label text "Series" next to/under the icon so it is obvious.
- Keep it on the Books screen, not bottom nav.

2. Series submenu navigation
- When inside Series & Connections submenu/detail screen, add a clear back button.

3. Remove bad seed label
- Remove "Candidate from local seed" everywhere across the entire app.
- It must not appear:
  - in Books detail
  - Series & Connections
  - title cards
  - notes
  - metadata display
- Keep the underlying connection data if useful, but never display that label.

4. Binding filter visibility
- Binding filter is still hidden behind Android navigation UI.
- Fix bottom insets/padding so the whole filter sheet/content is visible.
- Use navigationBarsPadding(), imePadding(), or suitable content padding.
- Ensure last filter controls are not obscured.

5. Binding options on Books main screen
- On Books menu, there is an In Library / Not In Library toggle on the main screen.
- When In Library is toggled on, show Binding filter chips directly on the main Books screen:
  - All bindings
  - Hardcover
  - Paperback
  - Unknown
- When Not In Library is toggled, hide/remove Binding options.
- Binding filtering should work immediately from the main Books screen.
- Keep Binding filters available in My Library as appropriate.

6. Tremblay data missing
- Verify Paul Tremblay records exist in the final merged app seed.
- If Tremblay data exists in data-tools outputs but not app assets, copy/sync it into the correct active app asset path.
- If missing from merged catalog, rebuild/merge only Tremblay catalog as needed.
- Include Paul Tremblay novels and collections only.
- Do not do unrelated full-catalog enrichment.

7. Castle Rock missing
- Add Castle Rock category under Series & Connections if available from existing data or safe curated seed.
- Place it at the bottom of the priority S&C list.
- Include known Castle Rock-related titles where confidently known from existing metadata/seed:
  - The Dead Zone
  - Cujo
  - The Dark Half
  - Needful Things
  - The Body
  - The Sun Dog
  - Elevation
  - Gwendy's Button Box, if present
  - Gwendy's Magic Feather, if present
  - Gwendy's Final Task, if present
- Do not add spoiler notes by default.

8. Verification output
After patching, print:
- confirmation Series label exists
- confirmation Series detail back button exists
- grep result showing "Candidate from local seed" is not displayed in UI strings/code
- Tremblay count in final app seed
- Castle Rock group exists
- Binding main-screen chips appear only when In Library is selected
- build still passes or exact build command to run
