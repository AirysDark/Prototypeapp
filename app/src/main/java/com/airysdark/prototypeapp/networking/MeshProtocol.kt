package com.airysdark.prototypeapp.networking

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Transport-independent first slice of the mesh protocol.
 *
 * This file defines bounded, expiring search packets and an in-memory relay/cache policy.
 * It does NOT implement BLE GATT, packet encryption, durable storage, or reverse-path transport.
 */
object MeshProtocol {
    const val VERSION = 1
    const val MAX_PACKET_BYTES = 8 * 1024
    const val MAX_HOPS = 8
    const val MAX_INTERESTS = 12
    const val MAX_INTEREST_LENGTH = 32
    const val MAX_REQUEST_LIFETIME_MS = 10 * 60 * 1000L

    fun newSearchId(): String = UUID.randomUUID().toString()

    fun encodeSearch(request: MeshSearchRequest): ByteArray {
        val safe = request.normalized()
        val json = JSONObject()
            .put("v", VERSION)
            .put("type", "search")
            .put("id", safe.id)
            .put("requester", safe.requesterId)
            .put("minAge", safe.minAge)
            .put("maxAge", safe.maxAge)
            .put("interests", JSONArray(safe.interests))
            .put("createdAt", safe.createdAtMillis)
            .put("expiresAt", safe.expiresAtMillis)
            .put("hopLimit", safe.hopLimit)
            .put("hopsRemaining", safe.hopsRemaining)
            .toString()
            .toByteArray(Charsets.UTF_8)
        require(json.size <= MAX_PACKET_BYTES) { "Mesh packet exceeds size limit" }
        return json
    }

    fun decodeSearch(bytes: ByteArray, nowMillis: Long = System.currentTimeMillis()): MeshSearchRequest {
        require(bytes.isNotEmpty() && bytes.size <= MAX_PACKET_BYTES) { "Invalid mesh packet size" }
        val json = JSONObject(bytes.toString(Charsets.UTF_8))
        require(json.optInt("v", -1) == VERSION) { "Unsupported mesh protocol version" }
        require(json.optString("type") == "search") { "Unexpected mesh packet type" }
        val interestsJson = json.optJSONArray("interests") ?: JSONArray()
        require(interestsJson.length() <= MAX_INTERESTS) { "Too many interests" }
        val interests = (0 until interestsJson.length()).map { interestsJson.optString(it) }
        val request = MeshSearchRequest(
            id = json.optString("id"),
            requesterId = json.optString("requester"),
            minAge = json.optInt("minAge", 18),
            maxAge = json.optInt("maxAge", 120),
            interests = interests,
            createdAtMillis = json.optLong("createdAt", 0L),
            expiresAtMillis = json.optLong("expiresAt", 0L),
            hopLimit = json.optInt("hopLimit", 0),
            hopsRemaining = json.optInt("hopsRemaining", -1)
        ).normalized()
        require(request.id.isNotBlank() && request.requesterId.isNotBlank()) { "Missing request identity" }
        require(request.createdAtMillis <= nowMillis + 60_000L) { "Request creation time is in the future" }
        require(request.expiresAtMillis > nowMillis) { "Search request has expired" }
        require(request.expiresAtMillis - request.createdAtMillis <= MAX_REQUEST_LIFETIME_MS) {
            "Search lifetime exceeds limit"
        }
        return request
    }
}

data class MeshSearchRequest(
    val id: String,
    val requesterId: String,
    val minAge: Int,
    val maxAge: Int,
    val interests: List<String>,
    val createdAtMillis: Long,
    val expiresAtMillis: Long,
    val hopLimit: Int,
    val hopsRemaining: Int
) {
    fun normalized(): MeshSearchRequest = copy(
        id = id.trim().take(64),
        requesterId = requesterId.trim().take(64),
        minAge = minAge.coerceIn(18, 120),
        maxAge = maxAge.coerceIn(18, 120),
        interests = interests.asSequence()
            .map { it.trim().take(MeshProtocol.MAX_INTEREST_LENGTH) }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(MeshProtocol.MAX_INTERESTS)
            .toList(),
        hopLimit = hopLimit.coerceIn(0, MeshProtocol.MAX_HOPS),
        hopsRemaining = hopsRemaining.coerceIn(0, MeshProtocol.MAX_HOPS)
    )

    fun isExpired(nowMillis: Long = System.currentTimeMillis()): Boolean =
        nowMillis >= expiresAtMillis

    fun forwarded(): MeshSearchRequest? {
        val safe = normalized()
        if (safe.hopsRemaining <= 0) return null
        return safe.copy(hopsRemaining = safe.hopsRemaining - 1)
    }

    fun matches(profile: CachedMeshProfile): Boolean {
        val safe = normalized()
        if (!profile.shareable || profile.age !in safe.minAge..safe.maxAge) return false
        if (safe.interests.isEmpty()) return true
        val profileInterests = profile.interests.map { it.lowercase() }.toSet()
        return safe.interests.any { it.lowercase() in profileInterests }
    }

    companion object {
        fun create(
            requesterId: String,
            minAge: Int,
            maxAge: Int,
            interests: List<String> = emptyList(),
            nowMillis: Long = System.currentTimeMillis(),
            lifetimeMillis: Long = 2 * 60 * 1000L,
            hopLimit: Int = 4
        ): MeshSearchRequest {
            require(requesterId.isNotBlank()) { "Requester ID is required" }
            require(lifetimeMillis in 1..MeshProtocol.MAX_REQUEST_LIFETIME_MS) { "Invalid lifetime" }
            val hops = hopLimit.coerceIn(0, MeshProtocol.MAX_HOPS)
            return MeshSearchRequest(
                id = MeshProtocol.newSearchId(),
                requesterId = requesterId,
                minAge = minAge,
                maxAge = maxAge,
                interests = interests,
                createdAtMillis = nowMillis,
                expiresAtMillis = nowMillis + lifetimeMillis,
                hopLimit = hops,
                hopsRemaining = hops
            ).normalized()
        }
    }
}

/**
 * Deliberately minimal cache record. Location is an optional user-approved coarse label,
 * never coordinates. Only records explicitly marked shareable can satisfy searches.
 */
data class CachedMeshProfile(
    val pseudonymousId: String,
    val age: Int,
    val interests: List<String>,
    val coarseLocationLabel: String? = null,
    val shareable: Boolean,
    val receivedAtMillis: Long = System.currentTimeMillis(),
    val expiresAtMillis: Long = receivedAtMillis + DEFAULT_TTL_MS
) {
    fun isExpired(nowMillis: Long = System.currentTimeMillis()): Boolean =
        nowMillis >= expiresAtMillis

    fun normalized(): CachedMeshProfile = copy(
        pseudonymousId = pseudonymousId.trim().take(64),
        age = age.coerceIn(18, 120),
        interests = interests.asSequence()
            .map { it.trim().take(MeshProtocol.MAX_INTEREST_LENGTH) }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(MeshProtocol.MAX_INTERESTS)
            .toList(),
        coarseLocationLabel = coarseLocationLabel?.trim()?.take(48)?.takeIf { it.isNotEmpty() }
    )

    companion object {
        const val DEFAULT_TTL_MS = 30 * 60 * 1000L
    }
}

/**
 * In-memory bounded cache. A later milestone can persist encrypted records with explicit
 * consent and withdrawal handling. No profile is cached unless it is marked shareable.
 */
class MeshProfileCache(
    private val maxEntries: Int = 200,
    private val ttlMillis: Long = CachedMeshProfile.DEFAULT_TTL_MS
) {
    private val entries = LinkedHashMap<String, CachedMeshProfile>()

    init {
        require(maxEntries in 1..2_000)
        require(ttlMillis in 1..24 * 60 * 60 * 1000L)
    }

    @Synchronized
    fun put(profile: CachedMeshProfile, nowMillis: Long = System.currentTimeMillis()) {
        prune(nowMillis)
        val safe = profile.normalized()
        if (!safe.shareable || safe.pseudonymousId.isBlank() || safe.isExpired(nowMillis)) return
        entries.remove(safe.pseudonymousId)
        entries[safe.pseudonymousId] = safe.copy(
            receivedAtMillis = nowMillis,
            expiresAtMillis = minOf(safe.expiresAtMillis, nowMillis + ttlMillis)
        )
        while (entries.size > maxEntries) {
            val oldest = entries.keys.firstOrNull() ?: break
            entries.remove(oldest)
        }
    }

    @Synchronized
    fun findMatches(request: MeshSearchRequest, nowMillis: Long = System.currentTimeMillis()): List<CachedMeshProfile> {
        prune(nowMillis)
        if (request.isExpired(nowMillis)) return emptyList()
        return entries.values.filter { request.matches(it) }
    }

    @Synchronized
    fun remove(pseudonymousId: String) {
        entries.remove(pseudonymousId)
    }

    @Synchronized
    fun clear() {
        entries.clear()
    }

    @Synchronized
    fun size(nowMillis: Long = System.currentTimeMillis()): Int {
        prune(nowMillis)
        return entries.size
    }

    @Synchronized
    private fun prune(nowMillis: Long) {
        val iterator = entries.entries.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().value.isExpired(nowMillis)) iterator.remove()
        }
    }
}

/**
 * Keeps short-lived deduplication/completion state and the previous hop for replies.
 * Previous-hop routing is local-only; it is not serialized into search packets.
 */
class MeshRelayLedger(
    private val maxTrackedRequests: Int = 1_000,
    private val stateTtlMillis: Long = MeshProtocol.MAX_REQUEST_LIFETIME_MS
) {
    private data class Entry(
        val expiresAtMillis: Long,
        var completed: Boolean = false,
        var previousHopPeerId: String? = null
    )

    private val entries = LinkedHashMap<String, Entry>()

    init {
        require(maxTrackedRequests in 1..10_000)
        require(stateTtlMillis in 1..MeshProtocol.MAX_REQUEST_LIFETIME_MS)
    }

    @Synchronized
    fun shouldAccept(request: MeshSearchRequest, previousHopPeerId: String?, nowMillis: Long = System.currentTimeMillis()): Boolean {
        prune(nowMillis)
        if (request.isExpired(nowMillis) || request.hopsRemaining > request.hopLimit) return false
        if (entries.containsKey(request.id)) return false
        entries[request.id] = Entry(
            expiresAtMillis = minOf(request.expiresAtMillis, nowMillis + stateTtlMillis),
            previousHopPeerId = previousHopPeerId?.take(64)
        )
        while (entries.size > maxTrackedRequests) {
            entries.remove(entries.keys.firstOrNull() ?: break)
        }
        return true
    }

    @Synchronized
    fun previousHopForReply(requestId: String, nowMillis: Long = System.currentTimeMillis()): String? {
        prune(nowMillis)
        return entries[requestId]?.takeUnless { it.completed }?.previousHopPeerId
    }

    @Synchronized
    fun markCompleted(requestId: String, nowMillis: Long = System.currentTimeMillis()): Boolean {
        prune(nowMillis)
        val entry = entries[requestId] ?: return false
        entry.completed = true
        entry.previousHopPeerId = null
        entry.expiresAtMillis.coerceAtMost(nowMillis + stateTtlMillis)
        return true
    }

    @Synchronized
    fun isCompleted(requestId: String, nowMillis: Long = System.currentTimeMillis()): Boolean {
        prune(nowMillis)
        return entries[requestId]?.completed == true
    }

    @Synchronized
    private fun prune(nowMillis: Long) {
        val iterator = entries.entries.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().value.expiresAtMillis <= nowMillis) iterator.remove()
        }
    }
}
