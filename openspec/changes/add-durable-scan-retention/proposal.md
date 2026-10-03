# Proposal

## Why

AirScan must refresh topology and later fares in the background without allowing transient source failures, worker crashes, overlapping scans, or partial results to corrupt the local view. It also needs explicit retention so routes disappear only after meaningful completed negative evidence.

## What Changes

- Add durable scan plans/jobs/runs with retryable execution.
- Add lease-based ownership and crash recovery.
- Separate successful negative evidence from fetch/parser/rate-limit failures.
- Add route lifecycle states and retention transitions based on completed evidence.
- Add atomic publication of completed snapshots.
- Prevent older snapshots from replacing newer current snapshots.
- Add operational scan metrics required to explain stale/missing data.
- Model scheduler/publication concurrency in TLA+/TLC and deterministic lifecycle transitions in Alloy/Dafny where valuable.

## Capabilities

### New Capabilities

- `scan-retention` — execute durable scans and update route lifecycle only from valid completed evidence.
- `snapshot-publication` — publish an atomic current data snapshot without exposing partial/older scan state as current.

### Modified Capabilities

None.

## Impact

Adds scheduler/storage state, lifecycle transitions, retry/recovery behavior, formal concurrency models, operational metrics, and current-snapshot query semantics. It depends on the route/evidence concepts introduced by add-route-topology.
