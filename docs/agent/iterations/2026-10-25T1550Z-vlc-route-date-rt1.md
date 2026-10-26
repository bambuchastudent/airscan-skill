# AirScan RT-1 — VLC airport-wide topology contracts and source validation

- UTC: 2026-10-25T15:50Z (draft evidence point)
- Actor: bounded implementation assistant / architecture research
- Base: `301d979f66b765651303337876fe8998a2b2ca99` on `develop`, planning correction PR #7 merged
- Draft PR: https://github.com/bambuchastudent/airscan-skill/pull/8
- OpenSpec: `add-route-topology` tasks 0.1–0.5 only

## Implemented candidate

Pure Java airport-pair, carrier, source/scan, positive topology/date evidence records and distinct provider contracts with typed failure and coverage. A source-neutral query validator enforces ISO calendar date inclusion. Worker module has a deterministic converter for a **manually reconstructed** tiny Aena facts excerpt; it is deliberately **not** a live page parser. Unit tests exercise multi-carrier merge, duplicates, unknown, normalized IATA, invalid/self route, malformed rows, provenance and inclusive intervals. See `docs/research/aena-vlc-rt1.md` and `docs/contracts/route-query-rt1.md`.

## Architecture and source decisions

Canonical route key: `DIRECT:<ORIGIN>:<DESTINATION>`; carrier is evidence, not identity. Updated INV-ROUTE-002/spec text. Aena official VLC route catalog is priority candidate over airline pages. Current page's advertised counts are not fixed invariants. Java raw HTTP/HTML/CSS parsing, publication completeness and lawful automated acquisition are **unvalidated**. Official Aena legal notice restricts reproduction/exploitation without permission; no crawler has been attempted. Date sources are separate RT-3 research. Worker `synthetic` payload constraint cannot be bypassed; explicit versioned discriminator is required in RT-2.

## Evidence and limits

- Planning baseline push CI https://github.com/bambuchastudent/airscan-skill/actions/runs/40207677493 — 9/9 PASS at BASE only.
- RT-1 exact-HEAD CI to be verified against draft PR before task checkboxes are marked. Container lacked Java 25, Maven, OpenSpec, Graphify, Serena, Firestore Emulator and network access to GitHub/Aena; no local Maven/OpenSpec/Emulator/model checker runs may be claimed.
- Current HTML/DOM and robots retrieval, Java HTTP, source-use permission, and complete parsed-catalog reconciliation remain unresolved; synthetic excerpt does not prove these.
- No fares, provider crawler, scheduled scan, RT-2/RT-3 execution, cloud or retention system changes.

## Next

Inspect exact-HEAD CI, fix candidate failures, then independent RT-1 review. Resolve Aena access/DOM and source license before describing RT-1 source validation as complete or proceeding to RT-2. Stop after review request; do not automatically begin RT-2.

## Verified code CI and task disposition (2026-10-25T15:58Z)

- Exact tested code SHA `57c75bc4854a41656b2a4364b66dfb083d9ad5ae`; CI https://github.com/bambuchastudent/airscan-skill/actions/runs/40209220626 — **9/9 PASS**.
- Root Maven 19/19; worker Maven 4/4; Firestore Emulator 5/5; Spotless / Checkstyle / strict archived+active OpenSpec / worker Google SDK isolation / Graphify / Serena / Alloy-TLC-Dafny bootstrap regressions all PASS.
- Graphify/Serena existing CI smoke verifies baseline symbol dependencies but does not add targeted route-domain relationship assertions. Model-check results remain bootstrap-only and do not claim route properties.
- `0.1` complete: Java value-object records and tested carrier-independent key, self-route rejection and provenance.
- `0.2` complete: typed source provider contracts with coverage/error outcomes and separation of topology/date.
- `0.3` **open**: Aena official candidate researched, but original DOM/Java fetch, robots and permission/terms clearance are unverified.
- `0.4` **open**: reconstructed fact-excerpt normalization is offline-tested; real captured site fixture and source catalog reconciliation are not.
- `0.5` complete: future two-endpoint JSON design, inclusive `LocalDate` tests, explicit UNKNOWN/UNCOVERED/CONFIRMED and short Aena Infovuelos date-horizon research. No date source integrated.
- **Disposition: NEEDS_CHANGES**, not READY_FOR_EXTERNAL_RT1_REVIEW under the user's full RT-1 criteria. External review of bounded candidate PR #8 is useful, but cannot waive source validation without explicit change approval.

## External source remediation and review decision — 2026-10-25

- Prior external reviewer comment: https://github.com/bambuchastudent/airscan-skill/pull/8#issuecomment-6400021207 at `1b5336f906be607bbfc7b5464ebe5186d46ad52b`, CI `40209473762` (9/9 PASS). 0.1, 0.2, 0.5 accepted; 0.3 and 0.4 explicitly blocked.
- Public Aena site content and 84/37 displayed snapshot count observed, but Aena §8 written-permission requirement prevents claiming automated/cached/reproduced source use; true HTML/status/DOM and worker fetch not available because DNS `curl: (6)` failure. No lawful source-native HTML fixture captured.
- Dataestur `AENA_DESTINOS_DL` is an explicit OpenAPI XLSX endpoint designed for software, yet describes historical monthly traffic, not verified advertised airport-wide VLC topology; Dataestur general content redistribution rights also unresolved. Amadeus Airport Routes bearer API may supply city codes instead of airport codes, unverified current coverage. OpenFlights routes last updated 2014. No alternative passed all product/licensing gates.
- Incremental code improvement: synthetic excerpt parser now reports duplicate/rejected candidate rows and tests deterministic reconciliation on its own custom structure; **not** a genuine Aena DOM fixture or an assertion of real catalog completeness. See `docs/research/aena-vlc-rt1.md` for accurate provenance, URLs, restrictions and architecture decision request.
- **0.1 PASS, 0.2 PASS, 0.3 OPEN, 0.4 OPEN, 0.5 PASS.** Preserve task checkboxes; no false green source acceptance. No RT-2/RT-3, fares, deployment, or worker protocol changes.
- **Disposition: NEEDS_SOURCE_DECISION** (Aena written permission/export is preferred; licensed complete airport-wide replacement requires separate architecture review).
- Remediation exact final HEAD / GitHub CI to be verified; before that, do not claim the new code or documentation CI passed.

## Remediation code verification — 2026-10-25T16:29Z
- Exact code SHA `1e9ec42f3aa63c29c7fa30b269969a80d67d409b`, CI https://github.com/bambuchastudent/airscan-skill/actions/runs/40211572694 — **9/9 PASS**.
- Root Maven **19/19**, worker Maven **4/4**, Firestore Emulator **5/5**, all zero failures/errors/skips; Spotless, Checkstyle, strict OpenSpec, Graphify/Serena existing smoke, worker SDK isolation, bootstrap formal checks PASS.
- Real Aena DOM, rights, Java fetch and actual catalog reconciliation remain **unverified**. 0.3/0.4 stay open; documentation HEAD needs own CI. Status: **NEEDS_SOURCE_DECISION**.

## Manual importer scope update — 2026-10-26

Architectural approval: PR #8 comment https://github.com/bambuchastudent/airscan-skill/pull/8#issuecomment-6400275771. RT-1 now supports **manual one-time Aena VLC source page import**, not unattended acquisition. Same PR, no reset of accepted route contracts. Added manual browser DOM exporter, offline Java HTML parser, JSON snapshot CLI and immutable history with fail-closed atomic latest-publish. Dependency is jsoup 1.23.2 (worker, root test-only); no changes to worker protocol, authentication, outbox or central Firestore.

Verification scope: source-free HTML samples exercise IATA normalization, carrier associations, duplicates/corruption, source count mismatch and preservation of a prior good snapshot. The HTML structural sample was built to match **visible Aena terminology**, not collected from the actual page DOM. Therefore it does **not** meet the genuine-site structural fixture requirement alone; a human browser-exported file is required before checking OpenSpec 0.3/0.4 or claiming source completeness. Request independent evidence review after obtaining the real DOM fixture. User-local saved HTML must not be committed wholesale due reuse/redistribution restrictions.

See `docs/aena-manual-import.md` for reproducible manual steps. Real browser DOM not captured in this agent environment (DNS failure), so any status asserting a fully verified Aena parser would be unsupported.
