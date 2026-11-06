# Prototypeapp

A local-first, pseudonymous Android app for consensual dating and hookups between adults.

## Principles
- Profiles are encrypted and stored locally; profile details are not broadcast over BLE.
- Pseudonymous local identity; no public legal name required.
- Nearby discovery is opt-in and stops when the app leaves the foreground.
- BLE uses a random 8-byte token that rotates every 15 minutes.
- Safety controls (block, report, visibility off) are required before public release.

## Current status — v0.2.1 core-app work
Implemented:
- Native Kotlin + Jetpack Compose profile editor with required display name, age validation (18–120), a live profile preview, bio character counter, and data-layer limits for pseudonym, bio, and preferences.
- Unit tests for profile JSON round-tripping, safe defaults, field limits, and the default-off discoverability setting.
- AES-GCM encrypted local profile storage using an Android Keystore key.
- Local pseudonymous ID.
- BLE scanning and non-connectable advertising using custom service data.
- Runtime Bluetooth permission requests for Android 12+ and location permission for older Android versions.
- A visible start/stop control, foreground-only discovery, temporary nearby IDs, and stale-peer cleanup.
- Immediate opt-out persistence and a confirmation-protected control to delete the encrypted local profile.

Not implemented:
- Exchanging profiles or proving that a nearby token belongs to a specific person.
- Authenticated peer handshakes, private chat, WebRTC, signaling, photo sharing, block/report flows, or server infrastructure.
- BLE GATT profile exchange, authenticated sessions, encryption for mesh packets, active multi-hop transport, reply routing, completion notices, private chat, WebRTC, photo sharing, block/report flows, or physical-device validation. The mesh protocol core is an initial transport-independent foundation only.

## Requirements
- Android Studio with JDK 17
- Android SDK 35
- Android 8.0 (API 26) or newer
- A phone with Bluetooth LE support for nearby discovery

## Build
Open this repository in Android Studio and let Gradle sync. Run the `app` configuration on a physical Android device. BLE behavior should be tested with two devices; emulator behavior varies.

## Next milestone — finish the core app first
Networking expansion is paused. Priorities are to make the existing app's user-facing features complete and honest before adding new transport systems.

1. Finish profile editing, validation, preview, and clear save/delete feedback.
2. Improve nearby discovery states, permission-denial guidance, and empty/error states.
3. Add usable profile-detail and connection-request screens only when their data flow is real; do not show fake matches or pretend requests have been sent.
4. Add local block/mute controls and privacy settings, with clear limits until remote identity exists.
5. Run unit tests and the APK build, then test the existing discovery flow on two physical Android phones.
6. Revisit profile exchange, chat, and mesh networking only after the core screens and safety behavior are stable.

The mesh protocol code is experimental and is not connected to the user interface or a transport. It is deliberately not the current development priority.

## Privacy notes
Pseudonymous does not mean anonymous. BLE advertisements can be observed nearby, and direct internet peer connections may expose IP addresses. The current BLE token is random and rotates, but an observer may still correlate radio activity during a rotation window. Never put names, age, preferences, photos, or GPS coordinates in advertisements. The new mesh protocol core is not encrypted or connected to a transport yet; do not treat it as ready to exchange real user data.

## Build and release an APK

### Automatic debug builds
Every push to `main` and pull request targeting `main` runs the Android build workflow. Successful runs upload `prototypeapp-debug-apk` for 14 days.

1. Open [GitHub Actions](https://github.com/AirysDark/Prototypeapp/actions).
2. Select a successful **Android build** run.
3. Under **Artifacts**, download `prototypeapp-debug-apk` and extract the ZIP.

### Publish a versioned APK release
The **Build and release APK** workflow runs when a version tag such as `v0.2.1` is pushed. It builds an installable debug-signed APK, calculates its SHA-256 checksum, and attaches both files to a GitHub Release.

From a local clone with the latest `main`:
```bash
git checkout main
git pull
git tag v0.2.1
git push origin v0.2.1
```
Use a new version number for each release. The workflow checks that the tag follows a version pattern and publishes release notes automatically. You can also run the workflow manually from **Actions → Build and release APK → Run workflow**, providing a tag that already exists.

**Signing limitation:** these are prototype/debug-signed APKs for direct testing, not production or Google Play releases. GitHub-hosted runners generate their debug signing key on the runner, so a later APK may not install as an update over an earlier one. For stable in-place updates and production distribution, configure a persistent release keystore using GitHub Actions secrets and a dedicated release-signing workflow. Never commit a keystore or its passwords to the repository.
