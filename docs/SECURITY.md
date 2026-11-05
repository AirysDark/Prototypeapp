# Security and privacy checklist

## Implemented in starter
- Profile serialized locally and encrypted with AES-GCM.
- Key generated and held by Android Keystore.
- Android app backup disabled.
- Network cleartext traffic disabled.
- Discovery defaults off.

## Required before beta
- [ ] Use a reviewed cryptographic protocol/library for end-to-end messaging; do not invent one.
- [ ] Authenticate peer keys and make key changes visible to users.
- [ ] Rotate BLE discovery identifiers and avoid stable identifiers in advertisements.
- [ ] Never advertise display name, age, photos, preferences, or GPS coordinates over BLE.
- [ ] Use coarse distance bands; avoid exact distances that enable trilateration.
- [ ] Add block/report and local deny lists.
- [ ] Add consent prompts for sharing profile fields and each private photo.
- [ ] Apply Android runtime permission checks and clear explanation before scanning/advertising.
- [ ] Rate-limit connection attempts and reject malformed payloads.
- [ ] Define deletion behavior for local profile, keys, messages, and cached media.
- [ ] Threat-model screenshots, recipient copying, IP exposure, and relay metadata.
- [ ] Establish adult-only usage policy and appropriate safety measures.

## Important
Pseudonymous is not the same as anonymous. A peer connection may reveal IP addresses, and nearby radio broadcasts can be observed by others. Do not promise perfect anonymity or that shared images cannot be copied.
