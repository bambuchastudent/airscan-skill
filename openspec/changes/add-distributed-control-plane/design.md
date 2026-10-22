# Design

## Context

AirScan needs a central database/API/dashboard that remains available when the developer Mac is offline, but crawling should remain portable because different providers may work best from different environments.

The preferred deployment is Google-first and optimized for hobby-scale cost without making free-tier quotas part of correctness.

## Goals / Non-Goals

Goals:

- central authoritative operational state;
- portable workers;
- idempotent ingestion;
- REST/OpenAPI first;
- MCP adapter reuse;
- Google Cloud/Firebase deployment;
- deterministic emulator/local tests;
- low write amplification and measurable cost drivers.

Non-goals:

- airline-specific adapters;
- fare ranking;
- high-throughput broker architecture;
- multi-region active/active control plane;
- BigQuery analytics before measured need.

## Decisions

### Control plane

Use Quarkus on Cloud Run as the preferred hosted application.

The control plane owns query/application API, worker protocol, authoritative operational persistence, snapshot publication, and the MCP adapter.

Cloud Run minimum instances default to zero.

### Static site

Use Firebase Hosting for the static Vite/MapLibre bundle.

Ordinary short REST calls may use a Firebase Hosting rewrite to Cloud Run where useful for same-origin hosting.

MCP transport should use the direct Cloud Run URL when streaming/request duration makes Hosting rewrites unsuitable.

### Operational database

Use Firestore as the preferred central operational/materialized store.

Avoid global hot documents and unnecessary index fanout. Use narrow transactions for lease/publication invariants. Use the Firestore emulator for deterministic integration tests where it faithfully covers required behavior.

### Historical archive

Use Cloud Storage for compressed immutable observation/scan batches. Firestore stores required current/materialized state plus archive references/hashes.

Do not require BigQuery for the first implementation. Introduce it only if historical analytics/search measurements justify the extra component.

### Worker-local state

Use SQLite for local cache and durable outbox/recovery. Worker state is never authoritative for published snapshots.

### Worker protocol

Initial logical operations:

- request/lease work;
- heartbeat/renew lease;
- submit normalized idempotent result batch;
- submit classified failure/diagnostics;
- fetch worker-compatible configuration where appropriate.

Exact HTTP resources and authentication are finalized during implementation design.

Every mutating submission carries stable worker/job/lease/batch identity sufficient for deduplication and audit.

### Authentication

Workers must authenticate for mutating operations. Compare a simple personal-project token mechanism with Google-native identity/OIDC for Cloud Run Jobs and local development before selecting the first implementation.

Do not distribute broad Firestore credentials/service-account keys merely to let workers write the database directly.

### Region

Prefer `europe-southwest1` (Madrid) for supported regional Google resources to reduce latency from Spain. Verify location support and cost at deployment time rather than assuming all products share identical regional behavior.

### Scheduling

Use one/few Cloud Scheduler triggers to wake the scan planner rather than a scheduler resource per source/route. Workers lease eligible persisted jobs.

Do not add Pub/Sub or Cloud Tasks until measured scheduling/throughput needs justify them.

### Cost observability

Track at least:

- Firestore reads/writes/deletes;
- archive bytes/objects;
- Cloud Run requests and compute time where available;
- scan/request counts by source;
- batch size and deduplication count.

Configure a Google Cloud budget alert when billing is attached. Document that a budget alert does not itself cap spending.

## Risks / Trade-offs

- [Firestore write quota/cost] -> batch/deduplicate ingestion, materialize only hot query state, archive bulk history in Cloud Storage.
- [Firestore contention] -> avoid single global documents, keep transactions narrow, test lease/publication races.
- [Worker retries duplicate data] -> idempotency key plus authoritative deduplication.
- [Cloud Run cold starts] -> accept for hobby workload; do not pay for minimum instances without measured need.
- [Shared worker token compromise] -> keep auth swappable and scope mutating endpoints; prefer stronger Google identity when operationally practical.
- [Cloud lock-in] -> isolate Firestore/Storage/Firebase adapters outside domain/application services.

## Iteration 1 decisions — runtime/API boundaries (2026-10-18)

### Runtime ownership and module decision

Keep the existing two independent build artifacts. The root Quarkus artifact owns the control-plane
application service and its REST and MCP adapters; `worker/pom.xml` remains an independently
built plain-Java executable with **no** control-plane classpath or Firestore dependency.
A common Maven module is not justified while the worker only has its bootstrap entrypoint.
Revisit shared DTO packaging only after a real, versioned worker HTTP contract demonstrates
a need; generated REST/OpenAPI clients are an alternative to sharing implementation classes.

The Quarkus application boundary is `CapabilitiesService.current()`, returning the immutable
`CapabilitiesSnapshot` API model. The canonical read operation is
`GET /api/v1/capabilities`. This is an intentionally narrow system-level capability
discovery query (not `/bootstrap/status`, not a travel query). Its only advertised operation
is `airscan.capabilities`. Version `v1` appears in both path and JSON payload.
The resource invokes the application service and has no MCP dependency.

The MCP-side `McpCapabilitiesAdapter` exposes MCP-shaped tool metadata (`name`,
`description`, `inputSchema`) and tool-call results (`content` text blocks and
`isError`). Its dispatcher calls **the identical application service** rather than
replicating capability values; a deterministic contract test replaces that service with
a sentinel and compares the real REST response to the MCP tool result. This is a
tool-dispatch adapter boundary, **not** a deployed MCP JSON-RPC/HTTP transport or an
MCP compatibility certification. Wire transport/session negotiation later without
reimplementing application semantics. No route/fare/worker tool is advertised.

### Worker identity, request identity, and authentication evaluation (3.3 design only)

The eventual worker protocol will separate these identities:

- `workerId`: registered logical worker principal, not a trusted self-asserted HTTP header.
- `jobId`: server-assigned durable logical work identifier.
- `leaseId`: server-issued attempt/lease identifier, bound to one job and principal.
- `batchId`: worker-generated stable ID persisted across retries for one logical result batch.
- The control plane will deduplicate against an authoritative composite identity
  (`jobId`, `leaseId`, `batchId`) and validate its binding to the authenticated principal.
  Detailed resource paths, expiration/release rules and storage transactions belong to
  Iteration 3; this design does not claim they have been implemented.

**Option A — scoped project worker token:** straightforward for a local process, CI secret,
and local-to-remote HTTPS; needs per-worker issuance, revocation and careful rotation. A
stolen bearer token permits requests within its scope until revoked. Do not use a global
administrator token or put tokens in URLs, logs, argv, images, or repository files.

**Option B — Google-native OIDC identity token:** Cloud Run Jobs can use short-lived
workload identity tokens with audience and issuer validation and a workload-to-worker
registration mapping. Avoid exportable service-account key JSON. Local development can
obtain a supported identity token, but this imposes Google login/configuration on offline
development and must not become a deterministic-test dependency.

**Decision:** implement Option A first **only for local/dev worker authorization** when
the authenticated worker protocol arrives (Iteration 3), behind a worker-identity
verification boundary that returns a typed principal. Prefer Option B for Google-hosted
workers and add its verifier independently, without changing job/lease/batch application
semantics. Neither verifier exists in Iteration 1: no worker-mutating API is exposed.
Production deployment must explicitly decide Cloud Run ingress/IAM vs application-layer
verification; do **not** assume an application token bypasses a private Cloud Run IAM
front door. All worker writes remain protected by an authenticated control-plane HTTP
boundary and never obtain direct Firestore write access.

**Local behavior:** no Google secrets required for fake-service tests. Later local workers
read a per-worker dev token from an untracked environment/OS secret store; no hardcoded
default or auth-off fallback, even on localhost. Local integration tests can inject a
fake verifier without real credentials.

**Cloud Run Job behavior:** prefer short-lived OIDC tokens minted for a narrowly scoped
runtime identity; validate signature, trusted issuer, configured audience, expiry and
registered principal/server-side permissions. Exact Google token acquisition and Cloud Run
ingress configuration require deployment-time external verification (Iteration 4).

**Threat model:** assume hostile internet clients, stolen/replayed bearer tokens,
misconfigured access controls, worker impersonation, privilege escalation across jobs,
duplicate/delayed batches, secret leakage in CI/logs, and service compromise. Require TLS,
per-principal permissions, server-bound leases, bounded payloads, logging without secrets,
token rotation/revocation, idempotent durable ingestion, and replay limits. Idempotency
is not authentication. CI must reject tracked credentials/runtime databases; production
secrets belong in a managed secret store or workload identity, not committed files.
Workers never receive broad Firestore service-account keys; database mutations and
authorization decisions remain solely in the control plane.

### Deliberately deferred

No worker lease, heartbeat, result ingestion, Firestore repository, SQLite outbox,
Cloud Storage archive, Cloud Run deployment, Hosting, scheduling or travel-domain
API is part of this iteration. No formal concurrency/state invariant is introduced.

## Iteration 2 persistence decisions (2026-10-18)

- The existing root Quarkus artifact owns `CentralOperationalStore` (application port),
  `CentralRecordService` (server-generated UUID identity), and the Firestore adapter.
  A tiny synthetic control record containing a bounded label proves create/read
  without exposing any unauthenticated mutating REST or MCP endpoint.
  The adapter accepts an explicitly supplied Firestore client. Production
  credential/client wiring is intentionally deferred: Iteration 2 never performs
  implicit ADC discovery at Quarkus startup.
- Firestore emulator integration is opt-in via mandatory CI job and Maven
  `-Pfirestore-emulator verify`. It exercises real Java SDK operations, creates
  and reads a synthetic record, disposes the client, creates a new client,
  and reads the same emulator-resident record without Google credentials.
  Default Maven unit tests use only a fake port. No emulator skip-if-absent:
  emulator test fails if environment variables or service are missing.
  Emulator data is memory-only and the suite does not test production indexes
  or transaction concurrency semantics.
- Worker-only `SqliteOutbox`: schema version via `PRAGMA user_version=1`,
  `batch_id` primary key, sanitized `payload`, `created_at` ISO timestamp,
  `state` PENDING/ACCEPTED. `INSERT OR IGNORE` is followed by deterministic
  payload/timestamp collision verification; the batch retry identity persists
  over close/reopen. WAL, 5000ms busy timeout and synchronous FULL are local
  durability choices. No worker HTTP, job/lease identity, or central authority.
- `ObservationArchive` is a pure application port. A deterministic local
  `LocalGzipObservationArchive` test adapter writes a tiny sanitized synthetic
  batch to a SHA-256 content-addressed gzip path, with source and batch
  identities also hashed into path components. CREATE_NEW forbids overwrites;
  if an object already exists, decompression must match the requested bytes.
  Its `ArchiveReference` retains source, batch, hash, object path, compression
  and byte sizes. This is not a GCS client implementation; an authenticated
  immutable Cloud Storage adapter is a later, separately verified concern.
- No direct worker Firestore or Cloud Storage SDK dependency. A CI dependency
  tree assertion enforces this. No lease/heartbeat/ingestion/auth/API addition.
  Existing formal bootstrap invariants are untouched because no distributed
  state machine or publication behavior is implemented.

## Iteration 2 final archive-publication remediation (2026-10-18)

Independent review of PR #4 at `d1bdaee60137d1fc367c1e573c845d92fc396711`
accepted the persistence boundaries but identified **MAJOR** crash-atomicity risk
in writing gzip bytes directly to the final immutable object path.

The local archive now stages each complete gzip in a unique
`Files.createTempFile` file in the destination's directory, writes through a
`FileChannel`, and calls `force(true)` before publication. It uses
`Files.createLink(destination, staging)` to publish the completed file as a
single atomic, no-overwrite directory entry; `FileAlreadyExistsException`
means another writer owns the final key. Verify that existing gzip decompresses
to the exact expected payload; corrupted or conflicting content fails closed.
`finally` deletes staging on success and Java-observable failure.

**Why hard link rather than `Files.move(..., ATOMIC_MOVE)`:** Java's NIO
contract explicitly permits `ATOMIC_MOVE` to overwrite an existing target,
even without `REPLACE_EXISTING`, on some file systems. That cannot satisfy
immutable create-only semantics under concurrent writers. Atomic hard-link
creation fails when the destination exists, preventing such replacement.
This optional file-system operation must be supported; when unavailable, fail
closed instead of a non-atomic copy or potentially destructive move.
This is local-only and does not change the planned GCS adapter semantics.

A failure before publication leaves only staging (which cleanup handles where
possible); a crash after link publication exposes only fully written,
file-channel-synced gzip data. A sudden process kill can leave an orphan
staging file, but it never becomes an authoritative final archive object.
Directory-entry persistence across power loss is not guaranteed merely by
`force(true)` on the file; losing the final link is preferable to exposing a
partial gzip. Filesystems without hard-link support are unsupported for this
local adapter until a separately verified safe create-only primitive exists.

SQLite's `PENDING -> ACCEPTED` model and deterministic
`ORDER BY created_at, batch_id` remain unchanged: there is no `IN_FLIGHT`
state or FIFO sequence. A future lost HTTP response must remain safe through
**control-plane idempotency** in Iteration 3; an `IN_FLIGHT` state can itself
strand work and is not justified here.

Future external-facing `ArchiveReference` fields (`sourceId`, `batchId`,
`objectKey`, `sha256`) must be validated against authoritative metadata;
they are not self-authenticating. SQLite JDBC's Java 25 native-access warning
is nonblocking; monitor JDK enforcement and validate flags or dependency
upgrades only when supported by reproducible evidence.

**Verification:** implementation SHA
`c4f25ff57e7425febb725b5c04385c9c1476f812`,
CI [39148837864](https://github.com/bambuchastudent/airscan-skill/actions/runs/39148837864)
**9/9 PASS**, including 3 archive tests, 11 root tests, 2 worker tests,
1 Firestore emulator integration test, Graphify/Serena and existing formal gates.
No worker protocol, central storage, authentication or deployment behavior changed.

## Local authenticated worker protocol POC (2026-10-23)

Iteration 2 was independently accepted and PR #4 merged at
`7b693d97d15c0f8642ca2c08ce6ece36bfaa4d18`, the verified
base for draft POC PR #5. No Google deployment, real GCS adapter, fare,
route, retention or snapshot publication was added.

### REST/OpenAPI contract, authority and identities

Local synthetic POC only. Canonical HTTP paths:
- `POST /api/v1/worker/leases`: `{jobId}` → `{jobId,leaseId,workerId,expiresAt}`;
- `POST /api/v1/worker/leases/{jobId}/heartbeat`: `{leaseId}` → same
  immutable lease ID with server-extended `expiresAt`;
- `POST /api/v1/worker/results`:
  `{jobId,leaseId,batchId,normalizedPayload}` → immutable accepted receipt;
- `POST /api/v1/worker/failures`:
  `{jobId,leaseId,classification}` → `{status:"RECOVERABLE"}`;
- `GET /api/v1/synthetic/accepted/{jobId}`: accepted receipt containing
  batch identity, normalized content hash, accepted timestamp and archive
  provenance, 404 until accepted.

The control plane seeds a UUID synthetic job using
`WorkerProtocolService.seedSyntheticJob` in the local integration harness,
**not through an unprotected seeding endpoint**. Firestore is authoritative
for job status and current lease identity. Lease ID is a fresh server UUID;
`workerId` is derived from `WorkerIdentityVerifier`, not a request
header. The client's stable `batchId` stays in the SQLite
PENDING/ACCEPTED outbox across restarts. This POC does not define
route/provider semantics. REST adapters delegate to shared application service.

### Local auth

`LocalTokenWorkerIdentityVerifier` receives a map of bearer token →
typed `WorkerPrincipal` from runtime configuration, with fail-closed
401 for missing/wrong token and 403 for wrong principal on an active lease.
The CI fixture generates two random 192-bit tokens into a permission-restricted
temporary file (`AIRSCAN_POC_WORKERS_FILE` passes **only its path**, never
secret values as Actions environment metadata); no literal token exists in
source or CI logs. POC application bootstrapping fails 503 without
`FIRESTORE_EMULATOR_HOST`, `AIRSCAN_TEST_PROJECT_ID`,
`AIRSCAN_POC_ARCHIVE_DIR` and the local secret file. No ADC or
service-account access is required; POC runtime cannot accidentally use
production Firestore. This local/dev verifier is not a production
multi-tenant auth/rotation service. Google OIDC remains a separate future
deployment verifier with issuer/audience/expiry/principal mapping.

### Lease and idempotent central state

`FirestoreWorkerProtocolStore` keeps a single
`poc_worker_jobs/{jobId}` document. Lease creation, renewal,
classified recoverable failure and acceptance are Firestore transactions;
a new active lease cannot coexist with an unexpired lease. Expired
leases become eligible for reacquisition. Heartbeat and result verify
server-known principal, existing lease and expiry; worker-supplied
expiry and worker ID are ignored. Failure types
`SOURCE_TRANSPORT` and `WORKER_INTERNAL` return the job to READY
without publishing successful or negative route evidence.

A first accepted synthetic JSON record (`{"kind":"synthetic","value":7}`)
is normalized and SHA-256-hashed, archived with immutable gzip reference
through `ObservationArchive`, then committed centrally in a Firestore
transaction. The authoritative single-job acceptance record enforces
the tuple `(jobId,leaseId,batchId)` and normalized content hash plus
worker ownership. Concurrent duplicates resolve to the same stored
`Accepted`; collisions or principal changes fail. A replay of the
same committed batch is accepted even after the original lease expires,
because the server checks committed receipt identity before requiring
an active lease; this is crucial to surviving a lost acknowledgment.
A crash after archive publication but before Firestore commit may leave
an orphan immutable archive object; retry safely republishes/validates
the same content-addressed object and commits at most one logical
ingestion. The archive's SHA-256 is not the authority for ingestion.

`WorkerPocClient` is compiled and tested in the independent worker
Maven artifact, with java.net.http HTTPS and localhost-only HTTP for
emulators. It has no Google SDK dependency. The root integration-test
Maven build includes worker Java sources **only as test sources**, never
in the root production application classpath. SQLite PENDING → ACCEPTED
remains unchanged, with no IN_FLIGHT or strict FIFO sequence.

### Emulator and formal scope

The mandatory `firestore-emulator` CI job uses a pinned gcloud
550.0.0 + Firestore emulator, creates temp token and archive fixtures,
and fails if fewer than 5 Failsafe tests execute (1 existing Firestore
persistence IT + 4 new WorkerPocIT scenarios). These exercise actual
Quarkus HTTP transport → application → emulator, worker HTTP client,
worker SQLite close/reopen and immutable local archive. Real Firestore
transactions are involved in concurrent two-worker leasing and duplicate
submissions. `GET /api/v1/synthetic/accepted` verifies the committed
accepted receipt; no mocks stand in for the central persistence.

**Formal decision:** INV-SCAN-001 and INV-SCAN-002 are operational
safety/recovery candidates. The POC's exact scope is a single synthetic
job document, with short transactional state transitions, a fake
clock driving expiry tests without sleeping and real emulator
concurrency tests. Existing Alloy/TLC/Dafny bootstrap gates still run
but do **not** prove this protocol. A full TLA+/TLC scheduler/fairness
model belongs to `add-durable-scan-retention` when multi-job queues
and real scheduling semantics are defined, rather than a ceremonial
model of one synthetic document. An external reviewer may require
additional bounded concurrency proof before cloud deployment.

**Limitations:** A publicly readable accepted synthetic receipt is
acceptable only for the isolated local emulator; before exposure on
the internet, impose appropriate query authorization/ingress.
Local test-supplied tokens are not production identity. No persistent
external scheduler, backup, distributed archive-to-Firestore atomic
commit, OIDC verifier, Cloud Run, real GCS, fares or topology exists.
