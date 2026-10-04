# Proposal

## Why

AirScan should allow crawler/search workers to run wherever they are cheapest or most practical while retaining one authoritative, inexpensive hosted database/API/dashboard. A single-machine SQLite architecture would make MCP and the shared map unavailable when the local machine is offline and would make multiple crawler workers unsafe.

## What Changes

- Introduce a hosted control-plane runtime role and a portable crawler-worker role.
- Make versioned REST/OpenAPI the primary machine contract and MCP a thin adapter.
- Add an authenticated worker protocol for lease, heartbeat, and idempotent result submission.
- Use Google Cloud Run as the preferred control-plane/MCP host.
- Use Cloud Firestore as the preferred authoritative operational/materialized store.
- Use Cloud Storage for compressed immutable scan/fare archives.
- Keep SQLite on workers for cache/spool/outbox and offline recovery.
- Use Firebase Hosting for the static web application.
- Establish low-cost scheduling/cost-observability/budget rules.
- Keep cloud-specific adapters outside domain logic.

## Capabilities

### New Capabilities

- `distributed-control-plane` — shared hosted operational state and query/control interfaces.
- `worker-protocol` — portable crawler workers lease work and submit idempotent batches without direct database mutation.

### Modified Capabilities

None.

## Impact

This change introduces runtime/deployment boundaries, central persistence adapters, worker communication, Google Cloud/Firebase configuration, emulator-based integration testing, authentication decisions, and cost/operational observability. It intentionally does not implement airline-specific crawling or fare ranking.
