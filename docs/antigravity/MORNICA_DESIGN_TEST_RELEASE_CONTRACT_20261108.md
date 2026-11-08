# MORONTO MORNICA — ANTIGRAVITY DESIGN & TEST-RELEASE CONTRACT

**Contract:** MM-DESIGN-RC-2026-11-08 / v1.0
**Owner:** ShadowSpark Technologies
**Agent:** Google Antigravity operating inside Android Studio; Hermes remains technical implementation owner of existing playback infrastructure; independent Codex/human QA must review release changes.
**Source:** `SHADOWSPARK-TECHNOLOGIES/Moronto-music-app`
**Design base:** draft PR #14 / `agent/mornica-loop4-catalog-ui-20261107` (inspect current SHA and dirty worktrees before doing anything).
**Target:** A release-candidate suitable for consented/internal **testing** within a ~2-hour focused session, if hard verification gates pass. This is NOT a promise of Play approval or public launch.
**Status:** Contract/brand assets only. No implementation tests or publication performed by this contract PR.

## 0. Acceptance and provenance rules
1. This signed-off product contract is the reference for UX changes. It does not override `AGENTS.md`, `docs/CONVENTIONS.md`, Media3 rights policy, signing, privacy, or release protections. Stop and report contradictions.
2. Preserve `com.shadowspark.morontomornica`, release signing lineage, existing local library, MediaSessionService, Media3 playback, entitlement-gated offline cache, demo notices and authorized YouTube external-link boundary.
3. Read `docs/agent-loop/STATE.md`, `.dev-workflow/` contracts, relevant `AGENTS.md`, PR #14 and all current source before editing. State docs contain dated historical notes, not current proof.
4. At start: show `pwd`, repo origin, `git worktree list --porcelain`, `git status --short --branch`, exact HEAD, AVD identity, SDK/JDK, source/branch of running APK, and PR state. Do NOT reset, clean, stash, overwrite or cherry-pick into the owner's dirty worktree.
5. Implement in a NEW clean isolated worktree/branch. Avoid simultaneous edits to the same files as Hermes. An uncommitted/active Hermes design change is a coordination blocker, not permission to overwrite.
6. Do not merge, publish, replace user's installed Play app, change signing keys, release-track configuration, upload to alternative stores, install unverified third-party MCP tools or use paid content APIs without a separate action-specific owner authorization.

## 1. Inspiration boundary: Apple Music → original Mornica
**Allowed to study:** high-level workflows only: personalized starting point; curated discovery and editorial groupings; quick library entry; global search with scope; persistent mini-player; expanded now playing; queue; playlists; downloadable badges; readable artist page; well-marked empty/offline state. Consult Apple's public guidance and only software/screens legitimately accessible to the operator.
**Forbidden:** pixel-tracing, reproducing Apple Music UI assets/logos/artwork/copy/type treatments, copying an exact screen arrangement/theme as a near-clone, lifted illustrations, copied transitions from proprietary code, modified Apple trademarks, false affiliation, protected API/DRM bypass, binary decompilation or private network interception. Merely altering colors or icon sizes is NOT a sufficient differentiation.
**Mornica own identity:** the `M`-pulse/Naija-wave symbol, dark midnight-navy + bright jade + electric azure with restrained amber and coral; resilient offline-first Nigerian phone workflows; separate honest DEMO/owned/local/rights-cleared catalogue; creator presence and transparent data-cost controls. Native Compose semantics and Material 3 interaction behavior.
**Apple Music REA rule:** REA is OPTIONAL and must be observed installed and documented before use. Use REA only for user-authorized black-box observation of publicly accessible/own-install interactions where a claim cannot be established from public screenshots/docs. Log exactly which first-party-visible action was observed (tab, player, scroll, search, back behavior, offline switch). Do not run automated reverse engineering of Apple proprietary binaries, inspect Apple DRM/private APIs, download copyrighted resources, scrape protected accounts, or store personal account data. Default to normal UI walkthrough and publicly documented pattern comparison. Use REA normally on **our own Mornica APK** for instrumentation/accessibility/navigation diagnosis if legally and technically suitable.

## 2. Product experience / IA contract
**Proposed principal destinations:** `Home` (Continue listening + truthful suggestions); `Explore` (Afrobeats, Amapiano, local playlists where truly licensed; external YouTube video links secondary); `My Music` (Tracks, Albums, Artists, Playlists, Downloads and Folders); `Search` (clear local vs licensed catalog scope). Keep Creator Hub entry discoverable from Home/artist profile or as explicit destination only if validated; no dead tabs.
**Critical path:** Open app → see music first → tap local/licensed track → compact mini-player stays available → expand full player → queue → save offline *only if entitled* → Downloads → play in flight/offline mode. No forced onboarding or modal before first play.
**Search:** One plain text input with suggestions from real local tracks/cached authorized catalog only. Empty/failure/offline states must not simulate live search results. External video search is visibly marked `Open on YouTube` and leaves the app. Preserve selected tab and back stack.
**Playback:** Persistent mini-player with artwork or original neutral placeholder, song+artist, actual playing/pause state from Media3 callback, accessible play/pause and next when available, responsive expanded Now Playing with seek, queue, repeat/shuffle and system notification state. No fake progress/times. Do not spawn second audio engine.
**Download:** Content-entitlement before action, clear mobile-data/Wi-Fi preference, pause/cancel/retry/progress/saved/free-space and permission errors, no interactive download CTA on stream-only. Downloaded is TRUE only after Media3 cache confirms complete, and offline playback has no streaming fallback. Rights expiry must fail closed.
**Creator Hub:** Compact identity cards and consent-based opt-in for independent artists. All current invented/sample creator profiles labelled DEMO; no false verification, fabricated play counts or social proof. No commercial payment acceptance until agreements and gateway compliance are proved.
**UI language:** Clear English by default, optional naturally phrased Nigerian English/Pidgin. Do not force Pidgin on every screen. Technical compliance information belongs in accessible contextual detail, but material licensing or source labels must remain visible by the related action.

## 3. Brand system + protected visual assets
**Palette (design proposal):** Ink `#071321`, Canvas `#0B1727`, Surface `#14243A`, Text `#F4FAFE`, Muted `#A5B6CA`, Jade `#12D9A1`, Azure `#20B6EB`, Amber `#FFBE55`, Coral `#FF6479`. Keep functional hierarchy: Jade = affirmative/now-playing, Azure = links/secondary, Amber = rights/demo warning, Coral = errors. Not color-only semantics.
**Brand symbol:** custom flowing `M` as a rhythmic double-peaked soundwave, 5-line equalizer, tiny distinct amber star. No Apple note, Apple logo, Apple Music gradient lockup, Spotify circle/wave, or exact competing-store brand shape.
**Required first-party assets:** adaptive launcher icon foreground/background + monochrome Android 13+ mark; splash mark; wordmark horizontal; 1:1 generic no-cover placeholder; thematic Discover hero; empty-library, no-downloads, network-error and permissions illustrations; creator profile placeholder; Play listing icon (512×512) + feature graphic (1024×500) + 2–8 genuine portrait screenshots captured from the app; privacy/support links and credit/attribution text. Generate unique originals. See `docs/brand/MORNICA_BRAND_SYSTEM.md` and included vector concepts.
**No deceptive production polish:** generated album covers/creator art can show DEMO prominently; no artist's likeness/name/title or invented licensed album artwork. Never make demo content look like a real Nigerian artist contract.

## 4. Behavioral design/NLP contract (effective, not manipulative)
**Intent and user agency:** optimize quick first play, successful offline task, recovery from network failure and user confidence, never time-on-app at any cost. Use goal gradients and small positive feedback ONLY after actual success. Undo/Cancel and predictable exits wherever practical.
**NLP/microcopy rules:** short active verbs, consistent labels, familiar Nigerian English, comprehension at low literacy levels, accessible error recovery, bilingual code-switching only where it helps. Prefer `Play`, `Save offline`, `In your library`, `Wi-Fi downloads`, `Open YouTube`. Local idioms like `No data? No wahala` acceptable as optional helper text, not a replacement for precise error/action labels.
**Do not:** use emotional vulnerability profiling, dark patterns, fake scarcity/urgency, false trending charts, disguised ad/sponsored placement, shame messages, forced notifications, hidden recurring payments, fabricated popularity, unauthorized contact sharing or misleading `FREE` labels.
**Evaluation:** measure task success, time-to-first-play, number of taps to find Downloads, accidental exits, back-stack errors, understood data-cost warnings, consent comprehension, TalkBack completion and A/B copy comprehension with genuine testers. Avoid covert tracking or user-specific psychological predictions.

## 5. Two-hour testing candidate execution order (strict timebox, no guaranteed delivery)
**0–15 min — Source truth and baseline**: identify actual latest PR #14, clean worktree, SDK, target emulator, current tests and exact app package/version; record before screenshots. Stop if build cannot be reproduced.
**15–35 min — IA and brand map**: choose *one* differentiated screen flow (Home/Explore/My Music/Search proposal), make original design tokens, icons and 1 coherent mini-player layout; evaluate scope vs 2-hour gate. Do not redesign every screen if it threatens baseline.
**35–75 min — Minimal high-value Compose patch**: upgrade first-run/Home/My Music hierarchy, make download eligibility obvious, cleaner mini-player and reachable Downloads, improve consistent back behavior and readable copy. Use reusable components/Material 3. In a second isolated slice, add splash/logo placeholders only if assets have passed review. Never introduce new unlicensed catalog/stream.
**75–100 min — Test/inspect**: actual `./gradlew --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --console=plain`, targeted Compose navigation/semantics tests, API 37 emulator touches, 360dp/412dp, 1.3x font, dark-mode, airplane/cache, screenshot set. Report exact results and failure count. CI may be missing; label it honestly.
**100–115 min — Independent review/gate**: QA other than code author inspects diff, app permissions, UI/UX, media entitlements, screenshot vs commit provenance, live package/signing comparison, secret scan, crash/ANR and store metadata. Reject unexpected permissions or false promises.
**115–120 min — Distribution decision:** if gate PASS, build and checksum an explicitly labelled **internal test candidate**. Do not publish or upload without authorization; if gate fails, stop and keep the previous verified test artifact. No late emergency hacks.

## 6. QA stop-the-line rules
**P0 BLOCK:** crashes, missing app launch, media outside entitlement, lost local library, data corruption, secrets, bad signing identity, non-playable local files, YouTube audio extraction, failing unit tests/lint/build, unknown origin or malicious third-party dependencies.
**P1 BLOCK for testers:** broken navigation, inaccessible playback controls, 2+ overlapping bottom bars, downloads falsely labelled complete, stream-only offers functional Download, permission dead-end, OS back unexpectedly exits app, real user data in screenshots, remote-image rights unknown.
**Release confidence:** emulator interaction ≠ real phone audio or Play policy approval. QA must distinguish API 37 screenshot vs actual physical-device playback, including the owner's Infinix if explicitly authorized. Record font sizes, screen widths, observed lag, and files with screenshot provenance.
**No silent lint bypass:** independent reviewer examines existing `UnsafeOptInUsageError` disable and actual Media3 annotation sites; do not add more lint suppressions merely for a green report.

## 7. Distribution channels and limits
**Google Play internal testing:** preferred first. Eligibility, signing/app ID lineage, app bundle/permissions, privacy and data-safety declarations, content rating, tester list and policy prerequisites must be checked in Play Console. Internal testing can be fast but actual availability/review is not guaranteed. For certain newer personal accounts, production requires 12 opted-in closed testers for 14 continuous days; check owner account applicability. No public production claim.
**Samsung Galaxy Store:** viable second channel after distinct seller registration, device-compatibility testing, metadata and Samsung approval. Store submission ≠ publication; needs account-owner approval.
**Huawei AppGallery:** evaluate Huawei-specific services/compatibility and submission review separately; not automatically compatible with Google-specific integrations.
**Direct Android APK for invited QA:** possible only from a securely hosted, checksum-verified, properly signed and legally redistributable build, with explicit consent; do not use an unmanaged public APK mirror or change the package signing lineage. Provide feedback channel and build label.
**Amazon Appstore for Android phones:** DO NOT schedule: new Android-phone app submissions ended in 2025.
**No two-hour public launch promise:** avoid posting listing graphics or app content before the actual build and rights are verified. External store review and account-specific prerequisites determine timing.

## 8. Handoff and signoff
Each loop must return: objective; worktree/branch/HEAD/dirty state; changed files; screenshot paths and device/build provenance; unit/lint/build/instrumented commands+exit codes; playback/cache/right-gate evidence; Play/signing/privacy policy status; independent reviewer; P0/P1 unresolved; immutable artifact SHA-256; release **GO / NO-GO / INTERNAL TEST ONLY**.
**Signoff owners:** Antigravity builder (implementation), Hermes (audio architecture compatibility), independent reviewer/Codex (quality), app owner (final store upload/publication). No self-certified acceptance.

## Primary standards/refs
- Apple Music navigation and search as HIGH-LEVEL references: https://support.apple.com/guide/iphone/get-started-with-music-iph850be8527/27/ios/27
- Apple HIG design principles: https://developer.apple.com/design/human-interface-guidelines/design-principles/
- Google Play impersonation: https://support.google.com/googleplay/android-developer/answer/9888374
- Google Play internal/closed requirements: https://support.google.com/googleplay/android-developer/answer/14151465
- Play feature graphic: https://support.google.com/googleplay/android-developer/answer/9866151
- Samsung Seller Portal: https://developer.samsung.com/galaxy-store/prepare.html
- Amazon Android-phone app submissions sunset: https://developer.amazon.com/apps-and-games/blogs/2025/02/upcoming-changes-to-amazon-appstore-for-android-devices-and-coins-program

**Definition of complete:** not `we made a pretty screen`; complete means verified, accessible, legally distributable Mornica test candidate with approval gate, truthful screenshots and release-specific traceable evidence.
