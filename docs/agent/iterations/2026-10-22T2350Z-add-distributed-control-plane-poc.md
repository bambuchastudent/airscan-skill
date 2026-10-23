# AirScan — local distributed-control-plane POC

**UTC:** 2026-10-22T23:50Z  
**Actor:** implementation orchestrator; independent external POC review pending  
**OpenSpec change:** `add-distributed-control-plane`, tasks 3.1/3.2/3.3/5.1/5.2/5.3  
**Actual base:** `develop` at `7b693d97d15c0f8642ca2c08ce6ece36bfaa4d18`; Iteration 2 PR #4 independently accepted and merged  
**Implementation/spec SHA:** `7c3432846bee57b1cb29277519001c03f95475e5`  
**Verified CI:** [39873870605](https://github.com/bambuchastudent/airscan-skill/actions/runs/39873870605) — **9/9 PASS**, including 5 real emulator integration tests  
**PR:** https://github.com/bambuchastudent/airscan-skill/pull/5 — draft, not merged

## Goal

First reproducible **local-only** distributed control-plane POC without airlines,
fares, routes, cloud deployment or LLM runtime dependencies. Worker leases server-seeded
synthetic job over authenticated HTTP; persists normalized payload and stable batch ID
in SQLite; submits; Firestore transaction records one acceptance with immutable archive
reference; response is intentionally discarded; worker opens the *same SQLite file*
using a fresh repository/client, resends original batch, gets identical acceptance,
marks local entry ACCEPTED and proves it no longer appears in pending.
Narrow REST GET returns the accepted receipt and archive provenance.

## Implemented

- `WorkerIdentityVerifier` returns typed `WorkerPrincipal` from scoped bearer
  fixture, not a self-proclaimed `X-Worker-Id`. No authentication-off fallback.
  401 missing/bad, 403 wrong active lease owner. Two random ephemeral local
  secrets provided by a permission-restricted file, no tracked secrets; OIDC deferred.
- `POST /api/v1/worker/leases`, `/leases/{jobId}/heartbeat`,
  `/results`, `/failures` with classified SOURCE_TRANSPORT/WORKER_INTERNAL.
  `GET /api/v1/synthetic/accepted/{jobId}` returns job/lease/batch IDs,
  normalized payload SHA-256, timestamp and immutable gzip reference.
  Server-only job seeding; no exposed mutation shortcut.
- `FirestoreWorkerProtocolStore` uses single document transactional leasing,
  heartbeat, failure and accepted write. At most one active lease; expiry
  recoverable. Client supplies stable `batchId` in SQLite; server enforces
  authoritative job/lease/batch/principal/hash identity. Accepted same-batch
  replay is allowed after lease expiry and returns exactly the original receipt.
  A conflicting normalized payload or principal cannot take ownership.
- `WorkerPocClient` uses JDK `HttpClient` with HTTPS, or local loopback HTTP
  solely for emulator. Worker SQLite remains `PENDING -> ACCEPTED`.
  Independent worker builds without Google SDK. Root includes worker code
  as integration **test-only** Java sources, not shared production module.
- `ObservationArchive` retains previously independently accepted atomic
  gzip/SHA-256 content-addressed provenance. Concurrent duplicate archive
  staging cannot overwrite objects; server transaction defines logical
  idempotency, not the hash alone.
- OpenSpec specs/design/tasks were updated for exact protocol and ID semantics;
  3.x and 5.1/5.2/5.3 marked complete for POC-level evidence, 4.x remain open.

## Evidence / verification

- `./mvnw -B -ntp spotless:check`: PASS.
- `./mvnw -B -ntp checkstyle:check`: PASS.
- `./mvnw -B -ntp clean verify`: PASS; **11 root tests**, zero errors/failures/skips.
- `./mvnw -B -ntp -f worker/pom.xml clean verify`: PASS; **2 worker tests**.
- `gcloud emulators firestore start --database-mode=firestore-native --host-port=127.0.0.1:8085`: PASS; emulator 1.20.2 via gcloud 550.0.0.
- `./mvnw -B -ntp -Pfirestore-emulator verify`: PASS; **5 emulator integration tests**, zero errors/failures/skips. CI refuses skipped/missing IT.
- `./mvnw -B -ntp -f worker/pom.xml dependency:tree -DoutputFile=worker-dependencies.txt`: PASS; Firestore/Storage/Firebase SDK absent.
- OpenSpec `--archived --strict` and `--changes --strict`: PASS.
- Graphify 0.9.92: PASS, protocol/client/application/store call-edge assertions.
- Serena pinned `d9e4c796...`: PASS, new semantic symbols and reference from REST `WorkerProtocolResource` to application `WorkerProtocolService.submit`.
- Java symbol smoke, Alloy 6.2.0, TLC 1.8.0, Dafny 4.11.0 and tracked-secret/runtime artifact scan: PASS.

The four POC emulator tests cover (1) HTTP/SQLite lost ack + restart + REST query,
(2) authentication, heartbeat ownership and both classified failure paths,
(3) concurrent two-worker lease exclusivity and duplicate result convergence,
(4) expiry recovery without sleeping. Existing `FirestoreCentralOperationalStoreIT`
is the fifth emulator test. The accepted result resides in one authoritative
single-job Firestore document and the archived immutable content path is verified.
No live Google credentials or production cloud network calls.

## Final independent technical review: worker dependency CI evidence remediation

**External technical review:** Local POC implementation **PASS**; one **MAJOR**
false-green worker dependency isolation CI finding, with no protocol bug.
Original reviewed HEAD `c6be76b201ef89fee4d4709d7cd1a182ae65a6b8`
and original CI `39874333647` are preserved as historical evidence
but **do not independently demonstrate worker SDK isolation**.

Maven's `dependency:tree -DoutputFile=worker-dependencies.txt` with
`-f worker/pom.xml` writes to `worker/worker-dependencies.txt`;
the former gate mistakenly checked `worker-dependencies.txt` at repo
root, yielding `grep: ... No such file or directory` inside an `if`,
which did not fail the CI job.

Only workflow and evidence were changed. A verified remediation commit
`3c7ddcd469d114e5a40164e2ee5451ee5bd942b1` has full
[CI 39880697503](https://github.com/bambuchastudent/airscan-skill/actions/runs/39880697503)
**9/9 PASS**. The new gate executes `test -s` and `test -r`
against `worker/worker-dependencies.txt`, prints an explicit
presence/nonempty marker and the entire dependency tree, and greps
the exact actual path for `google-cloud-firestore`,
`google-cloud-storage` and `firebase-admin`.
CI log confirms no forbidden dependency, no missing file and an explicit
`Worker central Google SDK isolation: PASS` line. Worker top-level
dependencies shown: Jackson JSON, SQLite JDBC and test-only JUnit.
Missing/empty/unreadable evidence now fails closed.

The complete unchanged test matrix on that remediation commit:
**11/11** root tests PASS, **2/2** worker tests PASS, **5/5**
Firestore Emulator integration tests PASS (zero failures, errors,
skips); OpenSpec strict, Graphify, Serena, static analysis,
worker-secret/runtime-artifact gate, and formal bootstrap regression
jobs PASS. No production or worker source changes. No cloud, airline,
route or fare development.

**Review status:** `READY_FOR_FINAL_EXTERNAL_POC_REVIEW`. PR #5
remains **draft/unmerged** pending final independent metadata/evidence
re-review. The final documentation-only closeout commit should receive
its own exact-HEAD CI; the code/workflow gate fix and its real evidence
are already independently reproducible at the linked SHA.

## Failures and verification limitations

Historical failed and cancelled CI candidates, including Spotless findings
and a mis-scoped assertion on the worker (2 tests versus 5 emulator IT),
are kept in append-only `docs/agent/WORKLOG.md`. Do not count them as green.

Emulator transactions support the narrow single-job POC but do not establish
production multi-job scheduler correctness, fairness, indexes or retention.
**Formal:** existing bootstrap Alloy/TLC/Dafny gates are NOT proof of the
new protocol. For this simple single-job transactional state machine,
reproducible emulator concurrency tests and controlled expiry instants
are the chosen POC evidence for INV-SCAN-001/002. Defer a full scheduler
TLA+ model until `add-durable-scan-retention` has its real state machine.
No claim about global cloud distributed atomicity is made: archive may
contain a harmless orphan after a crash before central acceptance.
No OIDC, Cloud Run deployment, Firebase, GCS client, paid APIs, snapshot,
airline provider, route, fare, ranking or retention logic was implemented.
The unauthenticated synthetic read endpoint must not be internet-exposed
without ingress/query authorization design. Java 25 SQLite native warning
is nonblocking; no JVM flags changed.

**Status:** READY_FOR_EXTERNAL_POC_REVIEW only after documentation-closeout CI is green.  
**Next:** independent external POC review only; do not merge PR #5 or begin cloud
deployment/route topology automatically.

### Reproduce

See `docs/LOCAL_POC.md` for complete local emulator, temporary token
fixture, temp archive directory and Maven test commands.

## Final independent external review — 2026-10-23

**Review outcome: PASS / LOCAL POC COMPLETE; BLOCKER 0; MAJOR 0.** The independent reviewer accepted PR #5 at exact SHA `239ef9e5adcdc846299b6010a9696aa12a623933`. They directly checked final [CI 39881017972](https://github.com/bambuchastudent/airscan-skill/actions/runs/39881017972) **9/9 PASS**; 11/11 root, 2/2 worker and 5/5 Firestore Emulator integration tests, zero failures/errors/skips; the fail-closed dependency test verifies actual, readable/nonempty `worker/worker-dependencies.txt`, and the old `No such file or directory` error no longer appears. Only workflow/evidence, not POC code, changed between the technical acceptance and this reviewed HEAD.

The reviewer separately accepted end-to-end worker → authentication → lease → SQLite durable outbox → HTTP submit → Firestore transactional authoritative idempotency → immutable archive → lost-response identical retry → one logical acceptance → REST read, plus concurrent lease/submission, principal isolation and expiry recovery. This is a **local/emulated POC** acceptance and does not certify deployment, production OIDC/ingress, cost limits, or route/fare semantics.

**Next approved action:** mark draft PR #5 ready and merge after exact-HEAD CI for this final review-evidence note; then read actual `develop` HEAD. **OpenSpec 4.1–4.5 stay [ ]**; deployed synthetic POC (Cloud Run + real Firestore + GCS + OIDC/ingress + Firebase Hosting/cost controls) is the next independent milestone. `add-route-topology` stays deferred until deployed POC PASS. This addendum preserves all previous historical code/CI evidence and the earlier pre-review statuses rather than rewriting their timestamps.
