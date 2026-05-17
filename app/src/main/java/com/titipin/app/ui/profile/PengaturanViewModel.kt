package com.titipin.app.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.UserData
import com.titipin.app.data.repository.AuthRepository
import com.titipin.app.data.repository.Result
import com.titipin.app.data.repository.UploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PengaturanUiState {
    object Loading : PengaturanUiState()
    data class Ready(val user: UserData) : PengaturanUiState()
    data class Error(val message: String) : PengaturanUiState()
}

sealed class PengaturanActionState {
    object Idle : PengaturanActionState()
    object Loading : PengaturanActionState()
    data class Success(val message: String = "Profil berhasil diperbarui") : PengaturanActionState()
    data class Error(val message: String) : PengaturanActionState()
}

@HiltViewModel
class PengaturanViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val uploadRepository: UploadRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PengaturanUiState>(PengaturanUiState.Loading)
    val uiState: StateFlow<PengaturanUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<PengaturanActionState>(PengaturanActionState.Idle)
    val actionState: StateFlow<PengaturanActionState> = _actionState.asStateFlow()

    // Toggle state untuk notifikasi — disimpan lokal saja untuk sekarang
    private val _notifJastip = MutableStateFlow(true)
    val notifJastip: StateFlow<Boolean> = _notifJastip.asStateFlow()

    private val _notifPesan = MutableStateFlow(false)
    val notifPesan: StateFlow<Boolean> = _notifPesan.asStateFlow()

    init { loadUser() }

    fun loadUser() {
        viewModelScope.launch {
            _uiState.value = PengaturanUiState.Loading
            _uiState.value = when (val result = authRepository.getMe()) {
                is Result.Success -> PengaturanUiState.Ready(result.data)
                is Result.Error   -> PengaturanUiState.Error(result.message)
            }
        }
    }

    fun toggleNotifJastip() { _notifJastip.value = !_notifJastip.value }
    fun toggleNotifPesan()  { _notifPesan.value  = !_notifPesan.value }

    fun updateProfile(
        name: String? = null,
        waNumber: String? = null,
        status: String? = null,
        avatarUrl: String? = null
    ) {
        viewModelScope.launch {
            _actionState.value = PengaturanActionState.Loading
            when (val result = authRepository.updateProfile(name, waNumber, status, avatarUrl)) {
                is Result.Success -> {
                    _uiState.value = PengaturanUiState.Ready(result.data)
                    _actionState.value = PengaturanActionState.Success("Profil berhasil diperbarui")
                }
                is Result.Error -> {
                    _actionState.value = PengaturanActionState.Error(result.message)
                }
            }
        }
    }

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            _actionState.value = PengaturanActionState.Loading
            when (val uploadResult = uploadRepository.uploadImage(uri)) {
                is Result.Success -> {
                    when (val result = authRepository.updateProfile(avatarUrl = uploadResult.data)) {
                        is Result.Success -> {
                            _uiState.value = PengaturanUiState.Ready(result.data)
                            _actionState.value = PengaturanActionState.Success("Foto profil berhasil diperbarui")
                        }
                        is Result.Error -> _actionState.value = PengaturanActionState.Error(result.message)
                    }
                }
                is Result.Error -> _actionState.value = PengaturanActionState.Error(uploadResult.message)
            }
        }
    }

    fun resendEmailVerification() {
        viewModelScope.launch {
            _actionState.value = PengaturanActionState.Loading
            _actionState.value = when (val result = authRepository.resendEmailVerification()) {
                is Result.Success -> PengaturanActionState.Success("Email verifikasi sudah dikirim ulang")
                is Result.Error -> PengaturanActionState.Error(result.message)
            }
        }
    }

    fun requestWaOtp() {
        viewModelScope.launch {
            _actionState.value = PengaturanActionState.Loading
            _actionState.value = when (val result = authRepository.requestWaOtp()) {
                is Result.Success -> PengaturanActionState.Success("OTP WhatsApp sudah dikirim")
                is Result.Error -> PengaturanActionState.Error(result.message)
            }
        }
    }

    fun verifyWaOtp(otp: String) {
        viewModelScope.launch {
            _actionState.value = PengaturanActionState.Loading
            when (val result = authRepository.verifyWaOtp(otp)) {
                is Result.Success -> {
                    val user = result.data
                    if (user != null) {
                        _uiState.value = PengaturanUiState.Ready(user)
                    } else {
                        loadUser()
                    }
                    _actionState.value = PengaturanActionState.Success("Nomor WhatsApp berhasil diverifikasi")
                }
                is Result.Error -> _actionState.value = PengaturanActionState.Error(result.message)
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _actionState.value = PengaturanActionState.Loading
            _actionState.value = when (val result = authRepository.changePassword(oldPassword, newPassword)) {
                is Result.Success -> PengaturanActionState.Success("Password berhasil diubah")
                is Result.Error -> PengaturanActionState.Error(result.message)
            }
        }
    }

    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            _actionState.value = PengaturanActionState.Loading
            _actionState.value = when (val result = authRepository.forgotPassword(email)) {
                is Result.Success -> PengaturanActionState.Success("Link reset password akan dikirim jika email terdaftar")
                is Result.Error -> PengaturanActionState.Error(result.message)
            }
        }
    }

    fun resetActionState() { _actionState.value = PengaturanActionState.Idle }
}
