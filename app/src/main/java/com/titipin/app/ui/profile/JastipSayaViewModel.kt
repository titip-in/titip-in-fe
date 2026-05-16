package com.titipin.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.JastipDto
import com.titipin.app.data.model.RequestDto
import com.titipin.app.data.repository.JastipRepository
import com.titipin.app.data.repository.RequestRepository
import com.titipin.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class JastipSayaState {
    object Loading : JastipSayaState()
    data class Success(
        val listings: List<JastipDto> = emptyList(),
        val requests: List<RequestDto> = emptyList()
    ) : JastipSayaState()
    data class Error(val message: String) : JastipSayaState()
}

sealed class JastipSayaActionState {
    object Idle    : JastipSayaActionState()
    object Loading : JastipSayaActionState()
    object Success : JastipSayaActionState()
    data class Error(val message: String) : JastipSayaActionState()
}

@HiltViewModel
class JastipSayaViewModel @Inject constructor(
    private val jastipRepository: JastipRepository,
    private val requestRepository: RequestRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _listState   = MutableStateFlow<JastipSayaState>(JastipSayaState.Loading)
    val listState: StateFlow<JastipSayaState> = _listState.asStateFlow()

    private val _actionState = MutableStateFlow<JastipSayaActionState>(JastipSayaActionState.Idle)
    val actionState: StateFlow<JastipSayaActionState> = _actionState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _listState.value = JastipSayaState.Loading
            val listingsDeferred = async { jastipRepository.getMyJastipList() }
            val requestsDeferred = async { requestRepository.getMyRequestList() }

            val listings = (listingsDeferred.await() as? Result.Success)?.data ?: emptyList()
            val requests = (requestsDeferred.await() as? Result.Success)?.data ?: emptyList()

            _listState.value = JastipSayaState.Success(listings = listings, requests = requests)
        }
    }

    // ── Jastip listing actions ─────────────────────────────────────
    fun updateJastipStatus(id: String, status: String) {
        viewModelScope.launch {
            _actionState.value = JastipSayaActionState.Loading
            _actionState.value = when (val r = jastipRepository.updateStatus(id, status)) {
                is Result.Success -> JastipSayaActionState.Success
                is Result.Error   -> JastipSayaActionState.Error(r.message)
            }
        }
    }

    fun deleteJastip(id: String) {
        viewModelScope.launch {
            _actionState.value = JastipSayaActionState.Loading
            _actionState.value = when (val r = jastipRepository.deleteJastip(id)) {
                is Result.Success -> JastipSayaActionState.Success
                is Result.Error   -> JastipSayaActionState.Error(r.message)
            }
        }
    }

    // ── Jastip request actions ─────────────────────────────────────
    fun updateRequestStatus(id: String, status: String) {
        viewModelScope.launch {
            _actionState.value = JastipSayaActionState.Loading
            _actionState.value = when (val r = requestRepository.updateRequestStatus(id, status)) {
                is Result.Success -> JastipSayaActionState.Success
                is Result.Error   -> JastipSayaActionState.Error(r.message)
            }
        }
    }

    fun deleteRequest(id: String) {
        viewModelScope.launch {
            _actionState.value = JastipSayaActionState.Loading
            _actionState.value = when (val r = requestRepository.deleteRequest(id)) {
                is Result.Success -> JastipSayaActionState.Success
                is Result.Error   -> JastipSayaActionState.Error(r.message)
            }
        }
    }

    fun resetActionState() { _actionState.value = JastipSayaActionState.Idle }
}