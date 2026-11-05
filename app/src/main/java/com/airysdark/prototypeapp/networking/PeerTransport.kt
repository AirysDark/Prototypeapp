package com.airysdark.prototypeapp.networking

interface PeerTransport {
    val transportName: String
    suspend fun connect(peerId: String): Result<Unit>
    suspend fun send(peerId: String, payload: ByteArray): Result<Unit>
    suspend fun disconnect(peerId: String)
}

data class NearbyPeer(
    val ephemeralId: String,
    val displayName: String,
    val approximateDistanceMeters: Int? = null,
    val transport: String
)
