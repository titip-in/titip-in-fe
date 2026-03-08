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

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.login(email, password)
            _authState.value = when (result) {
                is Result.Success -> AuthState.Success
                is Result.Error   -> AuthState.Error(result.message)
            }
        }
    }

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

    // reset setelah error ditampilkan — mencegah re-trigger saat recomposition
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
