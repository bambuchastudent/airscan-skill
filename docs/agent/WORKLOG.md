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

## 2026-10-18 — GitHub runner bootstrap and REST skeleton

**Actor:** ChatGPT implementation assistant; **OpenSpec:** bootstrap-agentic-foundation, tasks 1.1–1.4, 4.1–4.4 (IN PROGRESS).

**Observed verification:** GitHub Actions run https://github.com/bambuchastudent/airscan-skill/actions/runs/39113553968 for commit e9653c2: Java-and-contract PASS; OpenSpec PASS; Java Temurin 25.0.4.1, Maven 3.10.0; `openspec validate bootstrap-agentic-foundation --strict` PASS. `mvn verify` SKIPPED (pom.xml absent at that commit).

**Subsequent changes (not yet verified):** Added Java 25 Quarkus 3.27.2 Maven POM, CDI marker/test, REST/OpenAPI extension, bootstrap-only REST resource/test, and .gitignore. Relevant commit range 1def41f..6718f66. The new Maven build and Quarkus tests have NOT been observed passing; CI must be inspected before any task is checked off. No worker protocol, fare search, cloud deployment, or formal smoke checks implemented.

**Next:** inspect Actions runs for latest commit; fix any Maven failures; add explicit worker role only under bootstrap design; implement formal/tool smoke gates and complete review evidence.

## 2026-10-18 — independent audit remediation (incomplete)

Actor: ChatGPT implementation; OpenSpec: bootstrap-agentic-foundation only.
Baseline: d424d400c846f574a729d430f492b932396cd444.
Changed commits: 8eb6204..cdc34bb; Quarkus 3.40.1 LTS, Surefire Java 25 add-opens, HTTP REST/OpenAPI tests, separate worker Maven artifact, Maven 3.9.11 launcher, GitHub CI hardening, iteration/review draft.

Official source checks: Quarkus https://quarkus.io/releases/ recommends 3.40 LTS (3.40.1); https://quarkus.io/blog/quarkus-3-31-released/ identifies full Java 25 support starting 3.31; https://github.com/quarkusio/quarkus/issues/47769 explains JBoss Threads JVM option.

VERIFIED (earlier commit only): Actions run 39113553968, Java Temurin 25.0.4.1, Maven 3.10.0, OpenSpec 1.14.0 strict bootstrap validation.
NOT VERIFIED on new commits: `bash mvnw -B -ntp clean verify`, HTTP status, OpenAPI, worker Maven build, OpenSpec all changes, formatter/static, Graphify, Serena, Alloy, TLC, Dafny, secret scan.
FAILED (audit baseline): background IllegalAccessError/NoClassDefFoundError despite false-green Maven; no new run yet to demonstrate fix.
No task checkboxes checked. CI run URL for new commits: not yet available/observed.
Next: obtain latest run, correct errors, finish missing smoke gates and evidence. Status NEEDS_CHANGES.

## 2026-10-18 — static formatting gate

Actor: ChatGPT implementation; OpenSpec bootstrap-agentic-foundation tasks 1.4 and 4.3 (IN PROGRESS).
Commits: f102968..abe388c. Added pinned Spotless Maven plugin, CI `bash mvnw -B -ntp spotless:check`, and documented bootstrap commands in docs/TOOLING.md.
Verification: NOT VERIFIED — no successful workflow job logs were retrieved for these commits; `java -version` locally showed Java 21 and Maven is unavailable, so no local Java 25 build was claimed. GitHub workflow status cannot be inferred from code changes. Next: inspect CI and fix failures; formal/agent gates remain pending.

## 2026-10-18 — follow-up implementation: formatting and worker execution

Actor: ChatGPT implementation; change bootstrap-agentic-foundation only. Commits e31fbfa..e510254. Reformatted Java source and tests toward Google Java Format conventions; added CI execution of independent worker `main` and a tracked-runtime-artifact/credential filename gate. These are code changes, not successful verification evidence.

VERIFIED: local environment `java -version` returned OpenJDK 21.0.12.1; no Maven executable; external Maven Central inaccessible from this environment. NOT VERIFIED: actual `bash mvnw clean verify` on Java 25, Spotless check, worker entrypoint in CI, security gate, OpenSpec all changes. The GitHub connector exposed no general workflow-run listing and public workflow URL could not be fetched; no current CI run ID or logs were obtained. Graphify/Serena/Alloy/TLC/Dafny remain NOT VERIFIED. Next: obtain CI run evidence and address all remaining bootstrap gates.

## 2026-10-18 — priority remediation: formatter, wrapper, Quarkus

Commits 9607aad..1a59b14. Changed Spotless 2.43.0 -> 2.46.1 and google-java-format 1.25.2 -> 1.28.0; replaced quarkus-junit5 with quarkus-junit; added Surefire late `@{argLine}` with empty fallback and JBoss LogManager system property. Wrapper script now derives Maven version from properties and Git tree mode 100755 was committed at 9f18dd8. CI requires executable mode and invokes `./mvnw`. Java lang.invoke was deliberately not opened without evidence.

VERIFIED: GitHub create_tree/create_commit/update_ref succeeded for mode 100755. NOT VERIFIED: `./mvnw -version`, `./mvnw spotless:check`, Java 25 clean verify, REST/OpenAPI/worker and log cleanliness. No final SHA CI run/log available. Remaining blocker: custom launcher is not stock Apache Maven Wrapper. Do not mark OpenSpec tasks complete.

## 2026-10-18 — fail-closed test report gate

Commit d2bce65196b39fb7280ed1d7ebd1ef84fcacc2de. GitHub Actions now parses Surefire XML after `./mvnw clean verify` and rejects absent, skipped or failing tests; requires at least 3 Quarkus tests and 1 worker test. Background Quarkus/LogManager error signature check was extended. VERIFIED: CI workflow source changed. NOT VERIFIED: CI execution and runtime results; no run ID/log observed. Maven Wrapper is still custom and formal/semantic gates remain pending. No OpenSpec task checkbox updated.

## 2026-10-18 — Apache Maven Wrapper replacement

Commits 7294a66, 7218a8b, 7b31ad7, cef5c4d. Replaced custom `mvnw` with the upstream Apache Maven Wrapper shell script and added upstream `mvnw.cmd`, sourced from apache/maven-wrapper master at implementation time. Git tree explicitly records mode 100755 for `mvnw`. Maven 3.9.11 distribution remains configured in `.mvn/wrapper/maven-wrapper.properties`.

VERIFIED: GitHub file writes and Git tree mode change succeeded. NOT VERIFIED: runtime `./mvnw -version`, Spotless, Maven/Quarkus/worker tests or GitHub Actions logs. Do not mark bootstrap ready.

## 2026-10-18 — CI execution evidence investigation

Added an explicit CI step for wrapper distribution metadata and `./mvnw -version` at ddd3cb99a9901be8a39be234af29b26ad6fc6eb7. The GitHub connector `fetch_commit_workflow_runs` returned zero runs for 3f840c8, but this endpoint only returns pull-request-triggered runs; therefore this does **not** establish that push CI did not run. GitHub Actions public page could not be fetched in this environment. Local runner has OpenJDK 21.0.12.1, no `mvn`, and cannot resolve github.com; it is unsuitable for canonical Java 25 validation. Consequently no green CI run, runtime PASS, or final SHA evidence is claimed. Remaining work: obtain actual Actions push-run logs through a suitable GitHub Actions interface; remediate failures; only then proceed to formal/semantic evidence.

## 2026-10-18 — PR #1 reproducible CI and formal toolchain evidence

Branch: `ci/bootstrap-audit-gates` (not merged); PR: https://github.com/bambuchastudent/airscan-skill/pull/1.

VERIFIED: GitHub Actions run https://github.com/bambuchastudent/airscan-skill/actions/runs/39121502784 at commit `1360c01134e9ed4c91adaa8d41c0607fbe36e480` completed with five successful jobs: `java-and-contract`, `openspec`, `alloy-bootstrap`, `tlc-bootstrap`, `dafny-bootstrap`. The Java job includes Maven Wrapper, Spotless, Checkstyle, Java 25/Quarkus HTTP and worker tests. Formal smokes are deliberately tiny toolchain examples, not proofs of AirScan product invariants. Dafny verifies a pure bootstrap decision function and builds Java via `dafny build --target:java`.

NOT VERIFIED: Graphify installed version, graph creation, impact lookup; Serena activation, symbol references and status lookup. Neither tool is exposed through the available connected execution interfaces. These checks require actual external tool sessions with recorded output, not inferred results. Also pending: explicit runtime smoke of generated Dafny Java, and final evidence snapshot tied to final PR SHA after all gates are resolved. Status: NEEDS_CHANGES; not ready for external review.

## 2026-10-18 — Kotlin cache, Dafny runtime, Serena integration (PR #1)

- `14a85c0de5ff1444247aabbde2222c96dbbf4eb4`: Kotlin `.main.kts` Graphify shared-cache coordinator replaces Python orchestration. CI [39123970145](https://github.com/bambuchastudent/airscan-skill/actions/runs/39123970145) PASS (7 jobs).
- `d2bc2b9c8293ca616f2a0f9a12f50b17d14bff56`: added Dafny Java runtime execution. CI [39124094544](https://github.com/bambuchastudent/airscan-skill/actions/runs/39124094544) PASS (7 jobs); Dafny logs show 2 verified, 0 errors, and `dafny run --target:java` succeeded.
- `58b6d2843578720aca0183bc013819dadb86df34`: added Serena MCP Java semantic gate. CI [39124415407](https://github.com/bambuchastudent/airscan-skill/actions/runs/39124415407) FAILED due to conflicting `mcp<2` constraint (Serena requires MCP 2.2.0).
- `4ce5fe781d2f378c142df85d3f52b91b6e0afd37`: removed MCP constraint. CI [39124470305](https://github.com/bambuchastudent/airscan-skill/actions/runs/39124470305) has 7 PASS and Serena FAIL because unsupported `--headless` option prevented MCP startup.
- `8a6a27200829eabca539133631325200b687a018`: removed unsupported Serena flag; new CI evidence pending. No semantic Serena PASS claimed.
- `07b888417504f2b6c1906da346143a523fc6e86f`: reconciled OpenSpec task checkboxes with verified CI and explicit pending tasks.

**Current gate:** NEEDS_CHANGES. Do not merge PR #1 until Serena and final SHA evidence are green, then update memory, iteration and review report.

## 2026-10-18 — Serena passed and CI cache enabled

Commit `733714ab675ac744bf5ec1fb46c76dffb0bbd44f`, run https://github.com/bambuchastudent/airscan-skill/actions/runs/39124935556: all 8 jobs PASS, including real Serena MCP symbol/reference lookup. Maven, npm/pip, Kotlin, TLC, Alloy and Dafny caches enabled; concurrent obsolete runs cancelled. Remaining: documentation consistency, final SHA CI and independent review. Serena installed from unpinned upstream Git main; Graphify cache rebuilds all nodes on source changes. No merge.

## 2026-10-18 — external independent review accepted

User provided an independent auditor PASS report for reviewed HEAD `b0c7a82ab5909ac485fa21db0b03aeb45e8751c3`, with zero BLOCKER/MAJOR findings, one MINOR Node.js 20 deprecation, and INFO Graphify/Serena cache limitations. Attributed summary saved in `docs/agent/reviews/independent-bootstrap-review-2026-10-18.md`; OpenSpec 4.5 checked. Implementer did not conduct independent audit. Subsequent documentation commits need fresh CI; PR not merged.

## 2026-10-18 — second-review Graphify and documentation remediation (pending CI)

Second independent review found unsupported `graphifyy==0.1.14` and contradictory current-state documents. Graphify upgraded to exact `graphifyy==0.9.92` (PyPI release 2026-10-16), install `python -m pip install 'graphifyy==0.9.92'`. Invocation: `kotlin scripts/graphify-shared-cache.main.kts --root . --cache-dir /tmp/airscan-shared-graph --output graphify-evidence`; Python extraction `from graphify.extract import extract; extract(files, root=root, parallel=False)`. Symbol query checks `BootstrapResource` and `BootstrapMarker`; relationship query requires an extracted `calls` edge from `BootstrapResource.status` to `BootstrapMarker.status` with evidence at `graphify-evidence/relationship.json`. CI asserts first cache_hit=false, second=true, changed Java source=false. This is a full-graph rebuild, not per-file incremental. **These are configured assertions, not yet observed PASS.** Tooling/MEMORY/tasks rewritten to show one current state. Baseline CI [39126204822](https://github.com/bambuchastudent/airscan-skill/actions/runs/39126204822) was 8/8 PASS before upgrade. New final CI and independent re-review still required. Supply-chain hardening of mutable action tags and formal-tool download checksums deferred.

## 2026-10-18 — Graphify remediation verified, final technical review and evidence closeout

- **Reviewed implementation HEAD:** `ca8daf41ba7efe62b6287f19d9f4f2ca0bdfa0dd`; baseline `d424d400c846f574a729d430f492b932396cd444`.
- **CI:** [39126546579](https://github.com/bambuchastudent/airscan-skill/actions/runs/39126546579), **8/8 PASS**: `java-and-contract`, `openspec`, `tlc-bootstrap`, `alloy-bootstrap`, `dafny-bootstrap`, `java-symbol-smoke`, `graphify-java`, `serena-java`.
- **Graphify VERIFIED:** exact pin `graphifyy==0.9.92`; install `python -m pip install 'graphifyy==0.9.92'`; graph command `kotlin scripts/graphify-shared-cache.main.kts --root . --cache-dir /tmp/airscan-shared-graph --output graphify-evidence`. Java symbol query located `BootstrapResource` and `BootstrapMarker`; graph 27 nodes/47 edges. Relationship query found `BootstrapResource.status() --calls--> BootstrapMarker.status()`, source `BootstrapResource.java:L17`, AST origin, INFERRED confidence 0.85. Cold `cache_hit=false`, warm `cache_hit=true`, changed Java source `cache_hit=false`. Full graph rebuild on source change is accepted; no per-file incremental claim.
- **Serena VERIFIED:** MCP semantic symbol and reference queries, upstream immutable commit `d9e4c796fae0207ba6848d1df4b331122a5567b5`.
- **Other accepted gates:** Java 25/Quarkus 3.40.1/Maven 3.9.11, HTTP/OpenAPI, worker boundary, Spotless/Checkstyle, OpenSpec 1.14.0 strict checks (all four active changes), Alloy 6.2.0, TLC 1.8.0, Dafny 4.11.0 Java build/run, scope and tracked-artifact gates.
- **Final independent reviewer position:** implementation technically acceptable and architecture/roadmap conformant; `NEEDS_CHANGES` was solely for missing final evidence metadata and canonical review. Canonical review now recorded in `docs/reviews/bootstrap-agentic-foundation/2026-10-18T-final-review.md`; current-state documents and iteration summary reconciled.
- **Remaining gate:** green CI on the documentation-only closeout HEAD, then metadata-only independent review. Do not merge or begin downstream OpenSpec work. Follow-up hardening (GitHub action SHA pins, formal-tool checksums) is nonblocking.


## 2026-10-18 — bootstrap final PASS, merge, and OpenSpec archive cleanup

**Actor:** external architect/reviewer + post-merge cleanup; **OpenSpec:** `bootstrap-agentic-foundation` completion lifecycle.

- Final metadata-only independent review returned **PASS / READY** for closeout HEAD `351c6835e003a3e5863d3632d77dd92566a25b45`.
- Closeout GitHub Actions run `39127216895`: **8/8 PASS**.
- PR #1 merged into `develop` as `a2e527c153f403ca47aabcec70a9460f75499909`.
- Bootstrap implementation remains the independently accepted baseline from `ca8daf41ba7efe62b6287f19d9f4f2ca0bdfa0dd` / CI `39126546579`.
- Post-merge cleanup updates current-state documentation and archives the completed OpenSpec change under `openspec/changes/archive/2026-10-18-bootstrap-agentic-foundation/`.
- No runtime/product behavior is changed by this cleanup.
- Next active change: `add-distributed-control-plane`. Do not begin `add-route-topology` or `add-durable-scan-retention` until the control-plane change passes its own external review.

## 2026-10-18T14:46Z — distributed-control-plane Iteration 1 runtime/API/MCP boundary

**Actor:** ChatGPT implementation orchestrator (external independent review still required).
**Change:** `add-distributed-control-plane`; targets 1.1, 1.2, 1.3, 5.2; 3.3 design portion and 5.3 iteration verification only.
**Base:** `56c35812360d392a436b3e8f2df51bcd296948d5` (`develop`).
**Reviewed code HEAD:** `0dc8b112f62593d49ee77242e62affa5fb155f25`.
**Branch/PR:** `feat/add-distributed-control-plane-iteration-1`, PR #3 (draft, not merged).
**Implementation commits:** `f9a42095b0089a20cb752a60cb5b0e2fd200dcfe`,
`9cfc4ee17e8b750ce832c620f4f3f430373b038a`,
`0dc8b112f62593d49ee77242e62affa5fb155f25`.

**Intent/diff:** Root Quarkus gets `CapabilitiesService`/`CapabilitiesSnapshot`,
`GET /api/v1/capabilities` with served OpenAPI, and an MCP-shaped in-process
tool-dispatch adapter over the identical application service. Real sentinel-mock
parity contract tests verify the HTTP and MCP paths invoke the service. Kept
plain-Java worker build/runtime independent, with no shared Maven module.
Added `quarkus-rest-jackson` and test-only `quarkus-junit5-mockito`.
Extended Graphify/Serena checks to inspect both new call paths; wrote Option A
scoped local worker token vs Option B Cloud Run Job OIDC design and threat model.
No worker protocol or storage was implemented.

**Pre-change verification (same base commit):** GitHub Actions
[39128494433](https://github.com/bambuchastudent/airscan-skill/actions/runs/39128494433)
8/8 PASS, including strict archived/active OpenSpec, Graphify 0.9.92 and pinned Serena.
Graphify reported the existing `BootstrapResource.status -> BootstrapMarker.status`
call; Serena returned the corresponding semantic reference.

**Candidate verification at code HEAD `0dc8b112...`:** GitHub Actions
[39137373107](https://github.com/bambuchastudent/airscan-skill/actions/runs/39137373107)
**8/8 PASS**, observed completed, exact checks:

- `./mvnw -B -ntp spotless:check` — **PASS**.
- `./mvnw -B -ntp checkstyle:check` — **PASS**.
- `./mvnw -B -ntp clean verify` — **PASS**; 7 Quarkus/JUnit tests, 0 failures/errors/skips, including real HTTP/OpenAPI and mocked REST/MCP parity.
- `./mvnw -B -ntp -f worker/pom.xml clean verify` — **PASS**; 1 worker test, 0 failures/errors/skips; separate worker entrypoint execution PASS.
- `openspec validate --archived --strict` — **PASS**.
- `openspec validate --changes --strict` — **PASS**.
- `kotlin scripts/graphify-shared-cache.main.kts --root . --cache-dir /tmp/airscan-shared-graph --output graphify-evidence` (cold/warm/mutated source) — **PASS**; Graphify `graphifyy==0.9.92`, cache `false/true/false`. Extracted separate `calls` edges `CapabilitiesResource.current -> CapabilitiesService.current` (resource line 23) and `McpCapabilitiesAdapter.callTool -> CapabilitiesService.current` (adapter line 42). AST inference confidence 0.85; not a compiler-level proof.
- `kotlin scripts/serena-semantic-smoke.main.kts` — **PASS**; pinned Serena `d9e4c796fae0207ba6848d1df4b331122a5567b5`, semantic `find_symbol` and `find_referencing_symbols` confirming both application-service references.
- `python3 scripts/java-symbol-smoke.py --root . --output java-symbol-evidence` — **PASS**.
- `java -jar /tmp/alloy.jar exec -c '*' -o /tmp/alloy-results formal/bootstrap/BootstrapGate.als` — **PASS**; existing bounded bootstrap gate, Alloy 6.2.0.
- `java -cp /tmp/tla2tools.jar tlc2.TLC -config formal/bootstrap/BootstrapGate.cfg formal/bootstrap/BootstrapGate.tla` — **PASS**; existing finite bootstrap model, TLC 1.8.0.
- `dafny verify formal/bootstrap/BootstrapGate.dfy`, `dafny build --target:java ...`, `dafny run --target:java ...` — **PASS**; unchanged bootstrap proof gate, Dafny 4.11.0.
- Git tracked-secret/runtime-artifact shell check (`git ls-files | grep -Ei ...`) — **PASS** (no forbidden tracked files).
- `./mvnw -B -ntp -f worker/pom.xml dependency:tree` — **PASS**.

**Failure/remediation:** First candidate `f9a42095...` CI
[39137095805](https://github.com/bambuchastudent/airscan-skill/actions/runs/39137095805)
hit **FAILED** Spotless on a long OpenAPI test assertion and was superseded/cancelled;
`9cfc4ee1...` fixed the exact formatting finding and CI
[39137172791](https://github.com/bambuchastudent/airscan-skill/actions/runs/39137172791)
finished **8/8 PASS**. Then `0dc8b112...` added direct Graphify/Serena
impact verification and passed 8/8. These are separate runs, not cumulative claims.

**Versions observed:** Java Temurin 25.0.4.1; Maven Wrapper 3.9.11;
Quarkus 3.40.1; OpenSpec 1.14.0; Graphify 0.9.92; Serena pinned
`d9e4c796...`; Alloy 6.2.0; TLC 1.8.0; Dafny 4.11.0.
CI runners supply Java 25 and dependencies; local assistant container has Java 21
and no GitHub network/DNS, so local Maven/OpenSpec/Graphify/Serena command execution
was **NOT VERIFIED**. All PASS results above come from named GitHub Actions runs.

**Scope exclusions/limitations:** no Firestore, SQLite, Cloud Storage,
lease, heartbeat, ingestion, retry, worker credentials/auth implementation, deployed
MCP transport, hosting, scheduling, travel route/fare logic or new state-machine
invariant. Bootstrap formal-model PASS does not verify future distributed protocol.
No Google cloud authentication behavior tested. This entry documents code HEAD,
not a claim that this documentation closeout commit has already passed CI.

**Next:** review narrow Iteration 1 PR independently. Do not merge/mark
change complete/archive or begin Iteration 2 before that review.

## 2026-10-18T15:52Z — distributed-control-plane Iteration 2 persistence

**Actor:** ChatGPT implementation orchestrator; independent external review not yet performed.
**Change/tasks:** `add-distributed-control-plane` 2.1–2.3; 5.3 iteration evidence partial.
**Precondition:** user-attested independent Iteration 1 PASS; PR #3 was found unmerged, made ready and merged at `4e82c92b8c6cd1f4716207c4ac18c4ef189c404e`. Base `develop` verified at that SHA; dedicated branch `feat/add-distributed-control-plane-iteration-2` and draft PR #4.
**Verified code SHA:** `59012715f26c0957366b6d6b22f108632f480a84`.
**Verified CI:** [39142302544](https://github.com/bambuchastudent/airscan-skill/actions/runs/39142302544) — **9/9 PASS**.

**Changed at code SHA:** `CentralOperationalStore` port + `CentralRecordService` UUID synthetic record identity, Firestore SDK adapter and **mandatory emulator integration test**. Worker-only SQLite schema v1/WAL/FULL sync/5000ms busy timeout outbox, stable batch id and restart/retry/acceptance JUnit test. `ObservationArchive` port, SHA-256 and gzip content-addressed local immutable archive test adapter with source/batch/key/size/compression provenance. `pom.xml`, `worker/pom.xml`, `.gitignore` and `.github/workflows/bootstrap-runner.yml` updated. No worker HTTP/identity verifier, route/fare, cloud deployment or GCS production adapter.

**Exact observed commands/checks at verified code SHA `59012715...`:**

- `./mvnw -B -ntp spotless:check` — **PASS**.
- `./mvnw -B -ntp checkstyle:check` — **PASS**.
- `./mvnw -B -ntp clean verify` — **PASS**, 9 root JUnit tests, 0 failed/error/skipped; includes real REST/OpenAPI, MCP parity, central in-memory fake and archive immutability tests.
- `./mvnw -B -ntp -f worker/pom.xml clean verify` — **PASS**, 2 worker JUnit tests, 0 failed/error/skipped; close/reopen and accepted-state durability exercised.
- `./mvnw -B -ntp -f worker/pom.xml dependency:tree -DoutputFile=worker-dependencies.txt` plus grep for `google-cloud-firestore|google-cloud-storage|firebase-admin` — **PASS**, none present. Worker executable smoke PASS.
- `gcloud emulators firestore start --database-mode=firestore-native --host-port=127.0.0.1:8085` — **PASS**, actual process reached socket readiness in separate CI job; Cloud SDK pinned `550.0.0` with `cloud-firestore-emulator` component. `FIRESTORE_EMULATOR_HOST=127.0.0.1:8085`, synthetic `AIRSCAN_TEST_PROJECT_ID=airscan-emulator-it`; no Google credentials.
- `./mvnw -B -ntp -Pfirestore-emulator verify` — **PASS**, 9 root unit tests + exactly 1 **mandatory** Failsafe `FirestoreCentralOperationalStoreIT`, 0 failures/errors/skips. Real emulator write/read, close, new client/read PASSED. Failsafe report existence and no-skips asserted in CI.
- `openspec validate --archived --strict` — **PASS**; `openspec validate --changes --strict` — **PASS**.
- `kotlin scripts/graphify-shared-cache.main.kts --root . --cache-dir /tmp/airscan-shared-graph --output graphify-evidence` — **PASS** (Graphify `0.9.92`); baseline extracted REST/MCP shared-service calls. Explicit persistence symbol/edge checks added in documentation closeout; their execution requires final CI.
- `kotlin scripts/serena-semantic-smoke.main.kts` — **PASS**, immutable Serena `d9e4c796fae0207ba6848d1df4b331122a5567b5`; baseline semantic symbols/references. Explicit persistence reference checks added in closeout; verify on final CI.
- Java symbol smoke, existing Alloy 6.2.0, TLC 1.8.0, Dafny 4.11.0 gates — **PASS**; no new lease/snapshot model was introduced.
- Tracked secret/runtime file check (`git ls-files | grep -Ei ...`) — **PASS**.

**Failures/remediation (must not report as passes):**
- `8002bc39...` / CI [39141914181](https://github.com/bambuchastudent/airscan-skill/actions/runs/39141914181): Spotless **FAILED** (3 formatting findings) and superseded run cancelled.
- `5d98778d...` / CI [39141997607](https://github.com/bambuchastudent/airscan-skill/actions/runs/39141997607): superseded/cancelled.
- `a56514a5...` / CI [39142124509](https://github.com/bambuchastudent/airscan-skill/actions/runs/39142124509): main regression **PASS**, emulator **FAILED** because `FirestoreOptions.getService()` cached an already-closed client. Fixed by constructing fresh options before reopening.
- `59012715...` / CI `39142302544` **9/9 PASS**, emulator integration test genuinely executed and passed.

**Versions:** Java 25 Temurin, Maven Wrapper 3.9.11, Quarkus 3.40.1, Google Firestore Java client 3.49.0, SQLite JDBC 3.53.4.0, gcloud CLI 550.0.0, OpenSpec 1.14.0, Graphify 0.9.92 and pinned Serena, Alloy 6.2.0, TLC 1.8.0, Dafny 4.11.0.

**Limits:** Firestore emulator is in-memory and does not prove production transaction/index behavior; local archive is *not* Cloud Storage GCS SDK adapter. Worker SQLite only stores synthetic batches: no HTTP, lease, heartbeat or central ingestion. No cloud credentials were used. Local assistant container Java 21 cannot run Java 25 Maven suite or fetch GitHub due unavailable DNS; all PASS evidence is from linked GitHub CI.

**Current closeout:** this WORKLOG entry, durable MEMORY, marked OpenSpec 2.1–2.3 and iteration report are part of the documentation-only code review closeout; green CI for this **new** commit must be observed before independent review.
**Next:** independent external review of Iteration 2 only; do not merge PR #4 or start Iteration 3 without reviewer PASS.

## 2026-10-18T17:20Z — Iteration 2 archive atomic-publication MAJOR remediation

**Actor:** implementation orchestrator. **Change:** `add-distributed-control-plane`, task 2.3 (already checked, reviewed correction), not worker protocol. **External review input:** PR #4 at `d1bdaee60137d1fc367c1e573c845d92fc396711` accepted all persistence boundaries except **MAJOR** incomplete final archive object on a process/host crash during direct `CREATE_NEW` write.

**Implementation range:** `77501c7bb8d0e4b84046da34c59a6410f3efe682` (atomic staging), `8593ca7f6fd9a0efdd22990ee726ceb24576c5f6` (tests), `2551ac26030fd26ddef782075294013727881610` (imports), `c4f25ff57e7425febb725b5c04385c9c1476f812` (Spotless exact corrections).

**Files:** `src/main/java/dev/airscan/adapter/archive/LocalGzipObservationArchive.java`, `src/test/java/dev/airscan/adapter/archive/LocalGzipObservationArchiveTest.java`; design, WORKLOG, MEMORY and the existing Iteration 2 report updated in separate evidence-closeout commit.

**Publication:** unique same-directory `Files.createTempFile(..., ".tmp")`, complete gzip `FileChannel.write` loop + `force(true)`; atomic create-only `Files.createLink(destination, staging)` and unconditional best-effort-on-Java-execution staging cleanup via `finally`. Java documents that `Files.move(..., ATOMIC_MOVE)` can overwrite an existing destination depending on provider, so `createLink` is used instead: never replace; fail closed when hard linking unsupported. Existing destination is decompressed and byte-compared with expected source, corrupt/truncated file fails without overwrite; simultaneous writers of identical payload return identical reference. Power-loss directory durability is not claimed, but a visible final entry never represents an incomplete staged gzip. Unrecoverable abrupt kill may orphan staging only, never authoritative final object.

**Test changes:** existing archive test additionally checks SHA-256 and no staging files; new corrupt-existing gzip test verifies throw, byte-for-byte no overwrite and cleanup on failed publication; new four-concurrent-writer test verifies same immutable reference, correct bytes and no staging files. Three archive tests total.

**CI execution at implementation SHA `c4f25ff57e7425febb725b5c04385c9c1476f812`:**
[39148837864](https://github.com/bambuchastudent/airscan-skill/actions/runs/39148837864)
**9/9 PASS** (all jobs terminal).
- `./mvnw -B -ntp spotless:check` — PASS.
- `./mvnw -B -ntp checkstyle:check` — PASS.
- `./mvnw -B -ntp clean verify` — PASS; 11 root tests, 3 archive-specific, 0 failures/errors/skips; real Iteration 1 REST/OpenAPI/MCP regression PASS.
- `./mvnw -B -ntp -f worker/pom.xml clean verify` — PASS; 2 worker tests, worker process smoke PASS, no SQLite change.
- `./mvnw -B -ntp -f worker/pom.xml dependency:tree -DoutputFile=worker-dependencies.txt` plus no Google Firestore/Storage/Firebase SDK grep — PASS.
- `gcloud emulators firestore start --database-mode=firestore-native --host-port=127.0.0.1:8085` — PASS; gcloud SDK 550.0.0 / emulator 1.20.2.
- `./mvnw -B -ntp -Pfirestore-emulator verify` — PASS, mandatory 1 emulator Failsafe IT and 11 root regression tests, 0 failures/errors/skips, no credentials.
- `openspec validate --archived --strict` — PASS.
- `openspec validate --changes --strict` — PASS.
- `kotlin scripts/graphify-shared-cache.main.kts --root . --cache-dir /tmp/airscan-shared-graph --output graphify-evidence` — PASS, Graphify 0.9.92 persistence symbols/relationships preserved.
- `kotlin scripts/serena-semantic-smoke.main.kts` — PASS, pinned Serena `d9e4c796fae0207ba6848d1df4b331122a5567b5`, persistence symbol/reference checks.
- `python3 scripts/java-symbol-smoke.py --root . --output java-symbol-evidence`, existing Alloy 6.2.0, TLC 1.8.0 and Dafny 4.11.0 gates — PASS, no new state-machine invariant.
- Tracked-secret/runtime-artifact scan — PASS.

**Failed/superseded evidence:** PR-branch CI `39148684334` (`77501c7b...`) and `39148736465` (`8593ca7f...`) cancelled by newer pushes; `39148762379` (`2551ac26...`) failed Java Spotless (2 formatting findings; remaining jobs superseded/cancelled). `c4f25ff5...` fixed the exact findings and passed CI `39148837864`. Do not conflate cancelled with PASS.

**Explicit nonchanges/followups:** No `IN_FLIGHT` outbox state, strict FIFO queue ID, worker HTTP ingestion, auth, Firestore schema or deployment. SQLite Java 25 native-access warning remains nonblocking; monitor instead of changing JVM flags. Future externally supplied archive metadata is untrusted and must be corroborated against authoritative control-plane records. Final documentation-only closeout requires new CI; no external final PASS/merge/Iteration 3 claimed.
## 2026-10-22T23:50Z — add-distributed-control-plane local authenticated POC

**Actor:** ChatGPT implementation orchestrator. **Change:** `add-distributed-control-plane`; tasks 3.1, 3.2, 3.3, 5.1, 5.2 and iteration-level 5.3. **Base:** reviewed Iteration 2 PR #4 HEAD `d0e6fa642b9873cbe9f5520b12e1e514a96f12f4`, final CI `39149174508` 9/9 PASS. The user supplied an independent **PASS**; recorded the reviewer verdict on PR #4 and merged it at `7b693d97d15c0f8642ca2c08ce6ece36bfaa4d18`. Created dedicated `feat/add-distributed-control-plane-local-poc` and draft PR #5. No deployment, topology or travel behavior.

**Verified POC implementation/spec SHA:** `7c3432846bee57b1cb29277519001c03f95475e5`. **Verified CI:** [39873870605](https://github.com/bambuchastudent/airscan-skill/actions/runs/39873870605), **9/9 PASS**. **Documentation closeout** is a subsequent commit and must receive its own green CI before external POC review.

**Implementation and responsibility:** `WorkerProtocolService` + `WorkerProtocolStore` pure application boundary; server-assigned `jobId` and `leaseId`, typed `WorkerPrincipal` from `WorkerIdentityVerifier`, worker outbox-stable `batchId`; `LocalTokenWorkerIdentityVerifier` backed by ephemeral randomly generated CI token file; `FirestoreWorkerProtocolStore` transactional state; `ObservationArchive` immutable local gzip; `WorkerProtocolResource` versioned REST; `SyntheticAcceptedResource` narrow read. `WorkerPocClient` uses JDK HTTP in the independent worker artifact; POC IT compiles worker source as root **test source only** to use real HTTP client and SQLite outbox without crossing production artifact boundaries. No Google credentials. No worker Firestore SDK.

**Verified CI commands/results on SHA `7c343284...`:**
- `./mvnw -B -ntp spotless:check` — PASS (pinned Spotless 2.46.1 / google-java-format 1.28.0).
- `./mvnw -B -ntp checkstyle:check` — PASS.
- `./mvnw -B -ntp clean verify` — PASS; **11 root Surefire tests**, 0 failure/error/skip, including unchanged REST/OpenAPI and MCP parity.
- `./mvnw -B -ntp -f worker/pom.xml clean verify` — PASS; **2 worker Surefire tests**, 0 failure/error/skip; standalone bootstrap entrypoint PASS.
- `./mvnw -B -ntp -f worker/pom.xml dependency:tree -DoutputFile=worker-dependencies.txt` and forbidden `google-cloud-firestore|google-cloud-storage|firebase-admin` grep — PASS, no Google SDK in worker.
- `gcloud emulators firestore start --database-mode=firestore-native --host-port=127.0.0.1:8085` — PASS with gcloud 550.0.0 / Firestore emulator 1.20.2, local synthetic project only. Ephemeral 192-bit worker tokens generated in a file with `umask 077` (environment carries only file path).
- `./mvnw -B -ntp -Pfirestore-emulator verify` — PASS; **5 mandatory emulator Failsafe tests** (4 `WorkerPocIT` + 1 prior central store), 0 failures/errors/skips. CI asserts count >=5 and absence of skips; includes real HTTP worker client, lost acknowledgement, SQLite reopen, same `batchId` resend, authoritative Firestore acceptance, immutable archive and REST query.
- Firestore emulator concurrent lease test: two worker principals race on a single job, exactly one HTTP 200/one 409. Expiry recovery uses controlled Instants, no sleeps. Wrong principal heartbeat and result 403, missing/wrong token 401. Two concurrent identical result HTTP submissions converge on one immutable accepted receipt; changed-content same ID gets 409. Failure `SOURCE_TRANSPORT`/`WORKER_INTERNAL` returns READY without successful publication.
- `openspec validate --archived --strict` — PASS; `openspec validate --changes --strict` — PASS, OpenSpec 1.14.0.
- `kotlin scripts/graphify-shared-cache.main.kts --root . --cache-dir /tmp/airscan-shared-graph --output graphify-evidence` — PASS; pinned Graphify 0.9.92 Java AST confirms baseline REST/MCP, persistence and new protocol/worker/client relationships.
- `kotlin scripts/serena-semantic-smoke.main.kts` — PASS; pinned Serena `d9e4c796fae0207ba6848d1df4b331122a5567b5` verifies new `WorkerProtocolService/submit` reference from `WorkerProtocolResource` and persistence symbols.
- Java symbol smoke, unchanged Alloy 6.2.0, TLC 1.8.0, Dafny 4.11.0 gates — PASS; **bootstrap formal gates only**, not protocol proof. No new TLA+ model, justified in OpenSpec design. Tracked secret/runtime artifact grep — PASS.

**Failure/supersession history:** initial candidate `fa1be08...` CI `39872059536` FAILED Spotless (new Java formatting). Superseded commits/runs `39872292335`, `39872389156`, `39872496925`, `39872703391`, `39872837655` were cancelled (not PASS). Candidate `3d91a1b...` CI `39872881893` FAILED due CI worker test-results assertion inadvertently requiring 5 worker tests when only 2 expected; the separate emulator test job itself passed. Corrected assertions to require 2 worker tests and 5 emulator IT, committed as `32abb1be...`. Latest POC code/spec `7c34328...` CI `39873870605` **9/9 PASS**.

**Constraints / risks:** all worker mutating endpoints fail closed without configured emulator+token fixture, no production ADC/Cloud Run, token rotation/OIDC or public endpoint auth hardening. The accepted synthetic read endpoint is intentionally unauthenticated within isolated emulator and must not be internet-exposed without review. Archive and Firestore cannot atomically commit together; an interrupted pre-commit attempt can leave an orphan immutable archive but not a duplicate logical central acceptance. An external requester-supplied archive reference is not authoritative. Runtime correctness contains no LLM. SQLite remains PENDING→ACCEPTED (no IN_FLIGHT, no sequence). Java 25 sqlite-jdbc native-load warning remains a nonblocking follow-up.

**Local reproduction:** `docs/LOCAL_POC.md`; full handoff `docs/agent/iterations/2026-10-22T2350Z-add-distributed-control-plane-poc.md`. **Next:** independent review of draft PR #5 only; do not merge or begin cloud deployment/route topology automatically.

