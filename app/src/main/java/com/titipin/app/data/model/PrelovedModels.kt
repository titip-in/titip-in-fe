package com.titipin.app.data.model

// ── PRELOVED ───────────────────────────────────────────────────────
data class PrelovedDto(
    val id: String,
    val userId: String,
    val user: UserSummary,
    val title: String,
    val description: String?,
    val price: Double,
    val category: String,
    val condition: String,   // "NEW" | "LIKE_NEW" | "GOOD" | "FAIR"
    val imageUrl: String?,
    val status: String,      // "AVAILABLE" | "SOLD" | "RESERVED"
    val createdAt: String
)

data class CreatePrelovedRequest(
    val title: String,
    val description: String? = null,
    val price: Int,          // IDR, integer
    val category: String,
    val condition: String,
    val imageUrl: String? = null
)

data class UpdatePrelovedStatusRequest(
    val status: String       // "AVAILABLE" | "SOLD" | "RESERVED"
)

// ── HELPERS ────────────────────────────────────────────────────────
fun PrelovedDto.conditionLabel(): String = when (condition) {
    "NEW"      -> "Baru"
    "LIKE_NEW" -> "Seperti Baru"
    "GOOD"     -> "Bagus"
    "FAIR"     -> "Layak Pakai"
    else       -> condition
}

fun PrelovedDto.formattedPrice(): String {
    val formatted = price.toLong().toString()
        .reversed().chunked(3).joinToString(".").reversed()
    return "Rp $formatted"
}
