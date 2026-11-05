# Security and privacy checklist

## Implemented in v0.2 prototype
- Profile serialized locally and encrypted with AES-GCM.
- Key generated and held by Android Keystore.
- Android app backup disabled.
- Network cleartext traffic disabled.
- Discovery defaults off and requires an explicit opt-in plus a Start action.
- BLE advertisements omit profile fields and the persistent local identity.
- BLE token is random and rotates every 15 minutes; stale peer records expire.
- BLE discovery stops when the user opts out, presses Stop, or leaves the foreground.
- Android runtime Bluetooth permissions are requested on supported OS versions.

## Required before beta
- [ ] Build and test on supported Android versions and at least two physical BLE devices.
- [ ] Use a reviewed cryptographic protocol/library for end-to-end messaging; do not invent one.
- [ ] Authenticate peer keys and make key changes visible to users.
- [ ] Design a secure mapping between a BLE encounter token and a user-approved profile exchange.
- [ ] Review token rotation against BLE stack behavior and possible correlation attacks.
- [ ] Never advertise display name, age, photos, preferences, or GPS coordinates over BLE.
- [ ] Avoid exact distance estimates that enable trilateration.
- [ ] Add block/report and local deny lists.
- [ ] Add consent prompts for sharing profile fields and each private photo.
- [ ] Rate-limit connection attempts and reject malformed payloads.
- [ ] Define deletion behavior for local profile, keys, messages, and cached media.
- [ ] Threat-model screenshots, recipient copying, IP exposure, and relay metadata.
- [ ] Establish adult-only usage policy and appropriate safety measures.

## Important
Pseudonymous is not the same as anonymous. A peer connection may reveal IP addresses, and nearby radio broadcasts can be observed by others. The rotating BLE token is an identifier for temporary presence, not an authentication credential. Do not promise perfect anonymity or that shared images cannot be copied.
