package com.titipin.app.ui.jastip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.*
import com.titipin.app.data.repository.CategoryRepository
import com.titipin.app.data.repository.JastipRepository
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

// State untuk list screen
sealed class JastipListState {
    object Loading : JastipListState()
    data class Success(val data: List<JastipDto>) : JastipListState()
    data class Error(val message: String) : JastipListState()
}

sealed class JastipCategoryState {
    object Loading : JastipCategoryState()
    data class Success(val data: List<CategoryDto>) : JastipCategoryState()
    data class Error(val message: String) : JastipCategoryState()
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
    private val repository: JastipRepository,
    private val categoryRepository: CategoryRepository,
    private val dataStore: DataStoreManager,
    private val uploadRepository: UploadRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<JastipListState>(JastipListState.Loading)
    val listState: StateFlow<JastipListState> = _listState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _detailState = MutableStateFlow<JastipActionState>(JastipActionState.Idle)
    val detailState: StateFlow<JastipActionState> = _detailState.asStateFlow()

    private val _actionState = MutableStateFlow<JastipActionState>(JastipActionState.Idle)
    val actionState: StateFlow<JastipActionState> = _actionState.asStateFlow()

    private val _categoryState = MutableStateFlow<JastipCategoryState>(JastipCategoryState.Loading)
    val categoryState: StateFlow<JastipCategoryState> = _categoryState.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    // Load list saat screen pertama kali muncul
    init {
        loadJastipList()
        loadCategories()
        loadCurrentUser()
    }

    fun loadJastipList() {
        viewModelScope.launch {
            _listState.value = JastipListState.Loading
            _listState.value = when (val result = repository.getJastipList()) {
                is Result.Success -> JastipListState.Success(result.data)
                is Result.Error   -> JastipListState.Error(result.message)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _listState.value = when (val result = repository.getJastipList()) {
                is Result.Success -> JastipListState.Success(result.data)
                is Result.Error   -> JastipListState.Error(result.message)
            }
            _isRefreshing.value = false
        }
    }

    fun selectCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
    }

    fun loadCategories() {
        viewModelScope.launch {
            _categoryState.value = JastipCategoryState.Loading
            _categoryState.value = when (val result = categoryRepository.getCategories("jastip")) {
                is Result.Success -> JastipCategoryState.Success(result.data)
                is Result.Error -> JastipCategoryState.Error(result.message)
            }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUserId.value = dataStore.userId.firstOrNull()
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

    /**
     * Upload gambar dari Uri list ke /v1/upload, lalu buat listing Jastip.
     * [imageUris] — gambar yang dipilih user dari device (belum diupload).
     * [existingUrls] — URL yang sudah di-host (untuk edit form, opsional).
     */
    fun createJastip(
        title: String,
        fromLocation: String,
        toLocation: String,
        deadline: String,
        latitude: Double?,
        longitude: Double?,
        notes: String?,
        imageUris: List<Uri>,
        existingUrls: List<String> = emptyList(),
        categoryId: Int? = null
    ) {
        viewModelScope.launch {
            _actionState.value = JastipActionState.Loading

            // Upload semua gambar baru secara paralel
            val uploadedUrls = imageUris.map { uri ->
                async { uploadRepository.uploadImage(uri) }
            }.awaitAll().mapNotNull { result ->
                (result as? Result.Success)?.data
            }

            val allUrls = existingUrls + uploadedUrls
            if (allUrls.isEmpty()) {
                _actionState.value = JastipActionState.Error("Minimal 1 foto diperlukan")
                return@launch
            }

            val request = CreateJastipRequest(
                categoryId      = categoryId,
                title           = title,
                notes           = notes,
                fromLocation    = fromLocation,
                toLocation      = toLocation,
                deadline        = deadline,
                latitude        = latitude,
                longitude       = longitude,
                status          = "ACTIVE",
                primaryImageUrl = allUrls.first(),
                images          = allUrls
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
