package com.wachi.poclocaldata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wachi.poclocaldata.data.repository.DataStoreUserRepository
import com.wachi.poclocaldata.data.repository.SharedPreferencesUserRepository
import com.wachi.poclocaldata.ui.home.HomeScreen
import com.wachi.poclocaldata.ui.home.HomeViewModel
import com.wachi.poclocaldata.ui.theme.PoclocaldataTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = SharedPreferencesUserRepository(applicationContext)
        val dataStoreUserRepository = DataStoreUserRepository(applicationContext, repository.encryptedPrefs)
        val viewModelFactory = HomeViewModel.Factory(repository, dataStoreUserRepository)

        setContent {
            PoclocaldataTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}