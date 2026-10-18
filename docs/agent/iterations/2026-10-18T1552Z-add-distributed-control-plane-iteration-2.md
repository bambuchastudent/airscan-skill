# AirScan — add-distributed-control-plane Iteration 2

**Date UTC:** 2026-10-18T15:52Z
**Actor:** implementation orchestrator; external independent review pending.
**Base:** `4e82c92b8c6cd1f4716207c4ac18c4ef189c404e` (develop, PR #3 merged)
**Verified code SHA:** `59012715f26c0957366b6d6b22f108632f480a84`
**Code CI:** [39142302544](https://github.com/bambuchastudent/airscan-skill/actions/runs/39142302544) — **9/9 PASS**.
**PR:** https://github.com/bambuchastudent/airscan-skill/pull/4 (draft, never merged).

## Goal and dependency

OpenSpec `add-distributed-control-plane` persistence tasks 2.1–2.3 only.
External independent Iteration 1 review PASS was supplied by project owner;
PR #3 was **not** initially merged; reviewed I1 was merged as base.
No refactoring of the I1 REST/MCP application boundary.

## Implemented

**Firestore central:** `CentralOperationalStore` pure port, `CentralRecordService`
generates synthetic UUIDs, `FirestoreCentralOperationalStore` wraps the real
Google SDK. Only the control-plane application can issue synthetic creates.
No public unauthenticated mutation endpoint. A mandatory emulator CI job uses
a synthetic project ID, native-mode emulator, real SDK, and no Google auth.
The test writes, reads, closes, recreates the SDK client from fresh options,
and reads again. No production ADC auto-wiring at Quarkus bootstrap.

**SQLite worker:** standalone worker depends only on SQLite JDBC and JUnit;
`worker_outbox` schema v1 has stable primary key `batch_id`, serialized
synthetic payload, `created_at`, and `state` PENDING/ACCEPTED. On each open:
`PRAGMA journal_mode=WAL`, `busy_timeout=5000`, `synchronous=FULL`.
Idempotent same-envelope enqueue, collision detection, pending listing,
acceptance, and reopen behavior are deterministic JUnit tests. Worker
dependency tree is explicitly checked for absent Firestore/Storage SDKs.

**Archive:** `ObservationArchive` application port with `ArchiveReference`
source/batch/content hash/object key/gzip/original-compressed size metadata.
`LocalGzipObservationArchive` writes only tiny synthetic sanitized objects:
SHA-256 content addressed, gzip-compressed, `CREATE_NEW` (no overwrite),
repeated identical write returns same ref after content verification;
distinct content gets a distinct object. GCS production client is not
implemented in this iteration.

## Evidence

- `./mvnw -B -ntp spotless:check`: PASS.
- `./mvnw -B -ntp checkstyle:check`: PASS.
- `./mvnw -B -ntp clean verify`: PASS; **9 control-plane tests**, no skipped/failed.
- `./mvnw -B -ntp -f worker/pom.xml clean verify`: PASS; **2 worker tests**.
- `gcloud emulators firestore start --database-mode=firestore-native --host-port=127.0.0.1:8085`: PASS, gcloud **550.0.0** pinned.
- `./mvnw -B -ntp -Pfirestore-emulator verify`: PASS; **1 real Failsafe emulator IT**, no skips; explicit XML report assertion.
- `./mvnw -B -ntp -f worker/pom.xml dependency:tree -DoutputFile=worker-dependencies.txt`: PASS; forbidden Firestore/Storage SDK grep returns none.
- `openspec validate --archived --strict`: PASS.
- `openspec validate --changes --strict`: PASS.
- Graphify 0.9.92 and pinned Serena commands: PASS on code SHA; iteration-specific persistent symbols and references are asserted in the closeout CI scripts.
- Java AST smoke, unchanged Alloy/TLC/Dafny gates and tracked-secret/runtime-artifact gate: PASS.
- Complete chronological failure / command / version trail: `docs/agent/WORKLOG.md`.

The first candidate failed Spotless; a later candidate failed Java test compilation
on the Firestore client's AutoCloseable exception; then the emulator test found a
cached closed SDK client when reusing `FirestoreOptions`. These were fixed and
are not represented as passing checks.

## Scope and risks

- No Firestore emulator tests of production transaction locking/indexes.
- No live Cloud Storage adapter; deterministic local archive only.
- No HTTP worker lease, heartbeat, ingestion, idempotent central submission,
  token/OIDC verifier, Cloud Run/Firebase, scheduler, provider, route, fare,
  ranking, retention, snapshots, dashboard, or ground routing.
- The service/port is intentionally instantiated explicitly; runtime production
  Firestore credential wiring is not included before deployment/auth review.
- No new lease/snapshot safety claim; existing formal bootstrap checks are not
  proofs of distributed persistence correctness.
- OpenSpec checked: **2.1, 2.2, 2.3**. **3.x**, **4.x**, **5.1**, **5.3** open.
- Final documentation/semantic-review closeout SHA requires a fresh CI check.

**Status:** READY_FOR_EXTERNAL_REVIEW only upon green closeout CI.
**Next:** independent external review of Iteration 2 only; do not merge or
start Iteration 3 until external PASS.
