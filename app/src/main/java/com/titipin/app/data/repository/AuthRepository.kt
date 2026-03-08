package com.titipin.app.data.repository

import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.*
import com.titipin.app.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

// Sealed class = tipe yang terbatas kemungkinannya
// Result bisa Success atau Error, tidak ada yang lain
// Ini cara Kotlin handle success/failure tanpa throw Exception
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val dataStore: DataStoreManager
) {
    // ── LOGIN ──────────────────────────────────────────────────────
    // Repository bertugas:
    // 1. Hit API
    // 2. Parse response
    // 3. Simpan token ke DataStore kalau sukses
    // 4. Return Result ke ViewModel
    //
    // ViewModel TIDAK perlu tau detail HTTP, parsing, DataStore
    // Itu semua urusan Repository — ini prinsip Single Responsibility
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))

            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()!!.data!!

                // Simpan token setelah login sukses
                dataStore.saveAuthData(
                    accessToken  = authData.accessToken,
                    refreshToken = "",   // BE belum implement refresh token
                    userId       = authData.user.id,
                    userName     = authData.user.name
                )

                Result.Success(authData)
            } else {
                // Parse error message dari BE
                val errorMsg = response.body()?.error?.message
                    ?: response.body()?.message
                    ?: "Login gagal, coba lagi"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            // Network error (no internet, timeout, dll)
            Result.Error("Tidak bisa terhubung ke server. Cek koneksi internetmu.")
        }
    }

    // ── REGISTER ──────────────────────────────────────────────────
    suspend fun register(
        name: String,
        email: String,
        password: String,
        waNumber: String
    ): Result<AuthResponse> {
        return try {
            // Format WA number: hilangkan leading 0, pastikan pakai 62
            val formattedWa = formatWaNumber(waNumber)

            val response = apiService.register(
                RegisterRequest(name, email, password, formattedWa)
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()!!.data!!

                dataStore.saveAuthData(
                    accessToken  = authData.accessToken,
                    refreshToken = "",   // BE belum implement refresh token
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

    // ── LOGOUT ────────────────────────────────────────────────────
    suspend fun logout() {
        dataStore.clearAuthData()
    }

    // ── HELPER ────────────────────────────────────────────────────
    // Format nomor WA: user input "085750..." → simpan "6285750..."
    private fun formatWaNumber(input: String): String {
        val cleaned = input.trim().replace(" ", "").replace("-", "")
        return when {
            cleaned.startsWith("62") -> cleaned
            cleaned.startsWith("0")  -> "62${cleaned.substring(1)}"
            else -> "62$cleaned"
        }
    }
}