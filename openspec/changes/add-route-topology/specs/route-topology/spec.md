# Spec Delta

## Purpose

Maintain an explainable graph of direct air-route topology from configured origins so later fare and itinerary searches can operate on persisted evidence instead of rediscovering the network. The first user-value milestone proves this locally for Valencia by combining route evidence with separately modeled positive date-availability observations.

## ADDED Requirements

### Requirement: VLC Route/Date Discovery POC

The system SHALL support a local POC query for direct destinations from VLC over a caller-supplied inclusive date interval using real source observations processed through the accepted worker/control-plane boundary.

#### Scenario: Real VLC destinations are requested for a date interval

- **WHEN** a consumer requests direct destinations from VLC with valid inclusive `from` and `to` dates
- **THEN** every returned destination has at least one positive source observation whose availability date falls within that interval
- **AND** the response includes source provenance and observation freshness

#### Scenario: Route exists outside requested dates

- **WHEN** route-topology evidence exists but no positive availability observation falls inside the requested interval
- **THEN** that route is not returned as date-available
- **AND** the system does not treat the route itself as deleted or disproven

#### Scenario: Provider scan fails

- **WHEN** the live source request or parser fails
- **THEN** the system records/surfaces failure or staleness
- **AND** it does not convert that failure into negative route or availability evidence

#### Scenario: No matching availability exists

- **WHEN** a successful applicable scan yields no positive availability observations inside the requested interval
- **THEN** the query returns no matching destinations for that evidence window without inventing routes, dates, or fares

### Requirement: Configured origin airports

The system SHALL maintain a configurable set of origin airports and initially provide VLC, CDT, ALC, MAD, and BCN as the default origin set after the narrow VLC POC is accepted.

#### Scenario: Default origin graph is requested

- **WHEN** broader topology behavior is enabled without an origin override
- **THEN** the system uses VLC, CDT, ALC, MAD, and BCN as the configured origins

### Requirement: Direct route evidence

The system SHALL persist a direct route only with normalized endpoints, carrier identity when known, source provenance, and the scan observation that supplied the evidence.

#### Scenario: Source reports a direct route

- **WHEN** a successful topology scan reports a direct connection from a configured origin to another airport
- **THEN** the system records normalized route evidence linked to the source and scan

#### Scenario: Carrier is not available from source

- **WHEN** a source confirms a direct route but does not identify the operating carrier
- **THEN** the route evidence records carrier as unknown rather than guessing one

### Requirement: Dated availability evidence is separate from route existence

The system SHALL represent a positive observation that a direct route is available on a particular date separately from the route-topology fact itself.

#### Scenario: Source reports a flight date

- **WHEN** the selected source positively reports direct availability for a route on a specific date
- **THEN** the system stores a dated availability observation linked to route, source, and scan provenance

#### Scenario: Availability is absent

- **WHEN** no positive dated observation exists for a requested interval
- **THEN** the system does not infer that the underlying route does not exist

### Requirement: Route identity

The system SHALL use deterministic canonical identity for airports, carriers, and direct routes so repeated observations of the same logical route do not create unrelated duplicate routes.

#### Scenario: Same route is observed again

- **WHEN** another scan observes the same normalized origin, destination, and applicable carrier identity
- **THEN** the observation attaches to the same logical route identity

### Requirement: Provenance is queryable

The system SHALL allow a persisted route or dated-availability fact to be traced to the source observations and scan runs that support it.

#### Scenario: Route evidence is inspected

- **WHEN** a consumer requests evidence for a stored route
- **THEN** the system returns the supporting source identity and observation timestamps without requiring access to an LLM

### Requirement: Discovery is separate from fares

The system SHALL represent route existence and dated availability independently from fare observations.

#### Scenario: Available route has no fare observation

- **WHEN** topology and dated availability evidence exist but no fare has been collected
- **THEN** the route/date remains queryable without inventing a fare

### Requirement: Geographic neighborhood candidates

The system SHALL provide deterministic geographic-distance candidates for airports and relevant places while keeping geographic distance distinct from actual ground-transport connectivity.

#### Scenario: Nearby airport candidates are requested

- **WHEN** a consumer requests airports within a configured distance of a destination airport or place
- **THEN** the system returns candidates with calculated distance and does not claim public-transport connectivity unless separate ground-routing evidence exists
