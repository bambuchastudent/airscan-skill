# Primary-source and OSS code notes — P1

**Checked:** 2026-10-25 (Europe/Madrid). These are human-readable primary document/code inspections, **not successful API runtime tests**.

## Repository inspection evidence (GitHub refs pinned at time checked)

| Project | Inspected SHA | License and last push from GitHub API | Specific source inspected | Capability actually supported by code | Usage decision |
|---|---|---|---|---|---|
| [victorlane/flywizz](https://github.com/victorlane/flywizz) | `b3d6ec7024e10d355f79392b2027f453ed649a89` | MIT, 2026-10-19 | [`flywizz/wizzair.py`](https://github.com/victorlane/flywizz/blob/b3d6ec7024e10d355f79392b2027f453ed649a89/flywizz/wizzair.py), [README](https://github.com/victorlane/flywizz/blob/b3d6ec7024e10d355f79392b2027f453ed649a89/README.md), [internal API notes](https://github.com/victorlane/flywizz/blob/b3d6ec7024e10d355f79392b2027f453ed649a89/docs/internal-api-spec.md) | `get_network` and `get_destinations`; `get_flight_dates_multi` batches destinations by `MAX_ARRIVAL_STATIONS=5`, 62-day max **span**; `get_timetable`/farechart offers date/fare observations with 42-day span cap; explicit `BotGateError` for some paths. | **Code-design reference only pending airline authorization**. A session handshake, undocumented endpoint or "unblocked" non-booking endpoint is not proof extraction is permitted. Never transfer browser challenge headers to evade protections. |
| [2BAD/ryanair](https://github.com/2BAD/ryanair) | `076e369efb17bf79a9a380f57f52e5ebe85802a3` | MIT, 2026-10-20 | [`getDates.ts`](https://github.com/2BAD/ryanair/blob/076e369efb17bf79a9a380f57f52e5ebe85802a3/packages/client/source/flights/getDates.ts), [`getSchedules.ts`](https://github.com/2BAD/ryanair/blob/076e369efb17bf79a9a380f57f52e5ebe85802a3/packages/client/source/airports/getSchedules.ts) | `getDates(origin,destination)` -> date array via unversioned/internal fare-finder interface; `getSchedules(origin)`, `getSchedulesByRoute`, `getSchedulesByPeriod(origin,dest,year,month)` from timetable interface. | **Reference only** until scope, terms, and API access are reviewed; schedule granularity can be route/month, not automatically one call for all future dates. |
| [adambenhassen/ryanair-mcp](https://github.com/adambenhassen/ryanair-mcp) | `b2ef8b526d72582a35926890b2ed3be3d48db44e` | MIT, 2026-09-16 | [`internal/ryanair/schedules.go`](https://github.com/adambenhassen/ryanair-mcp/blob/b2ef8b526d72582a35926890b2ed3be3d48db44e/internal/ryanair/schedules.go), [fixtures](https://github.com/adambenhassen/ryanair-mcp/tree/b2ef8b526d72582a35926890b2ed3be3d48db44e/internal/ryanair/testdata) | `Schedules(origin,dest,year,month)` flattens timetable rows from a per-route/per-calendar-month response; code has fixtures for schedules, availabilities, airport destinations and networks. | **Reference only**; does not prove allowable access or a reliable all-carrier result. |
| [Transavia/ShoppingCartExample](https://github.com/Transavia/ShoppingCartExample) | `6760283768417b8ea8d9629c37d804a089ca7638` | MIT, 2018-04-23 | [README](https://github.com/Transavia/ShoppingCartExample/blob/6760283768417b8ea8d9629c37d804a089ca7638/README.md) | Official API demo mentions Flights Offers (search by route or dates) and historic public Routes/Airports/Flight Offers developer subscriptions. | **Historical official sample**, needs current Transavia portal validation; do not assume free signup is still provided. |
| [placintaalexandru/come-fly-with-me](https://github.com/placintaalexandru/come-fly-with-me) | `30ef80c3ea97bc5cbaf5dbecec2b2acc90a40518` | Apache-2.0, 2025-12-13 | [README](https://github.com/placintaalexandru/come-fly-with-me/blob/30ef80c3ea97bc5cbaf5dbecec2b2acc90a40518/README.md); Scrapy spiders for Wizz/Ryanair | Demonstrates internal-website API collection architecture and deployment; README itself documents Wizz API-version churn, intermittent 400s, changing cookie dependencies and excessive easyJet data fanout. | **Do not copy scraping design into production**; this is not an authorization or stability endorsement. |

Last pushes and licenses above are GitHub API metadata read 2026-10-25. A top-level MIT or Apache license governs library code, **not** airline API usage rights, response storage, publication, or rate access.

## Official source/access observations (not runtime tests)

- **Ryanair:** [Terms of Use](https://www.ryanair.com/ie/en/lp/legal/terms-of-use), sections 2–6; express application process for comparison-data license. Site content/APIs otherwise constrained to private, non-commercial uses and commercial automated extraction prohibited. Even a personal proof of concept needs careful legal interpretation; no direct automation approved here.
- **Wizz Air:** [official indexed terms PDF](https://wizzair.com/static/docs/default-source/downloadable-documents/website_terms_and_conditions_en_e9cf2b95.pdf) reportedly require prior written consent for website automated extraction, per public search-index extract. Direct PDF retrieval returned HTTP 405 in this research context, so full terms not independently examined; strict conservative outcome: **no unattended Wizz private-API probe** until confirmed permission. [Atlas direct-distribution partnership 2026-05-19](https://atlaslovestravel.com/resources/wizz-air-direct-lcc-api-partnership/) is an alternate partner channel, not general-use Wizz Air developer API.
- **Vueling:** [Official services/provider matrix](https://static.vueling.com/blogpartners/media/1103/vy-ndc-_-partners-services-and-functionalities.pdf), one-page PDF visually inspected. Not all NDC integrators support `Flight Calendar (Prices & Routes)`. Examples in the current matrix include TravelFusion, TripStack and selected others. [NDC-X announcement](https://www.vueling.com/es/blog-professionals/ndc/vueling-ya-disponible-en-amadeus-ndc-x/) discusses bookings. Service-provider/credential negotiation required for applicable product; explicit VLC/180-day/batch behavior unknown.
- **easyJet:** [Travel Trade Partners](https://www.easyjet.com/en/business/travel-trade-partners); [Distribution Charter](https://www.easyjet.com/es/negocios/distribution-charter), section 2 — reseller access via approved channel or a direct signed agreement; automated direct extraction restricted.
- **Volotea:** [Agency platform use rules](https://www.volotea.com/en/legal-conditions/terms-of-use-for-agencies/): platform requires registered professional agency and forbids scraping or unauthorized external automation, sections 2–3; agency platform must not be repurposed as a general data API.
- **Norwegian:** [official NAPI](https://www.norwegian.com/de/developer-resources/developer/sales-distribution/): explicitly requests registration, approval, credentials; includes availability and fares; date-window size/route batching not specified.
- **Transavia:** [Official API partner page](https://partner.transavia.com/en-EU/products-and-services/Our-API/): flight/route and date availability capabilities described, page appears old; verify with actual developer account before contract.
- **Eurowings:** [LHG Open API product](https://developer.lufthansa.com/product) mentions EW offers/status; [LHG developer landing page](https://developer.lufthansa.com/) says OpenAPI registrations on hold. [LHG NDC partner program](https://lhgroupairlines.com/ndc/en/) lists LH/LX/OS/SN/EN/4Y **not EW**. These programs must not be conflated. A third-party integration's limitations cannot be generalized to every possible EW channel.
- **Pegasus:** [Official NDC portal](https://devportal.flypgs.com/) describes `AirShopping`, `DoGetPortMatrix`, version 17.2, and agent credential/partnership requirements. PortMatrix != every flight date. No access token requested here.
- **Jet2:** Primary developer API not located in this pass. [api-evangelist/jet2](https://github.com/api-evangelist/jet2) and [APIs.io inventory](https://apis.io/providers/jet2/) report partner-gated distribution via Kyte, **secondary evidence only**. No claim of official Jet2 JSON schema.

## Cross-carrier capability notes

### OAG

[OAG product portal](https://developers.oag.com/apis) offers schedule records in effective interval + operating weekday form. [Nov 2024 data-field guide](https://knowledge.oag.com/docs/schedules-api-response-layout-descriptions) and [OAG update on date-range query](https://developers.oag.com/whatsnew) document the technique.

**Important parsing condition:** an effective record whose validity interval intersects a search interval is not necessarily a day on which the flight operates. Intersect `effectiveStart..effectiveEnd` with weekdays and exceptions, where published, before emitting per-day evidence. An [OAG update](https://developers.oag.com/whatsnew) explicitly notes schedule entries can fall within a requested effective-date range without operating in it.

Unverified: ability to query **all VLC departures** without a destination filter, maximum 180-day window, pages/call, costs and rights to retain a local copy. Do not claim single-call global coverage.

### Aviation Edge

[Official flights API documentation](https://aviation-edge.com/flight-api/) allows `flightsFuture` filtered by departing airport+one day. [Official FAQ](https://aviation-edge.com/frequently-asked-questions/) states future schedule is algorithmically generated from historical data, can reach 1 year ahead and does not cover first 7 days ahead.

**Meaning:** this is **PREDICTED/INDICATIVE** even when flight number/date is returned; not equivalent to source-confirmed current airline timetable, seat-for-sale availability, or calendar guaranteed to be current.

## Outstanding licensing and data quality dependencies

1. Confirm permissible automated schedule/data access (plus storage/retention rights) per proposed direct or aggregated source.
2. Obtain validated timestamped Aena VLC route/carrier snapshot separately from RT-1 PR #8; no denominator from an unverified number.
3. Verify whether the carrier actually serves each sampled VLC route during 30/90/180-day horizons before counting coverage.
4. Run live permitted samples with redacted status/response hashes only after access is confirmed and a suitable account/key exists.
5. Document source accuracy tiers: OFFICIAL_SCHEDULE (if source provides), PARTNER_INVENTORY, AGGREGATED_SCHEDULE, INDICATIVE_PREDICTION, UNVERIFIED.
