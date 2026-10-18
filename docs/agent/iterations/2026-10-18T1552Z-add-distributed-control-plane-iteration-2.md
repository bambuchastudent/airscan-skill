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

**Status:** Independent review found one MAJOR archive atomicity issue; a
targeted implementation fixes it, while final external re-review remains pending.
**Next:** Do not merge PR #4 or begin Iteration 3 before final external PASS.

## Final independent-review remediation — archive publication

**Reviewed baseline:** `d1bdaee60137d1fc367c1e573c845d92fc396711`
**Final implementation SHA:** `c4f25ff57e7425febb725b5c04385c9c1476f812`
**Implementation CI:** [39148837864](https://github.com/bambuchastudent/airscan-skill/actions/runs/39148837864) — **9/9 PASS**, 2026-10-18.
**PR review range:** reviewed baseline -> implementation SHA + this documentation closeout; latest documentation SHA and its CI are reported by PR #4 checks because a commit cannot embed its own SHA.

External review accepted the control-plane/SQLite/archive boundaries but identified
a MAJOR crash-atomicity flaw: direct `Files.write(final, ..., CREATE_NEW)`
could leave a truncated final gzip if interrupted. Remediation changes only
`LocalGzipObservationArchive`, `LocalGzipObservationArchiveTest` and related
evidence text.

### Final atomic publication contract

1. Gzip the tiny sanitized payload completely in memory.
2. Create a unique staging file in the final target directory with
   `Files.createTempFile`.
3. Write the entire gzip through `FileChannel` and `force(true)` before
   publishing.
4. Atomically publish with `Files.createLink(final, staging)`, which
   **cannot replace** a concurrently existing final path; the completed
   staged inode becomes visible at one final name.
5. If the target already exists, decompress it and compare all bytes with the
   expected original. Same content returns the same reference, corrupt/
   different content **fails closed**, no overwrite.
6. `finally` removes the staging file after normal and exceptional returns.

`Files.move(..., ATOMIC_MOVE)` alone cannot satisfy no-replace portability:
Java explicitly allows provider-dependent replacement of an existing target.
Hard-link publication is a stricter atomic create-only primitive. If unsupported
on a file system, fail closed; no non-atomic fallback. A process kill can leave
a harmless orphan staging file; it cannot poison the authoritative final path
with incompletely written gzip. `force(true)` syncs staged file contents,
not necessarily the final directory entry against sudden power loss.

### Tests and evidence

`LocalGzipObservationArchiveTest` now verifies:
- Normal publication produces a readable, SHA-256-matching final gzip.
- Identical retry returns equal `ArchiveReference` with identical final bytes.
- Different content under same source/batch creates a different key; old
  content remains valid.
- No `*.tmp` staging artifacts after successful writes.
- A deliberately truncated/corrupt pre-existing final object is neither
  overwritten nor accepted; the error path cleans staging.
- Four concurrent writers of identical identity converge on one immutable
  reference, readable bytes and zero staging debris.

**Verified:** `./mvnw -B -ntp spotless:check`, `./mvnw -B -ntp
checkstyle:check`, `./mvnw -B -ntp clean verify` (**11 root tests**),
`./mvnw -B -ntp -f worker/pom.xml clean verify` (**2 worker tests**),
`./mvnw -B -ntp -Pfirestore-emulator verify` (**1 Firestore IT**),
strict archived/active OpenSpec, Graphify, Serena, worker dependency isolation,
tracked-secret gate and existing Alloy/TLC/Dafny all **PASS** at implementation
SHA / CI `39148837864`.

**Review dispositions:** SQLite stays `PENDING -> ACCEPTED`, no `IN_FLIGHT`
and no insertion-sequence FIFO; retries depend on future *central* server
idempotency. Externally supplied archive reference metadata will require
control-plane validation. SQLite Java 25 native-access warning is a
nonblocking follow-up; JVM flags unchanged. The whole OpenSpec change is
not complete and Iteration 3 is not authorized.

**Status:** READY_FOR_FINAL_EXTERNAL_REVIEW once documentation-closeout CI
is green. No final independent reviewer PASS is claimed here.
