#!/usr/bin/env bash
set -Eeuo pipefail

# Read-only source preflight plus reproducible Android checks on a CLEAN isolated worktree.
# No git reset/clean/stash, no device installs, no signing, no cloud changes.

fail() { printf 'BLOCKED: %s\n' "$*" >&2; exit 2; }
command -v git >/dev/null 2>&1 || fail 'git not installed'
root=$(git rev-parse --show-toplevel 2>/dev/null) || fail 'not inside a Git worktree'
cd "$root"
remote=$(git remote get-url origin 2>/dev/null) || fail 'origin remote not configured'
[[ "$remote" == *'SHADOWSPARK-TECHNOLOGIES/Moronto-music-app'* ]] || fail "unexpected origin: $remote"
branch=$(git branch --show-current)
[[ -n "$branch" && "$branch" != 'main' && "$branch" != 'master' ]] || fail 'use a named isolated development branch, never main/master/detached'
[[ -z "$(git status --porcelain)" ]] || fail 'worktree has changes; preserve them and use a clean isolated worktree'
[[ -f app/build.gradle.kts && -f settings.gradle.kts ]] || fail 'Android Gradle project not found'
[[ -f gradlew ]] || fail 'Gradle wrapper missing'
command -v java >/dev/null 2>&1 || fail 'Java runtime missing'
if [[ -z "${ANDROID_HOME:-}" && -z "${ANDROID_SDK_ROOT:-}" && ! -f local.properties ]]; then
  fail 'Android SDK path unknown: configure environment or ignored local.properties safely, then retry'
fi

evidence_dir=$(mktemp -d "${TMPDIR:-/tmp}/mornica-hermes-verify.XXXXXXXX")
chmod 700 "$evidence_dir"
printf 'Evidence directory: %s\n' "$evidence_dir"
printf 'Repository: %s\n' "$remote" | tee "$evidence_dir/source.txt"
printf 'Branch: %s\n' "$branch" | tee -a "$evidence_dir/source.txt"
git rev-parse HEAD | tee -a "$evidence_dir/source.txt"
java -version 2>&1 | tee "$evidence_dir/java-version.txt"
printf '%s\n' 'Running unit tests, Android lint and debug APK build (no install/sign/release).'

set +e
bash ./gradlew --no-daemon --console=plain :app:testDebugUnitTest :app:lintDebug :app:assembleDebug 2>&1 | tee "$evidence_dir/gradle.log"
rc=${PIPESTATUS[0]}
set -e
printf 'Gradle exit code: %s\n' "$rc" | tee "$evidence_dir/result.txt"
if [[ -f app/build/outputs/apk/debug/app-debug.apk ]]; then
  sha256sum app/build/outputs/apk/debug/app-debug.apk | tee "$evidence_dir/apk-sha256.txt"
else
  printf '%s\n' 'Debug APK not found' | tee "$evidence_dir/apk-sha256.txt"
fi
printf 'JUnit XML files: '
if [[ -d app/build/test-results/testDebugUnitTest ]]; then
  find app/build/test-results/testDebugUnitTest -name '*.xml' -type f | wc -l
else
  printf '0 (directory absent)\n'
fi
printf '%s\n' 'Review JUnit XML and lint report for exact pass counts/warnings; no count inferred here.'
printf '%s\n' 'Post-run git status (no automatic cleanup):'
git status --short --branch
printf 'Evidence saved locally: %s\n' "$evidence_dir"
exit "$rc"
