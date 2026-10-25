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
