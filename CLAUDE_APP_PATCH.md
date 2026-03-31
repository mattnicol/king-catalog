Patch the generated Android app so that:

1. On first launch, it imports app/src/main/assets/king_catalog.json into Room.
2. Books and Library use the imported dataset.
3. Home sections are driven by library status tags:
   - is_reading_now
   - is_read
   - is_on_reading_list
4. Long press on Library items supports:
   - Mark Reading Now
   - Mark Read
   - Add/Remove Library
   - Add/Remove Reading List
5. Sorting works for:
   - release date
   - word count
   - audible length
6. Filtering works for:
   - story type
   - genre
   - decade
   - read
7. Collections are shown as parent titles only in main lists.
8. Covers load from local asset path first, then candidate URL if present.
9. README includes exact CLI build commands.
