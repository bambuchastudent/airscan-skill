# AirScan Runtime Architecture

## Goal

AirScan is a distributed, local-friendly system: crawling/search work may execute on several machines or clouds, while one low-cost Google-hosted control plane provides authoritative operational state, query APIs, MCP, and the static dashboard.

## Preferred Google-first topology

```text
Browser -> Firebase Hosting (Vite/MapLibre)
                 |
                 | ordinary /api HTTPS
                 v
        Cloud Run control plane <----- MCP clients (direct Cloud Run)
        REST/OpenAPI + MCP
          |                 |
          v                 v
      Firestore        Cloud Storage
   operational/hot    immutable scan/fare
      state            archive batches
          ^
          |
 authenticated worker HTTPS
 lease/heartbeat/submit
          |
   +------+------+----------------+
   |             |                |
local Mac   Cloud Run Job     other CI/VPS
SQLite      optional          worker
spool
```

## Runtime roles

### Control plane

Authoritative for scan plans/jobs and leases, normalized operational route state, current snapshots, materialized query indexes, worker-ingestion/idempotency decisions, freshness, and diagnostics.

Preferred host: Google Cloud Run with minimum instances zero.

REST/OpenAPI is the primary machine contract. MCP is an adapter over the same application/query services.

### Crawler worker

A worker may run on a local Mac, Cloud Run Job, CI runner, inexpensive VM, or another cloud. It obtains/renews a lease, calls source adapters, keeps temporary/cache/outbox state locally, normalizes results, and submits idempotent batches to the control plane.

Worker SQLite is cache/spool/outbox, not the source of truth.

## Interfaces

- **REST/OpenAPI:** stable versioned query/control contract.
- **CLI/JSON:** local operations and simple automation.
- **MCP:** thin adapter over shared application services.
- **Worker API:** authenticated lease/heartbeat/result protocol.
- **Web UI:** static Firebase Hosting application consuming the read API, not Firestore directly.

For long-lived/streaming MCP transport, use the direct Cloud Run endpoint instead of depending on Firebase Hosting rewrites.

## Data placement

### Firestore

Use for hot/queryable/current state: canonical airport/route records, source freshness, job/lease/run metadata, current snapshot pointers, materialized fare summaries/top-N indexes, and provenance/archive pointers.

Avoid writing every raw response or large historical batch as individual Firestore documents.

### Cloud Storage

Use compressed immutable objects for normalized observation batches, retained full fare-history segments, and sanitized source evidence when retention is justified. Objects must be tied to scan/source/provider version and content hash.

BigQuery is a later option only if historical analytics actually justify it.

### SQLite on workers

Use for HTTP/source cache metadata, in-progress work, durable outbox, retry state, and optional local developer inspection. Derived cache must be rebuildable.

## Scheduling

Start simple: one/few Cloud Scheduler triggers wake the planner; persisted jobs are leased by workers with backoff. Do not add Pub/Sub/Cloud Tasks/message brokers until measurements justify them.

## Region

Preferred first region for supported Google resources: `europe-southwest1` (Madrid), subject to verification for every selected service at deployment time.

## Cost discipline

Correctness must not depend on free-tier quotas.

- Cloud Run min instances default to zero.
- Batch/deduplicate worker submissions.
- Minimize Firestore index fanout and hot documents.
- Put bulky history in compressed Cloud Storage objects.
- Measure Firestore reads/writes, archive bytes, Cloud Run requests/compute, and scan counts.
- Configure Google Cloud budget alerts before non-trivial automation; budget alerts are not a hard spend cap.

## Security boundary

Workers do not receive direct authority to mutate authoritative Firestore state. Mutating worker requests must be authenticated. Exact machine auth is selected by the distributed-control-plane change.

## Portability

Google Cloud is the preferred deployment, not the domain model. Firestore/Cloud Run/Firebase specifics must remain in adapters/deployment code so route identity, scanning, retention, ranking, and provider parsing remain portable.
