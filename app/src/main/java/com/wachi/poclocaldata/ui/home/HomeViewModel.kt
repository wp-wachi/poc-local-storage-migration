package com.wachi.poclocaldata.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wachi.poclocaldata.data.model.UserPreferences
import com.wachi.poclocaldata.data.repository.ProtoDataStoreUserRepository
import com.wachi.poclocaldata.data.repository.SharedPreferencesUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(
    private val sharedPreferencesUserRepository: SharedPreferencesUserRepository,
    private val dataStoreUserRepository: ProtoDataStoreUserRepository,
) : ViewModel() {

    private val _user = MutableStateFlow(UserPreferences())
    val user: StateFlow<UserPreferences> = _user


    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            dataStoreUserRepository.getUserData().collectLatest { userData ->
                _user.value = userData
            }
        }
    }

    fun saveUserData(user: UserPreferences) {
        viewModelScope.launch {
            sharedPreferencesUserRepository.saveUserData(user)
            loadUserData()
        }
    }

    fun saveUserDataToDataStore(user: UserPreferences) {
        viewModelScope.launch {
            dataStoreUserRepository.saveUserData(user)
        }
    }

    class Factory(
        private val sharedPreferencesUserRepository: SharedPreferencesUserRepository,
        private val dataStoreUserRepository: ProtoDataStoreUserRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(sharedPreferencesUserRepository, dataStoreUserRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}