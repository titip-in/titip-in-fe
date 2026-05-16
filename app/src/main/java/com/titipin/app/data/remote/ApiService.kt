package com.titipin.app.data.remote

import com.titipin.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── AUTH ───────────────────────────────────────────────────────
    @POST("v1/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("v1/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("v1/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @GET("v1/me")
    suspend fun getMe(): Response<ApiResponse<UserData>>

    @PUT("v1/me")
    suspend fun updateMe(@Body request: UpdateProfileRequest): Response<ApiResponse<UserData>>

    // ── JASTIP ────────────────────────────────────────────────────
    @GET("jastip")
    suspend fun getJastipList(): Response<ApiResponse<List<JastipDto>>>

    @GET("jastip")
    suspend fun getMyJastipList(
        @Query("userId") userId: String
    ): Response<ApiResponse<List<JastipDto>>>

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

    @GET("preloved")
    suspend fun getMyPrelovedList(
        @Query("userId") userId: String
    ): Response<ApiResponse<List<PrelovedDto>>>

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

    // ── REQUESTS (Cari Jastip) ────────────────────────────────────
    @GET("requests")
    suspend fun getRequestList(): Response<ApiResponse<List<RequestDto>>>

    @POST("requests")
    suspend fun createRequest(@Body body: CreateRequestBody): Response<ApiResponse<RequestDto>>

    @PUT("requests/{id}/take")
    suspend fun takeRequest(@Path("id") id: String): Response<ApiResponse<TakeRequestResponse>>

    // ── WANTED (Barang Dicari) ────────────────────────────────────
    @GET("wanted")
    suspend fun getWantedList(): Response<ApiResponse<List<WantedDto>>>

    @POST("wanted")
    suspend fun createWanted(@Body body: CreateWantedBody): Response<ApiResponse<WantedDto>>

    @PUT("wanted/{id}/fulfill")
    suspend fun fulfillWanted(@Path("id") id: String): Response<ApiResponse<FulfillWantedResponse>>
}
