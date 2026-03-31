Patch the enrichment pipeline under data-tools/scripts so the final dataset in data-tools/enriched/king_catalog.json is as complete and app-ready as possible.

Goals:
1. For every title, attempt to resolve a US first edition cover candidate URL.
2. Add fields:
   - cover_candidate_url
   - cover_local_path
   - cover_source
   - cover_verified
   - review_status
3. Where word_count is blank, attempt a reliable fill from trusted sources only.
4. Where audible length is blank, attempt a reliable fill from trusted sources only.
5. Never invent data. If not confident, leave null and mark review_status = "needs_review".
6. Convert audible length into audible_minutes where possible.
7. Preserve collection logic:
   - collection titles are browseable parents
   - stories within collections remain child records
8. Output:
   - data-tools/enriched/king_catalog.json
   - data-tools/enriched/king_catalog_review.csv
9. Also download cover images locally into:
   - data-tools/enriched/covers/
   and write cover_local_path for successful downloads.
10. Make the script idempotent and safe to rerun.

Important:
- Prefer reliable bibliographic/open catalog sources for covers.
- Prefer reviewable candidate matching over aggressive guessing.
- Log summary counts for resolved covers, unresolved covers, word count fills, and audible fills.
