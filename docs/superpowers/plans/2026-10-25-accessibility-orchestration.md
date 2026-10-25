# Moronto Mornica Accessibility Implementation Plan

> **For agentic workers:** Use superpowers:subagent-driven-development or superpowers:executing-plans to implement task by task. Preserve this user-authorized orchestrator and parallel specialist structure.

**Goal:** Make Android library/player controls accessible and verify a traceable release candidate; follow with Lodgist accessibility fixes.

**Architecture:** Keep Compose screens and PlaybackRouter interfaces unchanged. The orchestrator owns integration and claims; implementation, QA and deployment-readiness specialists work on disjoint scopes. Serialize shared UI edits.

**Tech stack:** Kotlin, Jetpack Compose Material3, Media3, Gradle, Robolectric.

**Spec:** Repository AGENTS.md plus accessibility findings from review of cdbb490ef2a6c722f51926c51bf962fa8d7f2a98; acceptance requirements below.

## Global constraints
- Preserve existing tests, dependencies, runtime contracts, and unrelated files.
- No production/store publication, billing change or signing-secret disclosure in this workflow.
- Source review is not runtime verification. Stop after two failures with the same cause.
- Reviewed checkout is not verified as the installed app. Separate Mornica and Lodgist evidence.

## Review focus
- Narrow screens and 200% font size retain accessible controls and readable track information.
- Parent mini-player click never steals play/pause input.
- Native Compose semantics and touch expansion must be measured, not guessed.
- No active track omits irrelevant playback actions.
- Release identity includes package, version and source/artifact provenance.

## Orchestrator and parallel lanes
| Role | Owns | Must not modify |
|---|---|---|
| Orchestrator | Task ledger, scope, integration, evidence and release decision | Unassigned product code |
| Android implementer | MornicaApp.kt and MornicaTheme.kt | Tests, build/signing configuration |
| QA specialist | New MornicaAccessibilityTest.kt; independent test results | Implementation files |
| Deployment expert | Read-only package/version/signing/CI/track inspection; later assigned release docs | Production, credentials or signing configuration |

QA defines failing regressions before implementation. Implementation and test writers may proceed concurrently only after agreeing acceptance interfaces. At most two writers; isolate worktrees and commits. Orchestrator integrates and reruns combined checks. No separate agents write the same file.

## Task 1: Regression criteria and semantics
**Modify:** app/src/main/java/com/shadowspark/morontomornica/ui/MornicaApp.kt.
**Test:** app/src/test/java/com/shadowspark/morontomornica/MornicaAccessibilityTest.kt (new).
**Consumes:** Existing public MornicaApp(router, localRepo, permission callback), PlaybackRouter and UI state.
**Produces:** Same app interfaces; accessible state/action semantics.
- [ ] Add failing Compose tests: library nodes expose Role.Tab, exactly one is selected, and clicking another changes selected state.
- [ ] Add failing tests: both favorite controls and shuffle expose checked state and update when activated.
- [ ] Add failing test: Playback position slider exposes human-readable elapsed/total time, range and SetProgress action; invoking progress reaches seek behavior.
- [ ] Run focused tests and record actual failure; setup failure does not prove a product regression.
- [ ] Use selectableGroup/selectable with Role.Tab; avoid duplicate decorative icon names. Use native toggle controls. Add slider naming/state without clearing built-in range semantics.
- [ ] Run regressions and preserve router/service acceptance tests.

## Task 2: Touch sizing and layout
**Modify:** same UI file, plus app/src/main/java/com/shadowspark/morontomornica/ui/MornicaTheme.kt. Serialize after Task 1 UI edits.
**Test:** same new accessibility suite; QA owns it.
- [ ] Add layout-bounds and pointer hit tests for a 48dp target. Measure effective Compose touch expansion separately; confirm boundary taps do not trigger the parent expansion row or adjacent controls.
- [ ] Add test proving play/pause activation changes playback without expanding the parent player.
- [ ] Reserve 48dp outer clickable control with button role; preserve 38dp visual pill inside if desired. Set shared minimum token to 48dp.
- [ ] Test no-active-track state and narrow/large-font layout. Treat clipping risks as unconfirmed until rendered.
- [ ] Verify TalkBack reading order, states and seek behavior on an Android device.

## Task 3: Integrated verification
- [ ] Run ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug.
- [ ] Inspect independent review and resolve findings before release preparation.
- [ ] Record commands, outcomes and remaining gaps in docs/EXECUTION_LOG.md and docs/decisions.md.
Current blocker: Gradle 9.3.1 distribution is absent and download failed with Network is unreachable, even with --offline. No test/lint/build pass is established here. Repair access or use the existing trusted CI runner, without weakening checks.

## Task 4: Deployment readiness (parallel read-only inspection, release dependent on Task 3)
- [ ] Reconcile installed package/version with Play Console and candidate applicationId com.shadowspark.morontomornica. Older records mention another package; do not assume equivalence.
- [ ] Inspect actual existing track/highest versionCode before selecting a new code. Current source versionCode=1, versionName=1.0.0-oct19.
- [ ] Produce a signed release AAB only using the authorized signing mechanism; verify package, version, certificate and SHA-256 without exposing secrets.
- [ ] Bind artifact and checks to immutable source SHA. Existing debug APK and mutable mornica-latest tag do not prove Play release provenance.
- [ ] Prepare internal-test track, device smoke checks, cost/impact review and recovery plan. Determine whether this is a first publication or an update; staged rollout applies to updates. Compare installed signing certificate to the Play app-signing certificate, not merely the upload certificate. Actual publication needs the separately required release authorization.

## Task 5: Workflow skill extraction
- [ ] At every workflow close, capture repeatable inputs, steps, failure handling, proof gates and sanitized examples.
- [ ] Create/update a focused skill; deduplicate existing ECC/Modern Web Guidance skills. Validate and forward-test it on a separate task.
- [ ] Save through supported skill storage; report saved vs blocked honestly. Never label an unexecuted deployment procedure as tested.
- [ ] First deliverable: orchestrate-mornica-lodgist planning skill. Later candidates: verify-mornica-compose-accessibility, prepare-mornica-release, fix-lodgist-mobile-accessibility; create after evidence exists.

## Lodgist follow-on
After Mornica's integrated verification, locate the current Lodgist source and deployed commit. Assign a web implementer to button contrast and persistent payment-field labels; QA to 320/375/390px reflow, 200% text and keyboard checks; deployment expert to current host and immutable candidate/rollback checks. Keep this as a separate scoped implementation plan once paths are verified.

## Completion definition
Each task has reviewed changes, actual check results or explicit blockers, scope-clean diff and reusable learning captured. Approval of this plan is not a deployed app or passed Android test suite.
