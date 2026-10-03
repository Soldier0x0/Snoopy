# Snoopy

Snoopy captures one weigh-in from a smart scale on your phone. Stand on the scale, tap Record, and save the Bluetooth Low Energy log for that session.

[Latest release](https://github.com/Soldier0x0/Snoopy/releases/latest)

## Install

Download `snoopy-v0.1.apk` (or the newest `snoopy-v0.N.apk`) from [GitHub Releases](https://github.com/Soldier0x0/Snoopy/releases) and install it on your phone.

Install the new APK over the one already on the phone. You do not need to uninstall first. When the signing key matches, replacing the app wipes nothing and your saved session logs stay on the phone. Uninstalling Snoopy wipes those on-phone logs.

If Android blocks the file, allow the browser or Files app to install unknown apps, then open the APK again.

## Plan

See [docs/plan.md](docs/plan.md) for scope, the log format, and how releases are published.

## Releases

Pull-request builds produce the signed APK artifact. Merges to `main` publish that same APK to GitHub Releases when `CHANGELOG.md` has short bullet points for that version.

With no `ANDROID_KEYSTORE_BASE64` repository secret, GitHub Actions signs release APKs with the debug keystore bundled in `app/debug.keystore`. That certificate stays the same across builds, so a newer APK can install over an older one.

To sign with your own key instead, add these repository secrets: `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD`. If `ANDROID_KEYSTORE_BASE64` is set, all three password or alias secrets are required.
