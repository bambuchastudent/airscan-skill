# Tasks

## 1. Durable scan state

- [ ] 1.1 Define scan plan/job/run/lease/result contracts for the central control-plane store plus worker-local SQLite outbox/recovery state, then verify deterministic emulator/local persistence tests from clean state.
- [ ] 1.2 Implement lease acquisition/heartbeat/expiry/recovery through the authenticated worker API aligned with INV-SCAN-001/002 and verify concurrent integration tests cannot obtain two valid leases for one logical job.
- [ ] 1.3 Implement classified retry/backoff configuration and verify failed transport/parser outcomes cannot be recorded as successful negative observations.

## 2. Formal scheduler model

- [ ] 2.1 Create a finite TLA+/PlusCal model for job leasing, worker failure, retry, and recovery; verify TLC checks INV-SCAN-001/002 in the committed model configuration.
- [ ] 2.2 Add snapshot publication to the TLA+ model and verify TLC checks INV-SNAP-001/002 including late completion of older work.

## 3. Route retention

- [ ] 3.1 Specify lifecycle states/threshold configuration and update the Alloy model for INV-RET-001/002; verify bounded checks in committed scopes.
- [ ] 3.2 Implement the pure lifecycle transition in Dafny if the design remains suitable, verify it without unreviewed assumptions, integrate generated Java, and add Java boundary tests; otherwise document the reviewed decision and equivalent deterministic tests.
- [ ] 3.3 Persist lifecycle evidence/history without deleting old route observations and verify failed scans leave miss counters/lifecycle unchanged.

## 4. Atomic snapshot publication

- [ ] 4.1 Implement central persisted snapshot/generation state and Firestore transaction/compare-and-set publication semantics; verify partial/failed snapshots never become current.
- [ ] 4.2 Implement monotonic publication protection and verify an older late-finishing scan cannot replace a newer current snapshot.
- [ ] 4.3 Expose backing snapshot identity/freshness to query consumers and verify deterministic integration tests.

## 5. Operational evidence

- [ ] 5.1 Persist/expose last successful scan, duration, request/result counts, retries, parser failures, and staleness information sufficient to diagnose missing data; verify query tests.
- [ ] 5.2 Run Maven tests, TLC, relevant Alloy/Dafny checks, strict OpenSpec validation, and Graphify/Serena impact review before marking the change complete.
