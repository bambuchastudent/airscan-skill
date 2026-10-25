# AirScan Project Memory

This file is the curated durable memory for agents working on this repository. Keep it concise and factual. Update it when durable project state changes; use WORKLOG.md for chronology.

## Current state

- Bootstrap and the local distributed-control-plane POC are complete and merged. PR #5 merged into `develop`; accepted reviewed SHA `239ef9e5adcdc846299b6010a9696aa12a623933`, final PASS evidence commit `1e3b7361af19006d989c3a6542eac73c4187053e`, post-merge `develop` HEAD `e8d2916240275ea98aef808228fe830c29fb49a4`, CI `39923798076` **9/9 PASS**. Distributed-control-plane deployment tasks 4.1–4.5 remain open. The next user-value milestone is **VLC Route/Date Discovery POC** before cloud deployment: run locally, first ingest the airport-wide known VLC direct-route catalog (Aena official destination list is the primary source candidate) through the accepted worker/control-plane path, and only then enrich with independently sourced positive dated availability. Airline-specific evidence must not be mistaken for complete airport coverage. No fares or route expansion yet.
- Default branch: `develop`.
- Production implementation language: Java, not Python.
- Verified runtime baseline: Java 25 Temurin 25.0.4.1, Maven Wrapper 3.9.11, Quarkus 3.40.1.
- Preferred hosted control plane: Google Cloud Run.
- Preferred central operational/materialized store: Cloud Firestore.
- Preferred immutable history/archive: Cloud Storage compressed scan/fare batches.
- Worker-local persistence: SQLite cache/spool/outbox with offline recovery; it is not authoritative.
- Initial map UI direction: TypeScript + Vite + MapLibre GL JS on Firebase Hosting.
- Primary machine contract: REST/OpenAPI; MCP is a thin adapter over the same application services.
- Crawler workers may run locally, in Cloud Run Jobs, CI runners, or other hosts and communicate over authenticated HTTPS.
- Ground-routing direction: Transitous/MOTIS behind a replaceable provider boundary.
- Runtime must remain useful without an LLM.

## Initial origin airports

- VLC — Valencia
- CDT — Castellón
- ALC — Alicante
- MAD — Madrid
- BCN — Barcelona

The origin set must remain configurable.

## Initial low-cost provider focus

Phase 1:
- Ryanair
- Vueling
- easyJet
- Wizz Air
- Volotea

Provider architecture must remain extensible.

## Core architecture decisions

- Route topology discovery, schedule availability, fare observation, and ground routing are separate concerns/adapters.
- Route existence must not depend on current fare visibility.
- Fare observations are append-only historical evidence.
- Normalized facts retain provenance and scan identity.
- Failed/transient scans do not count as negative route evidence.
- Partial scans never become current published snapshots.
- Straight-line distance generates candidates; actual ground connectivity is separate evidence.
- Background scanning is durable/persisted rather than relying only on in-memory timers.
- Distributed workers never directly publish authoritative state or mutate Firestore; they submit idempotent result batches through the control plane.
- Firestore holds hot operational/materialized state; bulky append-only history belongs in compressed archive objects.
- Firebase/web clients consume the stable query API instead of using the Firestore schema as the domain contract.

## Agent-development model

- OpenSpec: behavior/change planning.
- Graphify: repository knowledge and blast-radius analysis.
- Serena: semantic IDE/MCP navigation and targeted edits.
- Alloy: bounded structural/domain invariant checks.
- TLA+/TLC: concurrency/state-machine checks.
- Dafny: selected pure deterministic verified core compiled/integrated with Java.
- GLM is the implementation orchestrator.
- Worker agents implement narrow tasks.
- External ChatGPT role is architect, planner, and independent reviewer of modifications.

## Required evidence trail

- `docs/agent/WORKLOG.md` — append-only verification/change log.
- `docs/agent/iterations/` — one handoff summary per meaningful iteration.
- `docs/reviews/` — self-contained review reports for completed changes/external review.

OpenSpec tasks are not complete without deterministic evidence and iteration documentation. OpenSpec changes are not ready to archive without a review report.

## Active OpenSpec changes and intended order

Completed and archived: `bootstrap-agentic-foundation`.

Accepted local milestone: `add-distributed-control-plane` local POC complete; deployment tasks 4.1–4.5 remain open.

Current milestone order:

1. **VLC Route/Date Discovery POC** inside `add-route-topology`, tasks 0.1–0.15.
2. Return to `add-distributed-control-plane` tasks 4.1–4.5 for deployed POC.
3. Resume broader `add-route-topology` scope (default origins, additional reviewed sources, geographic candidates).
4. `add-durable-scan-retention`.
5. Fare/ranking and later ground-routing work.

The VLC milestone is an intentional user-value exception to the earlier cloud-first dependency order. Do not introduce multiple topology providers or broader origins before the VLC POC receives independent PASS; separate dated schedule sources may be added as needed with explicit coverage. Do not infer unavailable flights from missing positive observations. See `docs/MILESTONES.md`.

## Current invariant areas

See `formal/INVARIANTS.md`.

Initial invariant families:
- canonical route identity;
- provenance;
- append-only fare evidence;
- retention based only on completed evidence;
- unique active scan lease;
- crash recovery;
- atomic/monotonic snapshot publication;
- deterministic top-N ranking.

## Completed bootstrap baseline (2026-10-18)

- Final implementation HEAD: `ca8daf41ba7efe62b6287f19d9f4f2ca0bdfa0dd`; implementation CI `39126546579`: **8/8 PASS**.
- Documentation/evidence closeout HEAD: `351c6835e003a3e5863d3632d77dd92566a25b45`; closeout CI `39127216895`: **8/8 PASS**.
- Independent final verdict: **PASS / READY**.
- PR #1 merged to `develop` as `a2e527c153f403ca47aabcec70a9460f75499909`.
- Verified baseline: Java 25 Temurin 25.0.4.1, Maven Wrapper 3.9.11, Quarkus 3.40.1, REST/OpenAPI, independent worker boundary, Spotless, Checkstyle, OpenSpec 1.14.0, Alloy 6.2.0, TLC 1.8.0, Dafny 4.11.0, Serena pinned at `d9e4c796fae0207ba6848d1df4b331122a5567b5`, Graphify `0.9.92`.
- Canonical review: `docs/reviews/bootstrap-agentic-foundation/2026-10-18T-final-review.md`.
- Nonblocking hardening remains: immutable GitHub Actions SHA pins, checksums for downloaded formal-tool distributions, Serena LSP cache persistence, finer Graphify incremental updates, and branch protection.

## Future design decisions (not bootstrap blockers)

- Worker authentication design (Iteration 1): first local/dev scoped worker token behind future identity verifier; Google-hosted workers should use short-lived OIDC; no verifier, token issuance, worker API or Cloud Run ingress implementation exists yet.
- Firestore/Cloud Run/Storage deployment region/cost requires deployment-time validation.
- First topology and fare adapters and route inactivity thresholds belong to future changes.

## Historical bootstrap remediation notes

Earlier CI failures, unverified tool statuses, Serena CLI incompatibility and mutable upstream dependency were resolved before CI 39126546579. For chronological evidence consult `docs/agent/WORKLOG.md`. This curated memory intentionally omits superseded state.

## Distributed control plane — accepted Iteration 1 (2026-10-18)

- Existing artifacts remain root Quarkus (application/REST/MCP dispatch) and independent `worker/` plain Java; no shared Maven module until a real worker protocol requires one.
- `CapabilitiesService.current()` returns a domain-neutral `CapabilitiesSnapshot`; REST `GET /api/v1/capabilities` and in-process `McpCapabilitiesAdapter` both invoke this same service. MCP JSON-RPC transport is **not** deployed.
- Deterministic sentinel substitution proves REST/MCP application parity; served OpenAPI, 7 control-plane tests and 1 worker test pass in CI `39137373107`.
- Graphify `calls` edges confirm REST/MCP to application service; Serena Java symbols/references confirm both uses. Verified details in WORKLOG and iteration report.
- Worker auth is **design-only**: scoped local/dev token first; future Cloud Run Job OIDC verifier. Worker/job/lease/batch identities are specified direction, not implemented protocol.
- 1.1, 1.2, 1.3 and synthetic 5.2 boundary verified; 3.3 design and 5.3 iteration-level verification partial for the broader change.
- PR #3 merged after user-confirmed independent PASS. Iteration 2 development is on PR #4 and is not merged.

## Distributed control plane — Iteration 2 persistence review candidate (2026-10-18)

- Application-only `CentralOperationalStore` and `CentralRecordService` own UUID synthetic identities; Firestore adapter lives in `adapter/firestore` and is never present in worker dependencies. No new REST/MCP write endpoints.
- Firestore native-mode emulator integration test uses a synthetic project, emulator host and fresh SDK client, no real Google credentials; **PASS** in CI `39142302544`. Production Firestore client injection/deployment remains deferred.
- Worker-only SQLite `SqliteOutbox` uses schema v1, WAL, 5000ms busy timeout and FULL sync, with stable `batchId`, payload, creation time and PENDING/ACCEPTED states. Crash/reopen and acceptance test PASS.
- Pure `ObservationArchive` port with local gzip/SHA-256 immutable adapter. Final archive object is published only after same-directory unique staging + completed gzip + file-channel force, using atomic **no-replace hard-link creation** (`Files.createLink`). Repeated content is verified; corrupt existing final fails closed; staging cleaned on completed and failed attempts. No production GCS SDK adapter.
- OpenSpec 2.1–2.3 remain checked. Archive atomicity remediation code SHA `c4f25ff5...` has 9/9 PASS in CI `39148837864`; final documentation-only closeout also requires CI. Task 5.3 stays open for full change.
- No lease, heartbeat, worker submission/auth, scheduler, deployment, providers or new formal state machine. Iteration 3 requires independent external review PASS of Iteration 2 first.

## Review constraints pending Iteration 3

- SQLite remains `PENDING -> ACCEPTED`, with no `IN_FLIGHT` state or sequence/FIFO ID; deterministic `created_at, batch_id` ordering is sufficient. Lost response/retry must be made safe centrally in Iteration 3.
- Archive reference metadata from future external clients is **not authority** and must be validated server-side against authoritative records.
- Java 25 SQLite native-access warning is a nonblocking runtime/upgrade monitoring item; no speculative JVM flags.
- PR #4 cannot merge and Iteration 3 cannot start before final independent external review PASS.

## Local distributed-control-plane synthetic POC (2026-10-23)

- Exact base `develop` merge SHA `7b693d97d15c0f8642ca2c08ce6ece36bfaa4d18`; PR #5 remains draft/unmerged until external review.
- Java 25 Quarkus `WorkerProtocolResource` provides versioned `POST /api/v1/worker/leases`, `/leases/{jobId}/heartbeat`, `/results`, `/failures` and narrow `GET /api/v1/synthetic/accepted/{jobId}`. Synthetic jobs are server-seeded only in harness.
- `WorkerIdentityVerifier` → typed `WorkerPrincipal`. Local scoped bearer token is loaded from an ephemeral file; no auth-off fallback; 401 for invalid/missing tokens and 403 for wrong-principal active lease. OIDC is deferred; public synthetic read endpoint is local/emulator-only, not an internet-safe production API.
- `FirestoreWorkerProtocolStore` uses single-document Firestore transactions for lease exclusivity, expiry recovery, heartbeat, classified failure and accepted receipt. The authoritative acceptance enforces worker+job+lease+batch+content hash; committed identical replay returns the **same receipt even after lease expiry**. Immutable `ObservationArchive` gzip reference is retained centrally; no duplicate logical ingestion. Archive publication can precede central commit, so crash may leave safe orphan content-addressed object. No GCS production adapter.
- Independent worker `WorkerPocClient` uses Java `HttpClient` with HTTPS or localhost HTTP only and `SqliteOutbox` PENDING→ACCEPTED. Root Maven compiles worker sources as **test-only** for emulator IT; production root/worker artifacts remain separate, worker has no Firestore/GCS/Firebase SDK.
- `WorkerPocIT` uses real Quarkus HTTP, Firestore Emulator, local gzip archive and SQLite reopen to simulate response loss/retry. CI `39873870605`: 11 root tests, 2 worker tests, **5 emulator IT** (4 POC + 1 central store), 9/9 jobs PASS, 0 failures/errors/skips. Graphify 0.9.92 and pinned Serena POC assertions also PASS; OpenSpec 3.1–3.3 and 5.1, 5.2, 5.3 marked verified. Deployment tasks 4.x remain open.
- Formal decision: synthetic one-job lease transactional implementation tests INV-SCAN-001/002 **narrowly**, but existing Alloy/TLC/Dafny are unchanged bootstrap gates and NOT a proof of new lease protocol. Defer full scheduler/retention TLA+ model to `add-durable-scan-retention`; concurrency emulator tests are the local POC gate.
- External independent technical POC review PASS with **one major evidence finding**: prior worker isolation CI falsely green because Maven wrote `worker/worker-dependencies.txt` while grep checked `worker-dependencies.txt`. Fixed workflow at `3c7ddcd469d114e5a40164e2ee5451ee5bd942b1`: assert `test -s` and `test -r`, print exact `worker/worker-dependencies.txt`, grep forbidden Google SDK strings against that exact file, fail on missing/empty/unreadable evidence. CI [39880697503](https://github.com/bambuchastudent/airscan-skill/actions/runs/39880697503) **9/9 PASS**; 11 root tests, 2 worker tests, 5 real emulator IT, all other original gates PASS. No protocol semantics changed. Final independent metadata/evidence review **PASS / LOCAL POC COMPLETE** at reviewed SHA `239ef9e5adcdc846299b6010a9696aa12a623933` with CI `39881017972` **9/9 PASS**, BLOCKER 0 / MAJOR 0. Merge PR #5 after review-evidence closeout CI; deployment tasks 4.1–4.5 remain open.
- Full invocation and evidence in `docs/LOCAL_POC.md` and `docs/agent/iterations/2026-10-22T2350Z-add-distributed-control-plane-poc.md`. Independent external POC PASS recorded on 2026-10-23; merge PR #5 after acceptance evidence CI, then base deployed POC on actual develop HEAD. Do not begin route topology until deployed POC is independently accepted.
