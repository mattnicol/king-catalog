Build an Android-only app in Kotlin + Jetpack Compose named "King Catalog".

Core navigation:
- Bottom nav with 4 tabs:
  1. Home
  2. Books
  3. Library
  4. Other Authors

Tech requirements:
- Kotlin
- Jetpack Compose
- Material 3
- Room local database
- Navigation Compose
- DataStore for preferences
- No authentication
- No cloud sync
- Offline-first
- Seed database from app/src/main/assets/king_catalog.json

Data rules:
- Treat collections as the main browseable title in Books and Library
- Child stories within collections remain in DB but are hidden from main lists by default
- Keep movie/adaptation info as a flag and optional IMDb link
- Support fields:
  - title
  - author
  - year
  - decade
  - word_count
  - audible_minutes
  - story_type
  - keywords
  - genres
  - is_collection
  - collection / collection_parent_id
  - has_adaptation
  - imdb_url
  - cover_local_path / cover image
  - is_owned
  - is_read
  - is_reading_now
  - is_on_reading_list
  - last_status_changed
  - notes

Home screen:
- Section 1: Currently Reading
- Section 2: Recently Read
- Section 3: Next Up
- These are driven by tags/status in Library data

Books screen:
- Search by title
- Sort by:
  - Release Date
  - Word Count
  - Audible Length
- Filter by:
  - Story Type
  - Genre
  - Decade
  - Read
- Show:
  - cover image
  - title
  - year
  - story type
  - collection badge
  - adaptation badge

Library screen:
- Show titles where is_owned = true
- Long press actions:
  - Reading now
  - Mark read
  - Add to Library / Remove from Library
  - Add to Reading List / Remove from Reading List

Other Authors screen:
- Same structure as Books
- Empty state for now
- Leave architecture ready for additional author imports later

Architecture:
- MVVM
- Repository layer
- Room entities, DAO, database, migrations scaffold
- Compose screens and navigation
- Import seed JSON on first run
- Use local cover paths where available
- If cover_local_path is absent, fall back to remote candidate URL only if already present in the seed data
- Provide a clean README with CLI build instructions

Deliverables:
1. Full Android project scaffold
2. Seed importer
3. Room schema
4. Search, sort, filter logic
5. Long-press library actions
6. Bottom nav app shell
7. README
