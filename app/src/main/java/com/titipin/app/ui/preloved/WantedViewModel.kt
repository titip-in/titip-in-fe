package com.titipin.app.ui.preloved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.model.FulfillWantedResponse
import com.titipin.app.data.model.WantedDto
import com.titipin.app.data.repository.Result
import com.titipin.app.data.repository.WantedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WantedListState {
    object Loading : WantedListState()
    data class Success(val data: List<WantedDto>) : WantedListState()
    data class Error(val message: String) : WantedListState()
}

sealed class WantedActionState {
    object Idle : WantedActionState()
    object Loading : WantedActionState()
    data class Success(val data: WantedDto? = null) : WantedActionState()
    // Setelah fulfill, kita butuh waNumber pencari untuk open WA
    data class FulfillSuccess(val result: FulfillWantedResponse) : WantedActionState()
    data class Error(val message: String) : WantedActionState()
}

@HiltViewModel
class WantedViewModel @Inject constructor(
    private val repository: WantedRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<WantedListState>(WantedListState.Loading)
    val listState: StateFlow<WantedListState> = _listState.asStateFlow()

    private val _actionState = MutableStateFlow<WantedActionState>(WantedActionState.Idle)
    val actionState: StateFlow<WantedActionState> = _actionState.asStateFlow()

    init { loadWantedList() }

    fun loadWantedList() {
        viewModelScope.launch {
            _listState.value = WantedListState.Loading
            _listState.value = when (val result = repository.getWantedList()) {
                is Result.Success -> WantedListState.Success(result.data)
                is Result.Error   -> WantedListState.Error(result.message)
            }
        }
    }

    fun createWanted(
        title: String,
        description: String?,
        maxPrice: Double?,
        category: String?
    ) {
        viewModelScope.launch {
            _actionState.value = WantedActionState.Loading
            _actionState.value = when (val result = repository.createWanted(title, description, maxPrice, category)) {
                is Result.Success -> WantedActionState.Success(result.data)
                is Result.Error   -> WantedActionState.Error(result.message)
            }
        }
    }

    fun fulfillWanted(id: String) {
        viewModelScope.launch {
            _actionState.value = WantedActionState.Loading
            _actionState.value = when (val result = repository.fulfillWanted(id)) {
                is Result.Success -> WantedActionState.FulfillSuccess(result.data)
                is Result.Error   -> WantedActionState.Error(result.message)
            }
        }
    }

    fun resetActionState() { _actionState.value = WantedActionState.Idle }
}