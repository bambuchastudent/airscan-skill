# add-distributed-control-plane — Iteration 1 handoff

**UTC:** 2026-10-18T14:46Z
**Actor:** implementation orchestrator; independent external review pending.
**Base:** `56c35812360d392a436b3e8f2df51bcd296948d5` (develop)
**Code HEAD:** `0dc8b112f62593d49ee77242e62affa5fb155f25`
**PR:** https://github.com/bambuchastudent/airscan-skill/pull/3 (draft)
**Verified CI:** https://github.com/bambuchastudent/airscan-skill/actions/runs/39137373107 (**8/8 PASS**)

## Goal and starting context

Bootstrap `bootstrap-agentic-foundation` was independently accepted and archived;
its runtime/code/verification baseline remains untouched. Only Iteration 1 of
`openspec/changes/add-distributed-control-plane/` is in scope: 1.1–1.3 and
3.3 auth **design**, plus narrow 5.2 boundary conformance.

## Implementation and architecture

- No extra module: the existing Quarkus root is the control-plane deployment
  artifact; the independent `worker/` plain-Java process remains unchanged.
  The worker has no Quarkus or Firestore compile-time dependency.
- `dev.airscan.application.CapabilitiesService.current()` is the shared
  application operation. Its immutable `CapabilitiesSnapshot` is domain-neutral.
- REST `GET /api/v1/capabilities` is a versioned, real HTTP/JSON endpoint in the
  served OpenAPI schema; it does **not** call through the MCP adapter.
- MCP `McpCapabilitiesAdapter` implements MCP-shaped tool metadata and dispatch
  for `airscan.capabilities`; its call invokes the same service as REST.
  No HTTP/JSON-RPC MCP session/transport is claimed.
- An injected Mockito sentinel application result is returned unchanged through
  both adapters; verified method invocation count is 2.
- `pom.xml` adds JSON support and test-only Mockito support; no Firestore SDK,
  SQLite dependency or network auth has been introduced.
- `design.md` now separates `workerId`, `jobId`, `leaseId`, `batchId`
  and evaluates dev scoped token vs Cloud Run Job OIDC. Initial local/dev token
  is a design direction, not a live credential mechanism. Runtime application
  semantics must not change when a future OIDC verifier is added.

## Evidence

At `0dc8b112...`, CI `39137373107` completed **8/8 PASS**:

- `./mvnw -B -ntp spotless:check` and `./mvnw -B -ntp checkstyle:check` — PASS.
- `./mvnw -B -ntp clean verify` — PASS, 7 Quarkus tests, 0 failing/skipped;
  real HTTP JSON/OpenAPI and Mockito-backed REST/MCP parity covered.
- `./mvnw -B -ntp -f worker/pom.xml clean verify` — PASS, 1 worker test; standalone
  Java worker bootstrap entrypoint executed successfully.
- `openspec validate --archived --strict` — PASS.
- `openspec validate --changes --strict` — PASS.
- `kotlin scripts/graphify-shared-cache.main.kts --root . --cache-dir /tmp/airscan-shared-graph --output graphify-evidence`
  — PASS, Graphify 0.9.92; newly checked `calls` edges REST -> application
  (`CapabilitiesResource.java:L23`) and MCP -> application
  (`McpCapabilitiesAdapter.java:L42`); both AST inferred at confidence 0.85.
  Existing bootstrap call preserved. Cold/warm/changed cache false/true/false.
- `kotlin scripts/serena-semantic-smoke.main.kts` — PASS, pinned Serena MCP
  `find_symbol`/`find_referencing_symbols` verified both application references.
- Java symbols and unchanged bootstrap Alloy/TLC/Dafny gates — PASS.
  These **do not** formally verify future leases, idempotency or snapshots.
- Tracked secret/runtime-artifact exclusion and worker dependency tree — PASS.

First draft candidate `f9a42095...` failed on Spotless only and was cancelled
as superseded. Formatting fix `9cfc4ee1...` passed CI `39137172791` 8/8,
then new explicit Graphify/Serena assertions passed in `39137373107`.
Exact commands and source-to-result evidence: `docs/agent/WORKLOG.md`.

## Files changed

- `pom.xml`
- `src/main/java/dev/airscan/application/CapabilitiesSnapshot.java`
- `src/main/java/dev/airscan/application/CapabilitiesService.java`
- `src/main/java/dev/airscan/api/v1/CapabilitiesResource.java`
- `src/main/java/dev/airscan/mcp/McpCapabilitiesAdapter.java`
- `src/test/java/dev/airscan/api/v1/CapabilitiesResourceTest.java`
- `src/test/java/dev/airscan/mcp/McpCapabilitiesAdapterTest.java`
- `scripts/graphify-shared-cache.main.kts`
- `scripts/serena-semantic-smoke.main.kts`
- `openspec/changes/add-distributed-control-plane/design.md`, `tasks.md`
- `docs/agent/WORKLOG.md`, `MEMORY.md`, this report.

## Decisions and scope limits

OpenSpec **completed for Iteration 1 boundaries:** 1.1, 1.2, 1.3, 5.2.
**Partial/open:** 3.3 worker auth design only; 5.3 current iteration CI/evidence
only, with additional change-level verification still needed.
All persistence, Cloud Run/Firebase config, worker lease/heartbeat/result/failure,
idempotent ingestion, SQLite outbox, archives, provider/topology/fare/ranking,
dashboard and ground routing remain **not implemented**.

## Risks and follow-up

- MCP transport/session handling is absent by design; test demonstrates shared
  service dispatch, not MCP protocol compatibility.
- The synthetic capabilities query does not prove future snapshot/fare parity.
- The local token/OIDC selection requires secure verifier implementation and
  Cloud Run IAM/ingress compatibility validation, both deferred. No live Google
  credentials were tested or stored.
- Graphify edges are inferred AST edges; Serena references plus runtime sentinel
  test provide independent corroboration.
- CI is the authoritative verification environment; this assistant runtime
  has Java 21 and cannot DNS-resolve GitHub for local checkout/build.
- This report cites the implementation code SHA. Documentation-only closeout
  HEAD requires a fresh CI run; do not claim it has passed preemptively.

**Status:** READY_FOR_EXTERNAL_REVIEW after documentation-closeout CI,
not independently accepted. **Next:** external ChatGPT architect's
independent review of this Iteration 1 diff/evidence only. Do not start
Iteration 2 before that review.
