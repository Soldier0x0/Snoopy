# Changelog

Android releases stay under 1.0: v0.1, then v0.2, and so on. Each published version has one section below, newest first. Add a new `## v0.N` section at the top when that version is ready. A later version does not need a section yet.

Each section is short bullet points of what that version added or changed. A release is published only when a green APK exists and this file has those points for that one version. A missing section does not fail the build and does not publish. An empty section fails. A note that only says the app is the same as the previous one is not published, and that check fails. The job stays on the free GitHub runner, reuses the Gradle cache, and does not build an APK when a change is only docs, this file, or the README. A green pull-request build is the APK that gets published, so a merge does not compile that same git tree again.

One pull request publishes one version. That version is the only one it may document. It may be a version such as v0.5, or a sub-version such as v0.5.1, and it is still only one of them. Do not add notes or a tag for a later version to keep the next build from failing. The workflow fails if this file has a section for any other version that has not already shipped.

## v0.1

First published APK for one smart-scale weigh-in on the phone.

- Stand on the scale, tap Record, and watch Bluetooth Low Energy messages arrive with time, direction, hex, and readable text when the bytes look like text.
- After the weigh-in, see the advertised scale name and any weight-like number, then share one session file with the raw bytes and the readable view together.
- Plain-language help when a companion app is holding the scale, or when the model cannot be recorded here.
- Logs stay on the phone. The app does not upload data, read an HCI snoop log, or parse body-fat fields.
