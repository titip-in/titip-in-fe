package com.titipin.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.local.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SplashDestination { Loading, Onboarding, SetupProfile, Home, Login }

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _destination = MutableStateFlow(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            kotlinx.coroutines.delay(800)

            val hasSeenOnboarding = dataStore.hasSeenOnboarding.first()
            if (!hasSeenOnboarding) {
                _destination.value = SplashDestination.Onboarding
                return@launch
            }

            val token = dataStore.accessToken.first()
            _destination.value = if (!token.isNullOrEmpty()) {
                val emailVerifiedAt = dataStore.userEmailVerifiedAt.first()
                val waVerifiedAt = dataStore.userWaVerifiedAt.first()
                if (emailVerifiedAt.isNullOrBlank() || waVerifiedAt.isNullOrBlank()) {
                    SplashDestination.SetupProfile
                } else {
                    SplashDestination.Home
                }
            } else {
                SplashDestination.Login
            }
        }
    }
}
