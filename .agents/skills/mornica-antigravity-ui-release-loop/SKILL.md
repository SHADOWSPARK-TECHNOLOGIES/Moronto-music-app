---
name: mornica-antigravity-ui-release-loop
description: Distinct Apple Music-informed, Nigeria-first Kotlin/Compose UI exploration and bounded QA loops, with optional compliant REA observation and explicit test-release gates.
---

# Mornica Antigravity UI and test-release skill

## Read first
1. `docs/antigravity/MORNICA_DESIGN_TEST_RELEASE_CONTRACT_20261108.md` (binding scoped contract)
2. `docs/brand/MORNICA_BRAND_SYSTEM.md` and `docs/brand/mornica-symbol.svg`
3. `AGENTS.md`, `docs/CONVENTIONS.md`, `.dev-workflow/`, `docs/agent-loop/STATE.md`
4. PR #14 and current `app/src/main` + tests at actual SHA; inspect live clean/dirty worktrees

## Operating model
1. **OBSERVE**: real Android UI + source/HEAD/SDK/AVD, 360/412dp, current bottom tabs, queue, download gates, real screenshots; record P0/P1 defects.
2. **BENCHMARK**: only user-facing Apple Music workflow concepts from Apple support/HIG or authorized app walkthrough. REA if installed and strictly needed for black-box observations; no binary reverse engineering, DRM bypass, traffic interception, asset extraction, paywall bypass or near-clone specification.
3. **SPEC**: one original Mornica user journey and branding treatment; distinguish common patterns from uniquely designed composables, voice, layout and animations. Document allowed files and tests.
4. **BUILD**: new isolated worktree. Preserve existing app ID/Media3 rights and local files. Prefer small Compose components with previews, screenshot automation and semantics over entire rewrite. Up to two non-overlapping writers; do not race Hermes.
5. **INDEPENDENT VERIFY**: build/unit/lint, UI semantics and screen captures, touch/Back/navigation, title length, data-aware/offline behavior. Reviewer cannot be author.
6. **GATE**: PASS only with verified evidence; FAIL/BLOCKED with exact reproducible issue. No forced green tests or hidden lint skips.
7. **REFLECT**: records in `docs/EXECUTION_LOG.md`, release ledger with changed paths, branch/HEAD, test output, screenshot hashes, risks, reviewer, owner decision.

## UI acceptance
- Primary content visible without scrolling through dense engineering copy. Never hide rights warnings behind a settings screen.
- Persistent player does not duplicate Media3 session; source and play/pause state are truthful.
- Download disabled/renamed `Streaming only` when not allowed; no source laundering or YouTube embed.
- Native Material 3 / TalkBack semantics, 48dp controls where applicable, font scale 1.3x, 360dp and 412dp, contrast targets (4.5:1 normal text), no clipped controls.
- Offline flow uses correct cached bytes, denial from rights policy, Wi-Fi preference, and deterministic retry/cancel feedback.
- Nigerian English concise default; Pidgin opt-in or thoughtfully sparse. No false popularity/scarcity or vulnerability targeting.
- Distinct Mornica wave/`M` identity; no copied Apple icons/screens/artwork/typography system.

## Time limit
Use 2-hour focused test candidate from contract, not speculative public release. Maximum 8 attempts per bounded objective, stop after 2 no-progress repeats. No merge/signing/publication/upload without owner-gated approval.

## Mandatory output
`Objective | GO/NO-GO/INTERNAL-TEST-ONLY | repo/worktree/HEAD/dirty paths | before/after screenshots | changed files | unit/lint/build/instrumented exit codes and counts | offline/licensing/TalkBack checks | independent reviewer | APK/AAB hash and signing identity (non-secret) | distribution eligibility | blockers | exact next action`

If required REA skill is absent or requires installation/provider setup, mark REA BLOCKED and perform normal UI observation from public documentation. Never silently install or enable a privileged analysis server.
