# Tasks

## 1. Runtime boundaries and API

- [ ] 1.1 Define control-plane and crawler-worker runtime boundaries without duplicating domain logic; verify clean build/tests for both runtime roles.
- [ ] 1.2 Define versioned REST/OpenAPI application/query contracts and verify generated/served OpenAPI schema in deterministic tests.
- [ ] 1.3 Add the MCP adapter boundary over shared application/query services with a contract test proving it does not bypass application semantics; do not implement travel-specific MCP tools beyond what existing application services support.

## 2. Central and worker persistence

- [ ] 2.1 Implement a Firestore-backed central store boundary using the emulator for deterministic tests; verify no worker component requires direct Firestore mutation credentials.
- [ ] 2.2 Implement worker-local SQLite cache/spool/outbox with crash-safe retry identity and verify restart/retry tests.
- [ ] 2.3 Implement Cloud Storage archive abstraction for compressed immutable batches with local/fake deterministic tests; verify archive metadata retains scan/source/content-hash provenance.

## 3. Worker protocol

- [ ] 3.1 Define authenticated worker lease/heartbeat/result protocol with stable worker/job/lease/batch identities and explicit failure semantics.
- [ ] 3.2 Implement idempotent result submission and verify repeated submission of the same accepted batch produces no duplicate logical ingestion.
- [ ] 3.3 Evaluate local-worker and Cloud Run Job authentication options, record the selected minimal mechanism and threat model, and verify secrets/credentials are excluded from git.

## 4. Google deployment

- [ ] 4.1 Add reproducible Cloud Run deployment configuration for the control plane with scale-to-zero defaults and no embedded credentials; verify configuration/build locally.
- [ ] 4.2 Add Firebase Hosting configuration for the static site and ordinary API integration; verify local Firebase hosting/emulator behavior where practical.
- [ ] 4.3 Add Cloud Storage/Firestore region/configuration and document the chosen region after checking service support.
- [ ] 4.4 Add a minimal Cloud Scheduler plan-trigger design/configuration only if needed for this change; do not add per-route schedules or a message broker.
- [ ] 4.5 Document budget alert setup and measurable cost drivers; do not claim alerts impose a hard spending cap.

## 5. End-to-end verification

- [ ] 5.1 Run a local/emulated end-to-end scenario where a worker leases synthetic work, stores a temporary outbox record, submits the same result batch twice, and the control plane retains one logical ingestion.
- [ ] 5.2 Verify the read/query REST endpoint works independently of MCP and the MCP adapter returns semantics from the same application service.
- [ ] 5.3 Run Maven tests, OpenSpec strict validation, relevant Graphify/Serena impact review, and update WORKLOG/MEMORY/iteration report before external review.
