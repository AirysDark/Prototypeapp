# Prototypeapp

A local-first, pseudonymous Android app for consensual nearby dating and hookups.

## Principles
- Profiles and profile photos stay on the device unless the user explicitly shares them.
- Pseudonymous local identity; no public legal name required.
- Nearby discovery is opt-in and should expose only a minimal profile card.
- Network transports are replaceable: nearby BLE discovery and internet WebRTC are separate layers.
- Safety controls (block, report, visibility off) are core requirements, not add-ons.

## Current status
This is the initial Android foundation. It includes a Compose UI, encrypted local profile storage using Android Keystore AES-GCM, and explicit interfaces for discovery and peer transport. BLE discovery and WebRTC signaling are scaffolds, not yet end-to-end working. WebRTC internet connections require a signaling path and often STUN/TURN infrastructure; that signaling service must not become a central profile database.

## Requirements
- Android Studio with JDK 17
- Android SDK 35
- Android 8.0 (API 26) or newer

## Build
Open this repository in Android Studio and let Gradle sync. Run the `app` configuration on an Android device or emulator.

## First milestone
1. Create/edit a local profile.
2. Persist profile data encrypted on-device.
3. Add opt-in discovery with ephemeral identifiers.
4. Implement and test BLE discovery.
5. Add WebRTC data channels with an audited signaling implementation.
6. Add verified encryption, block/report, consent-based photo sharing, and abuse controls before public release.

## Privacy notes
A P2P architecture does not automatically make an app anonymous. IP addresses can be visible to peers during direct connections; BLE advertisements can be observed nearby; screenshots and recipient copies cannot be reliably prevented. Do not advertise exact GPS coordinates or place profile details in BLE advertisements.
