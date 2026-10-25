# Provider Capability Research Gate — research execution plan

**Status:** PLANNED / NOT EXECUTED  
**Date opened:** 2026-10-25 (Europe/Madrid)  
**Owner / executor:** ChatGPT research agent in the current conversation  
**Independent reviewer:** another agent / independent review chat; executor SHALL NOT self-certify PASS  
**Baseline:** `develop@301d979f66b765651303337876fe8998a2b2ca99`  
**Related change:** `openspec/changes/add-route-topology`, especially RT-3 / task 0.11  
**Concurrent work:** RT-1 draft PR #8, which has outstanding Aena source/legal and parser-validation issues. Do not modify or represent PR #8 as accepted.

## Outcome

Identify the simplest legally usable, low-request-count, maintainable set of provider sources for **positive dated availability observations of real VLC direct flights**, independent of the already-planned airport-wide Aena topology catalog. Decide whether a multi-airline data source can replace multiple airline-specific adapters. Supply a defensible, measured implementation recommendation for RT-3.

This gate is **research and feasibility**, not RT-3 implementation and not a complete availability solution. It neither proves all-carrier schedule coverage nor authorizes a crawler.

## Non-negotiable architecture and scope

- Airport-wide VLC topology comes first (Aena is the initial candidate), never an airline-only route list represented as the full airport network.
- Date availability, route existence, fares, and actual seats-for-sale are distinct observation types.
- Positive dated evidence may be confirmed; missing observations remain UNKNOWN/UNCOVERED unless complete-coverage and negative-evidence semantics have been independently justified.
- Follow the existing authenticated worker / SQLite outbox / HTTP / control-plane idempotency pipeline if a selected source is implemented later.
- Research commercial and open source source paths, but **do not implement adapters, alter runtime, add scraping automation, or use production credentials in this gate**.
- Never evade CAPTCHA, login/access barriers, rate limits, bot defenses, or contractual restrictions; robots allowance is not permission to scrape.
- Use Java-first thinking for future production code. Inspect Python/TypeScript projects for protocols and capabilities without making either a required production runtime.
- Do not change or check off RT-1 tasks while PR #8 remains separately reviewed.

## Scope: source inventory

### Tier A — all five initial VLC-focused airline candidates

Ryanair, Vueling, easyJet, Wizz Air, Volotea.

### Tier B — secondary airline candidates

Norwegian, Transavia, Eurowings, Pegasus, Jet2.

### Cross-carrier alternatives

At least two potential schedule/date-data aggregator categories or named providers (e.g., OAG schedules, Aviation Edge, or appropriately licensed distribution APIs). Distinguish schedule datasets from saleable inventory/fare APIs.

## Execution phases

### P0 — Baselines and test cases

1. Verify actual repository/PR #8 state and inspect accepted `DatedAvailabilityProvider` contract where available.
2. Build a non-secret sample set from a timestamped, source-attributed VLC catalog snapshot: examples of carrier diversity, domestic/international, seasonal/unknown.
3. Choose fixed evaluation horizons: 30, 90 and 180 days ahead of the evaluation date; record dates and timezone explicitly.
4. Set up the evaluation template and evidence classification before source evaluation.

PR #8 is not accepted as a complete Aena snapshot. Until validated, route-level percentages are `NOT VERIFIED`, not invented.

### P1 — Official access and OSS triage (desk research)

For each Tier A and Tier B airline, examine:

- Official public/partner/distribution APIs, portal/docs and access terms;
- capability for airline/network discovery, route schedule, flight-date search, multi-destination calendars and optional fare calendar;
- authentication, registration, commercial requirements, rate/quotas, cost, date horizon, batch size;
- public HTML or other permitted access when APIs are absent;
- at least one OSS candidate where a credible project exists: exact repository URL, owner, license, last push/release, target language, inspected methods, upstream dependency and current known issues;
- whether documented claims are backed by actual source code or only README prose.

Classify each path by evidence level:

- `DOCUMENTED_OFFICIAL`;
- `OSS_CODE_INSPECTED`;
- `LIVE_VERIFIED`;
- `PARTNER_ACCESS_REQUIRED`;
- `UNVERIFIED`;
- `ACCESS_DISALLOWED_OR_UNCLEAR`;
- `INOPERABLE`.

These are evidence labels, not a global pass/fail ranking.

### P2 — Minimal feasibility probes

For the small number of *permitted, promising* access paths (ideally 2–3, not all 10):

1. Run non-destructive, low-volume read-only probes when permitted.
2. Record exact time, command, source, status, credentials requirement (never the credential), source sample hash, coverage, rate limitation and parser requirements.
3. Include at least one real VLC route/date probe if access permits.
4. Demonstrate absent/unconfirmed data is not misrepresented as an empty global schedule.
5. Respect and record published rate limits; if access is partner-only or legally unclear, do **not** probe. Mark `NOT VERIFIED — ACCESS REQUIRED` instead.
6. Store sanitized minimal fixtures, if permission permits; keep secrets, personal data and full proprietary responses out of git.

Live probes do not become mandatory CI, and a blocked provider does not justify CAPTCHA or hidden API circumvention.

### P3 — Capabilities, coverage, and query-cost model

Produce an evidence-linked matrix with:

- provider/carrier and access variant;
- topology, dated flights, date range, multi-destination batch, fare calendar (future only);
- temporal range and query granularity;
- maximum destinations/date-window per call where verified;
- attribution/codeshare caveats;
- terms and data retention/caching rights;
- forecast queries for 30/90/180 days, explicitly `MEASURED`, `DOCUMENTED`, `ESTIMATED` or `UNKNOWN`;
- request count, estimated wall latency from measured calls (only if actually probed), money/authorization cost, operational fragility;
- mapped subset of canonical Aena VLC routes where a verified catalog is available.

Calculate request fan-out based on **documented** batch/window constraints and observed route counts. Do not guess an undocumented API quota, latency, route count or coverage percentage.

Evaluate a date source's negative-evidence completeness separately: unknown is the default.

### P4 — Smallest viable integration decision

Use hard rejection gates first:

- legal/contractual accessibility for intended personal/local usage;
- credible read-only interface;
- preservation of provenance/coverage;
- no anti-bot circumvention;
- fit with Java worker and current protocol without heavy persistent browser infrastructure.

Then compare surviving paths for coverage and request count, latency (where measured), operational complexity, cost and maintenance.

Preferred outcome: a licensed multi-airline calendar/schedule source if it is accessible, affordable and adequately covers VLC; otherwise a small, capability-driven set of airline adapters. Do not choose a universal adapter based solely on an OSS README.

Provide:

1. primary recommended source strategy;
2. fallback or alternate where defensible;
3. `GO / CONDITIONAL GO / NO GO` for a bounded RT-3 pilot;
4. explicit unsupported carriers / gaps / next experiment;
5. minimal **proposed** provider capabilities metadata contract, without implementing it.

Example future descriptor dimensions (proposal, not final Java API):
`supportsTopology`, `supportsDateWindow`, `supportsMultiDestination`, `supportsFareCalendar`, `maxDateWindowDays`, `maxDestinationsPerRequest`, `authorizationMode`, `rateLimitPolicy`, `coverageLevel`, `observedAt`, `evidenceStatus`.

Keep topology SPI, date availability SPI, and future fare SPI distinct. The central planner should select based on capabilities, not carrier-specific if/else chains.

### P5 — Review package and independent gate

Create documentation-only research deliverables and send the final commit and evidence to an **independent external reviewer** who was not the research executor. Do not assert independent PASS yourself.

## Required deliverables

1. `docs/research/provider-capability-gate/PLAN.md` — this plan and gate definition.
2. `docs/research/provider-capability-gate/MATRIX.md` — ten carrier research rows/sections + at least two cross-carrier sources, with evidence links, source check dates, claim levels and unknowns.
3. `docs/research/provider-capability-gate/SOURCE_NOTES.md` — primary/official and OSS source notes, API method/code evidence, licensing/access constraints.
4. `docs/research/provider-capability-gate/PROBES.md` — reproducible safe smoke plan, exact command/result for each executed probe, sanitized evidence; explicit reasons for skipped probes.
5. `docs/research/provider-capability-gate/COVERAGE_AND_COST.md` — Aena mapping where verified; 30/90/180-day request-count and coverage estimates with assumptions.
6. `docs/research/provider-capability-gate/DECISION.md` — minimal selected strategy, rejected alternatives, risk register, pilot acceptance requirements and RT-3 handoff.
7. `docs/agent/WORKLOG.md` — append only evidence and PR/commit/CI links when execution actually happens.
8. `docs/agent/MEMORY.md` — current durable decision after independent acceptance, not before.
9. One iteration/research report under `docs/agent/iterations/`.

Source URLs are evidence, not permission. Each meaningful external claim needs a primary source or inspected upstream repository and access date.

## Measurable acceptance gate

A research deliverable can be `READY_FOR_EXTERNAL_REVIEW` only if:

- [ ] All five Tier A airlines have traceable official-access findings, capability assessment, status and constraints (even if result is `NOT AVAILABLE/UNKNOWN`).
- [ ] All five Tier B airlines have at least documented official-access assessment and verified OSS discovery/no-credible-project record.
- [ ] At least two cross-carrier approaches are compared for actual VLC schedule coverage, credentials/pricing and rights to local cache (unknowns explicit).
- [ ] For each credible OSS candidate: exact upstream URL, license, commit/release or activity date, method/code-level capability proof, current risks and untested claims.
- [ ] Source-aware matrix distinguishes **observed** flight dates from topology, saleable seats and fare quotes; no false negatives.
- [ ] For a representative 30/90/180-day VLC query: request volume and coverage are measured or explicitly modeled with formula, evidence provenance and `UNKNOWN` parameters (no fictitious measurements).
- [ ] At least two different source strategies have a read-only feasibility evaluation. If direct probing is inaccessible or prohibited, the non-execution is recorded with credible evidence; a successful live call is NOT fabricated.
- [ ] Aena-based route overlap and uncovered proportion is computed only if a validated catalog snapshot exists; otherwise dependency is explicitly `NOT VERIFIED`.
- [ ] The recommended integration is **minimal**: a limited number of independently justified adapters, or one licensed aggregator, without browser automation as a default.
- [ ] Each chosen primary/fallback has transparent limitations, legality, cost, SLA/fragility, caching/staleness and data-retention feasibility.
- [ ] The selected RT-3 pilot has clear `GO / CONDITIONAL GO / NO GO`, source choice, test cases, and stop conditions.
- [ ] The solution design reuses existing worker auth/lease/SQLite/idempotency and does not inject new Cloud SDK access into workers.
- [ ] Runtime code, deployed resources, existing RT-1 tasks and unrelated OpenSpec changes are not modified by this research gate.
- [ ] Evidence and review package exist, with factual `VERIFIED / NOT VERIFIED / FAILED` per evaluation; an independent reviewer makes the final `PASS / NEEDS_CHANGES` decision.

## Reviewer focus and decision semantics

The independent reviewer checks: primary-source accuracy; source-access/terms constraints; OSS claims against code and releases; date-coverage correctness; unsupported date negatives; request-count arithmetic and assumptions; security/access; minimal architecture; links/commit/time; and preservation of RT-1/RT-2 boundaries.

**Research PASS** means a defensible source-selection and test plan exists. It does **not** mean every airline is accessible or that RT-3 production readiness has been proven. A research PASS with `CONDITIONAL GO` may still require authorized API access or a separately reviewed live integration before RT-3.

Return: reviewed SHA, evidence references, per-gate VERIFIED/NOT VERIFIED/FAILED, findings with BLOCKER/MAJOR/MINOR, recommended strategy, outstanding dependencies, and final PASS/NEEDS_CHANGES.

## Stop conditions

Do not continue a particular source path when access is unauthorized, robots/terms prohibit the contemplated automation, anti-bot restrictions appear, official API permissions cannot be obtained, or the alleged live behavior cannot be reproduced. Record the limitation and compare alternatives rather than bypassing protections.

Do not make a claim of full VLC date coverage before matching the documented scope to a verified, timestamped airport-wide route catalog and proving the time-window queries.
