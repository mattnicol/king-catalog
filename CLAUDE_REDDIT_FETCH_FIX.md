Patch data-tools/scripts/enrich.py to fix Reddit thread ingestion for --connections.

Problem:
The current command:

python3 data-tools/scripts/enrich.py --author "Stephen King" --connections --reddit-thread-url https://www.reddit.com/r/stephenking/comments/1753bsl/an_ordered_sorted_and_categorized_list_of/ --dry-run --verbose

returns:
HTTP Error 403: Blocked

But this Reddit thread is accessible via its .json endpoint, so the script should not be trying to scrape the normal page HTML.

Required fix:
1. Normalize any Reddit thread URL to its JSON form:
   - append .json if needed
2. Fetch Reddit JSON, not HTML
3. Use browser-like request headers:
   - User-Agent
   - Accept
   - Accept-Language
4. Parse the JSON post body and comments safely
5. Extract candidate connection groups/titles from the thread body
6. In verbose mode, print:
   - original Reddit URL
   - normalized JSON URL
   - fetch status
   - number of candidate groups found
   - number of candidate titles found

Required behavior:
- Do not fail just because HTML page access is blocked
- If Reddit JSON fetch fails, print clear diagnostics
- Keep scope narrow to the Reddit ingestion path only

Verification:
After patching, run this exact command and print the results:

python3 data-tools/scripts/enrich.py \
  --author "Stephen King" \
  --connections \
  --reddit-thread-url https://www.reddit.com/r/stephenking/comments/1753bsl/an_ordered_sorted_and_categorized_list_of/ \
  --dry-run \
  --verbose

Do not claim success unless the command prints candidate groups/titles instead of HTTP 403 blocked.
