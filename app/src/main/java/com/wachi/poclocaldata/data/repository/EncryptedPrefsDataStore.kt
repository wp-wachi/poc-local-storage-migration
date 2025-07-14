package com.wachi.poclocaldata.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val ENCRYPTED_PREFERENCES_NAME = "encrypted_preferences"

class EncryptedPrefsDataStore(
    private val context: Context,
    private val sharedPref: SharedPreferences,
) {

    val Context.ds by preferencesDataStore(
        name = ENCRYPTED_PREFERENCES_NAME,
        produceMigrations = { context ->
            listOf(SharedPreferencesMigration({ sharedPref }))
        }
    )


    /* store the whole (IV + cipher) blob under a single key */
    private fun blobKey(name: String) = byteArrayPreferencesKey("${name}_blob")

    /** Save any String securely */
    suspend fun putEncryptedString(name: String, value: String) {
        val blob = Crypto.encryptWithIV(value.encodeToByteArray())
        context.ds.edit { it[blobKey(name)] = blob }
    }

    /** Observe the decrypted String as Flow<String?> */
    fun stringFlow(name: String): Flow<String?> = context.ds.data.map { prefs ->
        prefs[blobKey(name)]?.let { blob ->
            Crypto.decryptWithIV(blob).decodeToString()
        }
    }

    /** One-shot synchronous fetch (e.g. in a delegate) */
    suspend fun getString(name: String): String? =
        context.ds.data.first()[blobKey(name)]?.let { Crypto.decryptWithIV(it).decodeToString() }



    suspend fun putEncryptedString(key: Preferences.Key<String>, value: String) {
        val encrypted = Crypto.encryptWithIV(value.toByteArray())
        context.ds.edit { it[blobKey(key.name)] = encrypted }
    }

    fun getEncryptedStringFlow(key: Preferences.Key<String>): Flow<String?> =
        context.ds.data.map { prefs ->
            prefs[blobKey(key.name)]?.let { encrypted ->
                Crypto.decryptWithIV(encrypted).toString(Charsets.UTF_8)
            }
        }

    suspend fun getEncryptedString(key: Preferences.Key<String>): String? =
        context.ds.data.first()[blobKey(key.name)]?.let {
            Crypto.decryptWithIV(it).toString(Charsets.UTF_8)
        }
}