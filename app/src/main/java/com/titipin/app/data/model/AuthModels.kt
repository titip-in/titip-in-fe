package com.titipin.app.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("wa_number")
    val waNumber: String? = null
)

data class UpdateProfileRequest(
    val name: String? = null,
    @SerializedName("wa_number")
    val waNumber: String? = null,
    val status: String? = null,
    @SerializedName("avatar_url")
    val avatarUrl: String? = null
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    val password: String
)

data class VerifyEmailRequest(
    val token: String
)

data class ChangePasswordRequest(
    @SerializedName("old_password")
    val oldPassword: String,
    @SerializedName("new_password")
    val newPassword: String
)

data class VerifyWaOtpRequest(
    val otp: String
)

// semua response BE: { "success": bool, "message": "...", "data": {...}, "errors": {...} }
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errors: Any? = null,
    val error: ApiError? = null
)

data class ApiError(
    val code: String,
    val message: String
)

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String,
    val user: UserData
)

data class GoogleAuthUrlResponse(
    val url: String
)

data class UserData(
    val id: Int,
    val name: String,
    val email: String,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: String? = null,
    @SerializedName("wa_number")
    val waNumber: String? = null,
    @SerializedName("wa_verified_at")
    val waVerifiedAt: String? = null,
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,
    val status: String? = null,
    val tier: String = UserTier.BASIC,
    @SerializedName("boost_quota")
    val boostQuota: Int = 0,
    @SerializedName("is_banned")
    val isBanned: Boolean = false,
    @SerializedName("tier_expired_at")
    val tierExpiredAt: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class UploadImageResponse(
    @SerializedName("image_url")
    val imageUrl: String
)

data class BoostResponse(
    @SerializedName("remaining_quota")
    val remainingQuota: Int,
    @SerializedName("boosted_at")
    val boostedAt: String
)

object UserTier {
    const val BASIC = "basic"
    const val PLUS = "plus"
    const val PRO = "pro"
}

fun String?.normalizedTier(): String = when (this?.lowercase()) {
    UserTier.PLUS -> UserTier.PLUS
    UserTier.PRO -> UserTier.PRO
    else -> UserTier.BASIC
}

fun tierActiveLimit(tier: String?): Int = when (tier.normalizedTier()) {
    UserTier.PLUS -> 10
    UserTier.PRO -> 20
    else -> 3
}

fun tierImageLimit(tier: String?): Int = when (tier.normalizedTier()) {
    UserTier.PLUS -> 5
    UserTier.PRO -> 10
    else -> 3
}

fun tierBoostLimit(tier: String?): Int = when (tier.normalizedTier()) {
    UserTier.PLUS -> 1
    UserTier.PRO -> 5
    else -> 0
}

fun tierDisplayName(tier: String?): String = when (tier.normalizedTier()) {
    UserTier.PLUS -> "Titip Plus"
    UserTier.PRO -> "Titip Pro"
    else -> "Titip Basic"
}
