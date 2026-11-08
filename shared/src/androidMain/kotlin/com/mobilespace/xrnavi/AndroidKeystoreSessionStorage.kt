package com.mobilespace.xrnavi

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Stores the session encrypted with an AES-GCM key that never leaves the Android Keystore. */
class AndroidKeystoreSessionStorage(context: Context) : SecureSessionStorage {
    private val preferences = context.applicationContext.getSharedPreferences(
        "xrnavi_session", Context.MODE_PRIVATE
    )

    override fun read(): String? = runCatching {
        val packed = Base64.decode(preferences.getString(entry, null) ?: return null, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, packed.copyOfRange(0, ivSize)))
        String(cipher.doFinal(packed, ivSize, packed.size - ivSize), Charsets.UTF_8)
    }.getOrNull()

    override fun write(value: String): Boolean = runCatching {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val packed = cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        preferences.edit().putString(entry, Base64.encodeToString(packed, Base64.NO_WRAP)).commit()
    }.getOrDefault(false)

    override fun clear() {
        preferences.edit().remove(entry).commit()
    }

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(
                    alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
        }.generateKey()
    }

    private companion object {
        const val alias = "xrnavi_session_key"
        const val entry = "session"
        const val transformation = "AES/GCM/NoPadding"
        const val ivSize = 12
    }
}
