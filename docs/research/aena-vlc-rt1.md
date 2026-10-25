# RT-1 Aena VLC airport-wide source feasibility — 2026-10-25

## Candidate and findings

- Owner: **Aena S.M.E., S.A.**; official Valencia airport destination listing: https://www.aena.es/en/valencia/airlines-destinations/airport-destinations.html
- Captured publicly visible facts using a server-side web text renderer on 2026-10-25; page reported **84 destinations / 37 airlines** at inspection. This number is observational evidence, **not** a domain constant, expected fixed size or completeness guarantee. Visible IATA-coded destinations include AMS, BIO, BER, LPA, IBZ, LIS, and many more. Route examples list multiple carriers: AMS (Transavia, easyJet Europe, KLM, Vueling); BIO (Vueling, Volotea, Air Nostrum); BER (Ryanair, easyJet Europe). Aena labels information derived from current airline schedules and subject to change, with no date-by-date departures in this catalog.
- The listing displays airport destinations (IATA identities), country and carrier associations, but some entries do not expose a carrier in the retrieved rendered view. The absence of a visible carrier is **UNKNOWN** rather than proof of no carrier.
- The public crawler's text rendering showed headings/destination entries and carrier links without requiring provided credentials. **It did not expose original HTML bytes, CSS selectors, redirects, response headers, cookie jar, page JS or DOM construction details.** It is therefore *not yet verified* that Java 25 HttpClient + HTML parser can retrieve/parse the complete listing without JS or browser automation. The container network could not resolve www.aena.es (DNS error) and no automatic fetch was performed from it. Source is public HTML, not a documented machine API.
- Robots: direct robots.txt retrieval through available web tooling failed; robots compliance remains **unverified**. No documented API access, rate limit, caching TTL, or guaranteed schema has been found. Design for low-volume conditional GET if permitted, jitter/backoff and no login/CAPTCHA/anti-bot circumvention. Permission and crawling cadence must be reviewed before RT-2.
- Official legal notice: https://www.aena.es/es/aviso-legal.html — section 3 explicitly disclaims complete/exhaustive/current accuracy; section 8 reserves reproduction and exploitation absent written authorization. Section 7 reserves the right to restrict access. **Public readability is not automated-reuse authorization.** Request permission or determine another legitimate access path before building a production ingestion adapter. Page link rules are also restrictive; this document cites source locations for review, not a statement of Aena endorsement.
- Actual destination coverage: source *claims* 84 at inspection; no byte-preserved source payload and therefore **no verified source-entry versus parsed-entry reconciliation**. Seasonal/inactive versus operating flights cannot be inferred. No complete airport route assertion is made.

## RT-1 fixture status

`worker/src/test/resources/aena/vlc-sanitized-excerpt.txt` is a **manually reconstructed, minimized factual excerpt** based on visible source claims; it deliberately substitutes human-added `Destination:` and `Carriers:` delimiters. It is **not a captured Aena HTML/JSON payload**, contains no credentials, cookies, authentication or personal data and is not sufficient to validate a live selector. This limitation is a deliberate open gap rather than misleading a reviewer about live compatibility. The worker normalization test proves deduplication, carrier association, airport identity and explicit omissions **only on this sanitized excerpt format**.

## RT-2 source validation gate before adapter

1. Obtain permitted access and a single representative raw HTML response without bypass; record UTC, exact request method and origin, HTTP status, final URL, Content-Type, redirect chain, cookie/session need and Java HttpClient reproduction.
2. Check published robots/terms and permitted automation with a recorded reviewer decision. No browser-internal endpoint or anti-bot bypass.
3. Inspect genuine HTML destination cards and carrier link boundaries, taking an anonymized minimal DOM fixture; replace/augment RT-1 reconstructed excerpt with a real structural fixture before relying on parser completeness.
4. Independently extract source candidate count and compare to all normalized IATA entries. Every malformed/missing/self/duplicate route and unknown carrier must be recorded; **do not silently drop** candidate destinations. Do not hardcode 84 or any other live total.
5. Explicitly mark scan PARTIAL/UNKNOWN rather than publishing catalog completeness whenever extraction leaves omissions or the source itself has unknown coverage. Failure leaves previous evidence untouched.

## Alternatives if Aena automated access is unsuitable

- Request a documented/license-approved export/access method from Aena (preferred first action; airport-wide coverage retained).
- Evaluate a permitted airport-wide schedule/network dataset with independently verified origin/destination coverage and provenance, not a single-airline route landing page. ENAIRE's AIP aeronautical *ATS routes* are not passenger direct-flight topology and must not be substituted.
- Evaluate open transport data only if its license, currency and coverage of **advertised direct routes** satisfy the same contract; e.g. Dataestur https://www.dataestur.es/en/apidata/ lists an AENA_DESTINOS_DL downloadable transport dataset, but its schema, license, currency and direct-flight semantics were **not validated** here.
- Ryanair pages remain a possible **carrier-scoped sparse date-evidence** research input for RT-3, never the airport-wide topology authority.

**Decision:** Aena is the one primary official **candidate**, not an approved automated acquisition path yet. Status: access/DOM and data-use clearance required before marking Aena technically validated.
