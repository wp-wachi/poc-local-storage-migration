package com.wachi.poclocaldata.data.repository

import com.wachi.poclocaldata.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun saveUserData(user: UserPreferences)
    fun getUserData(): Flow<UserPreferences>
}