# AirScan Invariant Catalogue

This file is the stable index connecting product invariants to formal models, implementation, and tests.

Status values: `candidate`, `specified`, `modeled`, `implemented`, `verified`.

## INV-ROUTE-001 — Route endpoints differ

**Statement:** A direct air route must not have the same canonical airport as both origin and destination.

- Status: candidate
- Alloy: planned in route topology model
- TLA+: not applicable
- Dafny: candidate for canonical route construction
- Java/test: required in route domain tests

## INV-ROUTE-002 — Canonical route identity is deterministic

**Statement:** The same normalized origin, destination, and carrier identity must produce the same logical route identity independent of observation order.

- Status: candidate
- Alloy: uniqueness relation planned
- TLA+: not applicable
- Dafny: planned candidate
- Java/test: property/unit tests required

## INV-EVIDENCE-001 — Normalized facts retain provenance

**Statement:** A normalized route or fare fact that is persisted must be traceable to at least one source observation and scan run.

- Status: candidate
- Alloy: planned
- TLA+: not applicable
- Dafny: not applicable
- Java/test: repository constraints/integration tests required

## INV-FARE-001 — Fare observations are historical evidence

**Statement:** A newly observed fare creates a new historical observation rather than mutating a previous price observation into the new value.

- Status: candidate
- Alloy: relationship model optional
- TLA+: not applicable
- Dafny: not applicable
- Java/test: storage integration tests required

## INV-RET-001 — Failed scans cannot deactivate routes

**Statement:** A fetch, parser, authentication, rate-limit, or transport failure does not count as negative route evidence.

- Status: candidate
- Alloy: planned lifecycle model
- TLA+: possible scan-result mapping check
- Dafny: candidate lifecycle transition
- Java/test: lifecycle tests required

## INV-RET-002 — Route inactivity requires completed negative evidence

**Statement:** An active route may become suspected/inactive only from the configured number of relevant successfully completed scans that did not observe it.

- Status: candidate
- Alloy: planned
- TLA+: not required initially
- Dafny: planned candidate
- Java/test: lifecycle tests required

## INV-SCAN-001 — One active lease per logical job

**Statement:** At most one non-expired active lease exists for a logical scan job at a time.

- Status: candidate
- Alloy: not applicable
- TLA+: planned
- Dafny: not applicable
- Java/test: scheduler integration tests required

## INV-SCAN-002 — Expired work is recoverable

**Statement:** A worker crash or expired lease cannot permanently prevent an eligible scan job from being executed again.

- Status: candidate
- Alloy: not applicable
- TLA+: planned liveness/safety model
- Dafny: not applicable
- Java/test: recovery tests required

## INV-SNAP-001 — Partial scans are never current

**Statement:** A snapshot may become current only after all jobs required by that snapshot satisfy the configured completion policy.

- Status: candidate
- Alloy: relationship model optional
- TLA+: planned
- Dafny: not applicable
- Java/test: publication integration tests required

## INV-SNAP-002 — Snapshot publication is monotonic

**Statement:** An older completed snapshot must never replace a newer current snapshot for the same logical scan scope.

- Status: candidate
- Alloy: not applicable
- TLA+: planned
- Dafny: not applicable
- Java/test: concurrency/integration tests required

## INV-RANK-001 — Top-N results are deterministic

**Statement:** Given the same eligible observation set, horizon, origin, fare semantics, and tie-break rules, top-N selection returns the same ordered result.

- Status: candidate
- Alloy: not applicable
- TLA+: not applicable
- Dafny: planned candidate
- Java/test: property/unit tests required

## Maintenance rules

- Add or modify invariant IDs in the same change that introduces the behavior.
- Never silently reuse an ID for a different meaning.
- Formal models reference these IDs in comments/docs.
- A model checker result must record bounds/configuration where relevant.
- "Verified" means the stated tool/check ran successfully for its declared scope; it does not imply guarantees outside that scope.
