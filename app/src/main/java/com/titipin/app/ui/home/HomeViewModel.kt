package com.titipin.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.JastipDto
import com.titipin.app.data.model.PrelovedDto
import com.titipin.app.data.repository.JastipRepository
import com.titipin.app.data.repository.PrelovedRepository
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
    val allJastip: List<JastipDto> = emptyList(),       // semua data — untuk hitung total
    val allPreloved: List<PrelovedDto> = emptyList(),   // semua data — untuk hitung total
    val recentJastip: List<JastipDto> = emptyList(),    // 2 terbaru — untuk aktivitas
    val recentPreloved: List<PrelovedDto> = emptyList(), // 2 terbaru — untuk aktivitas
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataStore: DataStoreManager,
    private val jastipRepository: JastipRepository,
    private val prelovedRepository: PrelovedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            // Nama dari DataStore
            val name = dataStore.userName.first() ?: ""
            val initials = name.trim()
                .split(" ").filter { it.isNotBlank() }
                .take(2).joinToString("") { it.first().uppercase() }

            _uiState.value = _uiState.value.copy(
                userName     = name,
                userInitials = initials
            )

            // Fetch semua data — angka di card pakai total, aktivitas pakai 2 terbaru
            val jastipResult   = jastipRepository.getJastipList()
            val prelovedResult = prelovedRepository.getPrelovedList()

            val allJastip   = if (jastipResult   is Result.Success) jastipResult.data   else emptyList()
            val allPreloved = if (prelovedResult is Result.Success) prelovedResult.data else emptyList()

            _uiState.value = _uiState.value.copy(
                allJastip      = allJastip,
                allPreloved    = allPreloved,
                // Ambil 2 terbaru untuk feed aktivitas
                recentJastip   = allJastip.take(2),
                recentPreloved = allPreloved.take(2),
                isLoading      = false
            )
        }
    }
}