Patch the existing Android app and data import logic.

We have a critical regression:
- app update lost user state:
  - reading
  - books in library
- other authors disappeared

This strongly suggests the seed/migration/import logic is overwriting or resetting the database instead of preserving user state and merged catalog data.

GOALS

1. Preserve user state across updates
The following fields must be preserved when reseeding/updating catalog data:
- is_owned / in library
- is_read
- is_reading_now
- is_on_reading_list
- binding_owned
- binding_wanted
- any user notes/state fields

2. Restore merged catalog import
- Ensure the seed import uses the merged app seed dataset, not a partial King-only dataset
- Other authors must appear again after import

3. Safe reseed / migration behavior
- If DB is empty, seed it
- If seed file changes, do a safe merge/upsert
- Do NOT drop user state
- Do NOT mark seed complete if import fails
- Add defensive logging around:
  - seed source path
  - imported title count
  - preserved user-state count
  - author counts after import

4. Import semantics
- Catalog metadata fields may be updated from seed
- User state fields must be preserved from existing DB rows when titles match
- Matching should prefer stable IDs if present, otherwise safe title+author matching

5. Other Authors regression
- Ensure non-King titles from the merged seed remain visible in Other Authors and My Library when owned/tagged

6. Verification
After patching, print to stdout:
- which seed file is used
- total imported rows
- author counts after import
- how many rows preserved user state
- whether other authors are present in imported data

7. Build stability
- Keep the app buildable

IMPORTANT
This is not a cosmetic fix. Prioritize correctness of data preservation and merged import behavior.
