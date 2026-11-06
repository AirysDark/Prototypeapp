package com.airysdark.prototypeapp.data

import org.json.JSONObject

data class Profile(
    val pseudonym: String = "",
    val age: Int = 18,
    val bio: String = "",
    val lookingFor: String = "Chat and meet",
    val discoverable: Boolean = false
) {
    /**
     * Enforce the same limits at the data boundary as the UI. Callers must not be able to
     * persist oversized strings or an under-18 age by bypassing the profile editor.
     */
    fun normalized(): Profile = copy(
        pseudonym = pseudonym.trim().take(MAX_PSEUDONYM_LENGTH),
        age = age.coerceIn(18, 120),
        bio = bio.trim().take(MAX_BIO_LENGTH),
        lookingFor = lookingFor.trim().take(MAX_LOOKING_FOR_LENGTH)
    )

    fun toJson(): String {
        val safe = normalized()
        return JSONObject()
            .put("pseudonym", safe.pseudonym)
            .put("age", safe.age)
            .put("bio", safe.bio)
            .put("lookingFor", safe.lookingFor)
            .put("discoverable", safe.discoverable)
            .toString()
    }

    companion object {
        private const val MAX_PSEUDONYM_LENGTH = 32
        private const val MAX_BIO_LENGTH = 240
        private const val MAX_LOOKING_FOR_LENGTH = 80

        fun fromJson(json: String): Profile {
            val obj = JSONObject(json)
            return Profile(
                pseudonym = obj.optString("pseudonym", ""),
                age = obj.optInt("age", 18),
                bio = obj.optString("bio", ""),
                lookingFor = obj.optString("lookingFor", "Chat and meet"),
                discoverable = obj.optBoolean("discoverable", false)
            ).normalized()
        }
    }
}
