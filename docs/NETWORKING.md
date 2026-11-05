# Hybrid peer-to-peer networking

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
- The current prototype has not been built or device-tested yet.

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
