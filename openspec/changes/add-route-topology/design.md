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
- one airport-wide source for VLC topology, with Aena's official Valencia destination catalog as the first research candidate;
- separately sourced date availability and explicitly reported coverage/unknowns for a local VLC + date interval query;
- full ingestion through the accepted authenticated worker/control-plane path;
- deterministic query semantics that distinguish topology, availability, and source failure;
- distance-based neighborhood candidate generation after the VLC POC.

Non-goals for the VLC POC:

- fares or price ranking;
- cheapest-destination search;
- connections/itineraries;
- uncontrolled mass implementation of provider adapters;
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

and receive the VLC airport-wide known direct-route catalog annotated with source-confirmed flight dates inside that interval (when available), explicitly distinguishing confirmed dates from unknown/uncovered availability. A filtered 'confirmed dates only' view may be offered, but cannot imply exhaustive flight coverage.

The query must not infer availability merely from route existence.

Conceptually:

```text
official VLC topology source + separately validated date sources
   ↓
worker adapter(s)
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
- return only direct routes and distinguish airport-wide topology coverage from dated schedule coverage;
- expose destination airport identity;
- expose carrier identity when the source supplies it, otherwise unknown;
- expose supporting availability dates within the requested interval when confirmed, otherwise explicit UNKNOWN/UNVERIFIED coverage status;
- expose source/provenance and observation freshness;
- never invent a fare;
- distinguish no matching positive observations, absent date-source coverage, and provider/scan failure; empty positive results are not evidence of no flights.

### RT-1 manual one-shot topology import (PR #8 architecture correction)

The Aena VLC source is updated **rarely and only when a human explicitly initiates** an import, not through a scheduled crawler. RT-1 provides a Java CLI consuming a user-saved, fully rendered local browser HTML/DOM export; the user captures the source by deliberately saving the public page or manually exporting the rendered `<main>` DOM. This CLI never fetches URLs, authenticates to Aena, modifies Firestore, schedules polling or bypasses anti-bot controls. A user may refresh the local catalog later by running the same command with a new captured file and timestamp.

The CLI must identify advertised destination IATA codes (rightmost airport code for city/airport pairs such as `(MIL) (TSF)`), retain website-listed carrier associations as **listed**, preserve unknowns, diagnose malformed candidates/duplicates and reconcile unique results with the **count advertised by the saved source** if present. No number (historically 84, later 81) is hardcoded as an invariant. Incomplete or uncertain parsing must not replace a previous accepted snapshot. Output is local versioned JSON with deterministic ordering, source SHA-256, capture time, source URL and importer/coverage diagnostics; worker/control-plane ingestion is still RT-2.

A fabricated HTML table demonstrating visible labels is **not a genuine Aena DOM fixture**. For task 0.4 independent acceptance, a minimal sanitized fixture must retain the actual copied element hierarchy of a public, rendered Aena snapshot, and its candidate counts must reconcile. Full copyright-protected page captures stay user-local; neither a permission grant for redistribution nor complete current coverage is implied. See `docs/aena-manual-import.md`.

### Source selection and coverage

**Topology first: origin VLC, all carriers.** Research the official Aena Valencia airport destinations list as the first candidate: https://www.aena.es/en/valencia/airlines-destinations/airport-destinations.html. It currently advertises a broad airport-wide destination network with carrier associations, but is a *published route catalog*, not a date-by-date schedule. Verify parsing, actual source scope, completeness limitations and freshness; do not hardcode a current count as an invariant.

**Date evidence second.** Independently research reliable public/official schedules or flight-date sources applicable to routes discovered from the catalog. This may require several providers for useful coverage. Do not choose Ryanair as the topology provider merely because a few Ryanair landing pages expose positive dates. Any airline's landing-page observations must remain carrier-scoped, sparse and nonexhaustive. Do not assume full airport-wide date coverage from one airline.

Live access is evidence, not the deterministic test oracle. Commit sanitized fixtures and deterministic parser/normalization tests. Record legal/terms, access limits and source provenance for every live path. No hidden APIs, circumvention, or uncontrolled crawling.

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

### RT-1 — airport-wide VLC topology contracts and source validation

- define minimal domain, canonical airport-pair route identity and provenance;
- validate manually captured official Aena VLC destination HTML/DOM as the topology candidate (not Ryanair);
- provide a one-shot local file importer and sanitized source-native offline fixtures with deterministic parsing/reconciliation tests;
- define separate date evidence provider contract and plan, without prematurely choosing one airline;
- design endpoints for the complete known VLC route catalog and date annotation.

### RT-2 — VLC topology local vertical slice

- implement the airport-wide VLC topology adapter;
- ingest source observations through accepted worker → authenticated HTTP → control plane → central store;
- expose a real local `GET /api/v1/routes?origin=VLC` catalog (or justified equivalent);
- verify stable route IDs, carrier evidence, provenance and duplicate ingest;
- compare normalized coverage against the source snapshot and document gaps.

**Independent RT-2 gate:** all observed advertised direct destinations in the verified Aena snapshot are represented or every omission is explicitly diagnosed; no assumption about flights on a particular date.

### RT-3 — dates over VLC catalog

- select and implement only the necessary independently verified date source path(s) for an initial useful subset; define coverage explicitly;
- query a caller-supplied inclusive interval using positive dated observations;
- report CONFIRMED dates separately from UNKNOWN/UNCOVERED routes and failures; no unsupported exhaustive claims;
- run a clean-store local E2E with an actual source and record which fraction of Aena's route catalog had date-source coverage;
- document a credible path to broader date coverage without inventing timetable or fare facts.

**Independent user-value gate:** VLC topology + date annotations work end-to-end locally. An exhaustive all-carrier calendar must **not** be claimed without independently verified source coverage.

### After POC acceptance

Return to `add-distributed-control-plane` tasks 4.1–4.5 for deployed POC, then complete broader topology scope.

## VLC POC acceptance criteria

1. Local reproducible startup instructions.
2. An official airport-wide source supplies the known published **VLC direct-route catalog** across carriers (Aena is the initial candidate).
3. Parser fixtures are sanitized; catalog parsing offline tests are deterministic; live smoke verifies the source separately.
4. All observed destinations in the captured airport-wide source snapshot are normalized or omissions are explicitly diagnosed, without hard-coding destination counts.
5. Existing authenticated worker/control-plane ingestion is used; no direct worker Firestore mutations.
6. Canonical direct-route identity is stable, with multiple carrier evidence records attached to the same airport pair; replay remains idempotent.
7. `VLC` topology query returns airport-wide known advertised direct destinations with source/scan provenance and observation freshness, regardless of whether dated evidence exists.
8. Date evidence is **separate**. An inclusive date query reports confirmed observed flight days or UNKNOWN/UNCOVERED; it cannot say no flights solely because one source had no matches.
9. Failed provider/network/parser scans never become negative route/date evidence.
10. Carrier-specific data is labeled with its source and coverage; no Ryanair-only result may be presented as the entire VLC network.
11. No prices, inferred fares, cloud deployment, second origin, or ground routing are needed.
12. Maven, OpenSpec strict, Graphify, Serena, applicable invariant checks and evidence trail pass.
13. Independent external review approves the user-value POC.

## Risks / Trade-offs

- [Source access changes] -> isolate adapter, retain fixtures and provenance, keep live smoke separate.
- [Topology vs schedule ambiguity] -> store distinct evidence types; never infer one from the other.
- [Carrier attribution differs by source] -> preserve source-specific observations; unknown is better than guessed.
- [Seasonal/date semantics] -> the POC reports observed positive availability dates only, not a permanent schedule claim.
- [Live smoke non-determinism] -> deterministic CI validates fixtures/contracts; live evidence is timestamped and explicitly non-gating where appropriate.
- [Firestore write/index cost] -> reuse accepted idempotent ingestion and keep POC materialization minimal.
- [Cloud work delayed] -> intentional: first prove user value locally, then deploy the proven slice.
