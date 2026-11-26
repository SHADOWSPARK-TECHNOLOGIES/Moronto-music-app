# Moronto-music-app
first music app

## Repository guide: native Android app vs web app

This repository has carried two different products. They are not interchangeable.

### 1. Which branch is which

| Product | Branch | Identifying files |
| --- | --- | --- |
| Native Android app — Moronto Mornica (Kotlin, Jetpack Compose, Media3) | Native feature and maintenance branches, for example `feat/mornica-spotify-phase-c-player-depth`, the other `feat/mornica-spotify-phase-*`, `agent/*`, `codex/*`, and `release/versioncode-2`. The **local** `main` is a stale Android snapshot at `aa5d78d`. | `app/`, `gradlew`, `settings.gradle.kts`, applicationId `com.shadowspark.morontomornica` |
| Web app — Moronto Music (React, TypeScript, Vite, Express), synced from Google AI Studio | GitHub default branch `main` (`5133720` as of 2026-11-26). | `index.html`, `package.json`, `server.ts`, `src/`, `.aistudio/` |

The local `main` and the GitHub default `main` currently point to different products. Run
`git fetch origin --prune` and inspect the tree before assuming what a branch contains. Do not
commit native Android work to the web-app `main`, and do not assume a stale local
`origin/<branch>` ref matches the remote.

### 2. Source of truth for a Play Store release

The native Android line only. There is no Play Store artifact in the web app.

- `release/versioncode-2` carries the store version (`applicationId com.shadowspark.morontomornica`,
  `versionCode = 2`, `versionName = 1.0.1-oct19`).
- Native feature branches feed the release line through reviewed PRs. The CI workflow
  `.github/workflows/android-apk.yml` runs the Gradle gates and attaches a debug APK (on its
  configured trigger branch or via `workflow_dispatch`).
- GitHub `main` (the web app) is never the source of a Play Store build.
- A signed release still requires owner-provided signing credentials. Per `AGENTS.md`, no push,
  merge, signing change or Play Console action without explicit approval.

### 3. Build and run

Native Android (from a native feature branch):

```bash
./gradlew --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
# Install on a device: adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Web app (from the GitHub `main` branch, or an extracted AI Studio export):

```bash
bun install --frozen-lockfile   # bun.lock is the authoritative lockfile
bun run dev                     # http://localhost:3000
bun run lint                    # tsc --noEmit
```

`npm ci` is not the install path for this export: `package-lock.json` is stale relative to
`package.json`, so `bun.lock` must be used with the frozen flag to get the exact versions.

### 4. How changes flow: AI Studio -> GitHub -> local machine

- **AI Studio is a generation surface, not a merge authority.** It produces code and prompts and
  cannot run Gradle, lint, tests or an emulator. Treat any "build passes" claim from it as
  UNVERIFIED until reproduced locally.
- **Sync/export:** AI Studio output is synced to GitHub (the web app lives on default `main`) or
  downloaded as a ZIP. Prefer the exact archive the owner has; verify identity first with the
  `.aistudio/` marker, `metadata.json`, `package.json` and README. Extract into a scratch path —
  never overwrite a live checkout or its uncommitted work.
- **GitHub is the shared record of reviewed code.** Fetch before comparing branches, keep work on
  a named branch, and push/merge only with explicit owner approval. PRs list unit-verified,
  build-verified, device-verified and unverified results separately.
- **Local machine is where verification happens.** Install from the authoritative lockfile, run
  the product's gates above, preserve existing tests, and commit only intended files. A local
  feature branch is not the release source until it is reviewed and lands on the release line.
- **Cross-product changes are deliberate ports, not automatic mirroring.** The native app
  (`PlaybackRouter`, rights gate, YouTube boundary) and the web app (its own store and Web Audio
  engine) do not share state or a build. Port a feature explicitly, with review, and never weaken
  the native default-deny rights gate or the external-only YouTube rule. 
