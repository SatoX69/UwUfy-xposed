#!/usr/bin/env bash
set -euo pipefail

APP_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GRADLE_VERSION="8.7"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
CACHE_DIR="$GRADLE_USER_HOME/uwufy-gradle"
DIST_NAME="gradle-$GRADLE_VERSION"
DIST_ZIP="$CACHE_DIR/$DIST_NAME-bin.zip"
DIST_HOME="$CACHE_DIR/$DIST_NAME"
GRADLE_BIN="$DIST_HOME/bin/gradle"
GRADLE_URL="https://services.gradle.org/distributions/$DIST_NAME-bin.zip"

if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$CACHE_DIR"
  if [ ! -f "$DIST_ZIP" ]; then
    if command -v curl >/dev/null 2>&1; then
      curl -fsSL "$GRADLE_URL" -o "$DIST_ZIP"
    elif command -v wget >/dev/null 2>&1; then
      wget -qO "$DIST_ZIP" "$GRADLE_URL"
    else
      echo "curl or wget is required to download Gradle." >&2
      exit 1
    fi
  fi

  rm -rf "$DIST_HOME"
  if command -v unzip >/dev/null 2>&1; then
    unzip -q "$DIST_ZIP" -d "$CACHE_DIR"
  else
    echo "unzip is required to extract Gradle." >&2
    exit 1
  fi
fi

exec "$GRADLE_BIN" -p "$APP_HOME" "$@"
