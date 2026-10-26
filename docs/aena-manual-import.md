# One-shot VLC topology import (manual RT-1)

## Scope and source

The airport-wide Aena Valencia destination catalog is at:
https://www.aena.es/en/valencia/airlines-destinations/airport-destinations.html

An ordinary visitor can manually save a browser-rendered copy for local use. This is **not** permission for unattended retrieval, bulk reuse, commercial redistribution or republishing the page. Inspect Aena's legal notice before sharing any derivatives. There is **no** `--fetch-once` option, HTTP client, scheduler, background job or direct Firestore writes in RT-1.

## Reproducible manual capture

1. Open the official destination page in a desktop browser. Wait for all destination cards/rows to appear. Clear any active destination, country or airline filters. Confirm the page's advertised count and inspect that the full list is visible.
2. Open DevTools → Console, paste the contents of `scripts/export-aena-dom.js` **once** and execute it manually. The script copies the **current rendered** `<main>` DOM into `aena-vlc-rendered-dom.html` via a local browser download. It makes no requests and does not include cookies or storage. A normal browser `Save page as → HTML` is another option, but it may omit dynamic DOM inserted after initial load. The console export is preferable.
3. Keep the HTML file **local and private**. Do not check it into this repository. Capture the actual UTC timestamp from your browser (e.g. `new Date().toISOString()`); record the displayed destination count for human verification.
4. Run from repository root (requires Java 25 and Maven wrapper; use an absolute path for input if needed):

```bash
./mvnw -B -ntp -f worker/pom.xml compile exec:java \
  -Dexec.mainClass=dev.airscan.worker.aena.AenaCatalogImportCli \
  -Dexec.args="--input /path/to/aena-vlc-rendered-dom.html --output ./local/aena-vlc --captured-at 2026-10-26T13:00:00Z"
```

This code has **no programmatic Aena network access**. It parses a recognized structural table or complete list cards containing a destination and country/airline labels. If Aena changed its DOM or the browser saved only HTML without rendered cards, it fails closed/outputs `PARTIAL` and does not change `latest.json`.

## Snapshot semantics

The importer reads up to 10MB of local UTF-8 HTML. It computes a SHA-256 of the input file, detects route candidates, duplicates and rejected identities, selects the **rightmost** IATA airport code (so `(MIL) (TSF)` maps to `TSF` and `(FKB) (FKB)` maps to `FKB`), combines the exact **website-listed** carrier labels, and uses `UNKNOWN` if none is structurally attributable. Never infer that a listed/marketing airline is the operating airline.

When the source supplies a numeric destination count, the importer reconciles that number with **unique normalized** destinations. The count is never hardcoded. To publish, there must be a recognized destination structure, no malformed candidate identities, no unexplained data loss and exact count agreement. Unknown or mismatching count means `PARTIAL` and blocks current-state publication. Structural changes may require an independently reviewed parser enhancement rather than tolerating silent omissions.

Each run writes `vlc-<UTC-timestamp>-<source-hash-prefix>.json` in the given output directory with `sourceId=aena-vlc`, URL, capturedAt, importer schema, source content SHA-256, sorted routes, carrier labels and diagnostics. Historical files are immutable. **Only complete parses atomically replace `latest.json`**. Incomplete snapshots are retained for inspection, and the CLI returns an error after writing them; an old good `latest.json` is not changed. Do not merge/export these manually to the central store in RT-1.

## Genuine fixture evidence gap

The repository's `worker/src/test/resources/aena/vlc-browser-table-structural-example.html` is a **small, synthetic HTML structural model based on visible Aena labels** for deterministic offline coverage: destination, country, carrier links, repeated/city IATA, corruption, duplicates, unknown carrier and atomic output. It is **NOT a genuine capture of Aena's deployed DOM**. The older `vlc-sanitized-excerpt.txt` is likewise illustrative. Both are insufficient alone to accept OpenSpec 0.4.

An actual human browser export is still needed to (a) identify the real selectors/card layout, (b) demonstrate end-to-end import of every visible source candidate, and (c) produce a **minimal copyright-conscious structural fixture** that retains the real Aena element hierarchy while redacting unnecessary text/data. Commit only such a minimal fixture if rights permit, and add a test using it. Until then 0.3/0.4 remain open despite CI passes. Do not invent original page markup or pretend a synthetic table is the real site.
