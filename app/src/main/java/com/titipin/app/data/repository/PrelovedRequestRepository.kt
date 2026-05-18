package com.titipin.app.data.repository

import com.titipin.app.data.model.*
import com.titipin.app.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrelovedRequestRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getPrelovedRequestList(): Result<List<PrelovedRequestDto>> {
        return try {
            val response = apiService.getPrelovedRequestList()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data ?: emptyList())
            } else {
                response.toResultError("Gagal memuat data")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun getMyPrelovedRequestList(): Result<List<PrelovedRequestDto>> {
        return try {
            val response = apiService.getMyPrelovedRequestList()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data?.data ?: emptyList())
            } else {
                response.toResultError("Gagal memuat data")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun getPrelovedRequestDetail(id: String): Result<PrelovedRequestDto> {
        return try {
            val response = apiService.getPrelovedRequestDetail(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                response.toResultError("Data tidak ditemukan")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun createPrelovedRequest(
        categoryId: Int?,
        title: String,
        description: String?,
        maxPrice: Int?
    ): Result<PrelovedRequestDto> {
        return try {
            val response = apiService.createPrelovedRequest(
                CreatePrelovedRequestBody(
                    categoryId = categoryId,
                    title = title,
                    description = description,
                    maxPrice = maxPrice
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                response.toResultError("Gagal membuat pencarian")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun updatePrelovedRequest(
        id: String,
        body: UpdatePrelovedRequestBody
    ): Result<PrelovedRequestDto> {
        return try {
            val response = apiService.updatePrelovedRequest(id, body)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                response.toResultError("Gagal memperbarui")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun toggleStatus(id: String, currentStatus: String): Result<PrelovedRequestDto> {
        val newStatus = if (currentStatus == "OPEN") "CLOSED" else "OPEN"
        return updatePrelovedRequest(id, UpdatePrelovedRequestBody(status = newStatus))
    }

    suspend fun deletePrelovedRequest(id: String): Result<Unit> {
        return try {
            val response = apiService.deletePrelovedRequest(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(Unit)
            } else {
                response.toResultError("Gagal menghapus")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }
}
