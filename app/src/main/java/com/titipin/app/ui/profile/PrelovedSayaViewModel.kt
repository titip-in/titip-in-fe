package com.titipin.app.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.CategoryDto
import com.titipin.app.data.model.PrelovedDto
import com.titipin.app.data.model.PrelovedRequestDto
import com.titipin.app.data.model.UpdatePrelovedListingRequest
import com.titipin.app.data.model.UpdatePrelovedRequestBody
import com.titipin.app.data.repository.CategoryRepository
import com.titipin.app.data.repository.PrelovedRepository
import com.titipin.app.data.repository.PrelovedRequestRepository
import com.titipin.app.data.repository.Result
import com.titipin.app.data.repository.UploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    data class LimitReached(val message: String) : PrelovedSayaActionState()
}

@HiltViewModel
class PrelovedSayaViewModel @Inject constructor(
    private val prelovedRepository: PrelovedRepository,
    private val prelovedRequestRepository: PrelovedRequestRepository,
    private val categoryRepository: CategoryRepository,
    private val uploadRepository: UploadRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _listState   = MutableStateFlow<PrelovedSayaState>(PrelovedSayaState.Loading)
    val listState: StateFlow<PrelovedSayaState> = _listState.asStateFlow()

    private val _actionState = MutableStateFlow<PrelovedSayaActionState>(PrelovedSayaActionState.Idle)
    val actionState: StateFlow<PrelovedSayaActionState> = _actionState.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categories: StateFlow<List<CategoryDto>> = _categories.asStateFlow()

    // Untuk fetch detail sebelum buka edit sheet (agar foto muncul)
    private val _itemForEdit = MutableStateFlow<PrelovedDto?>(null)
    val itemForEdit: StateFlow<PrelovedDto?> = _itemForEdit.asStateFlow()

    private val _fetchingEditId = MutableStateFlow<String?>(null)
    val fetchingEditId: StateFlow<String?> = _fetchingEditId.asStateFlow()

    init {
        loadCategories()
        loadData()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            if (_categories.value.isNotEmpty()) return@launch
            when (val result = categoryRepository.getCategories("preloved")) {
                is Result.Success -> _categories.value = result.data
                is Result.Error -> Unit
            }
        }
    }

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
                is Result.Error   -> {
                    if (r.message.contains("maximum", ignoreCase = true) ||
                        r.message.contains("active", ignoreCase = true) ||
                        r.message.contains("Limit", ignoreCase = true)) {
                        PrelovedSayaActionState.LimitReached(r.message)
                    } else {
                        PrelovedSayaActionState.Error(r.message)
                    }
                }
            }
        }
    }

    fun updateListing(
        id: String,
        title: String,
        price: Int,
        condition: String,
        description: String?,
        categoryId: Int?,
        imageUris: List<Uri> = emptyList(),
        existingImageUrls: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _actionState.value = PrelovedSayaActionState.Loading
            val uploadedUrls = imageUris.map { uri ->
                async { uploadRepository.uploadImage(uri) }
            }.awaitAll().mapNotNull { (it as? Result.Success)?.data }
            val allUrls = existingImageUrls + uploadedUrls
            val body = UpdatePrelovedListingRequest(
                categoryId = categoryId,
                title = title,
                price = price,
                condition = condition,
                description = description,
                primaryImageUrl = allUrls.firstOrNull(),
                images = allUrls.takeIf { it.isNotEmpty() }
            )
            _actionState.value = when (val r = prelovedRepository.updatePreloved(id, body)) {
                is Result.Success -> PrelovedSayaActionState.Success
                is Result.Error -> PrelovedSayaActionState.Error(r.message)
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
                is Result.Error   -> {
                    if (r.message.contains("maximum", ignoreCase = true) ||
                        r.message.contains("active", ignoreCase = true) ||
                        r.message.contains("Limit", ignoreCase = true)) {
                        PrelovedSayaActionState.LimitReached(r.message)
                    } else {
                        PrelovedSayaActionState.Error(r.message)
                    }
                }
            }
        }
    }

    fun updateRequest(
        id: String,
        title: String,
        description: String?,
        maxPrice: Int?,
        categoryId: Int?
    ) {
        viewModelScope.launch {
            _actionState.value = PrelovedSayaActionState.Loading
            val body = UpdatePrelovedRequestBody(
                categoryId = categoryId,
                title = title,
                description = description,
                maxPrice = maxPrice
            )
            _actionState.value = when (val r = prelovedRequestRepository.updatePrelovedRequest(id, body)) {
                is Result.Success -> PrelovedSayaActionState.Success
                is Result.Error -> PrelovedSayaActionState.Error(r.message)
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

    // Fetch detail sebelum buka edit sheet (agar images ada dari detail endpoint)
    fun fetchPrelovedForEdit(id: String) {
        viewModelScope.launch {
            _fetchingEditId.value = id
            when (val r = prelovedRepository.getPrelovedDetail(id)) {
                is Result.Success -> _itemForEdit.value = r.data
                is Result.Error   -> {
                    // Fallback: gunakan data dari list (tanpa images)
                    val current = (_listState.value as? PrelovedSayaState.Success)
                    _itemForEdit.value = current?.listings?.find { it.id == id }
                }
            }
            _fetchingEditId.value = null
        }
    }

    fun clearItemForEdit() { _itemForEdit.value = null }

    fun resetActionState() { _actionState.value = PrelovedSayaActionState.Idle }
}
