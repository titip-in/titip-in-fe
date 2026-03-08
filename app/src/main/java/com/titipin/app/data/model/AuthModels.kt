package com.titipin.app.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val waNumber: String
)

// semua response BE: { "success": bool, "data": {...}, "message": "...", "error": {...} }
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: ApiError? = null
)

data class ApiError(
    val code: String,
    val message: String
)

data class AuthResponse(
    val accessToken: String,
    val user: UserData
)

data class UserData(
    val id: String,
    val name: String,
    val email: String,
    val waNumber: String,
    val avatarUrl: String? = null
)