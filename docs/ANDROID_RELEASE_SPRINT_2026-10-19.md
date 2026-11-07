# MORONTO MORNICA — October 19, 2026 Google Play Sprint

> Status: IMPLEMENTATION NOT YET VERIFIED. This is the execution contract, not proof that an app or Play release exists.

## Mission and precedence
Launch a **real native Android MVP** on Google Play **internal testing** as soon as qualified and feasible on October 19, 2026, with production publication contingent on Google Play eligibility and review. This brief supersedes earlier web-first planning for the immediate Android release slice; do not overwrite web planning silently. Google Antigravity is the implementation tool.

## Reality and platform gates
- At planning, GitHub repository `SHADOWSPARK-TECHNOLOGIES/Moronto-music-app` main contains README and LICENSE only, while this document is on a preparation branch. Inspect the real local checkout for newer uncommitted code before deciding to scaffold.
- Google Play's ordinary new Android phone apps target Android 16 / API 36 or higher as of Aug 31 2026.
- Prepare a signed **Android App Bundle (.aab)** with Play App Signing, not an unsigned APK advertised as released.
- New personal Play developer accounts created after Nov 13 2023 need a closed test with 12 opted-in testers for 14 continuous days and production access approval. Google review can take several days.
- Play Console account type, verification, package-name availability, keystore, Google Cloud/YouTube credentials, and whether production access is granted are UNKNOWN until the operator verifies them.
- Google Play requires true functionality and an accurate privacy policy and data-safety declarations for applicable tracks. Internal-only testing has different Data Safety form requirements; do not generalize its exemption to closed/production.

## Product cut — minimum genuinely useful Android app
1. Kotlin + Jetpack Compose + Material 3 with original MORONTO MORNICA brand; test actual phone screen sizes.
2. Android MediaStore for local audio discovery; request only necessary permission with meaningful explanations; no broad MANAGE_EXTERNAL_STORAGE.
3. Media3 ExoPlayer and MediaLibraryService / MediaSessionService as appropriate for background playback of **user-owned local files only**; demonstrate play/pause/seek, next/previous, repeat, shuffle, notification/lock-screen controls, audio focus, headset removal and empty/no-permission states.
4. A searchable local library with artist/album/folder navigation. Persist playlists and queue state where feasible with Room; keep database schema minimal.
5. Distinct YouTube Discover/Watch surface: only official YouTube Data API metadata and permitted visible embedded player with correct identity/referrer/controls; if API keys, embed compatibility or terms cannot be verified, use clearly labeled external YouTube deep links as a safe meaningful first-release fallback. Never rip, separate, background-play, or download YouTube media; never put YouTube viewing behind a paid subscription or remove YouTube ads.
6. Use original/rights-cleared visual assets. Do not bundle unlicensed recordings or mock popular-artist catalogs as genuine content. App must be usable with zero local audio without crashing.
7. Include an in-app link to a real, published, accurate privacy policy before public submission. No accounts, analytics SDKs, subscriptions, upload servers or unnecessary dangerous permissions in MVP.
8. Never claim Google endorsed or promoted the project.

## Stack and release choices
- Kotlin Gradle DSL, Compose Material 3, AndroidX Media3, Room only if justified. Prefer compatible pinned stable versions from official release notes. Do not introduce React Native/Flutter/PWA just to move faster without measured reason.
- Build using installed JDK/Android SDK verified from actual local environment; never install global dependencies or accept new cloud costs silently.
- Use local Gradle wrapper created by the build environment, version-controlled appropriately. Verify `./gradlew --version`, `./gradlew lint`, `./gradlew test`, `./gradlew assembleDebug`, `./gradlew bundleRelease` (with secure signing configuration only when provided).
- Do not publish or print keystore private keys, tokens or passwords. Do not create fake signing profiles.
- Preserve original repo files/tests. Use feature branch and tightly scoped changes. Stop after two identical failures. Do not edit unrelated repositories.

## Roles / delegation
- Architect (research/read-only): baseline inventory, package identifier and architecture proposal, file plan, API 36 obligations, video integration policy.
- Builder: implementation only inside approved repo/worktree and files; max 2 writers.
- QA (independent): real emulator/device smoke checks, SDK/API compliance, permissions, privacy, YouTube player visibility, invalid/missing files, battery/background behavior, CI.
- Release operator: AAB signing, Play Console internal testing, store listing, screenshots, content rating, app access, data safety and privacy policy, checklist. Must obtain operator signoff before external submissions, billing, or production.
- Product/growth: legitimate artist discovery, rights validation, no fake reviews/subscribers/earnings. Defer monetization activation.
- REA: optional read-only inspection of authorized APK for permissions, application structure and release diagnostics; never rely on REA instead of official Android lint/tests.

## Antigravity master execution prompt (paste into interactive agy)
```text
/goal
You are the MORONTO MORNICA Android Release Engineering Lead, operating on 19 October 2026.

Read AGENTS.md, docs/PRODUCT_SPEC.md, docs/ANTIGRAVITY_RUNBOOK.md and docs/ANDROID_RELEASE_SPRINT_2026-10-19.md. The current sprint document is authoritative for the Android MVP; prior web-first specs must not be mistaken for code that already exists.

PHASE 0: VERIFY. Print pwd, git origin, branch, status, HEAD, tree; verify JDK, Android SDK, Gradle wrapper, device/emulator availability and current Google Play API requirements. Inventory any existing Android code; if none, declare that and propose the smallest original Kotlin/Compose scaffold. Check actual model and available subagents. Do not ask to reinstall Hermes or expose secrets.

PHASE 1: PLAN. Give a small implementation architecture and exact files, dependencies, acceptance tests and rough effort. Explain why the chosen native Android approach gives real offline functionality and avoids a low-value webview. Distinguish YouTube external links from verified, compliant embedded playback. Obtain approval for the plan before writing a new subsystem.

PHASE 2: BUILD in a new feature branch/worktree, with no unrelated edits. Ship a functional local player with MediaStore, Media3 playback service, playlists, search, Material 3 polish and truthful zero-content states. Target API 36+. YouTube Discover/Watch must either use a tested official visible player or a clearly labeled external-link fallback without audio extraction or paid video access.

PHASE 3: VERIFY. Run actual lint, unit tests, compile and debug build. If environment contains emulator/device, test foreground/background playback, audio focus, headphones, permissions, empty library, rotating phone and loss of network. Record outputs and failures. Have independent reviewer inspect changed files, dependency licenses, secrets, Android API 36 compatibility, privacy and YouTube rules.

PHASE 4: RELEASE READINESS. Create a release-ready checklist: package ID, version code/name, source rights manifest, published privacy policy URL, real data-safety disclosures, store descriptions and screenshots from actual running app, app icon and feature graphic, signed AAB via privately secured upload keystore, and Play Console internal test eligibility. Upload to internal testing ONLY after operator approves exact build and Play Console account. Do not claim production release unless Google Play confirms it.

EXECUTION LIMITS: Maximum two concurrent writers. Stop after two identical failures and report exact logs. Distinguish VERIFIED/INFERRED/UNKNOWN/BLOCKED. Never auto-spend, auto-publish, or overwrite other projects. Present a concrete next task at each gate.

OUTPUT: absolute verified paths, branch+SHA, changed files, test command+exit code, APK/AAB path if actually built, UI evidence, unresolved rights/YouTube issues, Play Console eligibility, and whether internal/closed/production tracks are accessible.
```

## Go/No-Go checklist for today
- [ ] Exact local Git checkout and branch verified
- [ ] Android source exists and compiles targeting API 36+
- [ ] Real playback demonstrated on emulator/device
- [ ] YouTube integration compliant or honestly replaced with external-link fallback
- [ ] Lint, unit tests, release build success with logs
- [ ] Privacy policy and Data Safety disclosures match actual app
- [ ] Upload key stored safely, signed release .aab verified
- [ ] Google Play developer account verified and eligible internal track visible
- [ ] Operator approves uploading exact .aab
- [ ] Play Console confirms internal test release submitted/available
- [ ] Production status accurately recorded; 14-day test and Play review are external constraints

## Source references (official)
- https://developer.android.com/google/play/requirements/target-sdk
- https://support.google.com/googleplay/android-developer/answer/14151465
- https://support.google.com/googleplay/android-developer/answer/9859751
- https://developer.android.com/studio/publish/app-signing
- https://developer.android.com/media/media3/session/background-playback
- https://developers.google.com/youtube/terms/developer-policies-guide
- https://support.google.com/googleplay/android-developer/answer/10144311
- https://support.google.com/googleplay/android-developer/answer/10787469
- https://antigravity.google/docs/cli/headless/
