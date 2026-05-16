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

    // ── CATEGORY ───────────────────────────────────────────────────
    @GET("v1/categories")
    suspend fun getCategories(
        @Query("type") type: String? = null
    ): Response<ApiResponse<List<CategoryDto>>>

    // ── JASTIP ────────────────────────────────────────────────────
    @GET("v1/jastip/listings")
    suspend fun getJastipList(): Response<ApiResponse<List<JastipDto>>>

    @GET("v1/me/jastip/listings")
    suspend fun getMyJastipList(): Response<ApiResponse<PaginatedResponse<JastipDto>>>

    @GET("v1/jastip/listings/{id}")
    suspend fun getJastipDetail(@Path("id") id: String): Response<ApiResponse<JastipDto>>

    @POST("v1/jastip/listings")
    suspend fun createJastip(@Body request: CreateJastipRequest): Response<ApiResponse<JastipDto>>

    @PUT("v1/jastip/listings/{id}")
    suspend fun updateJastipStatus(
        @Path("id") id: String,
        @Body request: UpdateJastipStatusRequest
    ): Response<ApiResponse<JastipDto>>

    @PUT("v1/jastip/listings/{id}")
    suspend fun updateJastip(
        @Path("id") id: String,
        @Body request: UpdateJastipListingRequest
    ): Response<ApiResponse<JastipDto>>

    @DELETE("v1/jastip/listings/{id}")
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
    @GET("v1/jastip/requests")
    suspend fun getRequestList(): Response<ApiResponse<List<RequestDto>>>

    @GET("v1/me/jastip/requests")
    suspend fun getMyRequestList(): Response<ApiResponse<PaginatedResponse<RequestDto>>>

    @GET("v1/jastip/requests/{id}")
    suspend fun getRequestDetail(@Path("id") id: String): Response<ApiResponse<RequestDto>>

    @POST("v1/jastip/requests")
    suspend fun createRequest(@Body body: CreateRequestBody): Response<ApiResponse<RequestDto>>

    @PUT("v1/jastip/requests/{id}")
    suspend fun updateRequest(
        @Path("id") id: String,
        @Body body: UpdateRequestBody
    ): Response<ApiResponse<RequestDto>>

    @DELETE("v1/jastip/requests/{id}")
    suspend fun deleteRequest(@Path("id") id: String): Response<ApiResponse<Unit>>

    // ── WANTED (Barang Dicari) ────────────────────────────────────
    @GET("wanted")
    suspend fun getWantedList(): Response<ApiResponse<List<WantedDto>>>

    @POST("wanted")
    suspend fun createWanted(@Body body: CreateWantedBody): Response<ApiResponse<WantedDto>>

    @PUT("wanted/{id}/fulfill")
    suspend fun fulfillWanted(@Path("id") id: String): Response<ApiResponse<FulfillWantedResponse>>
}
