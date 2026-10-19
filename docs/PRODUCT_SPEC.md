# MORONTO MORNICA — Product and Commercial Specification (Draft, 2026-10-19)
## Verified baseline
- GitHub repository SHADOWSPARK-TECHNOLOGIES/Moronto-music-app has README and LICENSE at initial review; no working application verified.
- Earlier Mornto promotional concept: music discovery, search, playback, playlists, artist profiles, African/global music, lyrics and offline listening. These are concept features, not implemented or licensed.
- User goal: build with Google Antigravity and present a compelling Google Cloud-hosted platform with early commercial revenue.

## Chosen wedge
Independent Afrobeat/Amapiano artists, small labels, and creator collectives. First paid product: verified artist launch page + professional song previews + share link + fan-interest collection + update analytics. NOT Spotify-scale licensed streaming.

## Positioning
"MORONTO MORNICA — Music for Real People."
Treat the dual-brand title as a product naming working hypothesis until trademark, domain, audience and designer validation.

## Personas / jobs
A. Artist: create verified profile, provide ownership/license attestation, publish up to 3 previews, showcase releases, receive opt-in fan interest.
B. Fan: discover, search, listen to cleared samples, save a session playlist, follow an artist voluntarily.
C. Operator: manually vet content, handle removal, log provenance, review interest requests and service delivery.

## MVP scope and acceptance
1. Homepage: clear value proposition, artist CTA, sample discovery. Mobile and desktop, accessible keyboard/focus.
2. Discovery: demo/right-cleared track cards with explicit metadata; search by artist/title; pagination/empty state.
3. Player: real HTML audio, play/pause/seek, next, keyboard labels, error/unavailable states. No auto-playing sound.
4. Artist page: artist bio, links, previews, rights/provenance metadata, contact via opt-in mechanism with anti-spam controls.
5. Playlists: session/local-only if no authenticated store yet, with honest explanation of persistence.
6. Business: request an artist launch package; price displayed only when confirmed; otherwise "Request quote". Do not fake testimonials or fake user numbers.
7. Admin/rights: no public upload until moderation and proof-of-rights workflow exists. First version can use manually reviewed seed data.
8. Security, privacy: content moderation/removal, consent and privacy notice, no person tracking or unsanctioned scraping.
9. Engineering: lint, typecheck, unit, browser smoke, production build and docs prove reality; CI on branch.
10. Deployment-ready: .env.example, config separation, budget checklist, rollback instructions. No deployment until approval.

## Deferred
Commercial catalog, background/offline audio, copyrighted lyrics, recommendation ML, native Android/iOS, ad marketplace, payments/recurring billing, third-party scraping, user-generated uploads, open artist payouts. Add only after compliance and demand validation.

## Technology ADR proposal (not yet installed)
- UI: Next.js App Router + TypeScript + CSS tokens, accessible responsive components.
- State: typed local data model at first; introduce Firebase Auth/Firestore only when real user persistence is justified. Cloud Storage only for cleared media and with egress controls.
- Google hosting: Firebase App Hosting requires Blaze/pay-as-you-go billing. Treat backend creation and automatic rollout as explicit deployment decisions.
- Alternative: Google Cloud Run when server control or routing demands it. No speculative migration.

## Revenue experiments (not guarantees)
- 5 manually recruited artists invited to a demo call.
- 2 paid pilots for artist pages and onboarding: verify willingness to pay before building subscriptions.
- Artist package: branded link page + rights-cleared previews + profile setup + basic analytics, one-off manual payment with documented agreement.
- Metrics: demo-to-inquiry, pilot-to-paid, median delivery hours, gross margin, 7-day usage, consent rate, rights disputes.
- Profit = fees collected - payment fees - hosting/storage/egress - artist onboarding - support - refunds/taxes.
- No claims of Google partnership or financial projections as facts.

## Explicit launch gates
Gate A: working preview & automated checks. B: 3 real usage interviews. C: copyright/privacy/hosting cost review. D: written pricing and payment destination. E: production approval and rollback. F: first paid transaction with invoice/receipt.
