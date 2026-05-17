package com.titipin.app.data.repository

import com.titipin.app.data.model.*
import com.titipin.app.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JastipRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getJastipList(): Result<List<JastipDto>> {
        return try {
            val response = apiService.getJastipList()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data ?: emptyList())
            } else {
                response.toResultError("Gagal memuat data")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun getJastipDetail(id: String): Result<JastipDto> {
        return try {
            val response = apiService.getJastipDetail(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                response.toResultError("Jastip tidak ditemukan")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun searchJastip(query: String): Result<List<JastipDto>> {
        return try {
            val response = apiService.searchJastip(query)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.data?.data.orEmpty())
            } else {
                response.toResultError("Gagal mencari jastip")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun createJastip(request: CreateJastipRequest): Result<JastipDto> {
        return try {
            val response = apiService.createJastip(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                response.toResultError("Gagal membuat jastip")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun updateStatus(id: String, status: String): Result<JastipDto> {
        return try {
            val response = apiService.updateJastipStatus(id, UpdateJastipStatusRequest(status))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                response.toResultError("Gagal update status")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun updateJastip(id: String, request: UpdateJastipListingRequest): Result<JastipDto> {
        return try {
            val response = apiService.updateJastip(id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                response.toResultError("Gagal mengubah jastip")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }

    suspend fun deleteJastip(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteJastip(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(Unit)
            } else {
                response.toResultError("Gagal menghapus jastip")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server")
        }
    }
suspend fun getMyJastipList(userId: String? = null): Result<List<JastipDto>> {
    return try {
        val response = apiService.getMyJastipList()
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
