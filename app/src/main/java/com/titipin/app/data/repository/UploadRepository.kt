package com.titipin.app.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.titipin.app.data.remote.ApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadRepository @Inject constructor(
    private val api: ApiService,
    @ApplicationContext private val context: Context
) {

    /**
     * Upload satu image dari Uri ke POST /v1/upload.
     * Return: [Result.Success] berisi image_url, atau [Result.Error] berisi pesan error.
     */
    suspend fun uploadImage(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contentResolver: ContentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.Error("Gagal membaca file gambar")

            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", "upload.jpg", requestBody)

            val response = api.uploadImage(part)
            if (response.isSuccessful) {
                val url = response.body()?.data?.imageUrl
                if (url != null) Result.Success(url)
                else Result.Error("Server tidak mengembalikan URL gambar")
            } else {
                Result.Error("Upload gagal: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Terjadi kesalahan saat upload")
        }
    }

    /**
     * Upload beberapa gambar secara paralel.
     * Return: list URL yang berhasil diupload (bisa lebih sedikit dari input jika ada yang gagal).
     */
    suspend fun uploadImages(uris: List<Uri>): List<Result<String>> =
        withContext(Dispatchers.IO) {
            uris.map { uploadImage(it) }
        }
}
