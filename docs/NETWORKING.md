# Hybrid peer-to-peer networking plan

## Nearby BLE
1. Ask for explicit opt-in and request only the Android permissions required by the OS version.
2. Advertise a rotating random identifier, not profile content or a permanent user ID.
3. Discover peers and show a user-approved minimal card only after a follow-up exchange.
4. Establish an authenticated session before sending private data.
5. Provide a visible stop-discovery control.

BLE range and behavior vary by handset and environment. BLE discovery alone is not a reliable high-bandwidth chat transport.

## Internet WebRTC
1. Exchange WebRTC offer/answer and ICE candidates through an authenticated signaling mechanism.
2. Use STUN for connectivity discovery; TURN may be needed when direct connections fail.
3. Use data channels for peer traffic.
4. Add application-level peer authentication and reviewed end-to-end encryption.
5. Make direct-connection IP exposure clear; offer relay-only mode if privacy requirements justify the cost.

## Signaling options
- A minimal relay service that forwards connection setup only.
- QR/manual exchange for a no-account prototype, suitable only for peers physically together.
- A later federated signaling design.

No signaling endpoint is configured in this repository. Internet P2P is therefore not active yet.
