# Spec Delta

## Purpose

Maintain an explainable local graph of direct air-route topology from configured origins so later fare and itinerary searches can operate on persisted evidence instead of rediscovering the network.

## ADDED Requirements

### Requirement: Configured origin airports

The system SHALL maintain a configurable set of origin airports and initially provide VLC, CDT, ALC, MAD, and BCN as the default origin set.

#### Scenario: Default origin graph is requested

- **WHEN** route topology is queried without an origin override
- **THEN** the system uses VLC, CDT, ALC, MAD, and BCN as the configured origins

### Requirement: Direct route evidence

The system SHALL persist a direct route only with normalized endpoints, carrier identity when known, source provenance, and the scan observation that supplied the evidence.

#### Scenario: Source reports a direct route

- **WHEN** a successful topology scan reports a direct connection from a configured origin to another airport
- **THEN** the system records normalized route evidence linked to the source and scan

#### Scenario: Carrier is not available from source

- **WHEN** a source confirms a direct route but does not identify the operating carrier
- **THEN** the route evidence records carrier as unknown rather than guessing one

### Requirement: Route identity

The system SHALL use deterministic canonical identity for airports, carriers, and direct routes so repeated observations of the same logical route do not create unrelated duplicate routes.

#### Scenario: Same route is observed again

- **WHEN** another scan observes the same normalized origin, destination, and applicable carrier identity
- **THEN** the observation attaches to the same logical route identity

### Requirement: Provenance is queryable

The system SHALL allow a persisted route fact to be traced to the source observations and scan runs that support it.

#### Scenario: Route evidence is inspected

- **WHEN** a consumer requests evidence for a stored route
- **THEN** the system returns the supporting source identity and observation timestamps without requiring access to an LLM

### Requirement: Discovery is separate from fares

The system SHALL represent route existence independently from fare and schedule availability.

#### Scenario: Route has no fare observation

- **WHEN** topology evidence exists for a route but no fare has been collected for a requested date
- **THEN** the route remains queryable as a known route without inventing a fare or treating the route as absent

### Requirement: Geographic neighborhood candidates

The system SHALL provide deterministic geographic-distance candidates for airports and relevant places while keeping geographic distance distinct from actual ground-transport connectivity.

#### Scenario: Nearby airport candidates are requested

- **WHEN** a consumer requests airports within a configured distance of a destination airport or place
- **THEN** the system returns candidates with calculated distance and does not claim public-transport connectivity unless separate ground-routing evidence exists
