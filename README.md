# UwuFy LSPosed module

UwuFy is an LSPosed/Xposed module that waits until text stops changing for a configurable delay, then rewrites it into uwu-style text.

It hooks editable text fields in the target app process, so it works with Gboard, SwiftKey, Samsung Keyboard, and the rest of the keyboard circus. You do not need to scope the keyboard app itself. Scope the apps you type in.

## Features

- Idle-delay uwuification so it does not mutate every keystroke immediately
- Configurable delay in milliseconds
- Toggleable rules and randomization
- Word rewrites like `hello -> hewwo`, `you -> yuw`, `friend -> fwiend`
- Stutters, emoticons, actions, and exclamations
- Skip passwords and preserve URLs/emails/acronyms
- Reset button in the app
- GitHub Actions workflow that builds a release APK

## LSPosed notes

This project uses the legacy Xposed hook API because it is simple and compatible with LSPosed's classic module path. LSPosed documents that the modern API uses a different entry format and is still under active development, while the legacy API remains supported for compatibility. The module metadata includes `xposedsharedprefs` so the app's preferences can be read back from hooked processes.

## Install

1. Build the APK.
2. Install it like a normal app.
3. Enable the module in LSPosed.
4. Scope it to the apps whose text fields you want uwu-fied.
5. Open the app, tune the settings, and save.

## GitHub Actions build

The workflow in `.github/workflows/android.yml` downloads Gradle and the Android command line tools, installs the Android 34 platform and build tools, then runs:

```bash
gradle --no-daemon assembleRelease
```

The release build is signed with the debug keystore, so the APK is installable without extra signing setup.

## Termux build

### Packages

```bash
pkg update
pkg install openjdk-21 git unzip wget curl
```

### Gradle and SDK

Download Gradle 8.8 and the Android command line tools, then set:

```bash
export ANDROID_SDK_ROOT=$HOME/android-sdk
export ANDROID_HOME=$ANDROID_SDK_ROOT
export PATH="$HOME/gradle-8.8/bin:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"
```

Install the required SDK packages:

```bash
yes | sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

Then build:

```bash
gradle --no-daemon assembleRelease
```

## What it changes

The transformer applies these rules:

- `r` and `l` drift into `w`
- `th` softens to `d`
- common words get dictionary replacements
- some words get stutters
- some sentences pick up emoticons, actions, and exclamations
- URLs, emails, acronyms, and password fields are left alone

## Output

The APK is generated at:

```text
app/build/outputs/apk/release/app-release.apk
```
