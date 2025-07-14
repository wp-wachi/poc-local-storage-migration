package com.wachi.poclocaldata.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import androidx.core.content.edit
import com.wachi.poclocaldata.data.model.UserPreferences

class SharedPreferencesUserRepository(private val context: Context) : UserRepository {
    
    private val regularPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    }
    
    val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        EncryptedSharedPreferences.create(
            context,
            "encrypted_user_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    // Use encrypted prefs for demonstration
    private val prefs get() = encryptedPrefs
    
    override suspend fun saveUserData(user: UserPreferences) {
        prefs.edit {
            putString("first_name", user.firstName)
                .putString("last_name", user.lastName)
                .putString("phone_number", user.phoneNumber)
        }
    }
    
    override fun getUserData(): Flow<UserPreferences> = flow {
        val user = UserPreferences(
            firstName = prefs.getString("first_name", "") ?: "",
            lastName = prefs.getString("last_name", "") ?: "",
            phoneNumber = prefs.getString("phone_number", "") ?: ""
        )
        emit(user)
    }
}