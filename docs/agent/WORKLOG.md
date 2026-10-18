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
