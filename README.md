# UwuFy LSPosed module

This is an LSPosed module with a small launcher app for configuring delayed uwu-fication of editable text.

## What it does

- waits until text has been idle for a configurable amount of time
- rewrites the text with uwu-style substitutions
- randomly adds stutters, emoticons, actions, and exclamations
- preserves URLs, emails, passwords, and acronyms if enabled
- avoids transforming the text again while it is already being applied

## Important scope note

Scope the module to the apps you type in. Do not scope it only to the keyboard package, because the actual text lives in the app process.

## Build on GitHub Actions

The repository includes `.github/workflows/build.yml`. It uses the Gradle launcher in the repo root, Android SDK setup, and Gradle caching.

## Build in Termux

```bash
pkg update -y
pkg install -y openjdk-21 curl unzip
chmod +x gradlew
./gradlew assembleDebug
```

If you want a release APK, run:

```bash
./gradlew assembleRelease
```

## Install

1. Build the APK.
2. Install the APK.
3. Enable the module in LSPosed.
4. Reboot if LSPosed asks for it.
5. Scope it to the apps you want uwu-fied.

## Notes

The default configuration keeps the delay conservative so it only transforms text after it has stopped changing.
