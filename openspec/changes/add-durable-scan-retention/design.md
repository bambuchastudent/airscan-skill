# Design

## Context

This change depends on add-distributed-control-plane and persisted route evidence from add-route-topology. Background crawling introduces concurrency and partial-failure risks that should be fixed before live fare providers multiply.

## Goals / Non-Goals

Goals:

- durable scan jobs/runs;
- lease-based ownership and recovery;
- configurable retry/backoff;
- lifecycle transitions based only on valid completed evidence;
- atomic current-snapshot publication;
- deterministic observability;
- formal checking of concurrency/state transitions.

Non-goals:

- specific airline fare adapters;
- a high-throughput message broker;
- provider-specific fare crawling;
- dashboard UI.

## Decisions

### Durable state over in-memory scheduling

Persist authoritative plans/jobs/leases/runs in the central control-plane store (preferred implementation: Firestore). The scheduler may use Cloud Scheduler or local timers to wake the planner, but timers are not the source of truth.

Distributed workers obtain leases and submit results through the worker HTTPS protocol. Worker-local SQLite may persist leased work state and an outbox for crash/offline recovery, but it cannot publish authoritative snapshots by itself.

### Central and worker write discipline

Use Firestore transactions only for invariants that need compare-and-set/atomicity, such as lease acquisition and monotonic snapshot publication. Avoid a single global coordination document that becomes a hotspot.

Worker SQLite uses short explicit transactions and a crash-safe outbox. Network retries must be idempotent so reconnecting workers cannot duplicate logical observations.

### Lease model

A lease has explicit worker identity, owner/token, acquired time, and expiry. Completion/retry operations must validate ownership/version so stale workers cannot publish after losing the lease.

### Retry semantics

Classify failures before retry policy. Transient network/rate-limit/server failures may retry with exponential backoff and jitter. Permanent parser/contract/config errors require diagnostics and must not masquerade as successful negative scans.

### Retention transition

Apply INV-RET-001 and INV-RET-002. The lifecycle policy is configuration, not scattered constants. Dafny is preferred for the pure transition function if it cleanly maps states/evidence to the next state.

### Snapshot publication

A scan run accumulates observations without making them globally current. Publication records the current snapshot only when completion criteria are satisfied. Use a monotonic generation/version check to enforce INV-SNAP-001/002.

### Formal model

TLA+/PlusCal models leases, retry/recovery, and snapshot publication interleavings for:

- INV-SCAN-001
- INV-SCAN-002
- INV-SNAP-001
- INV-SNAP-002

Alloy models route lifecycle/evidence structure where useful. Dafny may implement the pure lifecycle transition.

## Risks / Trade-offs

- [Firestore contention/hotspotting] -> shard independent job state, avoid global counters, keep transactions narrow, and verify with emulator/concurrency tests.
- [Worker SQLite contention] -> bounded local concurrency, short transactions, and a controlled outbox write path.
- [Lease expiry during slow source request] -> explicit heartbeat/lease-renewal policy or conservative lease duration documented before implementation.
- [Overly aggressive route deactivation] -> only successful relevant misses count; thresholds are configurable and history is never erased.
- [State-machine implementation diverges from model] -> invariant IDs and tests map modeled transitions to production behavior.
