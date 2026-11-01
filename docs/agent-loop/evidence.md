# Verified evidence and remaining gates

Date: 2026-11-01
Objective: Loop 1 shared-header repair
State: AUTOMATED_GATE_PASS / DEVICE_GATE_PENDING

## Source evidence
- Repository: `SHADOWSPARK-TECHNOLOGIES/Moronto-music-app`.
- Baseline commit: `320cb67b8051153cdf505e0d021b88e602b8420c`.
- Candidate branch: `agent/mornica-loop1-header-20261101`.
- Baseline `MorontoTopHeader` had an unconstrained branding `Column` plus four adjacent icon buttons, creating a narrow branding width. The original `1390.mp4` recording showed title characters stacked vertically.
- Candidate constrains brand `Column` with `Modifier.weight(1f)`, sets title and subtitle `maxLines=1`, `softWrap=false` and ellipsis, and places Timer/Equalizer/Info items in an overflow `DropdownMenu`.
- Verified callback equivalence in baseline versus candidate: AI Discovery calls `onAiDiscoveryClick`; Timer and Equalizer both call `onEqualizerClick`; About calls `onInfoClick`.
- `app/build.gradle.kts` sets debug application ID to `com.shadowspark.morontomornica` (no `applicationIdSuffix`); manifest exposes `.MainActivity`. A debug APK could replace an installed app with the same package ID on the phone; do not install there until separately authorized.
- `LocalAudioPlaybackService` lives inside `domain/PlaybackRouter.kt` and is declared in the manifest as `.domain.LocalAudioPlaybackService`.
- SDK configuration: `sdk.dir=/home/moronto/Android/Sdk` in ignored `local.properties` of the isolated worktree.

## User-provided local build evidence
- Candidate checkout at `d7ffd6f`, including test fixes `488c401` and `5a50c4e`.
- Command: `./gradlew --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --console=plain`.
- Result: `BUILD SUCCESSFUL in 1m 21s`, `Gradle exit code: 0`.
- Gradle tasks `:app:compileDebugUnitTestKotlin`, `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug` succeeded.
- 55 actionable tasks: 7 executed, 48 up-to-date. SDK XML schema version warning was nonfatal.
- Test case count, warning count, APK hash and on-device screenshot evidence are not yet supplied.

## Device provenance / privacy safeguard
- ADB serial `0826317000000025` reported Infinix X6877 as a connected `device`.
- An earlier ADB dump showed `team.opay.pay/.wallet.TransactionActivity` in foreground. An agent then started the existing `com.shadowspark.morontomornica/.MainActivity`; "its current task has been brought to the front" is NOT proof candidate APK was installed. Its screenshot cannot prove a candidate UI fix.
- Before capturing any screenshot confirm foreground component is the *candidate test app*, never an unrelated application or financial screen; avoid recording personal information.

## Outstanding gate
1. Collect JUnit XML totals, lint report summary, debug APK SHA-256, and package/signature identity.
2. Discover or create a separate Android emulator; check no relevant installed app will be overwritten. Install the candidate only on that isolated emulator.
3. Verify the header, overflow actions and navigation at 360dp, 412dp and increased system font scale, saving screenshots with dimension+font metadata.
4. Test expanded player and Media3 playback without assuming a UI-only repair proves runtime compatibility.
5. Compare candidate against local UNCOMMITTED `MornicaApp.kt`, `MornicaScreens.kt` and other newer changes before merging. Original branch must remain unmodified.
6. No PR merge, no release, no Play Console operation without further authorization.

## Status
Automated build gate PASS. Visual/device and local-integration gates PENDING. Loop 1 full PASS is not yet justified.
