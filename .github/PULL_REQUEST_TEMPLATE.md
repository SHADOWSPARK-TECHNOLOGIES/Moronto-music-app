<!-- Verification Ledger must be completed for every PR. A build passing proves buildability only;
a test passing proves only the behavior that test observes. Mark plans/stale explicitly. -->
# Description
<!-- What does this change do, and why? Link the Sprint Contract / spec. -->

# Verification Ledger
```
[DEVELOPMENT VERIFICATION v1]
Goal: <current user outcome>
C1 <claim> proof: <observable> check: <command> status: passed|failed|stale|planned|unknown
```
Example rows (customize per change):
- unit-verified | gate denies stream-only SAVE_OFFLINE | :app:testDebugUnitTest | passed
- device-verified | authorized track reaches live playback | connectedDebugAndroidTest (LicensedCatalogPlaybackInstrumentedTest) | passed
- build-verified | APK assembles | :app:assembleDebug | passed
- unverified | audible on real speaker | needs human listen | planned

# Open gaps
- Anything that is not yet proven (e.g. "audio heard by a human", "real licensed catalogue",
  "airplane-mode download") goes here, explicitly.

# Checklist
- [ ] Worked only on assigned files/branch; no unrelated changes or test deletions
- [ ] No push, merge, deploy, signing change, or Play Console mutation
- [ ] Gate default-deny + host allowlist not weakened; no YouTube into the player
- [ ] `isLocalPlaying` not set optimistically
- [ ] Demo fixtures clearly labeled DEMO; no fabricated `rightsConfirmed`
- [ ] Unit AND device claims are backed by actual command output (paste minimal evidence)
