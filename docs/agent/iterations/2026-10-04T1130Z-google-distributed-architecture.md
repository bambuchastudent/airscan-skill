# Goal

Revise AirScan from a single-machine/local-database assumption to a Google-first distributed architecture while keeping crawler execution portable and low cost.

# Starting context

The repository already defined Java/Quarkus, local SQLite, hosted map goals, MCP, formal methods, and agent evidence rules. The user clarified that crawler/search execution may run in multiple places, while the database, static site, and MCP should stay available on free/near-free hosting with a Google technology preference.

# What changed

- Established control-plane and crawler-worker runtime roles.
- Selected Google-first deployment direction:
  - Cloud Run control plane/MCP;
  - Firestore operational/materialized store;
  - Cloud Storage compressed append-only history;
  - Firebase Hosting static UI;
  - SQLite worker cache/spool/outbox.
- Made REST/OpenAPI the primary machine contract.
- Kept MCP as a thin adapter over shared application services.
- Defined that workers never mutate authoritative Firestore directly.
- Added `add-distributed-control-plane` before route-topology/scan-retention implementation.
- Updated later OpenSpec designs so they do not assume SQLite is the authoritative central database.

# What did not change

- Java remains the production language.
- Quarkus remains the preferred backend framework pending bootstrap version verification.
- VLC/CDT/ALC/MAD/BCN remain the initial origins.
- OpenSpec/Graphify/Serena/Alloy/TLA+/Dafny workflow remains.
- No crawler, provider, cloud resource, or production code was implemented.

# Evidence

Architecture research used current official Google/Firebase documentation for Cloud Run, Firestore, Firebase Hosting, Cloud Storage pricing, Firestore Madrid location, and Google Cloud budget alerts.

No local deterministic code/tool checks were run because implementation has not started.

# Decisions

1. Central operational truth belongs to the control plane, not crawler workers.
2. Firestore stores hot operational/materialized state rather than every raw/historical observation.
3. Full retained scan/fare batches should be immutable compressed archive objects in Cloud Storage.
4. Worker SQLite is non-authoritative and designed for offline-safe outbox/retry.
5. REST/OpenAPI is the stable contract; MCP must not fork business logic.
6. Google-first deployment remains adapterized so domain semantics are not cloud-specific.

# Unresolved risks/questions

- Select concrete worker authentication mechanism during `add-distributed-control-plane`.
- Verify actual service-region/cost configuration before provisioning.
- Measure Firestore write/read volume before increasing scan cadence.
- Decide archive object format only after data/query requirements are measured.
- Verify whether one Cloud Run service or separate public-query/worker-control services gives the best security/cost trade-off.

# Next recommended action

Apply and verify `bootstrap-agentic-foundation` only, preserving the two runtime roles without provisioning Google resources. After external review, apply `add-distributed-control-plane`.

# Relevant artifacts

- `docs/ARCHITECTURE.md`
- `AGENTS.md`
- `docs/TOOLING.md`
- `openspec/config.yaml`
- `openspec/changes/bootstrap-agentic-foundation/`
- `openspec/changes/add-distributed-control-plane/`
- `openspec/changes/add-route-topology/`
- `openspec/changes/add-durable-scan-retention/`
