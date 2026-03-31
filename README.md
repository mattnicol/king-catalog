# King Catalog

An offline-first Android app for browsing and tracking the Stephen King bibliography.

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer, **or** JDK 17 + Android SDK (API 34)
- `ANDROID_HOME` environment variable set to your SDK path

## Build

```bash
cd app
./gradlew assembleDebug
```

The APK is written to `app/app/build/outputs/apk/debug/app-debug.apk`.

## Install to device / emulator

```bash
cd app
./gradlew installDebug
```

## Run tests

```bash
cd app
./gradlew test
```

## Project structure

```
app/                        Android project root
├── app/src/main/
│   ├── assets/
│   │   └── king_catalog.json       Seed data (230 entries)
│   └── java/com/mattnicol/kingcatalog/
│       ├── KingCatalogApp.kt       Application; triggers seed import on first run
│       ├── MainActivity.kt
│       ├── data/
│       │   ├── db/                 Room database, DAO, type converters
│       │   ├── model/              Domain models (Book, Adaptation)
│       │   ├── repository/         BookRepository
│       │   ├── datastore/          UserPreferencesRepository (DataStore)
│       │   └── seed/               SeedImporter – parses assets JSON on first launch
│       └── ui/
│           ├── navigation/         Bottom nav + NavHost
│           ├── components/         BookCard, BookCover
│           ├── screens/
│           │   ├── home/           Currently Reading / Recently Read / Next Up
│           │   ├── books/          Full catalog with search, sort, filter
│           │   ├── library/        Owned books with long-press actions
│           │   └── other/          Other authors (extensible)
│           └── theme/              Material 3 colour scheme
data-tools/                 Python scripts used to build the seed JSON
├── scripts/parse.py        Parse source TSV → king_catalog.json
└── scripts/enrich.py       Enrich JSON with covers (Open Library) and word counts
```

## Seed import

On first launch `KingCatalogApp` checks DataStore for a `seed_imported` flag. If absent it calls `SeedImporter.load()` which reads `assets/king_catalog.json`, maps every entry to a `BookEntity`, and bulk-inserts it via Room. The flag is set so the import never runs again.

## Adding another author's catalog

1. Produce a JSON file in the same schema as `king_catalog.json` but with a different `author` field on each entry.
2. Place the file in `assets/` and extend `SeedImporter` to load it.
3. The `observeOtherAuthors()` DAO query will pick up any rows where `author != 'Stephen King'`.

## Data pipeline

The enriched seed data lives at `data-tools/enriched/king_catalog.json`. To refresh it:

```bash
# requires Python 3.11+ and internet access
source .venv/bin/activate          # or: python3 -m venv .venv && pip install -r requirements.txt
python3 data-tools/scripts/parse.py    # re-parse source TSV (only needed if source changes)
python3 data-tools/scripts/enrich.py   # fetch Open Library covers + estimate missing word counts
```

After running, sync enriched outputs to app assets:

```bash
cp data-tools/enriched/covers/*.jpg app/app/src/main/assets/covers/
cp data-tools/enriched/king_catalog.json app/app/src/main/assets/king_catalog.json
```

Audible lengths are populated from the source TSV where present. Entries without audible data are left null (no reliable free API exists for bulk Audible metadata). Covers for short stories inside collections are typically unavailable on Open Library; those entries are flagged `needs_review` in `data-tools/enriched/king_catalog_review.csv`.

## Tech stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose 2.7 |
| Local DB | Room 2.6 |
| Preferences | DataStore 1.1 |
| Images | Coil 2.6 |
| JSON | Gson 2.10 |
| Build | AGP 8.3 / Kotlin 1.9 / KSP |
