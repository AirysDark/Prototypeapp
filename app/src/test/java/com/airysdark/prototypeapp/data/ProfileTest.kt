package com.airysdark.prototypeapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileTest {
    @Test
    fun normalizedProfileTrimsAndLimitsFields() {
        val profile = Profile(
            pseudonym = "  " + "A".repeat(40) + "  ",
            age = 140,
            bio = " B ".repeat(100),
            lookingFor = " C ".repeat(40),
            discoverable = true
        ).normalized()

        assertEquals(32, profile.pseudonym.length)
        assertEquals(120, profile.age)
        assertEquals(240, profile.bio.length)
        assertEquals(80, profile.lookingFor.length)
        assertTrue(profile.discoverable)
    }

    @Test
    fun profileJsonRoundTrips() {
        val original = Profile(
            pseudonym = "NightOwl",
            age = 29,
            bio = "Coffee and conversation.",
            lookingFor = "Chat and meet",
            discoverable = true
        )

        assertEquals(original, Profile.fromJson(original.toJson()))
    }

    @Test
    fun defaultProfileIsNotDiscoverable() {
        assertFalse(Profile().discoverable)
    }

    @Test
    fun oldOrIncompleteJsonGetsSafeDefaults() {
        val profile = Profile.fromJson("""{"pseudonym":"Tester"}""")

        assertEquals("Tester", profile.pseudonym)
        assertEquals(18, profile.age)
        assertEquals("Chat and meet", profile.lookingFor)
        assertFalse(profile.discoverable)
    }
}
