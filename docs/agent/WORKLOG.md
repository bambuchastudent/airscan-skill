# AirScan Work Log

Append-only evidence log. Newest entries may be appended at the bottom. Do not use this file as project memory; durable current facts belong in MEMORY.md.

## 2026-10-03T16:58Z — repository initialization

**Actor/role:** external architect/planner

**OpenSpec change/tasks:** planning/bootstrap preparation; no implementation task completed

**Commits:**
- `f3fca53196a66b72644d53d96c4b7ad53e268cb5` — initialize README

**Intent:**
- establish the AirScan project description before agentic/bootstrap specifications are committed.

**Changed:**
- `README.md`

**Verification actually performed:**
- GitHub repository metadata read successfully.
- Default branch confirmed as `develop` after initialization.
- Commit existence confirmed through GitHub API.

**Not yet verified:**
- Maven/Java/Quarkus runtime: not created yet.
- OpenSpec CLI validation: not run yet.
- Graphify/Serena: not configured yet.
- Alloy/TLC/Dafny: not configured yet.

**Next step:**
- commit AGENTS.md, tooling/formal conventions, project-local memory/evidence structure, and initial OpenSpec changes; then let the implementation orchestrator apply only `bootstrap-agentic-foundation`.


## 2026-10-04T11:30Z — Google-first distributed architecture revision

**Actor/role:** external architect/planner/reviewer

**OpenSpec change/tasks:** architecture/spec preparation; no implementation task completed

**Intent:**
- replace the single-machine persistence assumption with a distributed worker + hosted control-plane design;
- make REST/OpenAPI primary, MCP an adapter, and Firebase Hosting the human UI host;
- keep the hosted footprint Google-first and suitable for hobby-scale near-zero cost.

**Changed:**
- `docs/ARCHITECTURE.md`, README, AGENTS, TOOLING;
- OpenSpec config and existing bootstrap/topology/scan designs;
- new `add-distributed-control-plane` OpenSpec change;
- project memory and iteration handoff.

**External verification/research actually performed:**
- official Cloud Run pricing/free-tier documentation checked;
- official Firestore pricing/free quotas, transaction/contention guidance, and location documentation checked;
- official Firebase Hosting documentation/pricing and Cloud Run integration checked;
- official Cloud Storage/Firebase storage pricing checked;
- official Google Cloud budget documentation checked.

**Deterministic project verification not yet performed:**
- no Java code/build exists yet;
- OpenSpec CLI validation remains a bootstrap implementation task;
- no Google resources were provisioned;
- no Firestore emulator/Cloud Run/Firebase deployment test was run.

**Decisions:**
- Firestore is the preferred central operational/materialized store.
- Cloud Storage is preferred for compressed append-only history to avoid unnecessary Firestore write/index cost.
- SQLite remains worker-local cache/spool/outbox only.
- distributed workers submit through the control plane; no direct authoritative Firestore writes.
- REST/OpenAPI is primary; MCP is a thin adapter.

**Commits:**
- `8c052f5d8849748e2b8bca9cd8fdad2b546152f4` — distributed Google-first runtime docs/rules.
- `dfbb47b9a143e877883faf5ee2d3a2c0a50c004d` — align existing OpenSpec changes.
- `b127fdc8bb3a5f6bd03b9f7a2b1e60c6c997d0e3` — add distributed-control-plane change.

**Next step:**
- implement/review `bootstrap-agentic-foundation` with revised runtime boundaries only; then apply `add-distributed-control-plane`.
