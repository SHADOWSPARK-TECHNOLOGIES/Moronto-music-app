---
name: mornica-loop-engineering
description: Bounded Hermes orchestration and evidence-first Android development loops for Moronto Mornica; prevents source loss, false acceptance and unapproved release.
---

# Mornica loop engineering

## Trigger
Apply for all Hermes/Antigravity Moronto Mornica implementation, repair, verification and release-candidate work.

## Mandatory first read
1. docs/agent-loop/HERMES_EXECUTION_CONTRACT_2026-11-07.md
2. docs/PRODUCT_SPEC.md and app/build.gradle.kts
3. docs/agent-loop/{objective,status,evidence}.md on the header candidate branch
4. git status / branch / worktree / origin read-only audit before edits

## One bounded objective per cycle
**OBSERVE**: Identity, current evidence, dirty-state inventory, smallest reproduced defect, policy/privacy impact.
**PLAN**: Exact user-visible outcome, acceptance criteria, allowed files, tests and rollback. Separate already-implemented from proposed.
**SCOUT**: Read existing implementation, diagnose architecture/CI, report hypotheses as hypotheses.
**BUILD**: Change the minimum in a clean isolated worktree, only one objective. No more than two independent nonoverlapping writers.
**VERIFY**: Separate agent inspects diff and runs repeatable tests. At minimum Gradle :app:testDebugUnitTest, :app:lintDebug, :app:assembleDebug and safe emulator checks appropriate to change.
**GATE**: PASS only if verified; FAIL if acceptance failed; BLOCKED if missing source/SDK/log/evidence/credentials/approval.
**REFLECT**: Capture root cause, evidence, lesson, current branch/HEAD, next smallest objective. No unbounded autonomous retries.

## Loop limits
- 8 iterations maximum for one objective.
- Stop after 2 nonprogress/identical failures. Do not bypass a failing gate to obtain green status.
- No force-push, merge, deploy, public publish, signing change, billing change, secret disclosure or physical-device replacement without explicit owner approval.
- Never stomp dirty worktree; use isolation and source comparisons rather than reset/stash/clean.
- Prefer local Hermes-hosted Android verification if GitHub Actions logs are missing or billing/permissions block runners. Do not report a green CI based on local build alone.

## Completion evidence
Return this per-objective report:

| Field | Required |
| --- | --- |
| State | PASS / FAIL / BLOCKED |
| Source | repo, worktree, branch, HEAD, dirty files before work |
| Change | exact paths, diff summary, why smallest safe repair |
| Test | commands, exit codes, JUnit totals, lint findings |
| Artifact | output APK path + SHA-256 when built |
| UI/device | emulator identity, screenshot provenance, player/nav interactions when needed |
| Independent QA | reviewer findings and whether implemented fixes were rechecked |
| Safety | media separation, rights, secrets, Play/install/release untouched |
| Next | one objective, cost/time spent if observed, explicit approval needed |

## Forbidden acceptance shortcuts
An APK build alone does not prove audio playback. Screenshot capture alone does not prove touch navigation. Remote GitHub Actions failure with unavailable logs does not prove app compilation failure. A GitHub draft PR does not prove compatibility with local uncommitted Mornica files. Do not call a project COMPLETE while these remain unknown.

## Helpful command
On the correct, already-clean isolated Android worktree: bash scripts/hermes-mornica-verify.sh

Use existing repo-declared Gradle wrapper and toolchain. Never switch to web/Next.js even when an older Antigravity prompt suggests it.
