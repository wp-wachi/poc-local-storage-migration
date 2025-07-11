package com.wachi.poclocaldata.data.repository

import com.wachi.poclocaldata.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun saveUserData(user: User)
    fun getUserData(): Flow<User>
}