package com.titipin.app.data.remote

import com.titipin.app.data.local.DataStoreManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

// Interceptor = middleware untuk OkHttp
// Analoginya kayak plugin/middleware di Hapi.js yang intercept semua request
// Setiap HTTP request yang keluar dari app WAJIB lewat sini dulu
class AuthInterceptor @Inject constructor(
    private val dataStore: DataStoreManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Ambil token dari DataStore
        // runBlocking di sini karena intercept() bukan suspend function
        // OkHttp jalan di background thread, jadi aman — tidak block UI
        val token = runBlocking {
            dataStore.accessToken.firstOrNull()
        }

        // Kalau tidak ada token (belum login / sudah logout),
        // langsung terusin request tanpa Authorization header
        // Ini untuk endpoint public seperti GET /jastip, POST /auth/login
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Ada token → tambah header Authorization ke request
        // newBuilder() = copy request lama, tambah/ganti header, build ulang
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}