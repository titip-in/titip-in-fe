package com.titipin.app.data.model

// ── REQUEST (Cari Jastip) ──────────────────────────────────────────
data class RequestDto(
    val id: String,
    val userId: String,
    val user: UserSummary,
    val fromLocation: String,
    val toLocation: String,
    val notes: String?,
    val status: String,      // "OPEN" | "TAKEN" | "CLOSED"
    val createdAt: String
)

data class CreateRequestBody(
    val fromLocation: String,
    val toLocation: String,
    val notes: String? = null
)

data class TakeRequestResponse(
    val request: RequestDto,
    val takenBy: UserSummary
)

// ── WANTED (Barang Dicari) ─────────────────────────────────────────
data class WantedDto(
    val id: String,
    val userId: String,
    val user: UserSummary,
    val title: String,
    val description: String?,
    val maxPrice: Double?,   // IDR, optional
    val category: String?,
    val status: String,      // "OPEN" | "FOUND" | "CLOSED"
    val createdAt: String
)

data class CreateWantedBody(
    val title: String,
    val description: String? = null,
    val maxPrice: Double? = null,
    val category: String? = null
)

data class FulfillWantedResponse(
    val wantedItem: WantedDto,
    val foundBy: UserSummary
)

// ── HELPERS ────────────────────────────────────────────────────────
fun WantedDto.formattedMaxPrice(): String? {
    val price = maxPrice ?: return null
    val formatted = price.toLong().toString()
        .reversed().chunked(3).joinToString(".").reversed()
    return "Budget ~Rp $formatted"
}