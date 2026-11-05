package com.airysdark.prototypeapp.identity

import android.content.Context
import java.util.UUID

/** App-local random pseudonym identifier; not an account credential or proof of age. */
class LocalIdentity(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("identity_v1", Context.MODE_PRIVATE)

    fun getOrCreateId(): String {
        prefs.getString(KEY_ID, null)?.let { return it }
        val id = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_ID, id).apply()
        return prefs.getString(KEY_ID, id) ?: id
    }

    private companion object { const val KEY_ID = "local_pseudonym_id" }
}
