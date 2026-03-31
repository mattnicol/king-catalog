Patch the existing Android app only. Do not touch data scripts unless absolutely required.

Goals:
1. Make the entire app visually cohesive with a gothic/victorian red-and-black theme.
2. Use sharper edges and lower corner radii throughout.
3. Ensure Books page has a single Sort control and a single Filter control.
4. Ensure Filter supports multiple simultaneous filter choices.
5. Fix long-press interaction reliability on Books and Library items.
6. Ensure books already in Library expose the correct long-press actions:
   - Reading now
   - Mark read
   - Add/remove reading list
   - Remove from library
7. Show Word Count and Audible Length on each book item in Books.
8. Improve Home so each section is horizontally scrollable and each item shows:
   - cover
   - title
9. Preserve build stability and existing architecture.

After patching, ensure the app still builds.
