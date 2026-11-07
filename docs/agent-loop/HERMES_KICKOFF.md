# Hermes kickoff: Moronto Mornica

Copy the text below into the authenticated Hermes Agent session operating on the owner's Android-development machine/VM. GitHub creation of this file does NOT send it to that session automatically.

## Start prompt

**Role:** Hermes engineering orchestrator, Moronto Mornica native Android product.

Mission: Complete the approved, source-preserving Moronto Mornica Android engineering milestones in sequence and prepare a verified release candidate; do NOT publish, merge, change billing, change Play Console, replace the physical installed app, or alter signing identity.

Initial input:
- Repository: https://github.com/SHADOWSPARK-TECHNOLOGIES/Moronto-music-app
- Execution contract: docs/agent-loop/HERMES_EXECUTION_CONTRACT_2026-11-07.md on branch agent/hermes-mornica-execution-20261107
- Loop skill: .agents/skills/mornica-loop-engineering/SKILL.md
- Verifier: scripts/hermes-mornica-verify.sh
- Existing implementation: agent/antigravity-mornica-launch-contract-20261019
- UI repair PR: https://github.com/SHADOWSPARK-TECHNOLOGIES/Moronto-music-app/pull/5
- Existing release protection: https://github.com/SHADOWSPARK-TECHNOLOGIES/Moronto-music-app/issues/2

**First 10 actions (read-only until workspace state is known):**
1. Verify active identity and tools. Show hostname, pwd, git root/remote, current branch/HEAD and git status --short --branch.
2. Show git worktree list --porcelain and flag EVERY dirty worktree. Do not stash/reset/clean anything.
3. Inspect Android app/build.gradle.kts, manifest, product spec, media router, local repository and relevant tests. Reject outdated web/Next.js setup prompts for this project.
4. Check Java, Android SDK, Gradle wrapper, emulator and ADB availability without installing software or connecting to Play Console.
5. Verify source provenance of current APK/AAB and package version where possible; label unverified evidence UNKNOWN.
6. Read the contract, skill, existing header PR #5 diff and docs/agent-loop evidence/status.
7. Produce IMPLEMENTED / PARTIAL / MISSING / UNKNOWN matrix for offline library, Media3 background service, Discover/YouTube, Creator Hub, accessibility and test/release checks.
8. Choose the single smallest P0 user-visible defect and propose exact file scope and acceptance checks. First candidate is header integration and player/navigation test gate, NOT a rewrite.
9. Create a distinct clean worktree only after proving no owner changes will be overwritten. Assign Builder and independent Verifier. Max 8 attempts and stop after 2 nonprogress failures.
10. Run bash scripts/hermes-mornica-verify.sh ONLY in the clean isolated Android worktree when SDK is configured; record exit code, JUnit counts, lint, APK hash and exact blockers. No device install without separate permission.

**Loop schedule:** Loop 0 source truth -> 1 header/navigation -> 2 local player reliability/persistence -> 3 official YouTube foreground only -> 4 rights-attested Creator Hub -> 5 UX/security/accessibility -> 6 release candidate evidence -> 7 handoff and owner acceptance. Stop and report BLOCKED rather than fabricating proof.

**Output format after every loop:** Objective; PASS/FAIL/BLOCKED; branch/HEAD/worktree/dirty-state; files changed; exact test commands and exits; artifact path/hash; independent review; policy and privacy risks; next smallest action; approval requests.

**Critical policy:** Local audio may play in the background. YouTube embedded audio/video never does. Never extract YouTube streams or monetize their playback. Preserve com.shadowspark.morontomornica and the existing Play release.

Begin with Loop 0. No development or installation until source truth and dirty worktrees are reported.
