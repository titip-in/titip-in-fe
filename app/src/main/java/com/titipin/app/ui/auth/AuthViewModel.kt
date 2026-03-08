package com.titipin.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titipin.app.data.repository.AuthRepository
import com.titipin.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// State — semua kemungkinan kondisi UI auth
// Sealed class biar exhaustive — harus handle semua case
sealed class AuthState {
    object Idle : AuthState()       // initial state, belum ada aksi
    object Loading : AuthState()    // sedang proses (hit API)
    object Success : AuthState()    // berhasil login/register
    data class Error(val message: String) : AuthState() // gagal dengan pesan
}

// @HiltViewModel = Hilt tau cara inject ViewModel ini
// @Inject constructor = Hilt otomatis inject AuthRepository
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // StateFlow = versi Flow yang selalu punya nilai saat ini
    // MutableStateFlow = bisa diubah dari dalam ViewModel
    // asStateFlow() = expose versi read-only ke UI (UI tidak bisa ubah langsung)
    //
    // Konsep: ViewModel "publish" state, UI "subscribe" dan render sesuai state
    // Di XML dulu: LiveData. Di Compose modern: StateFlow
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // ── LOGIN ──────────────────────────────────────────────────────
    fun login(email: String, password: String) {
        // viewModelScope = coroutine scope yang tied ke lifecycle ViewModel
        // Kalau ViewModel destroyed (misal user keluar app), coroutine otomatis cancel
        // Di XML dulu ini harus manual cancel di onCleared()
        viewModelScope.launch {
            _authState.value = AuthState.Loading // tampilkan loading di UI

            val result = authRepository.login(email, password)

            // when = switch/when expression Kotlin — lebih ekspresif dari if-else
            _authState.value = when (result) {
                is Result.Success -> AuthState.Success
                is Result.Error   -> AuthState.Error(result.message)
            }
        }
    }

    // ── REGISTER ──────────────────────────────────────────────────
    fun register(name: String, email: String, password: String, waNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authRepository.register(name, email, password, waNumber)

            _authState.value = when (result) {
                is Result.Success -> AuthState.Success
                is Result.Error   -> AuthState.Error(result.message)
            }
        }
    }

    // Reset state ke Idle — dipanggil setelah error ditampilkan
    // Biar error tidak muncul terus kalau user rotate screen
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
