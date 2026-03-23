package com.titipin.app.ui.preloved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.model.*
import com.titipin.app.data.repository.PrelovedRepository
import com.titipin.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PrelovedListState {
    object Loading : PrelovedListState()
    data class Success(val data: List<PrelovedDto>) : PrelovedListState()
    data class Error(val message: String) : PrelovedListState()
}

sealed class PrelovedActionState {
    object Idle : PrelovedActionState()
    object Loading : PrelovedActionState()
    data class Success(val data: PrelovedDto? = null) : PrelovedActionState()
    data class Error(val message: String) : PrelovedActionState()
}

@HiltViewModel
class PrelovedViewModel @Inject constructor(
    private val repository: PrelovedRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<PrelovedListState>(PrelovedListState.Loading)
    val listState: StateFlow<PrelovedListState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow<PrelovedActionState>(PrelovedActionState.Idle)
    val detailState: StateFlow<PrelovedActionState> = _detailState.asStateFlow()

    private val _actionState = MutableStateFlow<PrelovedActionState>(PrelovedActionState.Idle)
    val actionState: StateFlow<PrelovedActionState> = _actionState.asStateFlow()

    init { loadPrelovedList() }

    fun loadPrelovedList() {
        viewModelScope.launch {
            _listState.value = PrelovedListState.Loading
            _listState.value = when (val result = repository.getPrelovedList()) {
                is Result.Success -> PrelovedListState.Success(result.data)
                is Result.Error   -> PrelovedListState.Error(result.message)
            }
        }
    }

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _detailState.value = PrelovedActionState.Loading
            _detailState.value = when (val result = repository.getPrelovedDetail(id)) {
                is Result.Success -> PrelovedActionState.Success(result.data)
                is Result.Error   -> PrelovedActionState.Error(result.message)
            }
        }
    }

    fun createPreloved(
        title: String,
        description: String?,
        price: Int,
        category: String,
        condition: String,
        imageUrl: String?
    ) {
        viewModelScope.launch {
            _actionState.value = PrelovedActionState.Loading
            val request = CreatePrelovedRequest(title, description, price, category, condition, imageUrl)
            _actionState.value = when (val result = repository.createPreloved(request)) {
                is Result.Success -> PrelovedActionState.Success(result.data)
                is Result.Error   -> PrelovedActionState.Error(result.message)
            }
        }
    }

    fun resetActionState() { _actionState.value = PrelovedActionState.Idle }
}
