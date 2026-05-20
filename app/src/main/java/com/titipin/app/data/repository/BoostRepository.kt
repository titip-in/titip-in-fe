package com.titipin.app.data.repository

import com.titipin.app.data.model.ApiResponse
import com.titipin.app.data.model.BoostResponse
import com.titipin.app.data.remote.ApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoostRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun boostJastipListing(id: String): Result<BoostResponse> =
        boost(defaultMessage = "Gagal boost jastip") { apiService.boostJastipListing(id) }

    suspend fun boostJastipRequest(id: String): Result<BoostResponse> =
        boost(defaultMessage = "Gagal boost request jastip") { apiService.boostJastipRequest(id) }

    suspend fun boostPrelovedListing(id: String): Result<BoostResponse> =
        boost(defaultMessage = "Gagal boost barang") { apiService.boostPrelovedListing(id) }

    suspend fun boostPrelovedRequest(id: String): Result<BoostResponse> =
        boost(defaultMessage = "Gagal boost pencarian") { apiService.boostPrelovedRequest(id) }

    private suspend fun boost(
        defaultMessage: String,
        call: suspend () -> Response<ApiResponse<BoostResponse>>
    ): Result<BoostResponse> {
        return try {
            val response = call()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.Success(response.body()!!.data!!)
            } else {
                val failure = response.apiFailure(defaultMessage)
                Result.Error(friendlyBoostMessage(failure.message), failure.code, failure.httpCode, failure.errors)
            }
        } catch (_: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    private fun friendlyBoostMessage(message: String): String {
        return when {
            message.contains("not have enough boost quota", ignoreCase = true) ->
                "Kuota boost habis. Upgrade ke Plus/Pro atau tunggu reset quota berikutnya."
            message.contains("inactive", ignoreCase = true) || message.contains("closed", ignoreCase = true) ->
                "Item yang tidak aktif tidak bisa di-boost."
            message.contains("not authorized", ignoreCase = true) ->
                "Kamu hanya bisa boost item milik sendiri."
            else -> message
        }
    }
}
