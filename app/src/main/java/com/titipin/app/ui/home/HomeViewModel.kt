package com.titipin.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.JastipDto
import com.titipin.app.data.model.PrelovedDto
import com.titipin.app.data.repository.JastipRepository
import com.titipin.app.data.repository.PrelovedRepository
import com.titipin.app.data.repository.RequestRepository
import com.titipin.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val userInitials: String = "",
    val allJastip: List<JastipDto> = emptyList(),
    val allPreloved: List<PrelovedDto> = emptyList(),
    val recentJastip: List<JastipDto> = emptyList(),
    val recentPreloved: List<PrelovedDto> = emptyList(),
    val requestCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataStore: DataStoreManager,
    private val jastipRepository: JastipRepository,
    private val prelovedRepository: PrelovedRepository,
    private val requestRepository: RequestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            val name = dataStore.userName.first() ?: ""
            val initials = name.trim()
                .split(" ").filter { it.isNotBlank() }
                .take(2).joinToString("") { it.first().uppercase() }

            _uiState.value = _uiState.value.copy(
                userName     = name,
                userInitials = initials
            )

            val jastipResult   = jastipRepository.getJastipList()
            val prelovedResult = prelovedRepository.getPrelovedList()
            val requestResult  = requestRepository.getRequestList()

            val allJastip    = if (jastipResult   is Result.Success) jastipResult.data   else emptyList()
            val allPreloved  = if (prelovedResult is Result.Success) prelovedResult.data else emptyList()
            val requestCount = if (requestResult  is Result.Success) requestResult.data.size else 0

            _uiState.value = _uiState.value.copy(
                allJastip      = allJastip,
                allPreloved    = allPreloved,
                recentJastip   = allJastip.take(2),
                recentPreloved = allPreloved.take(2),
                requestCount   = requestCount,
                isLoading      = false
            )
        }
    }
}