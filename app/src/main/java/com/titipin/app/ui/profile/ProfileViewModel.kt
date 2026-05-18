package com.titipin.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.model.UserData
import com.titipin.app.data.repository.AuthRepository
import com.titipin.app.data.repository.JastipRepository
import com.titipin.app.data.repository.PrelovedRepository
import com.titipin.app.data.repository.PrelovedRequestRepository
import com.titipin.app.data.repository.RequestRepository
import com.titipin.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import android.net.Uri
import com.titipin.app.data.repository.UploadRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: UserData, val usage: ProfileUsage = ProfileUsage()) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

data class ProfileUsage(
    val activeJastipListings: Int = 0,
    val activeJastipRequests: Int = 0,
    val activePrelovedListings: Int = 0,
    val activePrelovedRequests: Int = 0,
    val limit: Int = 5
) {
    val totalJastip: Int get() = activeJastipListings + activeJastipRequests
    val totalPreloved: Int get() = activePrelovedListings + activePrelovedRequests
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val uploadRepository: UploadRepository,
    private val jastipRepository: JastipRepository,
    private val requestRepository: RequestRepository,
    private val prelovedRepository: PrelovedRepository,
    private val prelovedRequestRepository: PrelovedRequestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isUploadingAvatar = MutableStateFlow(false)
    val isUploadingAvatar: StateFlow<Boolean> = _isUploadingAvatar.asStateFlow()

    private val _isUpdatingProfile = MutableStateFlow(false)
    val isUpdatingProfile: StateFlow<Boolean> = _isUpdatingProfile.asStateFlow()

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val profileDeferred = async { authRepository.getMe() }
            val jastipListingsDeferred = async { jastipRepository.getMyJastipList() }
            val jastipRequestsDeferred = async { requestRepository.getMyRequestList() }
            val prelovedListingsDeferred = async { prelovedRepository.getMyPrelovedList() }
            val prelovedRequestsDeferred = async { prelovedRequestRepository.getMyPrelovedRequestList() }

            _uiState.value = when (val result = profileDeferred.await()) {
                is Result.Success -> {
                    val usage = ProfileUsage(
                        activeJastipListings = ((jastipListingsDeferred.await() as? Result.Success)?.data ?: emptyList()).count { it.status == "ACTIVE" },
                        activeJastipRequests = ((jastipRequestsDeferred.await() as? Result.Success)?.data ?: emptyList()).count { it.status == "OPEN" },
                        activePrelovedListings = ((prelovedListingsDeferred.await() as? Result.Success)?.data ?: emptyList()).count { it.status == "AVAILABLE" },
                        activePrelovedRequests = ((prelovedRequestsDeferred.await() as? Result.Success)?.data ?: emptyList()).count { it.status == "OPEN" }
                    )
                    ProfileUiState.Success(result.data, usage)
                }
                is Result.Error   -> ProfileUiState.Error(result.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            _isUploadingAvatar.value = true
            when (val uploadResult = uploadRepository.uploadImage(uri)) {
                is Result.Success -> {
                    val currentUser = (_uiState.value as? ProfileUiState.Success)?.user
                    val currentUsage = (_uiState.value as? ProfileUiState.Success)?.usage ?: ProfileUsage()
                    when (val updateResult = authRepository.updateProfile(
                        name = currentUser?.name,
                        waNumber = currentUser?.waNumber,
                        status = currentUser?.status,
                        avatarUrl = uploadResult.data
                    )) {
                        is Result.Success -> {
                            _uiState.value = ProfileUiState.Success(updateResult.data, currentUsage)
                        }
                        is Result.Error -> {
                            _uiState.value = ProfileUiState.Error(updateResult.message)
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.value = ProfileUiState.Error(uploadResult.message)
                }
            }
            _isUploadingAvatar.value = false
        }
    }

    fun updateProfile(
        name: String? = null,
        waNumber: String? = null,
        status: String? = null,
        avatarUrl: String? = null
    ) {
        viewModelScope.launch {
            _isUpdatingProfile.value = true
            val currentUser = (_uiState.value as? ProfileUiState.Success)?.user
            val currentUsage = (_uiState.value as? ProfileUiState.Success)?.usage ?: ProfileUsage()
            when (val updateResult = authRepository.updateProfile(
                name = name ?: currentUser?.name,
                waNumber = waNumber ?: currentUser?.waNumber,
                status = status ?: currentUser?.status,
                avatarUrl = avatarUrl ?: currentUser?.avatarUrl
            )) {
                is Result.Success -> _uiState.value = ProfileUiState.Success(updateResult.data, currentUsage)
                is Result.Error -> _uiState.value = ProfileUiState.Error(updateResult.message)
            }
            _isUpdatingProfile.value = false
        }
    }

    fun requestWaOtp(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            when (val result = authRepository.requestWaOtp()) {
                is Result.Success -> onComplete(true, "OTP WhatsApp sudah dikirim")
                is Result.Error -> onComplete(false, result.message)
            }
        }
    }

    fun requestWaOtpForNumber(waNumber: String, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isUpdatingProfile.value = true
            val currentUser = (_uiState.value as? ProfileUiState.Success)?.user
            val currentUsage = (_uiState.value as? ProfileUiState.Success)?.usage ?: ProfileUsage()
            val updateResult = authRepository.updateProfile(
                name = currentUser?.name,
                waNumber = waNumber,
                status = currentUser?.status,
                avatarUrl = currentUser?.avatarUrl
            )
            _isUpdatingProfile.value = false
            if (updateResult is Result.Error) {
                onComplete(false, updateResult.message)
                return@launch
            }
            if (updateResult is Result.Success) {
                _uiState.value = ProfileUiState.Success(updateResult.data, currentUsage)
            }
            when (val otpResult = authRepository.requestWaOtp()) {
                is Result.Success -> onComplete(true, "OTP WhatsApp sudah dikirim")
                is Result.Error -> onComplete(false, otpResult.message)
            }
        }
    }

    fun verifyWaOtp(otp: String, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            when (val result = authRepository.verifyWaOtp(otp)) {
                is Result.Success -> {
                    result.data?.let { user ->
                        _uiState.value = ProfileUiState.Success(user)
                    } ?: loadProfile()
                    onComplete(true, "Nomor WhatsApp berhasil diverifikasi")
                }
                is Result.Error -> onComplete(false, result.message)
            }
        }
    }

    fun resendEmailVerification(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            when (val result = authRepository.resendEmailVerification()) {
                is Result.Success -> onComplete(true, "Email verifikasi sudah dikirim ulang")
                is Result.Error -> onComplete(false, result.message)
            }
        }
    }
}
