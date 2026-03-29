package com.titipin.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.JastipDto
import com.titipin.app.data.repository.JastipRepository
import com.titipin.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class JastipSayaState {
    object Loading : JastipSayaState()
    data class Success(val data: List<JastipDto>) : JastipSayaState()
    data class Error(val message: String) : JastipSayaState()
}

sealed class JastipSayaActionState {
    object Idle : JastipSayaActionState()
    object Loading : JastipSayaActionState()
    object Success : JastipSayaActionState()
    data class Error(val message: String) : JastipSayaActionState()
}

@HiltViewModel
class JastipSayaViewModel @Inject constructor(
    private val repository: JastipRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _listState = MutableStateFlow<JastipSayaState>(JastipSayaState.Loading)
    val listState: StateFlow<JastipSayaState> = _listState.asStateFlow()

    private val _actionState = MutableStateFlow<JastipSayaActionState>(JastipSayaActionState.Idle)
    val actionState: StateFlow<JastipSayaActionState> = _actionState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _listState.value = JastipSayaState.Loading
            val userId = dataStore.userId.first() ?: run {
                _listState.value = JastipSayaState.Error("Sesi tidak ditemukan")
                return@launch
            }
            _listState.value = when (val result = repository.getMyJastipList(userId)) {
                is Result.Success -> JastipSayaState.Success(result.data)
                is Result.Error   -> JastipSayaState.Error(result.message)
            }
        }
    }

    fun updateStatus(id: String, status: String) {
        viewModelScope.launch {
            _actionState.value = JastipSayaActionState.Loading
            _actionState.value = when (val result = repository.updateStatus(id, status)) {
                is Result.Success -> JastipSayaActionState.Success
                is Result.Error   -> JastipSayaActionState.Error(result.message)
            }
        }
    }

    fun deleteJastip(id: String) {
        viewModelScope.launch {
            _actionState.value = JastipSayaActionState.Loading
            _actionState.value = when (val result = repository.deleteJastip(id)) {
                is Result.Success -> JastipSayaActionState.Success
                is Result.Error   -> JastipSayaActionState.Error(result.message)
            }
        }
    }

    fun resetActionState() { _actionState.value = JastipSayaActionState.Idle }
}