#!/usr/bin/env bash
# Loop 1: capture a header-repair candidate on a dedicated emulator ONLY.
# Usage: bash docs/agent-loop/verify-emulator.sh emulator-5554
# Refuses physical phones and refuses to overwrite an already-installed application.
set -Eeuo pipefail

SERIAL="${1:-}"
APP_ID="com.shadowspark.morontomornica"
APK="app/build/outputs/apk/debug/app-debug.apk"
OUTPUT="docs/screenshots/loop1-candidate"

if [[ ! "$SERIAL" =~ ^emulator-[0-9]+$ ]]; then
  echo "BLOCKED: supply a dedicated emulator serial (emulator-NNNN), never a physical device." >&2
  exit 2
fi
if [[ ! -f "$APK" ]]; then
  echo "BLOCKED: no debug APK at $APK. Run the Gradle build gate first." >&2
  exit 2
fi
if ! command -v adb >/dev/null 2>&1; then
  echo "BLOCKED: adb not found on PATH." >&2
  exit 2
fi
if [[ "$(adb -s "$SERIAL" get-state 2>/dev/null)" != "device" ]]; then
  echo "BLOCKED: emulator is not connected/authorized: $SERIAL" >&2
  exit 2
fi
if [[ "$(adb -s "$SERIAL" shell getprop ro.kernel.qemu | tr -d '\r')" != "1" ]]; then
  echo "BLOCKED: device is not confirmed as an Android emulator." >&2
  exit 2
fi
existing="$(adb -s "$SERIAL" shell pm path "$APP_ID" 2>/dev/null | tr -d '\r' || true)"
if [[ "$existing" == *"package:"* ]]; then
  echo "BLOCKED: app is already installed on this emulator. Use a fresh AVD to preserve data." >&2
  exit 2
fi
if adb -s "$SERIAL" shell wm size | grep -q "Override size:" ||
   adb -s "$SERIAL" shell wm density | grep -q "Override density:"; then
  echo "BLOCKED: emulator already has display overrides; use a fresh AVD to avoid changing its settings." >&2
  exit 2
fi

original_font="$(adb -s "$SERIAL" shell settings get system font_scale | tr -d '\r')"
if [[ ! "$original_font" =~ ^[0-9]+([.][0-9]+)?$ ]]; then
  echo "BLOCKED: cannot read original system font scale." >&2
  exit 2
fi

restore() {
  adb -s "$SERIAL" shell wm size reset >/dev/null 2>&1 || true
  adb -s "$SERIAL" shell wm density reset >/dev/null 2>&1 || true
  adb -s "$SERIAL" shell settings put system font_scale "$original_font" >/dev/null 2>&1 || true
}
trap restore EXIT

echo "APK SHA-256:"
sha256sum "$APK"
echo "Installing debug candidate ONLY on emulator $SERIAL."
adb -s "$SERIAL" install "$APK"

mkdir -p "$OUTPUT"
capture() {
  local name="$1" size="$2" scale="$3"
  adb -s "$SERIAL" shell wm size "$size" >/dev/null
  adb -s "$SERIAL" shell wm density 160 >/dev/null
  adb -s "$SERIAL" shell settings put system font_scale "$scale" >/dev/null
  adb -s "$SERIAL" shell am force-stop "$APP_ID" >/dev/null
  adb -s "$SERIAL" shell am start -W -n "$APP_ID/.MainActivity" >/dev/null
  sleep 6

  local foreground
  foreground="$(adb -s "$SERIAL" shell dumpsys window | grep -m1 'mCurrentFocus=' || true)"
  if [[ "$foreground" != *"$APP_ID"* ]]; then
    echo "BLOCKED: unexpected foreground window ($foreground). No screenshot captured." >&2
    return 1
  fi
  adb -s "$SERIAL" exec-out screencap -p > "$OUTPUT/$name.png"
  echo "$name: $size at font scale $scale; foreground=$foreground; screenshot=$OUTPUT/$name.png"
}

capture "header-360dp-normal" "360x800" "1.0"
capture "header-412dp-normal" "412x915" "1.0"
capture "header-360dp-large-font" "360x800" "1.3"

echo "CAPTURED: emulator screenshots only. Visual review and playback tests remain mandatory."
