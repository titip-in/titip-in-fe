package com.titipin.app.data.repository

import com.titipin.app.data.model.CategoryDto
import com.titipin.app.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getCategories(type: String? = null): Result<List<CategoryDto>> {
        return try {
            val response = apiService.getCategories(type)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.data.orEmpty())
            } else {
                Result.Error(response.body()?.message ?: "Gagal memuat kategori")
            }
        } catch (e: Exception) {
            Result.Error("Tidak bisa terhubung ke server.")
        }
    }
}
