package com.titipin.app.ui.preloved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.*
import com.titipin.app.data.repository.AnalyticsRepository
import com.titipin.app.data.repository.BoostRepository
import com.titipin.app.data.repository.CategoryRepository
import com.titipin.app.data.repository.PrelovedRepository
import com.titipin.app.data.repository.Result
import com.titipin.app.data.repository.UploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    data class LimitReached(val message: String) : PrelovedActionState()
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
    private val uploadRepository: UploadRepository,
    private val boostRepository: BoostRepository,
    private val analyticsRepository: AnalyticsRepository
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

    private val _currentUserTier = MutableStateFlow(UserTier.BASIC)
    val currentUserTier: StateFlow<String> = _currentUserTier.asStateFlow()

    private val _currentBoostQuota = MutableStateFlow(0)
    val currentBoostQuota: StateFlow<Int> = _currentBoostQuota.asStateFlow()

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
            _currentUserTier.value = dataStore.userTier.firstOrNull().normalizedTier()
            _currentBoostQuota.value = dataStore.userBoostQuota.firstOrNull() ?: 0
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

            val uploadedUrls = when (val uploadResult = uploadImageUris(imageUris)) {
                is Result.Success -> uploadResult.data
                is Result.Error -> {
                    _actionState.value = PrelovedActionState.Error(uploadResult.message)
                    return@launch
                }
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
            val result = repository.updateStatus(id, status)
            _actionState.value = when (result) {
                is Result.Success -> PrelovedActionState.Success(result.data)
                is Result.Error   -> {
                    if (result.message.contains("maximum", ignoreCase = true) ||
                        result.message.contains("active", ignoreCase = true)) {
                        PrelovedActionState.LimitReached(result.message)
                    } else {
                        PrelovedActionState.Error(result.message)
                    }
                }
            }
        }
    }

    /** Edit barang preloved: title, price, condition, description. */
    fun updatePreloved(
        id: String,
        title: String,
        price: Int,
        condition: String,
        description: String?,
        categoryId: Int? = null,
        imageUris: List<Uri> = emptyList(),
        existingImageUrls: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _actionState.value = PrelovedActionState.Loading
            val uploadedUrls = when (val uploadResult = uploadImageUris(imageUris)) {
                is Result.Success -> uploadResult.data
                is Result.Error -> {
                    _actionState.value = PrelovedActionState.Error(uploadResult.message)
                    return@launch
                }
            }
            val allUrls = existingImageUrls + uploadedUrls
            val request = UpdatePrelovedListingRequest(
                categoryId  = categoryId,
                title       = title,
                price       = price,
                condition   = condition,
                description = description,
                primaryImageUrl = allUrls.firstOrNull(),
                images = allUrls.takeIf { it.isNotEmpty() }
            )
            _actionState.value = when (val result = repository.updatePreloved(id, request)) {
                is Result.Success -> PrelovedActionState.Success(result.data)
                is Result.Error   -> PrelovedActionState.Error(result.message)
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

    fun boostPreloved(id: String) {
        viewModelScope.launch {
            _actionState.value = PrelovedActionState.Loading
            _actionState.value = when (val result = boostRepository.boostPrelovedListing(id)) {
                is Result.Success -> {
                    _currentBoostQuota.value = result.data.remainingQuota
                    PrelovedActionState.Success()
                }
                is Result.Error -> PrelovedActionState.Error(result.message)
            }
        }
    }

    private suspend fun uploadImageUris(imageUris: List<Uri>): Result<List<String>> {
        val uploadedUrls = mutableListOf<String>()
        imageUris.forEachIndexed { index, uri ->
            when (val result = uploadRepository.uploadImage(uri)) {
                is Result.Success -> uploadedUrls += result.data
                is Result.Error -> return Result.Error(
                    "Upload foto ${index + 1} gagal: ${result.message}"
                )
            }
        }
        return Result.Success(uploadedUrls)
    }

    fun resetActionState() { _actionState.value = PrelovedActionState.Idle }

    /** Fire-and-forget: catat klik WA pada preloved listing */
    fun trackPrelovedClick(id: String) {
        analyticsRepository.trackClick("preloved_listing", id)
    }
}
