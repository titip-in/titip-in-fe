package com.titipin.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.PrelovedDto
import com.titipin.app.data.model.PrelovedRequestDto
import com.titipin.app.data.repository.PrelovedRepository
import com.titipin.app.data.repository.PrelovedRequestRepository
import com.titipin.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PrelovedSayaState {
    object Loading : PrelovedSayaState()
    data class Success(
        val listings: List<PrelovedDto> = emptyList(),
        val requests: List<PrelovedRequestDto> = emptyList()
    ) : PrelovedSayaState()
    data class Error(val message: String) : PrelovedSayaState()
}

sealed class PrelovedSayaActionState {
    object Idle    : PrelovedSayaActionState()
    object Loading : PrelovedSayaActionState()
    object Success : PrelovedSayaActionState()
    data class Error(val message: String) : PrelovedSayaActionState()
}

@HiltViewModel
class PrelovedSayaViewModel @Inject constructor(
    private val prelovedRepository: PrelovedRepository,
    private val prelovedRequestRepository: PrelovedRequestRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _listState   = MutableStateFlow<PrelovedSayaState>(PrelovedSayaState.Loading)
    val listState: StateFlow<PrelovedSayaState> = _listState.asStateFlow()

    private val _actionState = MutableStateFlow<PrelovedSayaActionState>(PrelovedSayaActionState.Idle)
    val actionState: StateFlow<PrelovedSayaActionState> = _actionState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _listState.value = PrelovedSayaState.Loading
            val listingsDeferred = async { prelovedRepository.getMyPrelovedList() }
            val requestsDeferred = async { prelovedRequestRepository.getMyPrelovedRequestList() }

            val listings = (listingsDeferred.await() as? Result.Success)?.data ?: emptyList()
            val requests = (requestsDeferred.await() as? Result.Success)?.data ?: emptyList()

            _listState.value = PrelovedSayaState.Success(listings = listings, requests = requests)
        }
    }

    // ── Preloved listing actions ───────────────────────────────────
    fun updateListingStatus(id: String, status: String) {
        viewModelScope.launch {
            _actionState.value = PrelovedSayaActionState.Loading
            _actionState.value = when (val r = prelovedRepository.updateStatus(id, status)) {
                is Result.Success -> PrelovedSayaActionState.Success
                is Result.Error   -> PrelovedSayaActionState.Error(r.message)
            }
        }
    }

    fun deleteListing(id: String) {
        viewModelScope.launch {
            _actionState.value = PrelovedSayaActionState.Loading
            _actionState.value = when (val r = prelovedRepository.deletePreloved(id)) {
                is Result.Success -> PrelovedSayaActionState.Success
                is Result.Error   -> PrelovedSayaActionState.Error(r.message)
            }
        }
    }

    // ── Preloved request actions ───────────────────────────────────
    fun updateRequestStatus(id: String, status: String) {
        viewModelScope.launch {
            _actionState.value = PrelovedSayaActionState.Loading
            _actionState.value = when (val r = prelovedRequestRepository.updatePrelovedRequest(id, com.titipin.app.data.model.UpdatePrelovedRequestBody(status = status))) {
                is Result.Success -> PrelovedSayaActionState.Success
                is Result.Error   -> PrelovedSayaActionState.Error(r.message)
            }
        }
    }

    fun deleteRequest(id: String) {
        viewModelScope.launch {
            _actionState.value = PrelovedSayaActionState.Loading
            _actionState.value = when (val r = prelovedRequestRepository.deletePrelovedRequest(id)) {
                is Result.Success -> PrelovedSayaActionState.Success
                is Result.Error   -> PrelovedSayaActionState.Error(r.message)
            }
        }
    }

    fun resetActionState() { _actionState.value = PrelovedSayaActionState.Idle }
}