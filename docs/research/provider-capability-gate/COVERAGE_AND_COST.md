# VLC coverage and query-cost model — P3 preliminary

**Date:** 2026-10-25; **status:** MODELED ONLY / NOT MEASURED. None of these counts represent actual HTTP calls made to an airline or aggregator. No validated Aena VLC route snapshot is available from RT-1 yet. Thus **actual per-carrier VLC route counts and coverage percentages = NOT VERIFIED**.

## Definitions and consistency

Let:
- `D` = number of **inclusive calendar dates** being evaluated: 30, 90, 180.
- `R` = number of unique canonical *direct VLC destination* routes in a timestamped verified airport-wide Aena snapshot. UNKNOWN.
- `R_c` = number of those routes attributed to carrier `c` under verified provenance. UNKNOWN for all carriers.
- `R_covered` = distinct canonical VLC routes with at least one **usable, authorized** current dated-source path, independent of actual positive dates. UNKNOWN.
- `R_confirmed(D)` = distinct canonical VLC routes with one or more **positive confirmed dated** observations in that D-day interval. UNKNOWN.
- `Q` = read request count excluding credential acquisition, session initialization, error retries, pagination, provider rate-limit backoff and network failures unless explicitly stated.
- `P` = provider's permitted page/window maximum. UNKNOWN unless noted.
- `C` = provider contract price or unit request charge. UNKNOWN unless officially quoted and applicable to this use case.

Calendar date observations should use `LocalDate`; source metadata/provenance use UTC `Instant`.

Do not calculate `R_covered/R`, `R_confirmed/R` or quality coverage until the snapshot is independently validated. A marketing destination count is not evidence for a denominator.

## Hypothesis/request comparison

| Candidate | Read query unit | 30 inclusive days | 90 days | 180 days | Evidence and hidden overhead |
|---|---|---|---|---|---|
| **OAG Schedules** | Effective validity intervals/operating weekdays, date-range filters; query max/page size unknown | `UNKNOWN` | `UNKNOWN` | `UNKNOWN` | Official API supports departure date range, but no confirmed 180-day/airport-only query or pagination. Cannot assert "one HTTP request covers VLC 180d". Needs licensed account, source verification and local storage rights. |
| **Aviation Edge Future Schedules** | **One departure airport on one future date** | **30** | **90** | **180** | MODEL from documented request shape; results are **algorithmically predicted** and do not cover next 7 days. Retry/404/quotas excluded. Not suitable as sole `CONFIRMED` dates source. |
| **Wizz Flywizz multi-date client** | Up to **5 destinations** per call, date span ≤62 days (**63 inclusive dates**) | `ceil(R_WZ / 5)` | `2 × ceil(R_WZ / 5)` | `3 × ceil(R_WZ / 5)` | **CODE-INSPECTED CAPABILITY ONLY**, unauthorized for live until rights clarified. Includes not the `get_network` fetch, session/CSRF setup, API version churn, errors or retry. This is not a measured Wizz quota. |
| **Wizz Flywizz priced timetable** | One route, span ≤42 days (**43 inclusive dates**) | `R_WZ` | `3 × R_WZ` | `5 × R_WZ` | CODE model, fare timetable not seat inventory. A 90-day horizon requires 3 windows of up to 43 days; 180 requires 5. Direct-site rights unverified. |
| **Ryanair unofficial monthly timetable** | One origin/destination pair for one **calendar month** | `sum over R_FR routes of calendar-month boundaries crossed by window` (typically 1–2) | typically 3–4 months per route | typically 6–7 months per route | CODE method only, actual schedule range, cached reuse, rate/access and permission unknown. Month alignment matters; formulas not verified against live API. |
| **Ryanair unofficial `getDates`** | One airport pair, dates returned without explicit range in inspected method | at least `R_FR` calls if per-route access is permitted | `R_FR` calls only if returned horizon covers request; **UNKNOWN** | same limitation; **UNKNOWN** | No documented output horizon; cannot count coverage for 90/180 or infer negative evidence. |
| **Transavia official partner API** | Route/date/offer product mentioned, but window/batch unspecified | `UNKNOWN` | `UNKNOWN` | `UNKNOWN` | No credentials or current partner contract verified. |
| **Vueling NDC Flight Calendar** | Calendar function supported at selected service providers; query dimensions/quota unspecified | `UNKNOWN` | `UNKNOWN` | `UNKNOWN` | Need to request 180-day calendar, destinations batching, rate and cost from provider. |
| **Norwegian NAPI; Pegasus NDC; easyJet contracted channels; Volotea agency; Jet2 partner channels; EW API** | Partner-access/entitlement unknown, often offer-search rather than bulk timetable | `UNKNOWN` | `UNKNOWN` | `UNKNOWN` | Do not invent batch sizes or quote counts. |

For Ryanair explicit sample if a fixed `R_FR=12` is hypothesized (NOT measured), `Q=12 × M` where `M` is calendar months intersecting the 180-day window; `M` can be 6 or 7 depending on start. This example is algebra, **not** VLC carrier count.

### Important interpretation

- `supportsDateRange` without `maxRange`, pagination, and actual route coverage does not demonstrate efficiency.
- An airport-day data endpoint amortizes destination fanout, but may require `D` queries and may be too approximate for flight-day confirmation.
- A recurring schedule snapshot with validity range + weekday mask may be far more cacheable than retail shopping offers that expire quickly.
- Cached schedules can reduce repeated query counts, but TTL, redistribution/storage permissions, schedule changes and completeness semantics are currently **UNKNOWN**.
- Destination in response != the operating carrier. Multiple marketing/codeshare entries may refer to one physical flight; normalize source/operating carrier identities without adding a fake route.
- Distinguish schedule published (`SCHEDULED`), modeled projection (`PREDICTED`), and fare for sale (`FARE_OBSERVED`). Only the appropriate evidence tier can support a given user-facing claim.
- The outcome of an empty result after partial source query is `UNKNOWN/UNCOVERED`, not `NO_FLIGHTS`.

## Coverage matrix waiting for a validated VLC snapshot

| Field | Value / status |
|---|---|
| Aena snapshot date & authorized acquisition | **NOT VERIFIED** — RT-1 PR #8 active |
| Unique canonical VLC route count | **UNKNOWN** |
| Distinct carrier associations | **UNKNOWN** |
| 30-day positively confirmed route count | **UNKNOWN** |
| 90-day positively confirmed route count | **UNKNOWN** |
| 180-day positively confirmed route count | **UNKNOWN** |
| Source-gap percentage | **NOT COMPUTABLE** |
| API call latency/throughput | **NOT MEASURED** |
| Validated € per 30/90/180-day complete airport refresh | **UNKNOWN** |
| Contractual right to persist/cache 180-day data | **UNVERIFIED** |

## Next evidence required

1. Independent RT-1 verified Aena snapshot with canonical destination list and carrier associations.
2. At least one authorized multi-carrier schedule source account with published entitlements, allowed storage and quota.
3. Actual test responses for VLC, including cases with an advertised route but no matching published date.
4. Count returned rows, pages, HTTP status, call time, cache status and completeness for D=30,90,180.
5. Compare `R_covered` and `R_confirmed(D)` to verified `R`; show unverified rather than 0 where source scope is partial.
6. Unit price and projected monthly expenses for selected refresh frequency; confirm cost within the user's acceptable budget before recurring jobs.
