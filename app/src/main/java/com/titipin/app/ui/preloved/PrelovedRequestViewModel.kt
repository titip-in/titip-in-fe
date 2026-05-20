package com.titipin.app.ui.preloved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.CategoryDto
import com.titipin.app.data.model.PrelovedRequestDto
import com.titipin.app.data.model.UpdatePrelovedRequestBody
import com.titipin.app.data.model.UserTier
import com.titipin.app.data.model.normalizedTier
import com.titipin.app.data.repository.CategoryRepository
import com.titipin.app.data.repository.PrelovedRequestRepository
import com.titipin.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ───────────────────────────────────────────────────────────────
sealed class PrelovedRequestListState {
    object Loading : PrelovedRequestListState()
    data class Success(val data: List<PrelovedRequestDto>) : PrelovedRequestListState()
    data class Error(val message: String) : PrelovedRequestListState()
}

sealed class PrelovedRequestCategoryState {
    object Loading : PrelovedRequestCategoryState()
    data class Success(val data: List<CategoryDto>) : PrelovedRequestCategoryState()
    object Error : PrelovedRequestCategoryState()
}

sealed class PrelovedRequestActionState {
    object Idle : PrelovedRequestActionState()
    object Loading : PrelovedRequestActionState()
    data class Success(val data: PrelovedRequestDto? = null) : PrelovedRequestActionState()
    data class Error(val message: String) : PrelovedRequestActionState()
    data class LimitReached(val message: String) : PrelovedRequestActionState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────
@HiltViewModel
class PrelovedRequestViewModel @Inject constructor(
    private val repository: PrelovedRequestRepository,
    private val categoryRepository: CategoryRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _listState = MutableStateFlow<PrelovedRequestListState>(PrelovedRequestListState.Loading)
    val listState: StateFlow<PrelovedRequestListState> = _listState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _categoryState = MutableStateFlow<PrelovedRequestCategoryState>(PrelovedRequestCategoryState.Loading)
    val categoryState: StateFlow<PrelovedRequestCategoryState> = _categoryState.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    private val _actionState = MutableStateFlow<PrelovedRequestActionState>(PrelovedRequestActionState.Idle)
    val actionState: StateFlow<PrelovedRequestActionState> = _actionState.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _currentUserTier = MutableStateFlow(UserTier.BASIC)
    val currentUserTier: StateFlow<String> = _currentUserTier.asStateFlow()

    init {
        loadPrelovedRequestList()
        loadCategories()
        viewModelScope.launch {
            _currentUserId.value = dataStore.userId.firstOrNull()
            _currentUserTier.value = dataStore.userTier.firstOrNull().normalizedTier()
        }
    }

    fun loadPrelovedRequestList() {
        viewModelScope.launch {
            _listState.value = PrelovedRequestListState.Loading
            _listState.value = when (val result = repository.getPrelovedRequestList()) {
                is Result.Success -> PrelovedRequestListState.Success(result.data)
                is Result.Error   -> PrelovedRequestListState.Error(result.message)
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = categoryRepository.getCategories(type = "preloved")) {
                is Result.Success -> _categoryState.value = PrelovedRequestCategoryState.Success(result.data)
                is Result.Error   -> _categoryState.value = PrelovedRequestCategoryState.Error
            }
        }
    }

    fun selectCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _listState.value = when (val result = repository.getPrelovedRequestList()) {
                is Result.Success -> PrelovedRequestListState.Success(result.data)
                is Result.Error   -> PrelovedRequestListState.Error(result.message)
            }
            _isRefreshing.value = false
        }
    }

    fun createPrelovedRequest(
        categoryId: Int?,
        title: String,
        description: String?,
        maxPrice: Int?
    ) {
        viewModelScope.launch {
            _actionState.value = PrelovedRequestActionState.Loading
            _actionState.value = when (val result = repository.createPrelovedRequest(
                categoryId = categoryId,
                title = title,
                description = description,
                maxPrice = maxPrice
            )) {
                is Result.Success -> PrelovedRequestActionState.Success(result.data)
                is Result.Error   -> PrelovedRequestActionState.Error(result.message)
            }
        }
    }

    fun deletePrelovedRequest(id: String) {
        viewModelScope.launch {
            _actionState.value = PrelovedRequestActionState.Loading
            _actionState.value = when (val result = repository.deletePrelovedRequest(id)) {
                is Result.Success -> PrelovedRequestActionState.Success()
                is Result.Error   -> PrelovedRequestActionState.Error(result.message)
            }
        }
    }

    fun toggleStatus(id: String, currentStatus: String) {
        viewModelScope.launch {
            _actionState.value = PrelovedRequestActionState.Loading
            val result = repository.toggleStatus(id, currentStatus)
            _actionState.value = when (result) {
                is Result.Success -> PrelovedRequestActionState.Success(result.data)
                is Result.Error   -> {
                    if (result.message.contains("maximum", ignoreCase = true) ||
                        result.message.contains("active", ignoreCase = true)) {
                        PrelovedRequestActionState.LimitReached(result.message)
                    } else {
                        PrelovedRequestActionState.Error(result.message)
                    }
                }
            }
        }
    }

    /** Edit preloved request fields (title, description, max price). */
    fun updatePrelovedRequestFields(
        id: String,
        title: String,
        description: String?,
        maxPrice: Int?,
        categoryId: Int? = null
    ) {
        viewModelScope.launch {
            _actionState.value = PrelovedRequestActionState.Loading
            val body = UpdatePrelovedRequestBody(
                categoryId  = categoryId,
                title       = title,
                description = description,
                maxPrice    = maxPrice
            )
            _actionState.value = when (val result = repository.updatePrelovedRequest(id, body)) {
                is Result.Success -> PrelovedRequestActionState.Success(result.data)
                is Result.Error   -> PrelovedRequestActionState.Error(result.message)
            }
        }
    }

    fun resetActionState() {
        _actionState.value = PrelovedRequestActionState.Idle
    }
}
