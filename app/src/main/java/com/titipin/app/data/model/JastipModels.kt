package com.titipin.app.data.model



data class UserSummary(
    val name: String,
    val waNumber: String,
    val avatarUrl: String? = null
)
data class JastipDto(
    val id: String,
    val userId: String,
    val user: UserSummary,
    val fromLocation: String,
    val toLocation: String,
    val deadline: String,           // "2026-03-20T14:00:00"
    val latitude: Double,
    val longitude: Double,
    val notes: String?,
    val status: String,             // "ACTIVE" | "CLOSED"
    val createdAt: String
)

// ── REQUEST MODELS ─────────────────────────────────────────────────

data class CreateJastipRequest(
    val fromLocation: String,
    val toLocation: String,
    val deadline: String,       // format: "2026-03-20T14:00:00"
    val latitude: Double,
    val longitude: Double,
    val notes: String? = null
)

data class UpdateJastipStatusRequest(
    val status: String          // "ACTIVE" | "CLOSED"
)






















