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
