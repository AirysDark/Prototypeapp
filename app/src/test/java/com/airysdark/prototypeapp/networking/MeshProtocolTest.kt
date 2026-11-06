package com.airysdark.prototypeapp.networking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MeshProtocolTest {
    @Test
    fun forwardedSearchDecrementsHopBudget() {
        val request = MeshSearchRequest.create(
            requesterId = "requester-1",
            minAge = 25,
            maxAge = 40,
            nowMillis = 1_000L,
            hopLimit = 3
        )

        assertEquals(3, request.hopsRemaining)
        assertEquals(2, request.forwarded()?.hopsRemaining)
        assertNull(request.copy(hopsRemaining = 0).forwarded())
    }

    @Test
    fun relayLedgerSuppressesDuplicatesAndStopsCompletedSearch() {
        val request = MeshSearchRequest.create(
            requesterId = "requester-1",
            minAge = 18,
            maxAge = 50,
            nowMillis = 1_000L
        )
        val ledger = MeshRelayLedger()

        assertTrue(ledger.shouldAccept(request, "peer-upstream", 1_000L))
        assertFalse(ledger.shouldAccept(request, "peer-other", 1_001L))
        assertEquals("peer-upstream", ledger.previousHopForReply(request.id, 1_002L))
        assertTrue(ledger.markCompleted(request.id, 1_003L))
        assertTrue(ledger.isCompleted(request.id, 1_004L))
        assertNull(ledger.previousHopForReply(request.id, 1_004L))
    }

    @Test
    fun cacheOnlyStoresShareableProfilesAndExpiresEntries() {
        val cache = MeshProfileCache(maxEntries = 5, ttlMillis = 5_000L)
        val privateProfile = CachedMeshProfile(
            pseudonymousId = "private",
            age = 30,
            interests = listOf("hiking"),
            shareable = false,
            receivedAtMillis = 1_000L,
            expiresAtMillis = 10_000L
        )
        val publicProfile = CachedMeshProfile(
            pseudonymousId = "shareable",
            age = 30,
            interests = listOf("Hiking"),
            coarseLocationLabel = "North side",
            shareable = true,
            receivedAtMillis = 1_000L,
            expiresAtMillis = 10_000L
        )

        cache.put(privateProfile, 1_000L)
        cache.put(publicProfile, 1_000L)
        assertEquals(1, cache.size(1_001L))

        val request = MeshSearchRequest.create(
            requesterId = "requester-1",
            minAge = 25,
            maxAge = 35,
            interests = listOf("hiking"),
            nowMillis = 1_000L
        )
        assertEquals("shareable", cache.findMatches(request, 1_002L).single().pseudonymousId)
        assertTrue(cache.findMatches(request, 6_000L).isEmpty())
        assertEquals(0, cache.size(6_000L))
    }

    @Test
    fun expiredSearchDoesNotMatchCachedProfiles() {
        val profile = CachedMeshProfile(
            pseudonymousId = "profile-1",
            age = 28,
            interests = emptyList(),
            shareable = true,
            receivedAtMillis = 1_000L,
            expiresAtMillis = 20_000L
        )
        val request = MeshSearchRequest.create(
            requesterId = "requester-1",
            minAge = 18,
            maxAge = 40,
            nowMillis = 1_000L,
            lifetimeMillis = 1_000L
        )

        assertFalse(request.matches(profile.copy(shareable = false)))
        assertTrue(request.isExpired(2_000L))
    }
}
