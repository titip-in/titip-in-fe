package com.titipin.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.local.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dataStore: DataStoreManager
) : ViewModel() {

    fun markSeen() {
        viewModelScope.launch {
            dataStore.markOnboardingSeen()
        }
    }
}