# Provider Capability Research Gate — preliminary architecture decision

**As of:** 2026-10-25  
**Status:** **P4 PROVISIONAL / CONDITIONAL GO FOR A LICENSED PILOT DESIGN — NOT IMPLEMENTATION GO**. No independent final review; no live API probes; no validated Aena VLC denominator. Do not merge as a final provider selection or change OpenSpec tasks on this evidence alone.

## Decision framing

The goal is a useful local VLC direct-flight date search with the **least number of permissible HTTP requests** and smallest maintenance burden, without losing route/availability/fare provenance.

Start by **buying/using one legal cross-carrier effective-schedule feed if accessible and affordable**, not by copying 10 reverse-engineered SDKs. The no-cost and request-efficient assumptions are unproven; first validate offers, call quotas, permission to cache and VLC data coverage.

### Primary candidate for authorized pilot: OAG Schedules

**Why:** OAG published effective schedules (date validity+operating weekdays) can be queried via date ranges, potentially much cheaper per route/date than checking each individual airline/flight day. Date expansion occurs locally, without network calls per day, using source validity/weekday semantics.

**Hard pending conditions:**
1. obtain correct API/subscription access and acceptable terms for local schedule caching, reproducible provenance and published user search;
2. verify that filtering only by VLC origin is available and identify 180-day window/page limits;
3. get written or published price/request quota and affordability;
4. run authorized VLC samples, reconcile against a separately verified Aena VLC graph, preserving `UNKNOWN/UNCOVERED`;
5. distinguish published schedules from ticket saleability; schedule records do not guarantee fares or seats.

**Status:** CONDITIONAL GO FOR **ACCESS/COST/FEASIBILITY RESEARCH**, not a production source selection.

### Alternative candidate: approved Vueling calendar / Transavia partner API

If a licensed cross-carrier schedule feed is not affordable or cannot be used locally:

- Request an explicitly supported `Flight Calendar` via an approved Vueling NDC partner (see provider matrix).
- Independently verify if Transavia's older public portal still grants Routes and Flight Offers API subscriptions.
- Consider Norwegian/Pegasus where directly applicable to real VLC routes and authorized commercial/individual access exists.
- Pick **at most one verified airline adapter** for a narrow pilot, not a speculative expansion.

**Status:** CONDITIONAL GO FOR PARTNER DISCOVERY; 30/90/180 capability and access unknown.

### Observation-only fallback: Aviation Edge

An authorized Aviation Edge airport-day future schedule source could be an inexpensive bulk **candidate generator** but its own FAQ describes future service as historically predicted, therefore:

- tag observations `PREDICTED / INDICATIVE` with source and observation time;
- do **not** mark `CONFIRMED` for a date without an independently appropriate schedule source;
- do not mark unknown routes negative merely because airport-day projections omit them;
- document missing next-7-day horizon and per-day request cost.

**Status:** POTENTIAL SUPPLEMENT, not sole source of confirmed VLC date availability.

### No-Go pending authorization

- Wizz Flywizz direct private API automation: technical 5-destination/63-calendar-day batch is code-inspected, but not a data-access license; website terms/search evidence require written consent for automatic extraction, and bot gate exists for some paths.
- Ryanair private/website API automation: maintained OSS clients with route/month and flight-dates methods exist, but license/data-access constraints remain.
- easyJet direct website scraping; Volotea registered agency platform automation; browser challenge bypass: not part of pilot.

Any direct airline API becomes GO only with explicit authorization and verified relevant date coverage.

## Why capability-driven selection matters

Keep topology, dated schedule, predicted timetable, fare and seat inventory **different contracts or evidence subtypes**.

An eventual `ProviderDescriptor` metadata shape should communicate (not implement now):

```text
sourceIdentity and provenance
dataKinds = {TOPOLOGY, PUBLISHED_SCHEDULE, DATED_AVAILABILITY, PREDICTED_SCHEDULE, FARE_QUOTE}
accessClass = {PUBLIC_DOCUMENTED, LICENSED_PARTNER, PRIVATE_UNAUTHORIZED, UNVERIFIED}
authorizedUseScope / retentionCachingRights
originCoverage / carrierCoverage (source-asserted vs verified)
supportsDateRange, maxInclusiveCalendarDays
supportsOriginOnly, supportsMultiDestination, maxDestinations
paginationModel, quota/rateBackoff
priceModel, updateCadence, freshnessTTL
confidence/evidenceStatus, lastVerifiedAt
```

Descriptor fields that have not been independently proved must be `UNKNOWN` rather than a guessed boolean or default numerical zero.

The planner should prefer permitted sources by **availability evidence coverage + request fanout + latency/cost + maintenance**, and never use source failure as a negative result. Until the first source is independently accepted, do not add dynamic scheduling or new worker protocol types.

## Proposed RT-3 mini-pilot gate (later, not implemented here)

1. Validated origin VLC topology snapshot, with all canonical direct routes or explicitly diagnosed omissions.
2. One selected source with documented license/API terms and allowed local cache/retention.
3. Authorized sample for at least one *real* VLC route and 30-day positive dates, with exact query/request evidence.
4. Negative/empty/failure test proving `UNKNOWN/UNCOVERED` rather than invented no-flight.
5. Cost model verified for 90/180-day search under real pagination and quota.
6. Adapter uses current worker auth/lease/outbox/submission/central idempotency; no direct Firestore from worker and no fake synthetic observations.
7. Fixture tests deterministic; live smoke non-gating; idempotency and provenance verified.
8. Independent technical review and then explicit implementation authorization.

## Open decisions

- OAG offer/price, 180-day/query, true all-origin schedule results, retention/export rights.
- Availability of an approved API account for a personal noncommercial AirScan project; provider licensing may be oriented to business customers.
- Airline partner integrations may be unavailable to individual developers.
- Detailed current Wizz terms full PDF (direct fetch failed in this research pass).
- Active VLC routes by airline; PR #8 Aena snapshot is not yet verified and should not be silently trusted.
- Real HTTP latency, page count, quotas and annual costs; no live key was used.
- Whether the first practical date result can rely on published schedules vs only airline-confirmed offers.

## Explicit reviewer handoff

The external reviewer should independently validate `MATRIX.md`, `SOURCE_NOTES.md`, `PROBES.md` and `COVERAGE_AND_COST.md` against linked primary sources, review source access/legal conclusions, verify that mathematical models are labeled *model* and not *measured*, and determine whether conditional GO is appropriate. They must not claim runtime verified status from the research.

**A research-review PASS would validate a defensible source selection strategy and safe next probe plan — not 100% VLC coverage, not deployment, and not permission to run private APIs.**
