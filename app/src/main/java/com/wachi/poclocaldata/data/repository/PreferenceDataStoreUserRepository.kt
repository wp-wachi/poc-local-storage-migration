package com.wachi.poclocaldata.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.dataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wachi.poclocaldata.data.model.UserPreferences
import com.wachi.poclocaldata.data.model.UserPreferencesSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val ENCRYPTED_USER_PREFERENCES_NAME = "encrypted_user_preferences"


class PreferenceDataStoreUserRepository(
    private val context: Context,
    private val sharedPref: SharedPreferences, // Optional, for migration purposes
) : UserRepository {

    private val Context.userDataStore by preferencesDataStore(
        name = ENCRYPTED_USER_PREFERENCES_NAME,
        produceMigrations = { context ->
            listOf(SharedPreferencesMigration({ sharedPref }))
        }
    )

    private object PreferencesKeys {
        val FIRST_NAME = stringPreferencesKey("first_name")
        val LAST_NAME = stringPreferencesKey("last_name")
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
    }

    override suspend fun saveUserData(user: UserPreferences) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_NAME] = user.firstName
            preferences[PreferencesKeys.LAST_NAME] = user.lastName
            preferences[PreferencesKeys.PHONE_NUMBER] = user.phoneNumber
        }
    }

    override fun getUserData(): Flow<UserPreferences> = context.userDataStore.data.map { preferences ->
        UserPreferences(
            firstName = preferences[PreferencesKeys.FIRST_NAME] ?: "",
            lastName = preferences[PreferencesKeys.LAST_NAME] ?: "",
            phoneNumber = preferences[PreferencesKeys.PHONE_NUMBER] ?: ""
        )
    }
}