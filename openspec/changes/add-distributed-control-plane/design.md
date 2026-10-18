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
