# AirScan

AirScan is a **local-first travel graph, low-cost route crawler, and fare observation system**.

Its purpose is to maintain a reusable local knowledge base of direct flight routes, observed fares, nearby airports, and ground-transport connections so route discovery does not have to start from web search every time.

## Initial origin airports

AirScan starts from five configurable Spanish origins:

- **VLC** — Valencia
- **CDT** — Castellón
- **ALC** — Alicante
- **MAD** — Madrid
- **BCN** — Barcelona

The origin set is configuration, not a hard-coded limit.

## Product goals

AirScan should:

- discover and retain direct-route topology from authoritative airport and airline sources;
- focus strongly on low-cost carriers connected to the configured origins;
- collect schedule and fare observations in the background with provenance and freshness metadata;
- preserve historical observations instead of overwriting prices;
- distinguish a route that exists from a fare that happened to be visible during one scan;
- calculate geographic proximity between destination airports and useful cities/airports;
- discover real ground/public-transport connectivity separately from straight-line distance;
- expose a local API and map UI;
- show the **10 cheapest observed destinations per origin** for the next **7, 90, and 180 days** based on the most recent successful applicable scan;
- remain useful without an LLM at runtime.

## Initial low-cost scope

First-wave carriers:

- Ryanair
- Vueling
- easyJet
- Wizz Air
- Volotea

Later providers may include Norwegian, Transavia, Eurowings, Pegasus, Jet2, and additional carriers discovered from the origin airports.

The provider architecture must remain extensible; the carrier list is not a closed enum.

## Architecture principles

AirScan separates four concerns:

1. **Route topology discovery** — which direct routes are advertised/known.
2. **Schedule availability** — when a route can actually be flown.
3. **Fare observation** — the price observed at a particular point in time.
4. **Ground routing** — how a destination airport connects to nearby cities and airports.

These concerns must not collapse into one scraper.

Normalized facts retain provenance. Fare observations are append-only. Route lifecycle uses evidence across successful scans so a transient fetch/parser failure cannot erase a route.

## Runtime direction

The planned runtime stack is:

- Java 25 LTS
- Maven
- stable Quarkus 3.x selected and pinned during bootstrap
- SQLite with explicit migrations and deliberate WAL/write-concurrency design
- MapLibre GL JS + TypeScript/Vite for the map UI
- Transitous/MOTIS behind a ground-routing provider boundary
- JUnit 5 + AssertJ for deterministic tests

Python is not the production implementation language.

## Agentic development

This repository is designed for AI-assisted development with stronger-than-normal guardrails:

- **OpenSpec** — feature/change planning and behavior specifications
- **Graphify** — local repository knowledge/impact graph
- **Serena** — semantic IDE/MCP navigation and editing
- **Alloy** — bounded checking of structural/domain invariants
- **TLA+/TLC** — concurrency and state-machine model checking
- **Dafny** — verified deterministic core logic compiled to Java where justified

See [AGENTS.md](AGENTS.md) for mandatory agent behavior and [docs/TOOLING.md](docs/TOOLING.md) for the tooling policy.

## Development state

The repository is currently in bootstrap/specification phase.

Pending OpenSpec changes are intentionally staged:

1. `bootstrap-agentic-foundation`
2. `add-route-topology`
3. `add-durable-scan-retention`

Do not jump directly to provider mass-implementation. Apply and verify changes in dependency order.
