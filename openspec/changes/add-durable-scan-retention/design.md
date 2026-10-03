# Design

## Context

This change depends on persisted route evidence from add-route-topology. Background crawling introduces concurrency and partial-failure risks that should be fixed before live fare providers multiply.

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
- distributed multi-node deployment;
- high-throughput message brokers;
- dashboard UI.

## Decisions

### Durable state over in-memory scheduling

Persist plans/jobs/leases/runs in SQLite. The scheduler may use timers to wake up, but timers are not the source of truth.

### SQLite write discipline

Design for SQLite's single-writer characteristics. Prefer short explicit transactions and a controlled writer boundary rather than allowing unbounded crawler threads to contend on writes.

### Lease model

A lease has explicit owner/token, acquired time, and expiry. Completion/retry operations must validate ownership/version so stale workers cannot publish after losing the lease.

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

- [SQLite contention under aggressive parallel scans] -> bounded workers, per-host limits, short transactions, controlled write path.
- [Lease expiry during slow source request] -> explicit heartbeat/lease-renewal policy or conservative lease duration documented before implementation.
- [Overly aggressive route deactivation] -> only successful relevant misses count; thresholds are configurable and history is never erased.
- [State-machine implementation diverges from model] -> invariant IDs and tests map modeled transitions to production behavior.
