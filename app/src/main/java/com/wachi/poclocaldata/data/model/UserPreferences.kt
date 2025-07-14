package com.wachi.poclocaldata.data.model

import android.util.Log
import androidx.datastore.core.Serializer
import com.wachi.poclocaldata.data.repository.Crypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.util.Base64

@Serializable
data class UserPreferences(
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
)

object UserPreferencesSerializer: Serializer<UserPreferences> {
    override val defaultValue: UserPreferences
        get() = UserPreferences()

    override suspend fun readFrom(input: InputStream): UserPreferences {
        val encryptedBytes = withContext(Dispatchers.IO) {
            input.use {
                it.readBytes()
            }
        }
        Log.i("UserPreferencesSerializer", "readFrom - encryptedBytes: ${encryptedBytes.decodeToString()}")
        val encryptedBytesDecoded = Base64.getDecoder().decode(encryptedBytes)
        Log.i("UserPreferencesSerializer", "readFrom - encryptedBytesDecoded: ${encryptedBytesDecoded.decodeToString()}")
        val decryptedByte = Crypto.decrypt(encryptedBytesDecoded)
        Log.i("UserPreferencesSerializer", "readFrom - decryptedByte: ${decryptedByte.decodeToString()}")
        val decodedJsonString = decryptedByte.decodeToString()
        Log.i("UserPreferencesSerializer", "readFrom - decodedJsonString: $decodedJsonString")
        return Json.decodeFromString(decodedJsonString)
    }

    override suspend fun writeTo(
        t: UserPreferences,
        output: OutputStream,
    ) {
        val json = Json.encodeToString(t)
        Log.i("UserPreferencesSerializer", "writeTo - json: $json")
        val bytes = json.toByteArray()
        Log.i("UserPreferencesSerializer", "writeTo - bytes: ${bytes.decodeToString()}")
        val encryptedBytes = Crypto.encrypt(bytes)
        Log.i("UserPreferencesSerializer", "writeTo - encryptedBytes: ${encryptedBytes.decodeToString()}")
        val encryptedBytesBase64 = Base64.getEncoder().encodeToString(encryptedBytes)
        Log.i("UserPreferencesSerializer", "writeTo - encryptedBytesBase64: $encryptedBytesBase64")
        withContext(Dispatchers.IO) {
            output.use {
                it.write(encryptedBytesBase64.toByteArray())
            }
        }
    }
}