# Provider read-only feasibility and probe log — P2

**Status as of 2026-10-25:** FEASIBILITY EVALUATED / **NO LIVE PROVIDER DATA REQUESTS EXECUTED**.

P1 included reading official public documentation and inspecting upstream OSS code. The web documentation-fetch mechanism is **not** a live flight-data API test, and no 200/401/403 HTTP status for provider API endpoints was observed in our own runtime. We used no third-party credentials, browser sessions, CSRF/cookies or circumvented defenses.

## Strategy 1 — OAG Schedules API (official licensed multi-carrier)

- **Published interface:** [OAG portal](https://developers.oag.com/apis) and [date-range feature](https://developers.oag.com/whatsnew).
- **Hypothesis:** obtain a single authorized effective-schedule source for many carriers/dates in VLC, reducing provider-specific adapters.
- **Desk feasibility result:** date-range effective schedules/weekday mask documented; **possible architecture fit**.
- **Live GET:** NOT EXECUTED. Requires valid subscription key and verification of entitlement/quotas/licence/cache rights.
- **Not evidenced:** can query VLC alone (without per-destination filter), 180-day maximum, pagination counts, actual VLC route coverage, price and caching terms.
- **Proposed safe read-only probe after access approval:** request a narrow documented departure-date range for VLC (with no invented URL parameters), capture status, request count, response count, query metadata, pagination and latency; cross-check direct-only and operate-day expansion with one known VLC route. No raw commercial payload committed.
- **Failure stop:** rejection, missing entitlement, no permitted local retention or prohibitive price -> do not build connector.

## Strategy 2 — Aviation Edge Future Schedules (official cross-carrier predicted airport-day)

- **Published interface:** [API guide](https://aviation-edge.com/flight-api/); [FAQ on forecast basis/near-term blackout](https://aviation-edge.com/frequently-asked-questions/).
- **Hypothesis:** 1 date+airport request answers all future departures for VLC on that date, lower route fan-out but higher date fan-out.
- **Desk feasibility result:** request syntax documented and fits 30/90/180 **modeled** read counts; **NOT a confirmed timetable feed** — predictions derive from historical data; next 7 days unavailable.
- **Live GET:** NOT EXECUTED. Requires an authorized subscription/API key. No sample was presented as a genuine current VLC response.
- **Proposed safe read-only probe after access approval:** exactly one VLC day at least 7 days ahead, read-only and within a subscription, then compare returned dates/carriers to a separately confirmed airline schedule; label `PREDICTED` and never automatically promote to `CONFIRMED`.
- **Failure stop:** no local storage permission, data too speculative, missing routes/coverage or quota too small.

## Strategy 3 — Wizz Flywizz / Ryanair OSS (code-only, not authorized live)

- **P1 code review completed:** pinned code refs in `SOURCE_NOTES.md`.
- **Live GET/POST:** **NOT EXECUTED**. The inspected Wizz client targets internal interfaces with session tokens and acknowledges a Kasada gate for certain endpoints. Ryanair website/APIs have restrictive terms and written licensing path. Library MIT rights **do not grant** source-data access rights.
- **Conditions for any future live probe:** authorized API/data access and explicit unattended usage rights; no browser challenge/header evasion; match real published VLC routes; minimal safe read-only call documented before execution.
- **Result:** code-level batch arithmetic available; live reliability, access, VLC content and legal approval remain unverified.

## Strategy 4 — official airline partners (not directly probed)

Vueling NDC calendar, Transavia API, Norwegian NAPI, Pegasus NDC, easyJet approved channels, Volotea agency platform, Lufthansa EW Open API and Jet2 distribution all require further verified authorization or availability. No access request was filed and no partner credentials were provisioned in this phase. Where service portals expose public docs only, those docs are not live performance tests.

## Probe evidence template for any later authorized test

```text
PROBE_ID:
UTC_TIME:
USER_APPROVED_ACCESS:
PROVIDER / DOCUMENTED_API_VERSION:
REQUEST_SCOPE: VLC + exact dates and allowed endpoint (no secret)
AGREEMENT / RIGHTS: reference only, no credentials
HTTP_STATUS / REDIRECTS: observed
ROWS / PAGES / REQUESTS: observed
LATENCY_MS: observed
RATE_LIMIT_HEADERS: observed or NOT PUBLISHED
POSITIVE_DATES / MISSING_DATA_STATUS:
SOURCE_SHA256: redacted/sanitized hash
CACHE_REUSE_POLICY:
OBSERVATIONS vs INTERPRETATIONS:
STOP_CONDITION:
```

## P2 conclusion

Two different multi-carrier source strategies received independent *documentation-only feasibility evaluations*. They are **not live-verified**. Running authenticated network probes and measuring data/latency requires appropriate access and provider terms. No self-declared runtime PASS is justified.
