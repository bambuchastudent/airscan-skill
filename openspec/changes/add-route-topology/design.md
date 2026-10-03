# Design

## Context

This change follows bootstrap-agentic-foundation. Fare crawling depends on a stable route graph, but route topology and fare state have different refresh rates and failure semantics.

## Goals / Non-Goals

Goals:

- canonical route graph and provenance;
- provider SPI for topology discovery;
- deterministic persistence/querying;
- offline parser tests from sanitized fixtures;
- initial official/authoritative source adapter after verification;
- distance-based neighborhood candidate generation.

Non-goals:

- live fares;
- schedule search;
- route inactivity policy beyond retaining observations;
- ground public-transport routing;
- dashboard ranking.

## Decisions

### Ports/adapters boundary

Domain code does not parse HTML or call external APIs. A topology-provider boundary returns normalized source observations plus provenance metadata. External adapters translate source-specific responses into that boundary.

### Source precedence

Prefer official airport/operator route information for topology. For Aena-managed airports, investigate official Aena route/destination data. For CDT, investigate official Castellón Airport route data. Do not design around undocumented endpoints without evidence.

Airline sources may provide additional topology evidence but are not required for the first source adapter.

### Persistence

Use SQLite migrations. Preserve normalized entities separately from evidence observations. Do not overwrite historical evidence when the same route is observed again.

The schema must support first_seen/last_seen derivation without losing individual observation provenance.

### Identity and invariants

Apply at minimum:

- INV-ROUTE-001
- INV-ROUTE-002
- INV-EVIDENCE-001

Use Alloy for structural identity/evidence relationships. Use Dafny for canonical route identity only if the contract is stable enough to justify generated Java at this stage.

### Distance

Store airport coordinates from a documented dataset/source. Geographic distance uses a deterministic geodesic/Haversine-style calculation for candidate generation. It does not create a ground-connection edge.

## Risks / Trade-offs

- [Official page layout changes] -> fixture-based parser tests and adapter isolation.
- [Carrier attribution differs by source] -> evidence model retains source-specific observations; normalization must not invent certainty.
- [Codeshare/seasonal ambiguity] -> route topology records what the source states and leaves schedule semantics to later capabilities.
- [Coordinate dataset drift] -> provenance/version the airport catalog source.
