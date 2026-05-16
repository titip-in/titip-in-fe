package com.titipin.app.ui.preloved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.*
import com.titipin.app.data.repository.CategoryRepository
import com.titipin.app.data.repository.PrelovedRepository
import com.titipin.app.data.repository.Result
import com.titipin.app.data.repository.UploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
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

sealed class PrelovedCategoryState {
    object Loading : PrelovedCategoryState()
    data class Success(val data: List<CategoryDto>) : PrelovedCategoryState()
    data class Error(val message: String) : PrelovedCategoryState()
}

@HiltViewModel
class PrelovedViewModel @Inject constructor(
    private val repository: PrelovedRepository,
    private val categoryRepository: CategoryRepository,
    private val dataStore: DataStoreManager,
    private val uploadRepository: UploadRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<PrelovedListState>(PrelovedListState.Loading)
    val listState: StateFlow<PrelovedListState> = _listState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _detailState = MutableStateFlow<PrelovedActionState>(PrelovedActionState.Idle)
    val detailState: StateFlow<PrelovedActionState> = _detailState.asStateFlow()

    private val _actionState = MutableStateFlow<PrelovedActionState>(PrelovedActionState.Idle)
    val actionState: StateFlow<PrelovedActionState> = _actionState.asStateFlow()

    private val _categoryState = MutableStateFlow<PrelovedCategoryState>(PrelovedCategoryState.Loading)
    val categoryState: StateFlow<PrelovedCategoryState> = _categoryState.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    init {
        loadPrelovedList()
        loadCategories()
        loadCurrentUser()
    }

    fun loadPrelovedList() {
        viewModelScope.launch {
            _listState.value = PrelovedListState.Loading
            _listState.value = when (val result = repository.getPrelovedList()) {
                is Result.Success -> PrelovedListState.Success(result.data)
                is Result.Error   -> PrelovedListState.Error(result.message)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _listState.value = when (val result = repository.getPrelovedList()) {
                is Result.Success -> PrelovedListState.Success(result.data)
                is Result.Error   -> PrelovedListState.Error(result.message)
            }
            _isRefreshing.value = false
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

    fun loadCategories() {
        viewModelScope.launch {
            _categoryState.value = PrelovedCategoryState.Loading
            _categoryState.value = when (val result = categoryRepository.getCategories("preloved")) {
                is Result.Success -> PrelovedCategoryState.Success(result.data)
                is Result.Error -> PrelovedCategoryState.Error(result.message)
            }
        }
    }

    fun selectCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUserId.value = dataStore.userId.firstOrNull()
        }
    }

    fun createPreloved(
        categoryId: Int?,
        title: String,
        description: String?,
        price: Int,
        condition: String,
        imageUris: List<Uri>,
        existingUrls: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _actionState.value = PrelovedActionState.Loading

            val uploadedUrls = imageUris.map { uri ->
                async { uploadRepository.uploadImage(uri) }
            }.awaitAll().mapNotNull { result ->
                (result as? Result.Success)?.data
            }

            val allUrls = existingUrls + uploadedUrls
            if (allUrls.isEmpty()) {
                _actionState.value = PrelovedActionState.Error("Minimal 1 foto diperlukan")
                return@launch
            }

            val request = CreatePrelovedRequest(
                categoryId      = categoryId,
                title           = title,
                description     = description,
                price           = price,
                condition       = condition,
                primaryImageUrl = allUrls.first(),
                status          = "AVAILABLE",
                images          = allUrls
            )
            _actionState.value = when (val result = repository.createPreloved(request)) {
                is Result.Success -> PrelovedActionState.Success(result.data)
                is Result.Error   -> PrelovedActionState.Error(result.message)
            }
        }
    }

    fun updateStatus(id: String, status: String) {
        viewModelScope.launch {
            _actionState.value = PrelovedActionState.Loading
            _actionState.value = when (val result = repository.updateStatus(id, status)) {
                is Result.Success -> PrelovedActionState.Success(result.data)
                is Result.Error -> PrelovedActionState.Error(result.message)
            }
        }
    }

    fun deletePreloved(id: String) {
        viewModelScope.launch {
            _actionState.value = PrelovedActionState.Loading
            _actionState.value = when (val result = repository.deletePreloved(id)) {
                is Result.Success -> PrelovedActionState.Success()
                is Result.Error -> PrelovedActionState.Error(result.message)
            }
        }
    }

    fun resetActionState() { _actionState.value = PrelovedActionState.Idle }
}
