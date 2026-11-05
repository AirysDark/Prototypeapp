package com.airysdark.prototypeapp.data

import org.json.JSONObject

data class Profile(
    val pseudonym: String = "",
    val age: Int = 18,
    val bio: String = "",
    val lookingFor: String = "Chat and meet",
    val discoverable: Boolean = false
) {
    fun toJson(): String = JSONObject()
        .put("pseudonym", pseudonym)
        .put("age", age)
        .put("bio", bio)
        .put("lookingFor", lookingFor)
        .put("discoverable", discoverable)
        .toString()

    companion object {
        fun fromJson(json: String): Profile {
            val obj = JSONObject(json)
            return Profile(
                pseudonym = obj.optString("pseudonym", ""),
                age = obj.optInt("age", 18).coerceIn(18, 120),
                bio = obj.optString("bio", ""),
                lookingFor = obj.optString("lookingFor", "Chat and meet"),
                discoverable = obj.optBoolean("discoverable", false)
            )
        }
    }
}
