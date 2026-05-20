package com.titipin.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.model.AnalyticsData
import com.titipin.app.data.repository.AnalyticsRepository
import com.titipin.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    data class Success(val data: AnalyticsData, val tier: String) : AnalyticsUiState()
    /** 403 = tier tidak cukup */
    data class Paywall(val message: String) : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val authRepository: com.titipin.app.data.repository.AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init { loadAnalytics() }

    fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.value = AnalyticsUiState.Loading
            
            val meResult = authRepository.getMe()
            val tier = if (meResult is Result.Success) meResult.data.tier else com.titipin.app.data.model.UserTier.BASIC
            
            _uiState.value = when (val result = analyticsRepository.getAnalytics()) {
                is Result.Success -> AnalyticsUiState.Success(result.data, tier)
                is Result.Error   -> {
                    if (result.httpCode == 403) {
                        AnalyticsUiState.Paywall(result.message)
                    } else {
                        AnalyticsUiState.Error(result.message)
                    }
                }
            }
        }
    }
}
