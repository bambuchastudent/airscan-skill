# Spec Delta

## Purpose

Maintain an explainable graph of direct air-route topology from configured origins so later fare and itinerary searches can operate on persisted evidence instead of rediscovering the network. The first user-value milestone proves this locally for Valencia by combining route evidence with separately modeled positive date-availability observations.

## ADDED Requirements

### Requirement: Airport-wide VLC direct-route catalog is the first milestone

The system SHALL first discover and preserve the advertised direct destination network of Valencia (VLC) across all carriers from an appropriate airport-wide topology source. Airline-specific pages SHALL NOT be treated as the complete airport network.

#### Scenario: VLC route catalog requested without dates

- **WHEN** a consumer requests the known direct routes from VLC without a date filter
- **THEN** it receives all normalized direct destinations supported by the successfully ingested airport-wide source snapshot
- **AND** every route has source/scan/freshness provenance
- **AND** multiple carrier observations do not fork a canonical origin–destination route

#### Scenario: Airport-wide source is incomplete or fails

- **WHEN** a source scan is partial, fails or its coverage cannot be established
- **THEN** the system exposes that failure/incomplete-coverage status
- **AND** does not infer that absent routes have ceased to exist

### Requirement: Dated VLC availability is a separate enrichment

The system SHALL support an inclusive date-range query that adds **positive observed** flight-date evidence to the existing airport-wide VLC topology catalog, preserving explicit coverage/unknown statuses.

#### Scenario: Availability confirmed for selected dates

- **WHEN** a date source reports positive direct-flight dates for a known VLC route inside a consumer-supplied inclusive interval
- **THEN** the response links those dates to that canonical route with the date source's provenance and observation freshness

#### Scenario: Airline-only date source covers a subset

- **WHEN** one airline-specific source supplies dated evidence for some known VLC routes
- **THEN** the system reports those as confirmed observations and marks uncovered routes UNKNOWN/UNCOVERED
- **AND** the result SHALL NOT claim to represent all available flights from VLC on the dates

#### Scenario: No positive date observations

- **WHEN** an existing VLC route has no positive observed dates inside the selected interval
- **THEN** its airport-wide route existence remains intact
- **AND** the date result is UNKNOWN/UNCONFIRMED unless an independently defined complete negative-evidence policy applies

#### Scenario: Date provider fails

- **WHEN** source fetch or parsing fails
- **THEN** the system surfaces error/staleness
- **AND** it does not turn failure into a claim of zero flights

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
