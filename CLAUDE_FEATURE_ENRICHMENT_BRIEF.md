Patch the existing repository to implement the following feature enrichment work.

IMPORTANT:
- Improve and refactor the existing enrich.py into a proper CLI tool that can be run outside Claude.
- The script must print useful progress and summary output to screen.
- The script must support --help with clear usage instructions.
- Patch existing code and scripts; do not create disconnected alternatives.

==================================================
PART 1 — IMPROVED CATALOG ENRICHMENT TOOLING
==================================================

The current enrich.py is too broad and opaque. Refactor it into a more operator-friendly CLI.

Required CLI capabilities:
- --help
- --title "..."                 (repeatable)
- --author "..."                (repeatable)
- --all-authors
- --blanks-only
- --fields metadata,description,keywords,covers,goodreads,connections
- --descriptions
- --keywords
- --connections
- --source wikipedia,goodreads,openlibrary,reddit
- --reddit-thread-url <url>     (repeatable)
- --dry-run
- --verbose
- --rebuild-seed / --no-rebuild-seed
- --once-only-curation
- --include-spoilers
- --review-only

Behavior:
1. Support targeting:
   - specific titles
   - all titles for specific authors
   - full-catalog runs when explicitly requested
2. Support "blank fields only" mode
3. Support one-time curation passes for:
   - descriptions
   - keywords
   These should only update empty fields when requested with once-only semantics.
4. Print to stdout:
   - which files are being processed
   - which titles are being processed
   - which fields are being updated
   - before/after summary counts
   - review flags / unresolved items
5. Make the usage easy enough that the user can run it without Claude.

Data improvement goals:
- Improve metadata quality across the catalog
- Improve descriptions
- Improve keywords
- Cross-reference reliable sources such as Wikipedia, Goodreads, Open Library, etc.
- Never invent data
- If not confident, leave null and mark review

==================================================
PART 2 — SERIES / CONNECTIONS / EASTER EGGS
==================================================

Add schema + app support for a user-facing "Series / Connections" feature.

Data model:
- Add a nicely named field and structure for:
  - series
  - trilogies
  - cycles
  - broader universe connections
  - easter eggs
- Use a structured representation rather than one plain string.
- Support:
  - group name
  - kind (series / trilogy / connection / easter_egg / universe)
  - role (core / supplemental)
  - display order
  - spoiler flag
  - note
- Spoilers should be hidden by default unless explicitly requested.

Source handling:
- Accept one or more Reddit thread URLs via CLI.
- Parse them as seed input for candidate connections.
- Cross-reference with other sources before writing final structured data.
- Store review flags where confidence is low.

App behavior:
- Add a "Series / Connections" entry point in the app.
- Show connection groups (e.g. The Dark Tower, Holly Gibney, Bill Hodges, Castle Rock, etc.)
- Allow drilling into a group
- Within a group, separate:
  - core stories
  - supplemental / related material
- Do not show spoiler text by default
- Provide an option/toggle to reveal spoiler notes

NOTE:
The exact Reddit thread URL may not yet be provided. Implement the CLI and ingestion path so it can be supplied later.

==================================================
PART 3 — HARDCOVER / SOFTCOVER
==================================================

Add library binding support.

Data model:
- binding_owned: null | paperback | hardcover
- binding_wanted: null | paperback | hardcover

App behavior:
- When adding a title to My Library, allow selection of binding
- When editing a library item, allow updating binding
- Add filters in My Library for:
  - All
  - Hardcover
  - Paperback
  - Unknown
- Preserve existing library/read/readlist behavior

==================================================
PART 4 — OUTPUT / DX
==================================================

1. Update script help text and README usage examples.
2. Print example commands to stdout when --help is used.
3. Ensure the tool can run without Claude.
4. Keep the app buildable.
