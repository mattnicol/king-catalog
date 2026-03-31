You are patching an existing Android + data project named King Catalog.

FIRST: audit and patch the enrichment pipeline if needed.
The final enriched dataset is data-tools/enriched/king_catalog.json.

Enrichment requirements:
1. Ensure every title has the best available cover candidate for the US first edition.
2. Download covers locally where possible into data-tools/enriched/covers/.
3. Populate these fields where possible:
   - cover_candidate_url
   - cover_local_path
   - cover_source
   - cover_verified
4. Where word_count is missing, attempt a reliable fill from trusted sources only.
5. Where audible length is missing, attempt a reliable fill from trusted sources only.
6. Convert audible values into audible_minutes where possible.
7. Never invent data. If confidence is low, leave null and flag review_status = "needs_review".
8. Preserve collection parent/child logic.

SECOND: patch the Android app.

UI Theme requirements:
- Entire app theme should be rebuilt around:
  - black / near-black surfaces
  - deep blood reds
  - muted parchment/ivory text accents where suitable
  - sharp edges rather than rounded soft UI
  - gothic / victorian / spooky mood
- Use Material 3 theme tokens centrally:
  - color scheme
  - typography
  - shapes
- Reduce corner radii substantially.
- Improve contrast and readability.

MVP feature fixes:
1. Update the UI theme according to the above requisites.
2. Collapse sort options into a single "Sort" entry.
3. Collapse filter options into a single "Filter" entry.
4. Each of Sort and Filter should open its own control flow and support multiple options where relevant.
5. Fix long press for adding to library.
6. If a book is in library, allow long-press tags/actions for:
   - Reading now
   - Mark read
   - Add to reading list
   - Remove from reading list
   - Remove from library
7. Include Word Count and Audible Length on each book row/card in Books.
8. On Home:
   - make the layout more polished
   - show book cover + title in each section
   - sections:
     - Currently Reading
     - Recently Read
     - Next Up
   - each section should be horizontally scrollable

Implementation notes:
- Keep Android app offline-first.
- Seed data should continue to come from app assets.
- Use local cover paths first where available.
- Keep collection parent titles as the main visible list items.
- Do not break existing build.

Deliverables:
- patched enrichment scripts if needed
- refreshed data-tools/enriched outputs if needed
- patched Android app
- README updates if any commands changed
