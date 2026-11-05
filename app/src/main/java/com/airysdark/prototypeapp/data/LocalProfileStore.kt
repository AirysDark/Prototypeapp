package com.airysdark.prototypeapp.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Starter store using AES-GCM and an Android Keystore-managed key. */
class LocalProfileStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("profile_v1", Context.MODE_PRIVATE)

    fun load(): Profile {
        val blob = prefs.getString(KEY_PROFILE, null) ?: return Profile()
        return runCatching {
            val packed = Base64.decode(blob, Base64.NO_WRAP)
            require(packed.size > IV_LENGTH)
            val iv = packed.copyOfRange(0, IV_LENGTH)
            val ciphertext = packed.copyOfRange(IV_LENGTH, packed.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
            Profile.fromJson(String(cipher.doFinal(ciphertext), Charsets.UTF_8))
        }.getOrElse { Profile() }
    }

    fun save(profile: Profile) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.iv + cipher.doFinal(profile.toJson().toByteArray(Charsets.UTF_8))
        check(prefs.edit().putString(KEY_PROFILE, Base64.encodeToString(encrypted, Base64.NO_WRAP)).commit()) {
            "Unable to save local profile"
        }
    }

    fun clear() { prefs.edit().remove(KEY_PROFILE).apply() }

    private fun getOrCreateKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(
            KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setKeySize(256).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true).build())
        return generator.generateKey()
    }

    private companion object {
        const val KEY_ALIAS = "prototypeapp_profile_key_v1"
        const val KEY_PROFILE = "encrypted_profile"
        const val IV_LENGTH = 12
    }
}
