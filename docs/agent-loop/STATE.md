# Mornica — Agent State (persistent working memory)

Maintained by agents across sessions. **Update after every verified change.** If you cannot see
proof (branch, SHA, passing command output), mark it stale — a note is not evidence.

## Active loop
Nigeria-first streaming/offline plan: 1 = access policy (done) · 2 = native Media3 online playback
(done) · 3 = offline downloads (loop 3 DONE: download side; offline-PLAYBACK device proof PENDING — see ledger) ·
4 = player + Downloads UI (NEXT) · 5 = airplane-mode & background testing · 6 = independent QA +
release candidate (no publish without owner approval).
Authoritative spec: `docs/agent-loop/MORNICA_NIGERIA_FIRST_STREAMING_OFFLINE_SPEC.md`.

## Last verified state (2026-11-07)
- Local branch (NOT pushed, no upstream): `agent/mornica-gate-consolidation-20261107`
- Stack: hermes-execution base → PR #10 commits → merge of PR #9 → `dd4619a` gate consolidation →
  `7c591c5` loop 2 → `17833fc` execution contracts + CatalogSource seam → the Loop-3 commit (offline
  downloads). See `git log --oneline` for the exact loop-3 SHA.
- Working tree: clean (consolidation worktree `/home/moronto/.hermes/cache/scratch/mornica-wt-consolidate`)
- Repo: SHADOWSPARK-TECHNOLOGIES/Moronto-music-app (remote origin). Owner's checkout is
  `/home/moronto/StudioProjects/Moronto-music-app` (on the old antigravity branch; DO NOT disturb).

## Verification ledger (as of loop 3)
- [x] unit-verified: `:app:testDebugUnitTest` 47/47 (gate 7, playback 7, catalog-download 8,
      download-store 6, acceptance 12, accessibility 7)
- [x] device-verified: `connectedDebugAndroidTest` 2/2 on API 37 x86_64 emulator:
      CatalogDownloadInstrumentedTest (authorized demo track → DownloadManager COMPLETED; stream-only
      refused, never in index) AND CatalogOfflinePlaybackInstrumentedTest (download → bytes durably in
      Media3 cache keyed by URI; stream-only refused)
- [x] build-verified: `:app:assembleDebug` produces debug APK
- [ ] PENDING offline PLAYBACK (radio truly off): app cannot toggle airplane mode (Android protects
      the AIRPLANE_MODE broadcast even with WRITE_SECURE_SETTINGS granted; dead-upstream CacheDataSource
      probe surfaced that cache-serving to playback needs a follow-up investigation). Bytes verified in
      cache but rendering-with-no-network is NOT yet claimed.
- [ ] unverified: audible real-speaker (human); real licensed catalogue (present is demo fixtures);
      Downloads/player UI (loop 4); lint on this branch.

## Loop 3 implementation notes (important for the next worker)
- Media3 1.5.x: download classes live in `androidx.media3.exoplayer.offline` (NOT `.download`).
- A standalone `DownloadManager` does NOT auto-run downloads — drive them through `CatalogDownloadService`
  (foreground `DownloadService`). `DownloadManager.setRequirements(Requirements(NETWORK))` lets downloads
  start on any (even metered) network; tighten to Wi-Fi-only in a shipping build.
- `DownloadRequest` has no `setMediaId`; builder is `DownloadRequest.Builder(mediaId, Uri)`.
- Do NOT set `customCacheKey` on download requests — playback's CacheDataSource keys by URI, so both must
  share the URI key for offline reads.
- `DownloadStore` is a JSON store (no Room) — a deliberate, flagged deviation from the Loop-3 contract;
  switch to Room behind KSP only if a release candidate needs it.
