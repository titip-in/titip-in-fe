package com.titipin.app.data.repository

import com.titipin.app.data.local.DataStoreManager
import com.titipin.app.data.model.*
import com.titipin.app.data.remote.ApiService
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(
        val message: String,
        val code: String? = null,
        val httpCode: Int? = null,
        val errors: String? = null
    ) : Result<Nothing>()
}

data class ApiFailure(
    val message: String,
    val code: String? = null,
    val httpCode: Int? = null,
    val errors: String? = null
)

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
                response.toResultError("Login gagal, coba lagi")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server. Cek koneksi internetmu.")
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        waNumber: String?
    ): Result<AuthResponse> {
        return try {
            val formattedWa = waNumber?.takeIf { it.isNotBlank() }?.let(::formatWaNumber)
            val response = apiService.register(RegisterRequest(name, email, password, formattedWa))
            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()!!.data!!
                Result.Success(authData)
            } else {
                response.toResultError("Registrasi gagal, coba lagi")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server. Cek koneksi internetmu.")
        }
    }

    suspend fun getGoogleAuthUrl(): Result<String> {
        return try {
            val response = apiService.getGoogleAuthUrl()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.data?.url.orEmpty())
            } else {
                response.toResultError("Gagal menyiapkan login Google")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server.")
        }
    }

    suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(Unit)
            } else {
                response.toResultError("Gagal mengirim link reset password")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server.")
        }
    }

    suspend fun resetPassword(token: String, password: String): Result<Unit> {
        return try {
            val response = apiService.resetPassword(ResetPasswordRequest(token, password))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(Unit)
            } else {
                response.toResultError("Gagal mengatur password baru")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server.")
        }
    }

    suspend fun verifyEmail(token: String): Result<Unit> {
        return try {
            val response = apiService.verifyEmail(VerifyEmailRequest(token))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(Unit)
            } else {
                response.toResultError("Gagal verifikasi email")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server.")
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
                response.toResultError("Gagal memuat profil")
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
                response.toResultError("Gagal memperbarui profil")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server.")
        }
    }

    suspend fun resendEmailVerification(): Result<Unit> {
        return try {
            val response = apiService.resendEmailVerification()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(Unit)
            } else {
                response.toResultError("Gagal mengirim ulang email verifikasi")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server.")
        }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            val response = apiService.changePassword(ChangePasswordRequest(oldPassword, newPassword))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(Unit)
            } else {
                response.toResultError("Gagal mengubah password")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server.")
        }
    }

    suspend fun requestWaOtp(): Result<Unit> {
        return try {
            val response = apiService.requestWaOtp()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(Unit)
            } else {
                response.toResultError("Gagal mengirim OTP WhatsApp")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server.")
        }
    }

    suspend fun verifyWaOtp(otp: String): Result<UserData?> {
        return try {
            val response = apiService.verifyWaOtp(VerifyWaOtpRequest(otp))
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()?.data
                if (user != null) dataStore.saveAuthDataFromUser(user)
                Result.Success(user)
            } else {
                response.toResultError("Gagal verifikasi OTP WhatsApp")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server.")
        }
    }

    suspend fun upgradeSubscription(tier: String, paymentProofUrl: String): Result<Unit> {
        return try {
            val response = apiService.upgradeSubscription(
                UpgradeSubscriptionRequest(tier = tier, paymentProofUrl = paymentProofUrl)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(Unit)
            } else {
                response.toResultError("Gagal mengirim permintaan upgrade")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server.")
        }
    }

    /**
     * Soft delete akun user. Backend akan rename email & WA.
     * FE wajib clear local storage setelah response 200.
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val response = apiService.deleteAccount()
            if (response.isSuccessful && response.body()?.success == true) {
                dataStore.clearAuthData()
                Result.Success(Unit)
            } else {
                response.toResultError("Gagal menghapus akun")
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

    private fun <T> retrofit2.Response<ApiResponse<T>>.toResultError(defaultMessage: String): Result.Error {
        val failure = apiFailure(defaultMessage)
        return Result.Error(
            message = failure.message,
            code = failure.code,
            httpCode = failure.httpCode,
            errors = failure.errors
        )
    }
}
