package com.titipin.app.ui.jastip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.model.*
import com.titipin.app.data.repository.JastipRepository
import com.titipin.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// State untuk list screen
sealed class JastipListState {
    object Loading : JastipListState()
    data class Success(val data: List<JastipDto>) : JastipListState()
    data class Error(val message: String) : JastipListState()
}

// State untuk detail + form (operasi single item)
sealed class JastipActionState {
    object Idle : JastipActionState()
    object Loading : JastipActionState()
    data class Success(val data: JastipDto? = null) : JastipActionState()
    data class Error(val message: String) : JastipActionState()
}

@HiltViewModel
class JastipViewModel @Inject constructor(
    private val repository: JastipRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<JastipListState>(JastipListState.Loading)
    val listState: StateFlow<JastipListState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow<JastipActionState>(JastipActionState.Idle)
    val detailState: StateFlow<JastipActionState> = _detailState.asStateFlow()

    private val _actionState = MutableStateFlow<JastipActionState>(JastipActionState.Idle)
    val actionState: StateFlow<JastipActionState> = _actionState.asStateFlow()

    // Load list saat screen pertama kali muncul
    init { loadJastipList() }

    fun loadJastipList() {
        viewModelScope.launch {
            _listState.value = JastipListState.Loading
            _listState.value = when (val result = repository.getJastipList()) {
                is Result.Success -> JastipListState.Success(result.data)
                is Result.Error   -> JastipListState.Error(result.message)
            }
        }
    }

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _detailState.value = JastipActionState.Loading
            _detailState.value = when (val result = repository.getJastipDetail(id)) {
                is Result.Success -> JastipActionState.Success(result.data)
                is Result.Error   -> JastipActionState.Error(result.message)
            }
        }
    }

    fun createJastip(
        fromLocation: String,
        toLocation: String,
        deadline: String,
        latitude: Double,
        longitude: Double,
        notes: String?
    ) {
        viewModelScope.launch {
            _actionState.value = JastipActionState.Loading
            val request = CreateJastipRequest(
                fromLocation, toLocation, deadline, latitude, longitude, notes
            )
            _actionState.value = when (val result = repository.createJastip(request)) {
                is Result.Success -> JastipActionState.Success(result.data)
                is Result.Error   -> JastipActionState.Error(result.message)
            }
        }
    }

    fun updateStatus(id: String, status: String) {
        viewModelScope.launch {
            _actionState.value = JastipActionState.Loading
            _actionState.value = when (val result = repository.updateStatus(id, status)) {
                is Result.Success -> JastipActionState.Success(result.data)
                is Result.Error   -> JastipActionState.Error(result.message)
            }
        }
    }

    fun deleteJastip(id: String) {
        viewModelScope.launch {
            _actionState.value = JastipActionState.Loading
            _actionState.value = when (val result = repository.deleteJastip(id)) {
                is Result.Success -> JastipActionState.Success()
                is Result.Error   -> JastipActionState.Error(result.message)
            }
        }
    }

    fun resetActionState() { _actionState.value = JastipActionState.Idle }
}