package com.titipin.app.ui.jastip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.model.RequestDto
import com.titipin.app.data.model.TakeRequestResponse
import com.titipin.app.data.repository.RequestRepository
import com.titipin.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RequestListState {
    object Loading : RequestListState()
    data class Success(val data: List<RequestDto>) : RequestListState()
    data class Error(val message: String) : RequestListState()
}

sealed class RequestActionState {
    object Idle : RequestActionState()
    object Loading : RequestActionState()
    data class Success(val data: RequestDto? = null) : RequestActionState()
    // Setelah take request, kita butuh waNumber untuk open WA
    data class TakeSuccess(val takenResult: TakeRequestResponse) : RequestActionState()
    data class Error(val message: String) : RequestActionState()
}

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val repository: RequestRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<RequestListState>(RequestListState.Loading)
    val listState: StateFlow<RequestListState> = _listState.asStateFlow()

    private val _actionState = MutableStateFlow<RequestActionState>(RequestActionState.Idle)
    val actionState: StateFlow<RequestActionState> = _actionState.asStateFlow()

    init { loadRequestList() }

    fun loadRequestList() {
        viewModelScope.launch {
            _listState.value = RequestListState.Loading
            _listState.value = when (val result = repository.getRequestList()) {
                is Result.Success -> RequestListState.Success(result.data)
                is Result.Error   -> RequestListState.Error(result.message)
            }
        }
    }

    fun createRequest(fromLocation: String, toLocation: String, notes: String?) {
        viewModelScope.launch {
            _actionState.value = RequestActionState.Loading
            _actionState.value = when (val result = repository.createRequest(fromLocation, toLocation, notes)) {
                is Result.Success -> RequestActionState.Success(result.data)
                is Result.Error   -> RequestActionState.Error(result.message)
            }
        }
    }

    fun takeRequest(id: String) {
        viewModelScope.launch {
            _actionState.value = RequestActionState.Loading
            _actionState.value = when (val result = repository.takeRequest(id)) {
                is Result.Success -> RequestActionState.TakeSuccess(result.data)
                is Result.Error   -> RequestActionState.Error(result.message)
            }
        }
    }

    fun resetActionState() { _actionState.value = RequestActionState.Idle }
}