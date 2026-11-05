package com.airysdark.prototypeapp.discovery

import com.airysdark.prototypeapp.networking.NearbyPeer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Safe unconfigured discovery scaffold. Advertisements must use rotating opaque IDs only;
 * never broadcast names, age, photos, sexual preferences, or exact location.
 */
interface NearbyDiscovery {
    val peers: Flow<List<NearbyPeer>>
    suspend fun start(): Result<Unit>
    suspend fun stop()
}

class DiscoveryNotConfigured : NearbyDiscovery {
    override val peers: Flow<List<NearbyPeer>> = emptyFlow()
    override suspend fun start(): Result<Unit> =
        Result.failure(IllegalStateException("Nearby discovery is not implemented yet"))
    override suspend fun stop() = Unit
}
