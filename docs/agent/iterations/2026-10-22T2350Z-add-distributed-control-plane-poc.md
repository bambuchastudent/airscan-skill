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
