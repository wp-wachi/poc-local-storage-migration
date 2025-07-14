package com.wachi.poclocaldata.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataMigration
import androidx.datastore.dataStore
import androidx.datastore.migrations.SharedPreferencesView
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wachi.poclocaldata.data.model.UserPreferences
import com.wachi.poclocaldata.data.model.UserPreferencesSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.apply
import kotlin.collections.remove
import kotlin.text.contains

private const val ENCRYPTED_USER_PREFERENCES_NAME = "encrypted_user_preferences"


class ProtoDataStoreUserRepository(
    private val context: Context,
    private val sharedPref: SharedPreferences,
) : UserRepository {

    private val Context.userDataStore by dataStore(
        fileName = ENCRYPTED_USER_PREFERENCES_NAME,
        serializer = UserPreferencesSerializer,
        produceMigrations = { context ->
            listOf(sharedPrefsMigration)
        }
    )

    private val sharedPrefsMigration = object : DataMigration<UserPreferences> {
        override suspend fun shouldMigrate(currentData: UserPreferences): Boolean {
            // Check if current UserPreferences is empty and SharedPreferences has our user data
            return (currentData.firstName.isEmpty() && currentData.lastName.isEmpty()) &&
                    (sharedPref.contains("first_name") || sharedPref.contains("last_name") ||
                            sharedPref.contains("phone_number"))
        }

        override suspend fun migrate(currentData: UserPreferences): UserPreferences {
            // Get user data from SharedPreferences
            val firstName = sharedPref.getString("first_name", "") ?: ""
            val lastName = sharedPref.getString("last_name", "") ?: ""
            val phoneNumber = sharedPref.getString("phone_number", "") ?: ""

            // Return a new UserPreferences with data from SharedPreferences
            return UserPreferences(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber
            )
        }

        override suspend fun cleanUp() {
            // Optional: clear SharedPreferences after migration
            sharedPref.edit()
                .remove("first_name")
                .remove("last_name")
                .remove("phone_number")
                .apply()
        }
    }

    override suspend fun saveUserData(user: UserPreferences) {
        context.userDataStore.updateData { preferences ->
            preferences.copy(
                firstName = user.firstName,
                lastName = user.lastName,
                phoneNumber = user.phoneNumber
            )
        }
    }

    override fun getUserData(): Flow<UserPreferences> = context.userDataStore.data.map { preferences ->
        UserPreferences(
            firstName = preferences.firstName,
            lastName = preferences.lastName,
            phoneNumber = preferences.phoneNumber
        )
    }
}