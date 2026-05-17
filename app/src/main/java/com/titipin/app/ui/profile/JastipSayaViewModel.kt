package com.titipin.app.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.CategoryDto
import com.titipin.app.data.model.JastipDto
import com.titipin.app.data.model.RequestDto
import com.titipin.app.data.model.UpdateJastipListingRequest
import com.titipin.app.data.model.UpdateRequestBody
import com.titipin.app.data.repository.CategoryRepository
import com.titipin.app.data.repository.JastipRepository
import com.titipin.app.data.repository.RequestRepository
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

sealed class JastipSayaState {
    object Loading : JastipSayaState()
    data class Success(
        val listings: List<JastipDto> = emptyList(),
        val requests: List<RequestDto> = emptyList()
    ) : JastipSayaState()
    data class Error(val message: String) : JastipSayaState()
}

sealed class JastipSayaActionState {
    object Idle    : JastipSayaActionState()
    object Loading : JastipSayaActionState()
    object Success : JastipSayaActionState()
    data class Error(val message: String) : JastipSayaActionState()
    data class LimitReached(val message: String) : JastipSayaActionState()
}

@HiltViewModel
class JastipSayaViewModel @Inject constructor(
    private val jastipRepository: JastipRepository,
    private val requestRepository: RequestRepository,
    private val categoryRepository: CategoryRepository,
    private val uploadRepository: UploadRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _listState   = MutableStateFlow<JastipSayaState>(JastipSayaState.Loading)
    val listState: StateFlow<JastipSayaState> = _listState.asStateFlow()

    private val _actionState = MutableStateFlow<JastipSayaActionState>(JastipSayaActionState.Idle)
    val actionState: StateFlow<JastipSayaActionState> = _actionState.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categories: StateFlow<List<CategoryDto>> = _categories.asStateFlow()

    // Untuk fetch detail sebelum buka edit sheet (agar foto muncul)
    private val _itemForEdit = MutableStateFlow<JastipDto?>(null)
    val itemForEdit: StateFlow<JastipDto?> = _itemForEdit.asStateFlow()

    private val _fetchingEditId = MutableStateFlow<String?>(null)
    val fetchingEditId: StateFlow<String?> = _fetchingEditId.asStateFlow()

    init {
        loadCategories()
        loadData()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            if (_categories.value.isNotEmpty()) return@launch
            when (val result = categoryRepository.getCategories("jastip")) {
                is Result.Success -> _categories.value = result.data
                is Result.Error -> Unit
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _listState.value = JastipSayaState.Loading
            val listingsDeferred = async { jastipRepository.getMyJastipList() }
            val requestsDeferred = async { requestRepository.getMyRequestList() }

            val listings = (listingsDeferred.await() as? Result.Success)?.data ?: emptyList()
            val requests = (requestsDeferred.await() as? Result.Success)?.data ?: emptyList()

            _listState.value = JastipSayaState.Success(listings = listings, requests = requests)
        }
    }

    // ── Jastip listing actions ─────────────────────────────────────
    fun updateJastipStatus(id: String, status: String) {
        viewModelScope.launch {
            _actionState.value = JastipSayaActionState.Loading
            _actionState.value = when (val r = jastipRepository.updateStatus(id, status)) {
                is Result.Success -> JastipSayaActionState.Success
                is Result.Error   -> {
                    if (r.message.contains("maximum", ignoreCase = true) ||
                        r.message.contains("active", ignoreCase = true) ||
                        r.message.contains("Limit", ignoreCase = true)) {
                        JastipSayaActionState.LimitReached(r.message)
                    } else {
                        JastipSayaActionState.Error(r.message)
                    }
                }
            }
        }
    }

    fun updateJastip(
        id: String,
        title: String,
        fromLocation: String,
        toLocation: String,
        deadline: String,
        notes: String?,
        categoryId: Int?,
        imageUris: List<Uri> = emptyList(),
        existingImageUrls: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _actionState.value = JastipSayaActionState.Loading
            val uploadedUrls = imageUris.map { uri ->
                async { uploadRepository.uploadImage(uri) }
            }.awaitAll().mapNotNull { (it as? Result.Success)?.data }
            val allUrls = existingImageUrls + uploadedUrls
            val body = UpdateJastipListingRequest(
                categoryId = categoryId,
                title = title,
                fromLocation = fromLocation,
                toLocation = toLocation,
                deadline = deadline,
                notes = notes,
                primaryImageUrl = allUrls.firstOrNull(),
                images = allUrls.takeIf { it.isNotEmpty() }
            )
            _actionState.value = when (val r = jastipRepository.updateJastip(id, body)) {
                is Result.Success -> JastipSayaActionState.Success
                is Result.Error -> JastipSayaActionState.Error(r.message)
            }
        }
    }

    fun reopenJastip(id: String, deadline: String) {
        viewModelScope.launch {
            _actionState.value = JastipSayaActionState.Loading
            val body = UpdateJastipListingRequest(status = "ACTIVE", deadline = deadline)
            _actionState.value = when (val r = jastipRepository.updateJastip(id, body)) {
                is Result.Success -> JastipSayaActionState.Success
                is Result.Error -> JastipSayaActionState.Error(r.message)
            }
        }
    }

    fun deleteJastip(id: String) {
        viewModelScope.launch {
            _actionState.value = JastipSayaActionState.Loading
            _actionState.value = when (val r = jastipRepository.deleteJastip(id)) {
                is Result.Success -> JastipSayaActionState.Success
                is Result.Error   -> JastipSayaActionState.Error(r.message)
            }
        }
    }

    // ── Jastip request actions ─────────────────────────────────────
    fun updateRequestStatus(id: String, status: String) {
        viewModelScope.launch {
            _actionState.value = JastipSayaActionState.Loading
            _actionState.value = when (val r = requestRepository.updateRequestStatus(id, status)) {
                is Result.Success -> JastipSayaActionState.Success
                is Result.Error   -> {
                    if (r.message.contains("maximum", ignoreCase = true) ||
                        r.message.contains("active", ignoreCase = true) ||
                        r.message.contains("Limit", ignoreCase = true)) {
                        JastipSayaActionState.LimitReached(r.message)
                    } else {
                        JastipSayaActionState.Error(r.message)
                    }
                }
            }
        }
    }

    fun updateRequest(
        id: String,
        title: String,
        fromLocation: String,
        toLocation: String,
        notes: String?,
        categoryId: Int?
    ) {
        viewModelScope.launch {
            _actionState.value = JastipSayaActionState.Loading
            val body = UpdateRequestBody(
                categoryId = categoryId,
                title = title,
                fromLocation = fromLocation,
                toLocation = toLocation,
                notes = notes
            )
            _actionState.value = when (val r = requestRepository.updateRequest(id, body)) {
                is Result.Success -> JastipSayaActionState.Success
                is Result.Error -> JastipSayaActionState.Error(r.message)
            }
        }
    }

    fun deleteRequest(id: String) {
        viewModelScope.launch {
            _actionState.value = JastipSayaActionState.Loading
            _actionState.value = when (val r = requestRepository.deleteRequest(id)) {
                is Result.Success -> JastipSayaActionState.Success
                is Result.Error   -> JastipSayaActionState.Error(r.message)
            }
        }
    }

    // Fetch detail sebelum buka edit sheet (agar images ada dari detail endpoint)
    fun fetchJastipForEdit(id: String) {
        viewModelScope.launch {
            _fetchingEditId.value = id
            when (val r = jastipRepository.getJastipDetail(id)) {
                is Result.Success -> _itemForEdit.value = r.data
                is Result.Error   -> {
                    // Fallback: gunakan data dari list (tanpa images)
                    val current = (_listState.value as? JastipSayaState.Success)
                    _itemForEdit.value = current?.listings?.find { it.id == id }
                }
            }
            _fetchingEditId.value = null
        }
    }

    fun clearItemForEdit() { _itemForEdit.value = null }

    fun resetActionState() { _actionState.value = JastipSayaActionState.Idle }
}
