package com.titipin.app.data.repository

import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.*
import com.titipin.app.data.remote.ApiService
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONObject
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
                    tokenType    = authData.tokenType,
                    user         = authData.user
                )
                Result.Success(authData)
            } else {
                val errorMsg = response.errorMessage()
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
                    tokenType    = authData.tokenType,
                    user         = authData.user
                )
                Result.Success(authData)
            } else {
                val errorMsg = response.errorMessage()
                    ?: "Registrasi gagal, coba lagi"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server. Cek koneksi internetmu.")
        }
    }

    suspend fun logout() {
        try {
            apiService.logout()
        } catch (_: Exception) {
            // Local session still has to be cleared even if the token is expired or offline.
        } finally {
            dataStore.clearAuthData()
        }
    }

    // ── GET ME — ambil profil user yang sedang login ───────────────
    suspend fun getMe(): Result<UserData> {
        return try {
            val response = apiService.getMe()
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()!!.data!!
                dataStore.saveAuthDataFromUser(user)
                Result.Success(user)
            } else {
                if (response.code() == 401) dataStore.clearAuthData()
                Result.Error(response.errorMessage() ?: "Gagal memuat profil")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server.")
        }
    }

    suspend fun updateProfile(
        name: String? = null,
        waNumber: String? = null,
        status: String? = null,
        avatarUrl: String? = null
    ): Result<UserData> {
        return try {
            val request = UpdateProfileRequest(
                name = name,
                waNumber = waNumber?.let(::formatWaNumber),
                status = status,
                avatarUrl = avatarUrl
            )
            val response = apiService.updateMe(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()!!.data!!
                dataStore.saveAuthDataFromUser(user)
                Result.Success(user)
            } else {
                if (response.code() == 401) dataStore.clearAuthData()
                Result.Error(response.errorMessage() ?: "Gagal memperbarui profil")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server.")
        }
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

    private suspend fun DataStoreManager.saveAuthDataFromUser(user: UserData) {
        val token = accessToken.firstOrNull() ?: return
        val type = tokenType.firstOrNull() ?: "Bearer"
        saveAuthData(
            accessToken = token,
            tokenType = type,
            user = user
        )
    }

    private fun <T> retrofit2.Response<ApiResponse<T>>.errorMessage(): String? {
        body()?.error?.message?.let { return it }
        body()?.message?.let { return it }

        val raw = errorBody()?.string().orEmpty()
        if (raw.isBlank()) return null

        return runCatching {
            val json = JSONObject(raw)
            json.optString("message").takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
