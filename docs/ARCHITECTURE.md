# Architecture

## Goals
- Android-first native Kotlin application.
- Local-first profile data and pseudonymous identity.
- Separate UI, storage, discovery, and transport layers.
- No central user-profile database.

## Data flow
1. The user edits a profile in Compose.
2. The app encrypts the serialized profile using AES-GCM.
3. The encryption key is generated and retained by Android Keystore.
4. Discovery, when implemented and explicitly enabled, advertises a rotating opaque identifier only.
5. A peer connection exchanges only user-approved profile fields.
6. Messaging will use authenticated end-to-end encryption; transport encryption alone is not enough.

## Components
- `data/Profile.kt`: profile model and serialization.
- `data/LocalProfileStore.kt`: starter encrypted single-profile store.
- `identity/LocalIdentity.kt`: app-local random pseudonymous ID.
- `discovery/NearbyDiscovery.kt`: discovery interface and safe unconfigured implementation.
- `networking/PeerTransport.kt`: transport contract.
- `networking/SignalingClient.kt`: interface for WebRTC setup signaling.
- `ui/HomeScreen.kt`: initial profile editor.

## Hybrid networking
- Nearby mode: BLE advertisements and scanning; exchange minimal opaque IDs, then establish a safer authenticated session over a suitable nearby transport.
- Internet mode: WebRTC data channels. WebRTC needs signaling (offer/answer and ICE candidate exchange); STUN helps NAT discovery and TURN may relay traffic where direct connectivity fails.
- Signaling should not store profiles or messages. A direct connection can expose peer IP addresses, so explain that limitation to users.

## Known starter limitations
- Encrypted profile storage is a small starter implementation, not a full database or secure chat store.
- The local pseudonym is not an account credential and does not prove age.
- Discovery, WebRTC, cryptographic peer authentication, photo sharing, chat, block/report, and account recovery are not yet implemented.
- Discovery preference currently saves locally but does not broadcast.
