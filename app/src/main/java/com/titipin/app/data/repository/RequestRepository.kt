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
                Result.Error(response.body()?.error?.message ?: "Gagal memuat data")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun createRequest(
        fromLocation: String,
        toLocation: String,
        notes: String?
    ): Result<RequestDto> {
        return try {
            val response = apiService.createRequest(
                CreateRequestBody(fromLocation, toLocation, notes)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.error?.message ?: "Gagal membuat request")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun takeRequest(id: String): Result<TakeRequestResponse> {
        return try {
            val response = apiService.takeRequest(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.error?.message ?: "Gagal mengambil request")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }
}