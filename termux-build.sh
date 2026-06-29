#!/usr/bin/env bash
set -euo pipefail

pkg update -y
pkg install -y openjdk-21 curl unzip

chmod +x gradlew
./gradlew assembleDebug
