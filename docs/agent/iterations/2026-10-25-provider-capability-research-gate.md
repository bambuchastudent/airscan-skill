# Research iteration: provider-capability-gate P1 desk / P2 documentary / P3 model / P4 conditional proposal

**Date:** 2026-10-25 (Europe/Madrid)  
**Owner/executor:** ChatGPT researcher (cannot independently review its own work)  
**Base:** `develop@301d979f66b765651303337876fe8998a2b2ca99`  
**Research branch:** `research/provider-capability-gate`  
**Concurrent RT-1:** PR #8 open draft; separately reviewed, untouched by this research.  
**Status:** READY_FOR_EXTERNAL_RESEARCH_REVIEW (subject to documentation audit); **NOT** runtime-proven RT-3, **NOT** an independent PASS.

## Work done

- Investigated official partner/distribution/API documentation for 10 airlines (Ryanair, Wizz Air, Vueling, easyJet, Volotea, Norwegian, Transavia, Eurowings, Pegasus, Jet2).
- Inspected and pinned upstream OSS sources for Flywizz, 2BAD/ryanair, ryanair-mcp, official Transavia example and come-fly-with-me; evaluated code methods, active dates, license vs terms.
- Cross-carrier evaluation of OAG published effective schedules and Aviation Edge predicted airport-day schedules.
- P2 read-only feasibility by official documentation for OAG and Aviation Edge; **no credentialed live API calls and no airline private API calls**.
- P3 mathematical request fanout for D=30/90/180 with measured vs estimated/unknown explicit, no invented coverage denominator from Aena.
- P4 conditional preference: authorized, appropriately priced OAG Schedules, fall back to authorized airline calendar adapters; Aviation Edge predictions only indicative; no direct Wizz/Ryanair OSS automation authorization.
- Documented future RT-3 handoff and stop conditions.

## Files

- `docs/research/provider-capability-gate/PLAN.md`
- `docs/research/provider-capability-gate/MATRIX.md`
- `docs/research/provider-capability-gate/SOURCE_NOTES.md`
- `docs/research/provider-capability-gate/PROBES.md`
- `docs/research/provider-capability-gate/COVERAGE_AND_COST.md`
- `docs/research/provider-capability-gate/DECISION.md`

## Evidence

- Official provider/terms links are embedded near each relevant claim in MATRIX and SOURCE_NOTES.
- GitHub OSS source SHAs pinned inside SOURCE_NOTES; repo metadata last pushed as checked 2026-10-25.
- Wizz client batch=5, 62-day inclusive-endpoint span limit, timetable=42-day span from inspected code/README; NOT tested live.
- Ryanair date and per-month schedules methods from inspected code; NOT tested live.
- OAG effective calendar and Aviation Edge historical-derived future prediction from provider docs; NOT tested live.

## Verification limits

- All **data runtime metrics:** NOT MEASURED.
- Live HTTP response to each provider API: NOT EXECUTED (permission/credentials not validated).
- Aena whole VLC canonical graph: NOT VERIFIED (separate RT-1 PR #8 blockers).
- Local retention and cache rights: NOT VERIFIED for all proposed partners.
- Quantified VLC carrier/direct route percentages: NOT COMPUTABLE.
- Maven/OpenSpec/Formal/Graphify/Serena: NOT RUN FOR RESEARCH DOCUMENTS; no Java/OpenSpec/formal code was changed. CI on the final research branch should be recorded after the PR triggers.
- Legal interpretation is conservative engineering screening, not legal advice.

## Next gate

Independent reviewer must verify all substantive source claims, licensing and code methods; check request-count formulas, unsupported certainty, and whether the recommended fallback remains minimal. Return PASS/NEEDS_CHANGES with blocker/major/minor findings and exact reviewed SHA and CI. If access/cost cannot be validated, any PASS is limited to **research strategy with conditional implementation**, never live integration approval.

**Do not merge RT-3 implementation, automate airline scraping, or change PR #8 under this research gate.**
