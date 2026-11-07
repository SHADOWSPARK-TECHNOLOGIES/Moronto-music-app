# Mornica — Agent State (persistent working memory)

Maintained by agents across sessions. **Update after every verified change.** If you cannot see
proof (branch, SHA, passing command output), mark it stale — a note is not evidence.

## Active loop
Nigeria-first streaming/offline plan: 1 = access policy (done) · 2 = native Media3 online playback
(done) · 3 = offline downloads (NEXT) · 4 = player + Downloads UI · 5 = airplane-mode & background
testing · 6 = independent QA + release candidate (no publish without owner approval).
Authoritative spec: `docs/agent-loop/MORNICA_NIGERIA_FIRST_STREAMING_OFFLINE_SPEC.md`.

## Last verified state (2026-11-07)
- Local branch (NOT pushed, no upstream): `agent/mornica-gate-consolidation-20261107`
- HEAD: `7c591c5` feat(android): loop 2 - wire licensed catalog gate into native Media3 playback
- Stack: hermes-execution base → PR #10 commits → merge of PR #9 → `dd4619a` gate consolidation → `7c591c5` loop 2
- Working tree: clean (consolidation worktree `/home/moronto/.hermes/cache/scratch/mornica-wt-consolidate`)
- Repo: SHADOWSPARK-TECHNOLOGIES/Moronto-music-app (remote origin). Owner's checkout is
  `/home/moronto/StudioProjects/Moronto-music-app` (on the old antigravity branch; DO NOT disturb).

## Verification ledger (as of loop 2)
- [x] unit-verified: `:app:testDebugUnitTest` 33/33 (gate 7, playback 7, acceptance 12, accessibility 7)
- [x] device-verified: `connectedDebugAndroidTest` 1/1 (9.1s) `LicensedCatalogPlaybackInstrumentedTest`
      on API 37 x86_64 emulator — gate auth → Media3 → live network audio, router reported isLocalPlaying
- [x] build-verified: `:app:assembleDebug` produces debug APK
- [ ] unverified: audible on real speaker (needs human test); real licensed catalogue; offline
      downloads (loop 3); player/Downloads UI (loop 4); lint on the consolidated branch.

## Emulator quick-start (avoid rediscovering)
- AVDs live under `/home/moronto/.config/.android/avd` (XDG). Launch with
  `ANDROID_AVD_HOME=/home/moronto/.config/.android/avd` or the emulator will say "Unknown AVD".
- AVD names: `mornica_loop2` (created 2026-11-07), pre-existing `Medium_Phone_API_37.0`.
- Boot: `emulator -avd mornica_loop2 -no-window -no-snapshot -no-boot-anim -gpu swiftshader_indirect -no-metrics`.
- Media3 rule: create/release ExoPlayer on the MAIN thread — wrap in `InstrumentationRegistry.runOnMainSync`.
