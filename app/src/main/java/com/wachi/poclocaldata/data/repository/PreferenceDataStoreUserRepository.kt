package com.wachi.poclocaldata.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.dataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.preferencesDataStoreFile
import com.wachi.poclocaldata.data.model.UserPreferences
import com.wachi.poclocaldata.data.model.UserPreferencesSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private const val ENCRYPTED_USER_PREFERENCES_NAME = "encrypted_user_preferences"


class PreferenceDataStoreUserRepository(
    context: Context,
    sharedPref: SharedPreferences, // Optional, for migration purposes
) : UserRepository {

    private val encryptedPrefsDataStore = EncryptedPrefsDataStore(context, sharedPref)

    private object PreferencesKeys {
        val FIRST_NAME = stringPreferencesKey("first_name")
        val LAST_NAME = stringPreferencesKey("last_name")
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
    }

    override suspend fun saveUserData(user: UserPreferences) {
        encryptedPrefsDataStore.run {
            putEncryptedString(PreferencesKeys.FIRST_NAME, user.firstName)
            putEncryptedString(PreferencesKeys.LAST_NAME, user.lastName)
            putEncryptedString(PreferencesKeys.PHONE_NUMBER, user.phoneNumber)
        }
    }

    override fun getUserData(): Flow<UserPreferences> {
        return encryptedPrefsDataStore.run {
            val firstNameFlow = getEncryptedStringFlow(PreferencesKeys.FIRST_NAME)
            val lastNameFlow = getEncryptedStringFlow(PreferencesKeys.LAST_NAME)
            val phoneNumberFlow = getEncryptedStringFlow(PreferencesKeys.PHONE_NUMBER)

            combine(
                firstNameFlow,
                lastNameFlow,
                phoneNumberFlow
            ) { first, last, phone ->
                UserPreferences(first ?: "", last ?: "", phone ?: "")
            }
        }
    }
}