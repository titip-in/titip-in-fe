package com.titipin.app.data.remote

import com.titipin.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── AUTH ───────────────────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @GET("auth/me")
    suspend fun getMe(): Response<ApiResponse<UserData>>

    // ── JASTIP ────────────────────────────────────────────────────
    @GET("jastip")
    suspend fun getJastipList(): Response<ApiResponse<List<JastipDto>>>

    @GET("jastip/{id}")
    suspend fun getJastipDetail(@Path("id") id: String): Response<ApiResponse<JastipDto>>

    @POST("jastip")
    suspend fun createJastip(@Body request: CreateJastipRequest): Response<ApiResponse<JastipDto>>

    @PUT("jastip/{id}")
    suspend fun updateJastipStatus(
        @Path("id") id: String,
        @Body request: UpdateJastipStatusRequest
    ): Response<ApiResponse<JastipDto>>

    @DELETE("jastip/{id}")
    suspend fun deleteJastip(@Path("id") id: String): Response<ApiResponse<Unit>>

    // ── PRELOVED ──────────────────────────────────────────────────
    @GET("preloved")
    suspend fun getPrelovedList(): Response<ApiResponse<List<PrelovedDto>>>

    @GET("preloved/{id}")
    suspend fun getPrelovedDetail(@Path("id") id: String): Response<ApiResponse<PrelovedDto>>

    @POST("preloved")
    suspend fun createPreloved(@Body request: CreatePrelovedRequest): Response<ApiResponse<PrelovedDto>>

    @PUT("preloved/{id}")
    suspend fun updatePrelovedStatus(
        @Path("id") id: String,
        @Body request: UpdatePrelovedStatusRequest
    ): Response<ApiResponse<PrelovedDto>>

    @DELETE("preloved/{id}")
    suspend fun deletePreloved(@Path("id") id: String): Response<ApiResponse<Unit>>
}