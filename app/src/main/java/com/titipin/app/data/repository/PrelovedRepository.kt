package com.titipin.app.data.repository

import com.titipin.app.data.model.*
import com.titipin.app.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrelovedRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getPrelovedList(): Result<List<PrelovedDto>> {
        return try {
            val response = apiService.getPrelovedList()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data ?: emptyList())
            } else {
                Result.Error(response.body()?.error?.message ?: "Gagal memuat data")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun getPrelovedDetail(id: String): Result<PrelovedDto> {
        return try {
            val response = apiService.getPrelovedDetail(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.error?.message ?: "Item tidak ditemukan")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun searchPreloved(query: String): Result<List<PrelovedDto>> {
        return try {
            val response = apiService.searchPreloved(query)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.data?.data.orEmpty())
            } else {
                Result.Error(response.body()?.message ?: response.body()?.error?.message ?: "Gagal mencari preloved")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun createPreloved(request: CreatePrelovedRequest): Result<PrelovedDto> {
        return try {
            val response = apiService.createPreloved(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: response.body()?.error?.message ?: "Gagal posting barang")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun updateStatus(id: String, status: String): Result<PrelovedDto> {
        return try {
            val response = apiService.updatePrelovedStatus(id, UpdatePrelovedStatusRequest(status))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: response.body()?.error?.message ?: "Gagal update status")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun updatePreloved(id: String, request: UpdatePrelovedListingRequest): Result<PrelovedDto> {
        return try {
            val response = apiService.updatePreloved(id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: response.body()?.error?.message ?: "Gagal mengubah barang")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun deletePreloved(id: String): Result<Unit> {
        return try {
            val response = apiService.deletePreloved(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(Unit)
            } else {
                Result.Error(response.body()?.error?.message ?: "Gagal menghapus item")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun getMyPrelovedList(userId: String? = null): Result<List<PrelovedDto>> {
        return try {
            val response = apiService.getMyPrelovedList()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.data?.data.orEmpty())
            } else {
                Result.Error(response.body()?.message ?: response.body()?.error?.message ?: "Gagal memuat data")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }
}
