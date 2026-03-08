package com.titipin.app.data.remote

import com.titipin.app.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Interface Retrofit — define semua endpoint API
// Ini sama persis kayak di XML project dulu, ga ada bedanya di Compose
// Retrofit akan otomatis generate implementasinya
interface ApiService {

    // POST /auth/login
    // @Body = request body (JSON otomatis di-convert dari data class)
    // Response<T> = wrapper Retrofit yang include HTTP status code
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<AuthResponse>>

    // POST /auth/register
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<AuthResponse>>

    // Nanti tambah endpoint lain di sini sesuai phase
    // @GET("jastip"), @POST("jastip"), dst
}