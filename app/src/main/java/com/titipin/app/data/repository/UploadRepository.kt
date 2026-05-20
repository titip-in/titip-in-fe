package com.titipin.app.data.repository

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.titipin.app.data.remote.ApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.UUID
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
            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val part = MultipartBody.Part.createFormData(
                "image",
                "upload-${UUID.randomUUID()}.$extension",
                requestBody
            )

            val response = api.uploadImage(part)
            if (response.isSuccessful) {
                val url = response.body()?.data?.imageUrl
                if (url != null) Result.Success(url)
                else Result.Error("Server tidak mengembalikan URL gambar")
            } else {
                response.toResultError("Upload gagal: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Terjadi kesalahan saat upload")
        }
    }

    suspend fun uploadCenterCroppedSquareImage(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contentResolver: ContentResolver = context.contentResolver
            val original = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                ?: return@withContext Result.Error("Gagal membaca file gambar")
            val side = minOf(original.width, original.height)
            val x = ((original.width - side) / 2).coerceAtLeast(0)
            val y = ((original.height - side) / 2).coerceAtLeast(0)
            val cropped = Bitmap.createBitmap(original, x, y, side, side)
            val scaled = Bitmap.createScaledBitmap(cropped, 720, 720, true)
            val output = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 88, output)
            val bytes = output.toByteArray()

            if (cropped != original) cropped.recycle()
            if (scaled != cropped) scaled.recycle()
            original.recycle()

            val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData(
                "image",
                "avatar-${UUID.randomUUID()}.jpg",
                requestBody
            )

            val response = api.uploadImage(part)
            if (response.isSuccessful) {
                val url = response.body()?.data?.imageUrl
                if (url != null) Result.Success(url)
                else Result.Error("Server tidak mengembalikan URL gambar")
            } else {
                response.toResultError("Upload gagal: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Terjadi kesalahan saat upload")
        }
    }

    suspend fun uploadAvatarBitmap(bitmap: Bitmap): Result<String> = withContext(Dispatchers.IO) {
        try {
            val output = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 88, output)
            val requestBody = output.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData(
                "image",
                "avatar-${UUID.randomUUID()}.jpg",
                requestBody
            )

            val response = api.uploadImage(part)
            if (response.isSuccessful) {
                val url = response.body()?.data?.imageUrl
                if (url != null) Result.Success(url)
                else Result.Error("Server tidak mengembalikan URL gambar")
            } else {
                response.toResultError("Upload gagal: ${response.code()} ${response.message()}")
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
