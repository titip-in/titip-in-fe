package com.titipin.app.data.remote

import com.titipin.app.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── AUTH ───────────────────────────────────────────────────────
    @POST("v1/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("v1/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @GET("v1/auth/google")
    suspend fun getGoogleAuthUrl(): Response<ApiResponse<GoogleAuthUrlResponse>>

    @POST("v1/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Unit>>

    @POST("v1/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<Unit>>

    @POST("v1/email/verify")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<ApiResponse<Unit>>

    @POST("v1/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @POST("v1/email/resend")
    suspend fun resendEmailVerification(): Response<ApiResponse<Unit>>

    @GET("v1/me")
    suspend fun getMe(): Response<ApiResponse<UserData>>

    @PUT("v1/me")
    suspend fun updateMe(@Body request: UpdateProfileRequest): Response<ApiResponse<UserData>>

    @PUT("v1/me/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Unit>>

    @POST("v1/me/whatsapp/request-otp")
    suspend fun requestWaOtp(): Response<ApiResponse<Unit>>

    @POST("v1/me/whatsapp/verify-otp")
    suspend fun verifyWaOtp(@Body request: VerifyWaOtpRequest): Response<ApiResponse<UserData>>

    // ── UPLOAD ─────────────────────────────────────────────────────
    @Multipart
    @POST("v1/upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<ApiResponse<UploadImageResponse>>

    // ── CATEGORY ───────────────────────────────────────────────────
    @GET("v1/categories")
    suspend fun getCategories(
        @Query("type") type: String? = null
    ): Response<ApiResponse<List<CategoryDto>>>

    // ── SEARCH ─────────────────────────────────────────────────────
    @GET("v1/search")
    suspend fun searchJastip(
        @Query("q") query: String,
        @Query("type") type: String = "jastip"
    ): Response<ApiResponse<PaginatedResponse<JastipDto>>>

    @GET("v1/search")
    suspend fun searchPreloved(
        @Query("q") query: String,
        @Query("type") type: String = "preloved"
    ): Response<ApiResponse<PaginatedResponse<PrelovedDto>>>

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

    @POST("v1/jastip/listings/{id}/boost")
    suspend fun boostJastipListing(@Path("id") id: String): Response<ApiResponse<BoostResponse>>

    // ── PRELOVED ──────────────────────────────────────────────────
    @GET("v1/preloved/listings")
    suspend fun getPrelovedList(): Response<ApiResponse<List<PrelovedDto>>>

    @GET("v1/me/preloved/listings")
    suspend fun getMyPrelovedList(): Response<ApiResponse<PaginatedResponse<PrelovedDto>>>

    @GET("v1/preloved/listings/{id}")
    suspend fun getPrelovedDetail(@Path("id") id: String): Response<ApiResponse<PrelovedDto>>

    @POST("v1/preloved/listings")
    suspend fun createPreloved(@Body request: CreatePrelovedRequest): Response<ApiResponse<PrelovedDto>>

    @PUT("v1/preloved/listings/{id}")
    suspend fun updatePrelovedStatus(
        @Path("id") id: String,
        @Body request: UpdatePrelovedStatusRequest
    ): Response<ApiResponse<PrelovedDto>>

    @PUT("v1/preloved/listings/{id}")
    suspend fun updatePreloved(
        @Path("id") id: String,
        @Body request: UpdatePrelovedListingRequest
    ): Response<ApiResponse<PrelovedDto>>

    @DELETE("v1/preloved/listings/{id}")
    suspend fun deletePreloved(@Path("id") id: String): Response<ApiResponse<Unit>>

    @POST("v1/preloved/listings/{id}/boost")
    suspend fun boostPrelovedListing(@Path("id") id: String): Response<ApiResponse<BoostResponse>>

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

    @POST("v1/jastip/requests/{id}/boost")
    suspend fun boostJastipRequest(@Path("id") id: String): Response<ApiResponse<BoostResponse>>

    // ── PRELOVED REQUESTS (Barang Dicari) ────────────────────────
    @GET("v1/preloved/requests")
    suspend fun getPrelovedRequestList(): Response<ApiResponse<List<PrelovedRequestDto>>>

    @GET("v1/me/preloved/requests")
    suspend fun getMyPrelovedRequestList(): Response<ApiResponse<PaginatedResponse<PrelovedRequestDto>>>

    @GET("v1/preloved/requests/{id}")
    suspend fun getPrelovedRequestDetail(@Path("id") id: String): Response<ApiResponse<PrelovedRequestDto>>

    @POST("v1/preloved/requests")
    suspend fun createPrelovedRequest(@Body body: CreatePrelovedRequestBody): Response<ApiResponse<PrelovedRequestDto>>

    @PUT("v1/preloved/requests/{id}")
    suspend fun updatePrelovedRequest(
        @Path("id") id: String,
        @Body body: UpdatePrelovedRequestBody
    ): Response<ApiResponse<PrelovedRequestDto>>

    @DELETE("v1/preloved/requests/{id}")
    suspend fun deletePrelovedRequest(@Path("id") id: String): Response<ApiResponse<Unit>>

    @POST("v1/preloved/requests/{id}/boost")
    suspend fun boostPrelovedRequest(@Path("id") id: String): Response<ApiResponse<BoostResponse>>

    // ── ANALYTICS ─────────────────────────────────────────────────
    @POST("v1/items/{type}/{id}/click")
    suspend fun trackClick(
        @Path("type") type: String,
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>

    @GET("v1/me/analytics")
    suspend fun getAnalytics(): Response<ApiResponse<AnalyticsData>>

    // ── SUBSCRIPTION ───────────────────────────────────────────────
    @POST("v1/me/subscriptions/upgrade")
    suspend fun upgradeSubscription(
        @Body request: UpgradeSubscriptionRequest
    ): Response<ApiResponse<Unit>>

    // ── DELETE ACCOUNT ─────────────────────────────────────────────
    @DELETE("v1/me")
    suspend fun deleteAccount(): Response<ApiResponse<Unit>>
}
