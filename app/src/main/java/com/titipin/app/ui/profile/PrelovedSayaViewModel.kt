package com.titipin.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.PrelovedDto
import com.titipin.app.data.repository.PrelovedRepository
import com.titipin.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PrelovedSayaState {
    object Loading : PrelovedSayaState()
    data class Success(val data: List<PrelovedDto>) : PrelovedSayaState()
    data class Error(val message: String) : PrelovedSayaState()
}

sealed class PrelovedSayaActionState {
    object Idle : PrelovedSayaActionState()
    object Loading : PrelovedSayaActionState()
    object Success : PrelovedSayaActionState()
    data class Error(val message: String) : PrelovedSayaActionState()
}

@HiltViewModel
class PrelovedSayaViewModel @Inject constructor(
    private val repository: PrelovedRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _listState = MutableStateFlow<PrelovedSayaState>(PrelovedSayaState.Loading)
    val listState: StateFlow<PrelovedSayaState> = _listState.asStateFlow()

    private val _actionState = MutableStateFlow<PrelovedSayaActionState>(PrelovedSayaActionState.Idle)
    val actionState: StateFlow<PrelovedSayaActionState> = _actionState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _listState.value = PrelovedSayaState.Loading
            val userId = dataStore.userId.first() ?: run {
                _listState.value = PrelovedSayaState.Error("Sesi tidak ditemukan")
                return@launch
            }
            _listState.value = when (val result = repository.getMyPrelovedList(userId)) {
                is Result.Success -> PrelovedSayaState.Success(result.data)
                is Result.Error   -> PrelovedSayaState.Error(result.message)
            }
        }
    }

    fun updateStatus(id: String, status: String) {
        viewModelScope.launch {
            _actionState.value = PrelovedSayaActionState.Loading
            _actionState.value = when (val result = repository.updateStatus(id, status)) {
                is Result.Success -> PrelovedSayaActionState.Success
                is Result.Error   -> PrelovedSayaActionState.Error(result.message)
            }
        }
    }

    fun deletePreloved(id: String) {
        viewModelScope.launch {
            _actionState.value = PrelovedSayaActionState.Loading
            _actionState.value = when (val result = repository.deletePreloved(id)) {
                is Result.Success -> PrelovedSayaActionState.Success
                is Result.Error   -> PrelovedSayaActionState.Error(result.message)
            }
        }
    }

    fun resetActionState() { _actionState.value = PrelovedSayaActionState.Idle }
}