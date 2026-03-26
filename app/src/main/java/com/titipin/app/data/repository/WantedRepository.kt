package com.titipin.app.data.repository

import com.titipin.app.data.model.*
import com.titipin.app.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WantedRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getWantedList(): Result<List<WantedDto>> {
        return try {
            val response = apiService.getWantedList()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data ?: emptyList())
            } else {
                Result.Error(response.body()?.error?.message ?: "Gagal memuat data")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun createWanted(
        title: String,
        description: String?,
        maxPrice: Double?,
        category: String?
    ): Result<WantedDto> {
        return try {
            val response = apiService.createWanted(
                CreateWantedBody(title, description, maxPrice, category)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.error?.message ?: "Gagal membuat pencarian")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun fulfillWanted(id: String): Result<FulfillWantedResponse> {
        return try {
            val response = apiService.fulfillWanted(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.error?.message ?: "Gagal fulfill wanted")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }
}