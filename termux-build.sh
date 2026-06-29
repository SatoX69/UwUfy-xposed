#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

if [ -z "${ANDROID_SDK_ROOT:-}" ]; then
  export ANDROID_SDK_ROOT="$HOME/android-sdk"
fi
export ANDROID_HOME="$ANDROID_SDK_ROOT"

if ! command -v gradle >/dev/null 2>&1; then
  echo "gradle not found"
  exit 1
fi

if ! command -v sdkmanager >/dev/null 2>&1; then
  echo "sdkmanager not found in PATH"
  exit 1
fi

if [ ! -f local.properties ]; then
  echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties
fi

yes | sdkmanager --licenses >/dev/null
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
gradle --no-daemon assembleRelease
