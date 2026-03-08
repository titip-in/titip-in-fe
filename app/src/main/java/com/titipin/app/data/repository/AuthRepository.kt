package com.titipin.app.data.repository

import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.*
import com.titipin.app.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val dataStore: DataStoreManager
) {
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()!!.data!!
                dataStore.saveAuthData(
                    accessToken  = authData.accessToken,
                    refreshToken = "",
                    userId       = authData.user.id,
                    userName     = authData.user.name
                )
                Result.Success(authData)
            } else {
                val errorMsg = response.body()?.error?.message
                    ?: response.body()?.message
                    ?: "Login gagal, coba lagi"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server. Cek koneksi internetmu.")
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        waNumber: String
    ): Result<AuthResponse> {
        return try {
            val formattedWa = formatWaNumber(waNumber)
            val response = apiService.register(RegisterRequest(name, email, password, formattedWa))
            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()!!.data!!
                dataStore.saveAuthData(
                    accessToken  = authData.accessToken,
                    refreshToken = "",
                    userId       = authData.user.id,
                    userName     = authData.user.name
                )
                Result.Success(authData)
            } else {
                val errorMsg = response.body()?.error?.message
                    ?: response.body()?.message
                    ?: "Registrasi gagal, coba lagi"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server. Cek koneksi internetmu.")
        }
    }

    suspend fun logout() {
        dataStore.clearAuthData()
    }

    // "085750..." → "6285750..."
    private fun formatWaNumber(input: String): String {
        val cleaned = input.trim().replace(" ", "").replace("-", "")
        return when {
            cleaned.startsWith("62") -> cleaned
            cleaned.startsWith("0")  -> "62${cleaned.substring(1)}"
            else -> "62$cleaned"
        }
    }
}