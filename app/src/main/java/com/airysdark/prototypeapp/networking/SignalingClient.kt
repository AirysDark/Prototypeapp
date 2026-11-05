package com.airysdark.prototypeapp.networking

/**
 * WebRTC needs offer/answer and ICE-candidate exchange. No endpoint is hard-coded.
 * Signaling should carry connection setup only, not profiles or chat archives.
 */
interface SignalingClient {
    suspend fun sendSignal(peerId: String, signal: String): Result<Unit>
    suspend fun close()
}
