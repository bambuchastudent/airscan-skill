# Design

## Context

This change follows the accepted local `add-distributed-control-plane` POC. The hosted Google deployment portion of that change (tasks 4.1–4.5) remains open.

The next product-risk question is not cloud hosting but whether the existing worker/control-plane pipeline can acquire, normalize, persist, and query useful real travel data. Therefore a narrow **VLC Route/Date Discovery POC** is inserted before deployed POC work.

Fare crawling still depends on a stable route graph, and route topology, schedule availability, fare state, and ground routing remain separate concerns with different refresh and failure semantics.

## Goals / Non-Goals

Goals:

- canonical route graph and provenance;
- provider SPI for topology discovery;
- deterministic persistence/querying;
- offline parser tests from sanitized fixtures;
- exactly one initial verified real source path for the VLC POC;
- narrow source-confirmed date availability sufficient to answer a local VLC + date interval query;
- full ingestion through the accepted authenticated worker/control-plane path;
- deterministic query semantics that distinguish topology, availability, and source failure;
- distance-based neighborhood candidate generation after the VLC POC.

Non-goals for the VLC POC:

- fares or price ranking;
- cheapest-destination search;
- connections/itineraries;
- multiple providers;
- full default-origin coverage;
- route inactivity policy;
- ground public-transport routing;
- dashboard work;
- production Cloud Run/GCS/Firebase deployment.

A narrow date-availability observation is explicitly allowed for the VLC POC. General schedule search remains outside this change until separately specified.

## Milestone boundary: VLC Route/Date Discovery POC

The POC is complete when a developer can run AirScan locally, specify:

- origin = `VLC`;
- an inclusive `from` date;
- an inclusive `to` date;

and receive real direct destinations for which the selected source supplied positive availability evidence within that interval.

The query must not infer availability merely from route existence.

Conceptually:

```text
real source
   ↓
worker provider adapter
   ↓
normalized topology + dated availability observations
   ↓
authenticated worker submission
   ↓
control-plane validation/idempotent persistence
   ↓
local REST query
   ↓
VLC destinations with evidence dates + provenance
```

### Query semantics

The local POC query should be versioned and may take a form such as:

```text
GET /api/v1/routes/search?origin=VLC&from=YYYY-MM-DD&to=YYYY-MM-DD
```

The exact resource name may change during implementation, but the contract must:

- reject invalid IATA/date/range input;
- use inclusive date boundaries;
- return only direct routes;
- expose destination airport identity;
- expose carrier identity when the source supplies it, otherwise unknown;
- expose at least one supporting availability date within the requested interval;
- expose source/provenance and observation freshness;
- never invent a fare;
- distinguish no matching availability from provider/scan failure.

### Source selection

Research the source before implementing an adapter.

Prefer an official airport/operator or airline source with documented or observable stable access sufficient for VLC direct destinations and date availability. Do not present an undocumented private endpoint as stable.

If no single authoritative source supplies both topology and date availability, stop and document the evidence. A narrowly composed topology source + schedule source is allowed only after the boundary and provenance implications are reviewed.

Live access is evidence, not the deterministic test oracle. Commit sanitized fixtures and deterministic parser/normalization tests.

### Ports/adapters boundary

Domain code does not parse source payloads or call external APIs.

Keep separate contracts for:

- route topology observation;
- narrow date-availability observation;
- later fare observation.

External adapters translate source-specific responses into normalized observations carrying source identity and observation time.

### Persistence

Use the accepted control-plane ingestion/persistence boundary. The worker never writes Firestore directly.

For the POC, persist only the minimum normalized route/evidence/availability state required for the query and audit trail. Repeated submissions must attach to stable route identity and remain idempotent.

Worker SQLite may cache or spool observations, but is not authoritative.

### Identity and invariants

Apply at minimum:

- INV-ROUTE-001
- INV-ROUTE-002
- INV-EVIDENCE-001

The POC must also preserve:

- route existence is independent from date availability;
- source failure is not negative availability evidence;
- a positive date-availability observation is traceable to source/scan;
- repeated ingestion does not create duplicate logical routes or observations.

Use Alloy for stable route/evidence structural invariants. Decide on Dafny only from measured complexity; do not add ceremonial formal artifacts.

### Distance

Geographic candidate generation remains after the VLC POC. Store airport coordinates from a documented source and keep distance separate from ground connectivity.

## Iteration plan

### Iteration RT-1 — contracts, source research, fixtures

Deliver:

- minimal airport/carrier/route/source/scan/evidence contracts;
- stable canonical direct-route identity;
- topology provider contract;
- narrow availability provider contract;
- source research for the first VLC-capable real source;
- sanitized fixtures from the selected source;
- parser/normalization contract tests;
- query contract design.

Stop for external review if source access/stability is ambiguous.

### Iteration RT-2 — VLC live vertical slice

Deliver:

- exactly one real provider path;
- worker execution through existing authenticated lease/outbox/submission pipeline;
- central idempotent persistence of route + availability provenance;
- local REST query for VLC + date range;
- deterministic fixture tests plus explicitly separated live smoke evidence;
- clean-store E2E showing real observations become queryable.

This iteration targets the **VLC Route/Date Discovery POC**.

### Gate — independent user-value review

Do not resume cloud deployment until an external review confirms the milestone acceptance criteria below.

### After POC acceptance

Return to `add-distributed-control-plane` tasks 4.1–4.5 for deployed POC.

After deployed POC acceptance, continue the remaining topology scope:

- configurable default origins;
- broader source coverage only when justified;
- geographic candidate generation;
- full topology closeout.

## VLC POC acceptance criteria

All must be true:

1. AirScan can be started locally using documented commands.
2. One real, reviewed source adapter executes from the worker runtime.
3. At least one live smoke run for `VLC` succeeds without embedding secrets in git.
4. A clean local/emulated central store receives normalized direct-route evidence through the existing worker/control-plane HTTP protocol.
5. Date availability is represented separately from route existence.
6. Querying `VLC` with a valid date interval returns zero or more real destinations based only on positive source evidence in that interval.
7. For every returned destination, the response exposes source/provenance, observation freshness, and at least one matching date.
8. Repeating the same accepted scan/batch does not create duplicate logical routes or observations.
9. A provider/network/parser failure is surfaced as failure/staleness and does not mean “no routes”.
10. No fares/prices are fabricated or inferred.
11. Deterministic fixture tests run without network access; live smoke is separated from mandatory deterministic CI.
12. Maven, OpenSpec strict, Graphify, Serena, relevant invariant checks, and evidence documentation pass.
13. The result can be reproduced from `docs/` instructions by a developer on the local machine.
14. No production cloud deployment is required for this milestone.

## Risks / Trade-offs

- [Source access changes] -> isolate adapter, retain fixtures and provenance, keep live smoke separate.
- [Topology vs schedule ambiguity] -> store distinct evidence types; never infer one from the other.
- [Carrier attribution differs by source] -> preserve source-specific observations; unknown is better than guessed.
- [Seasonal/date semantics] -> the POC reports observed positive availability dates only, not a permanent schedule claim.
- [Live smoke non-determinism] -> deterministic CI validates fixtures/contracts; live evidence is timestamped and explicitly non-gating where appropriate.
- [Firestore write/index cost] -> reuse accepted idempotent ingestion and keep POC materialization minimal.
- [Cloud work delayed] -> intentional: first prove user value locally, then deploy the proven slice.
