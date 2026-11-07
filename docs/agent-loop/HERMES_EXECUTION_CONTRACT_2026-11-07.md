# HERMES EXECUTION CONTRACT: MORONTO MORNICA

**Contract ID:** MM-HERMES-2026-11-07  
**Owner:** ShadowSpark Technologies  
**Agent:** Hermes (orchestrator), with Antigravity/Builder and an independent Verifier  
**Scope:** Native Android Moronto Mornica only  
**Status:** Engineering execution authorized; merge, public release, billing, production publication, physical-device replacement and Play Console changes NOT authorized.

## 1. Product and authoritative source
- Repository: SHADOWSPARK-TECHNOLOGIES/Moronto-music-app.
- Application ID: com.shadowspark.morontomornica. Never change app ID, signing identity or release/version train without explicit owner approval.
- Kotlin + Jetpack Compose + Material 3 + Media3/ExoPlayer/MediaSessionService + MediaStore; Android app is the target, NOT a Next.js/Firebase rebuild.
- Development baseline: agent/antigravity-mornica-launch-contract-20261019 (verified head 320cb67b8051153cdf505e0d021b88e602b8420c on 2026-11-07).
- Header candidate: agent/mornica-loop1-header-20261101 (PR #5, draft, verified head 7fc86d93565f515a86d31092f0ef4e09e00474bc).
- main currently has README/LICENSE only. Do not substitute main as the implementation baseline.
- Read app/build.gradle.kts, app/src/main/AndroidManifest.xml, docs/PRODUCT_SPEC.md, docs/agent-loop/{objective,status,evidence}.md and AGENTS.md. If AGENTS.md or an older runbook proposes a web application, follow the native Android build and PRODUCT_SPEC for Mornica; log any contradiction.
- The original owner development worktree may contain newer, uncommitted app changes. Preserve them in place. The agent MUST NOT reset, stash, clean, force-push, overwrite, cherry-pick into, or modify it without review.

## 1A. Product decision superseding older YouTube-embed instructions (2026-11-07)
- Read `docs/agent-loop/MORNICA_NIGERIA_FIRST_STREAMING_OFFLINE_SPEC.md` as the authoritative current product direction.
- **One native Mornica-branded audio player** supports user-owned files, directly licensed streams and explicitly licensed offline downloads. No embedded YouTube playback in Mornica. Do not create fake Mornica playback backed by hidden YouTube streams.
- Optional discovery of YouTube videos means a clearly labeled `Watch on YouTube` external intent, not in-app playback. Do not include YouTube API services in the first Mornica release without an explicit separately reviewed feature decision. If enabled in the future, follow source attribution, APIs, privacy and brand rules; Terms-only disclosure is insufficient.
- Stream/download rights are not interchangeable. Implement downloads only from sources explicitly licensed for offline saves, never Snaptube-style YouTube stream extraction. Copy and UI must reflect entitlement availability.
- Nigerian-first localization: simple English with optional naturally phrased Naija/Pidgin variant. Favor understandable labels such as `Find your next jam`, `Save for offline`, `Downloads`, `No data? No wahala.`, and `Wi-Fi downloads only`; do not falsely advertise universal song downloads.
- When this decision conflicts with older `docs/PRODUCT_SPEC.md`, `AGENTS.md` or `docs/ANTIGRAVITY_RUNBOOK.md`, prioritize this newer release contract and log the inconsistency. Do not overwrite source implementation blindly.

## 2. Immutable media, rights and safety requirements
1. Local and rights-cleared audio: MediaStore and Media3/ExoPlayer. User-owned local files and licensed Mornica catalog songs may play offline and in the background via a foreground MediaSessionService when authorized. Honor audio focus, noisy/headset disconnect, system controls, playback lifecycle, playlists, seek bounds, shuffle/repeat and persistent state where genuinely implemented.
2. YouTube: no embedded player, hidden player, YouTube-proxied Media3 audio, downloaded/cached streams, stream extraction, undocumented playback URLs or background YouTube audio. If a source has an optional video link, show an explicitly labeled external `Watch on YouTube` action opening YouTube or a browser; no YouTube API use without a distinct, compliant feature review.
3. Source isolation: the native player accepts MediaStore, licensed HTTPS catalog tracks and rights-authorized cached downloads only. External YouTube links leave the Mornica playback service and do not transfer video IDs, stream endpoints or extracted media into Media3.
4. Catalog: rights and source metadata must record streamAllowed/offlineAllowed, territory, expiry and origin. Fail closed for unknown licenses. Use an offline index/download cache only for permitted tracks; no hardcoded secrets, YouTube metadata dependency or fake claims of catalog availability.
5. Creator Hub: honest demo and rights-attested independent artist profiles. User-initiated commercial inquiry only; no fabricated verified profiles, partnerships, receipt, payment, or consent. Do not enable payments without a separate approved design, compliance review and live test.
6. No unlicensed audio, copied private user data, unauthorized account access, or exposure of signing keys and API credentials. Keep passwords/keys off GitHub and out of logs.

## 3. Engineering control and loop budget
- Hermes orchestrates Observe -> Plan -> Delegate Builder -> Execute -> Independent Verifier -> Quality Gate -> Reflect.
- Run ONE clearly defined user-facing objective at a time, with up to TWO non-overlapping writers; independent Verifier cannot approve code it wrote.
- Maximum 8 attempts per objective; STOP after 2 attempts with no demonstrable progress, the same repeated failure, unsafe side effects, absent evidence, missing credentials, or uncertain ownership.
- Begin with read-only workspace/branch/remote/status inspection; create a clean isolated worktree/branch AFTER inspecting user modifications; do not operate on a dirty user's worktree.
- Use only repository-declared Gradle wrapper and required SDK/JDK toolchain (inspect current files and java -version first). Do not silently install SDK components, raise cloud costs or change dependency versions for convenience.
- Each loop must record branch, HEAD, exact changed files, tests, logs, screenshots/device provenance where appropriate, observed failure, final PASS/FAIL/BLOCKED and next action in a dedicated branch report. Distinguish old proof from current test evidence.
- No automatic PR merge, force push, production deploy, release upload, signing-key change, Play Console action, cloud billing change, or replacing installed app on owner's Infinix. Require a separate explicit approval for each release-level action.
- GitHub Actions failures may be infrastructure/billing-related; do not diagnose Kotlin from a missing job log. Prefer a reproducible Hermes-hosted Android verification command when CI is inaccessible.

## 4. Bounded milestone sequence and objective acceptance
### Loop 0: Source and environment truth
- Verify git remote, HEAD/branches, clean/dirty working trees, SDK/JDK/Gradle, emulator availability, existing release certificate/version and active Play track WITHOUT changing them.
- Make a source-to-feature inventory. Mark every feature IMPLEMENTED / PARTIAL / MISSING / UNKNOWN with code and test paths. Flag Next.js/Android documentation drift.
- Exit: dated evidence-backed baseline, blocked/not-blocked report and first scoped task; no files overwritten.

### Loop 1: UI header, responsive navigation and PR #5 integration
- Inspect PR #5, compare with newer owner-modified MornicaApp.kt and MornicaScreens.kt; do not overwrite uncommitted changes.
- Fix only confirmed accessibility/layout defects: header at 360dp/412dp and 1.3x font, overflow Timer/Equalizer/About, search, readable bottom nav, expanded player, truthful labels.
- Exit: verified screenshots with APK/build/branch identity, touch-navigation checks, current automated checks, and independent review. Keep PR draft until owner approval.

### Loop 2: Offline library and Media3 reliability
- Audit scan and permission-denied cases across Android API versions; Songs/Folders/Albums/Artists/Playlists and demo fallback.
- Validate playback controls, persist queue/favorites/playlists where required by PRODUCT_SPEC, audio focus, headset unplug, interruptions, background, lock screen, recovery and no duplicate players.
- Exit: unit/component tests plus isolated emulator/consented device evidence for all critical transitions; no local track URI contains a YouTube URL.

### Loop 3: Native streaming, licensed offline downloads and optional external discovery
- Implement or verify a real licensed catalog (or clearly marked rights-cleared demo fixtures) with rights metadata and a genuine native Mornica player.
- Build Android Media3 DownloadManager, DownloadService, DownloadIndex and CacheDataSource only for tracks licensed for offline use. Include Wi-Fi preference, progress, pause/resume/cancel/retry, storage handling, license expiry, connectivity interruptions and accurate Downloaded status.
- Test streamAllowed/offlineAllowed enforcement, attribution, territory, authorization and airplane-mode playback with **no streaming fallback**. No fake-download button.
- Optional YouTube link is externally launched and explicitly labeled. No embedded player, YouTube media ingestion, cached YouTube video/audio, YouTube background playback or stream extraction.
- Exit: demonstrated authorized stream-to-offline journey, correct blocked behavior for non-downloadable tracks, independent permissions/rights checks and no YouTube source in Media3.

### Loop 4: Creator Hub and Nigerian-first listening experience
- Implement/verify truthful creator cards, rights attestations, visible DEMO status, opt-in contact intake with validation, explicit consent and data-handling notes. Include simple Nigerian-friendly strings for Home, Search, Player, Downloads, low-data/airplane mode and licensing exceptions.
- Exit: no fake feature/button/verification, no automatic outreach or payment; copy accessibility/localization reviewed on 360dp/412dp screens, privacy/rights evidence and consent checks.

### Loop 5: Product quality, performance and reliability
- Sweep UX across 360dp/412dp, font scale, dark mode, permission rejection, no connectivity, empty library, repeated rotations, audio interruptions and navigation.
- Run independent security/privacy/accessibility review; measure startup and basic performance if a supported emulator/device is available. Document genuinely unsupported tests as BLOCKED.
- Exit: critical and high-severity defects fixed and independently retested.

### Loop 6: Release-candidate engineering (not publication)
- Execute ./gradlew --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --console=plain; additionally run available instrumentation/connected tests on a SAFE isolated emulator.
- Inspect JUnit XML and lint reports; collect APK SHA-256, application ID, versionCode, permissions, service types, source rights and dependency/license inventory.
- Prepare signed AAB only if the owner provides secure existing signing access and explicitly authorizes signing. Do not transmit signing files or secrets. Avoid installing over existing Play app.
- Independently verify current official Play policies and account-specific declarations before recommending any production action.
- Exit: candidate artifact(s), proof package, redacted change log, rollback and release checklist; remain NOT RELEASED.

### Loop 7: Owner acceptance and closeout
- Provide demo video/screenshots without personal notifications or payment screens, reproducible build command, working vs deferred feature table, severity-zero blocker list, privacy/policy checklist, revenue assumptions, operator handoff and signed-off next action.
- Final COMPLETE only if every required acceptance gate passed with evidence and the owner accepts; otherwise PARTIAL or BLOCKED with exact blockers. Publishing is separate.

## 5. Mandatory check matrix
| Check | Required evidence |
| --- | --- |
| App provenance | repo, branch, HEAD, Gradle manifest, package/version/signing fingerprints (safe metadata only) |
| Build | complete commands, exit codes, build logs and resulting APK hash |
| Tests | test counts, failed tests, XML report paths, coverage only if measured |
| Lint/accessibility | lint report and screenshots/semantics/TalkBack results if actually tested |
| Playback | local foreground/background, noisy, seek, repeat/shuffle, permissions, queue integrity |
| Sources & external video | all Mornica player URLs owned/licensed; clearly labeled external YouTube intents only; no YouTube embeds, audio extraction, downloads or hidden playback |
| Native stream/download | catalog license and entitlement, progress/retry, Wi-Fi choice, offline full-play airplane-mode test, download denial for non-licensed tracks |\n| Creator/business | demo rights, user consent, truthful lead capture, no unauthorized payments |
| Changes | diff, reviewer identity, clean status, no secrets or unreviewed conflicts |
| Release gate | explicit owner authorization required; production status unchanged |

## 6. Agent report contract
At the end of EVERY loop return EXACTLY: Objective; Status (PASS/FAIL/BLOCKED); Repo/Worktree/Branch/HEAD; pre-existing dirty files; changed files and diff summary; commands and exact results; tests and artifact paths/hash; independent review verdict; risks/unknowns; next smallest action; human approval needed. Record cost, elapsed effort and attempts used if known. Do not claim tests ran if they did not.

## 7. Launch order
1. Read this contract, the Nigeria-first streaming/offline spec and .agents/skills/mornica-loop-engineering/SKILL.md.
2. Execute the kickoff instructions in docs/agent-loop/HERMES_KICKOFF.md in read-only mode.
3. Stop and report if no authenticated access to the actual Android repository / SDK / emulator; do not infer from stale notes.
4. For clean isolated worktrees only, run bash scripts/hermes-mornica-verify.sh for a real verified test/lint/build report.
5. Continue bounded loops in order, creating drafts/commits on isolated branches, never merging or releasing without human approval.

Contract is an instruction and scope document, not proof that Hermes has received it or started running.
