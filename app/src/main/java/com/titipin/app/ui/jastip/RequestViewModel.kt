package com.titipin.app.ui.jastip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.model.CategoryDto
import com.titipin.app.data.model.RequestDto
import com.titipin.app.data.repository.CategoryRepository
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
    object FeatureInProgress : RequestActionState()
    data class Error(val message: String) : RequestActionState()
}

sealed class RequestCategoryState {
    object Loading : RequestCategoryState()
    data class Success(val data: List<CategoryDto>) : RequestCategoryState()
    data class Error(val message: String) : RequestCategoryState()
}

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val repository: RequestRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<RequestListState>(RequestListState.Loading)
    val listState: StateFlow<RequestListState> = _listState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _actionState = MutableStateFlow<RequestActionState>(RequestActionState.Idle)
    val actionState: StateFlow<RequestActionState> = _actionState.asStateFlow()

    private val _categoryState = MutableStateFlow<RequestCategoryState>(RequestCategoryState.Loading)
    val categoryState: StateFlow<RequestCategoryState> = _categoryState.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    init {
        loadRequestList()
        loadCategories()
    }

    fun loadRequestList() {
        viewModelScope.launch {
            _listState.value = RequestListState.Loading
            _listState.value = when (val result = repository.getRequestList()) {
                is Result.Success -> RequestListState.Success(result.data)
                is Result.Error   -> RequestListState.Error(result.message)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _listState.value = when (val result = repository.getRequestList()) {
                is Result.Success -> RequestListState.Success(result.data)
                is Result.Error   -> RequestListState.Error(result.message)
            }
            _isRefreshing.value = false
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _categoryState.value = RequestCategoryState.Loading
            _categoryState.value = when (val result = categoryRepository.getCategories("jastip")) {
                is Result.Success -> RequestCategoryState.Success(result.data)
                is Result.Error -> RequestCategoryState.Error(result.message)
            }
        }
    }

    fun selectCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
    }

    fun createRequest(
        categoryId: Int?,
        title: String,
        fromLocation: String,
        toLocation: String,
        notes: String?
    ) {
        viewModelScope.launch {
            _actionState.value = RequestActionState.Loading
            _actionState.value = when (val result = repository.createRequest(categoryId, title, fromLocation, toLocation, notes)) {
                is Result.Success -> RequestActionState.Success(result.data)
                is Result.Error   -> RequestActionState.Error(result.message)
            }
        }
    }

    fun showFeatureInProgress() {
        _actionState.value = RequestActionState.FeatureInProgress
    }

    fun resetActionState() { _actionState.value = RequestActionState.Idle }
}
