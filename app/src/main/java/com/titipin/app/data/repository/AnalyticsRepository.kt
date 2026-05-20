package com.titipin.app.data.repository

import com.titipin.app.data.model.AnalyticsData
import com.titipin.app.data.remote.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepository @Inject constructor(
    private val apiService: ApiService
) {

    /**
     * Track klik WA pada listing/request. Fire-and-forget, tidak perlu menunggu response.
     * Dipanggil secara background tanpa loading state di UI.
     */
    fun trackClick(type: String, id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiService.trackClick(type, id)
            } catch (_: Exception) {
                // Intentionally ignored — tracking failure should not affect UX
            }
        }
    }

    /**
     * Ambil data analytics user. Hanya Plus/Pro yang berhasil (Basic dapat 403).
     */
    suspend fun getAnalytics(): Result<AnalyticsData> {
        return try {
            val response = apiService.getAnalytics()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                val httpCode = response.code()
                val message = response.body()?.message
                    ?: if (httpCode == 403)
                        "Analytics hanya tersedia untuk pengguna Plus dan Pro. Upgrade untuk membuka fitur ini."
                    else
                        "Gagal memuat analytics"
                Result.Error(message, httpCode = httpCode)
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server.")
        }
    }
}
