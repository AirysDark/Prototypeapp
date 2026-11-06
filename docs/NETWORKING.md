# Hybrid peer-to-peer networking

## Mesh core — first implementation slice

The transport-independent mesh core is in `networking/MeshProtocol.kt`. It is a protocol foundation, not yet connected to the UI or BLE transport.

- Versioned JSON search packet format (`v=1`) with an 8 KiB encoded packet ceiling.
- Search IDs are random UUIDs; requests have bounded lifetime (maximum 10 minutes) and hop count (maximum 8).
- Search matching uses age range and optional interests against locally cached, explicitly shareable profile summaries.
- The in-memory profile cache has a configurable entry limit, expires records, and stores only an optional coarse location label—not coordinates.
- The relay ledger suppresses duplicate request IDs, tracks the previous hop locally for future reply routing, and marks completed searches so they can be stopped.
- Cache and ledger are in-memory only; process death clears them.

### Current limitations and safety boundaries

- The mesh core is not yet wired to BLE GATT or any other transport. No multi-hop network is active.
- Search packet JSON is not encrypted or authenticated yet. Do not send it over an untrusted transport until authenticated encryption and peer/session validation are implemented.
- Reverse-path storage is only a local ledger primitive; result packets and completion/withdrawal notices are not implemented.
- Profile caching is only an in-memory model and policy. There is no profile exchange UI or consent flow feeding it yet.
- The cache is not a location tracker. Precise GPS coordinates must never be included in mesh packets; only a user-approved coarse label may be shared.
- A completion notice cannot reach disconnected relays reliably. Every request therefore expires automatically, and future transport code must enforce expiry even if a stop notice is missed.
- Matching a cached summary does not prove the profile is authentic or that its owner is nearby.

## Nearby BLE — v0.2 prototype

The Android app currently scans for and advertises custom BLE service data while the user has explicitly opted in and pressed Start. The advertisement contains only an 8-byte cryptographically random token and the app's custom service-data UUID. It does not include a display name, age, photo, preferences, local account ID, or GPS coordinates.

- Token rotation target: every 15 minutes.
- Old peer entries expire after approximately 75 seconds without a scan result.
- Scanning uses balanced power mode.
- Advertising is non-connectable; it is presence signaling only.
- Discovery stops when the user presses Stop, opts out, or the activity leaves the foreground.
- Runtime permissions are requested for the relevant Android version.

Important limitations:
- A rotating token is not proof of identity and is not an authenticated session.
- Nearby results currently show only a generic “Nearby member” label and temporary token prefix.
- No profile exchange, BLE GATT connection, messaging, or key verification is implemented.
- Android and handset BLE stacks may throttle, reject, or alter scan/advertising behavior. Test on at least two physical devices.

## Planned implementation order

1. Add unit tests for packet bounds, expiry, deduplication, hop limits, cache expiry, and matching.
2. Add explicit user consent and profile-sharing settings.
3. Implement authenticated BLE GATT sessions and a bounded transport envelope.
4. Wire the mesh search/cache/ledger into BLE connections, then add result routing and completion notices.
5. Add encrypted store-and-forward messaging with end-to-end encryption.
6. Test with at least three physical Android devices, including disconnected and reconnecting peers.

## Internet WebRTC — not implemented

1. Exchange WebRTC offer/answer and ICE candidates through an authenticated signaling mechanism.
2. Use STUN for connectivity discovery; TURN may be needed when direct connections fail.
3. Use data channels for peer traffic.
4. Add application-level peer authentication and reviewed end-to-end encryption.
5. Make direct-connection IP exposure clear; consider relay-only mode if privacy requirements justify the cost.

## Signaling options

- A minimal relay service that forwards connection setup only and does not store profile data.
- QR/manual exchange for a no-account prototype, suitable only for peers physically together.
- A later federated signaling design.

No signaling endpoint is configured in this repository. Internet P2P is not active.
