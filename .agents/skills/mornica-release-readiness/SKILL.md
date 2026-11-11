---
name: mornica-release-readiness
description: Use when preparing Moronto Mornica for a release candidate / Play Store review, validating rights & privacy provenance, signing, or when writing evidence/PR/release reports. Complementary to mornica-offline-ready-audit (device verification) and mornica-loop-engineering (loop contract).
---

# Mornica — release & rights-readiness (no publish)

Project skill for the Moronto Mornica Android agent. Release-candidate work is a GATE, not an action:
nothing is pushed / merged / signed-for-release / deployed / published without the owner's explicit
per-request approval.

## When to use
- Preparing a release candidate (loop 6 of `docs/agent-loop/STATE.md`): assemble a signed build,
  verify scope + rights + privacy, prepare evidence, obtain sign-off.
- Auditing that the app never hides/embeds/downloads YouTube content or fabricates a license.
- Writing the per-pr `docs/EXECUTION_LOG.md` / `docs/decisions.md` entries and the PR description.

## Invariants that must hold before ANY release claim
- **YouTube is external, clearly labeled discovery only.** Never embed, download, or stream
  youtube.com / youtu.be / googlevideo.com through the Media3 player. `LicensedCatalogRights`
  hard-blocks those hosts (host allowlist cannot grant them). The service layer re-checks and stops.
- **`rightsConfirmed` is true ONLY from an authenticated backend** confirming a real agreement. Demo
  fixtures are visibly labeled `DEMO` with `licenseReference = "DEMO-*"`. Never fabricate a license or
  a streaming/offline entitlement.
- **LicensedCatalogRights.evaluate()** runs before ANY MediaItem reaches ExoPlayer (default-deny). Do
  not weaken territory/expiry/stream/offline checks.
- **`isLocalPlaying` is never set optimistically** — only the controller's `onPlayerIsPlayingChanged`.
- Signing: release keystore comes from env / gradle props (`UPLOAD_KEYSTORE_PATH`, credentials) or the
  default path; NEVER commit secrets. A missing keystore must block a signed release, not fake one.

## The release-readiness checklist (report each as VERIFIED / INFERRED / UNKNOWN / BLOCKED)
1. Scope: only approved files/branch touched; no surprise dependency, global config, or cloud change.
2. Rights & provenance: catalog tracks are demo fixtures with DEMO labels; no unverified LICENSED claims.
3. Privacy: `docs/PRIVACY_POLICY.md` reflects current data handling; no secret in the app/APK.
4. Build: `./gradlew --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` green.
5. Device: a human or multimodal check confirms rendered UI + audible playback (a passing test or a
   structural uiautomator audit is proof of the pipeline, NOT of pixels/audio).
6. Offline: "Offline ready" only appears when a rights snapshot is persisted and
   `offlinePlaybackAllowed` returns true; download/playback proven on-device.
7. Release artifacts: signed APK/AAB produced by the unmodified release workflow; keystore present;
   no Play Console / deploy / billing / signing mutation.
8. Approval: an explicit owner sign-off is attached before merge/publish. Without it: BLOCK.

## How to validate without publishing
- Prepare the artifact locally and record its SHA-256 + the exact command that produced it.
- Keep release workflows and GitHub Actions untouched.
- If a step needs a credential/keystore that is absent or the account owner must clear a gate
  (e.g., Play Console address verification), report it as BLOCKED; do not invent a workaround.

## Reporting (from the repo's PR template)
Report unit-verified / device-verified / unverified SEPARATELY. Label evidence with file/line/command.
After two identical failures on the same cause, stop, preserve exact errors, propose a fix, and wait —
no self-asserted success, no unlimited retries. Preserve evidence in `docs/EXECUTION_LOG.md` and
decisions in `docs/decisions.md`; keep `docs/agent-loop/STATE.md` updated.
