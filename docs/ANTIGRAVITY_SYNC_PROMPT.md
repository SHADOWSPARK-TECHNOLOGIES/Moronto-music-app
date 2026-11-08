# Antigravity → Mornica sync prompt (Hermes handoff entry)

Use this to bring **Google Antigravity** (Android Studio coding agent) in sync with the Hermes handoff for
Moronto Mornica. Sync means: same branch, same pushed HEAD, clean tree, the 7 project skills loaded, and the
takeover state understood before any work starts.

Source of record for the takeover: `docs/HANDOFF_ANTIGRAVITY_2026-11-08.md` and `docs/ANTIGRAVITY_RUNBOOK.md`.

## How Antigravity accesses the project skills
- Root `AGENTS.md` + the nearest-file override `domain/AGENTS.md` are auto-loaded project rules.
- Project skills live at `.agents/skills/<name>/SKILL.md`. Antigravity discovers them while working in the
  repo; invoke them by name in the prompt (e.g. "use the skill `mornica-offline-ready-audit`") and confirm
  with `/agents` (and `/skills` if offered).
- Custom roles are in `.agents/agents/<role>.md` (moronto-architect / builder / growth / qa) via `/agents`; rules
  and workflows are in `.agents/rules/*.md` and `.agents/workflows/*.md`.
- The 7 committed skills:
  `mornica-loop-engineering` (loop/verification contract — read first),
  `mornica-offline-ready-audit` (NEW), `mornica-release-readiness` (NEW),
  `android-clean-architecture`, `design-system`, `make-interfaces-feel-better`, `verification-loop`.
- Hermes mirror with extra device detail (host-only, not in the repo):
  `~/.hermes/skills/software-development/mornica-emulator-playback-proof/`.

Expected repo state after sync: branch `agent/mornica-loop4-as-inspect-20261107`, HEAD `329c74f`,
`git status` clean, `origin` up to date.

---

## Prompt — paste into Antigravity

```text
Act as the Moronto engineering architect taking over Moronto Mornica Android from a Hermes handoff.

PHASE 0 — ENVIRONMENT VERIFY (read-only; do NOT edit/install yet).
Confirm and report VERIFIED or STOP: (a) pwd is the Moronto-music-app checkout; (b) `git remote -v` shows
SHADOWSPARK-TECHNOLOGIES/Moronto-music-app; (c) current branch is `agent/mornica-loop4-as-inspect-20261107`;
(d) `git status` is clean; (e) `git fetch origin` then confirm local is at origin (HEAD should be 329c74f).
If the branch is not checked out: ask the user before fetching/checking out the branch (do NOT stomp a dirty
working tree). If this is not the Moronto-music-app repo, STOP and report.

PHASE 1 — SYNC TO THE HANDOFF STATE.
Check out / fetch `agent/mornica-loop4-as-inspect-20261107` so we are on the exact commit I pushed.
Then read, in order:
  1. docs/HANDOFF_ANTIGRAVITY_2026-11-08.md   (authoritative takeover doc)
  2. docs/ANTIGRAVITY_RUNBOOK.md              (dated takeover note at the bottom)
  3. docs/agent-loop/STATE.md, docs/EXECUTION_LOG.md (evidence ledger)
These define the current verified state, the changes, and the next objective. Do not redo completed work.

PHASE 2 — LOAD THE PROJECT SKILLS (they are committed, not created new).
Antigravity reads project skills from `.agents/skills/<name>/SKILL.md` under the repo. Enable and FOLLOW, in
priority order:
  1. `mornica-loop-engineering`   — the loop/verification contract (read first, follow its GATE rules).
  2. `mornica-offline-ready-audit` — offline-ready wiring, Media3 1.5 download API shape, streaming-only rule,
                                       persistent-player rule, and the ADB/uiautomator device-audit recipe.
  3. `mornica-release-readiness`   — rights/privacy/signing/release gates (no publish without owner approval).
Also honor `android-clean-architecture`, `design-system`, `make-interfaces-feel-better`, `verification-loop`.
Use `/agents` (and `/skills`) to confirm they are loaded. Respect the nearest-file `domain/AGENTS.md` gate
invariants (default-deny, no YouTube into Media3, isLocalPlaying never optimistic).

PHASE 3 — CURRENT STATE (do not re-claim otherwise).
Loop-4 (licensed catalog + downloads UI) is DEVICE-VERIFIED: bottom nav, 5 library tabs, Stream, Download,
Cancel, Back, persistent player all interaction-tested; the "Offline ready" runtime defect was fixed (persist
rights snapshot + DownloadManager completion listener) and verified end-to-end on the emulator. Build green on
this toolchain: `./gradlew --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`
→ 59/59 unit tests, 0 lint errors, debug APK 20.4 MB. Rendered pixels are structurally verified (uiautomator),
NOT pixel-reviewed.

PHASE 4 — SMALLEST NEXT OBJECTIVE (propose, then do one).
Per docs/agent-loop/STATE.md, propose the smallest next objective (e.g. a pixel-level/multimodal review of
screenshots/audit-20261108/, then a loop-5 background/airplane-mode item). One bounded objective per cycle.
Implement it in a clean isolated worktree, keep the branch, and run the full verification command above.
Stop after 2 identical failures with exact logs; no fabricated success.

GATE — NO push, merge, deploy, publish, signing change, or Play Console action without the owner's explicit
per-request approval. Report unit-verified vs device-verified vs unverified SEPARATELY with evidence
(file/line/command). Record evidence in docs/EXECUTION_LOG.md and docs/decisions.md; keep docs/agent-loop/STATE.md
current. When done, hand me: source/HEAD, change, tests, artifact SHA-256, UI/device evidence, QA, safety, next.
```

---

### Author / provenance
Written 2026-11-08 by the Hermes session that performed the loop-4 audit and Offline-Ready fix. Keep in sync
with `docs/HANDOFF_ANTIGRAVITY_2026-11-08.md`; update HEAD here whenever the branch is advanced.
