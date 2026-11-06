# Prototypeapp

A local-first, pseudonymous Android app for consensual dating and hookups between adults.

## Principles
- Profiles are encrypted and stored locally; profile details are not broadcast over BLE.
- Pseudonymous local identity; no public legal name required.
- Nearby discovery is opt-in and stops when the app leaves the foreground.
- BLE uses a random 8-byte token that rotates every 15 minutes.
- Safety controls (block, report, visibility off) are required before public release.

## Current status — v0.2.0
Implemented:
- Native Kotlin + Jetpack Compose profile editor with age validation (18+).
- AES-GCM encrypted local profile storage using an Android Keystore key.
- Local pseudonymous ID.
- BLE scanning and non-connectable advertising using custom service data.
- Runtime Bluetooth permission requests for Android 12+ and location permission for older Android versions.
- A visible start/stop control, foreground-only discovery, temporary nearby IDs, and stale-peer cleanup.
- Immediate opt-out persistence and a confirmation-protected control to delete the encrypted local profile.

Not implemented:
- Exchanging profiles or proving that a nearby token belongs to a specific person.
- Authenticated peer handshakes, private chat, WebRTC, signaling, photo sharing, block/report flows, or server infrastructure.
- Automated tests or physical-device validation. A GitHub Actions debug build completed successfully for commit `591c4697`; the workflow now also uploads the generated APK as a downloadable artifact for subsequent successful runs.

## Requirements
- Android Studio with JDK 17
- Android SDK 35
- Android 8.0 (API 26) or newer
- A phone with Bluetooth LE support for nearby discovery

## Build
Open this repository in Android Studio and let Gradle sync. Run the `app` configuration on a physical Android device. BLE behavior should be tested with two devices; emulator behavior varies.

## Next milestone
1. Build and test the app on two Android phones.
2. Add a privacy-preserving, authenticated exchange that maps an ephemeral BLE encounter to a user-approved profile card.
3. Add peer authentication and encrypted messaging using reviewed protocols/libraries.
4. Add block/report, consent-based photo sharing, rate limits, and abuse controls.
5. Add WebRTC data channels and an authenticated signaling service for internet connections.

## Privacy notes
Pseudonymous does not mean anonymous. BLE advertisements can be observed nearby, and direct internet peer connections may expose IP addresses. The current BLE token is random and rotates, but an observer may still correlate radio activity during a rotation window. Never put names, age, preferences, photos, or GPS coordinates in advertisements.
