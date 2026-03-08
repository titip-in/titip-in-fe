package com.titipin.app.data.model

// ── REQUEST MODELS ─────────────────────────────────────────────────
// Harus match persis field name dengan BE (case-sensitive!)

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

// ── RESPONSE MODELS ────────────────────────────────────────────────
// Wrapper global — semua response BE pakai format ini:
// { "success": true, "data": {...}, "message": "..." }
// { "success": false, "error": { "code": "...", "message": "..." } }
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

// AuthResponse — sesuai BE actual (belum ada refreshToken)
// BE: AuthResponse(accessToken, user)
// Nanti kalau BE tambah refreshToken, tinggal tambah field di sini
data class AuthResponse(
    val accessToken: String,
    val user: UserData
)

// UserData — sesuai UserDto di BE
// Tidak ada rating/totalJastip/totalPreloved karena BE belum implement
data class UserData(
    val id: String,
    val name: String,
    val email: String,
    val waNumber: String,
    val avatarUrl: String? = null   // nullable, sama seperti BE
)