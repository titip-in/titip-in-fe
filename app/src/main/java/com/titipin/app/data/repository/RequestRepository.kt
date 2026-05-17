package com.titipin.app.data.repository

import com.titipin.app.data.model.*
import com.titipin.app.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getRequestList(): Result<List<RequestDto>> {
        return try {
            val response = apiService.getRequestList()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data ?: emptyList())
            } else {
                response.toResultError("Gagal memuat data")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun createRequest(
        categoryId: Int?,
        title: String,
        fromLocation: String,
        toLocation: String,
        notes: String?
    ): Result<RequestDto> {
        return try {
            val response = apiService.createRequest(
                CreateRequestBody(
                    categoryId = categoryId,
                    title = title,
                    notes = notes,
                    fromLocation = fromLocation,
                    toLocation = toLocation
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                response.toResultError("Gagal membuat request")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun getRequestDetail(id: String): Result<RequestDto> {
        return try {
            val response = apiService.getRequestDetail(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                response.toResultError("Request tidak ditemukan")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun updateRequestStatus(id: String, status: String): Result<RequestDto> {
        return try {
            val response = apiService.updateRequest(id, UpdateRequestBody(status = status))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                response.toResultError("Gagal update request")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun updateRequest(id: String, body: UpdateRequestBody): Result<RequestDto> {
        return try {
            val response = apiService.updateRequest(id, body)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                response.toResultError("Gagal mengubah request")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun deleteRequest(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteRequest(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(Unit)
            } else {
                response.toResultError("Gagal menghapus request")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun getMyRequestList(): Result<List<RequestDto>> {
        return try {
            val response = apiService.getMyRequestList()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.data?.data.orEmpty())
            } else {
                response.toResultError("Gagal memuat data")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }
}
