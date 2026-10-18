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
