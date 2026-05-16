package com.titipin.app.data.model

import com.google.gson.annotations.SerializedName

// ── REQUEST (Cari Jastip) ──────────────────────────────────────────
data class RequestDto(
    val id: String,
    @SerializedName("user_id")
    val userId: Int? = null,
    val user: UserSummary = UserSummary(name = "Pengguna Titip.in", waNumber = ""),
    @SerializedName("category_id")
    val categoryId: Int? = null,
    val category: CategoryDto? = null,
    val title: String,
    @SerializedName("description")
    val notes: String?,
    @SerializedName("from_loc")
    val fromLocation: String,
    @SerializedName("to_loc")
    val toLocation: String,
    val status: String,      // "OPEN" | "TAKEN" | "CLOSED"
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class CreateRequestBody(
    @SerializedName("category_id")
    val categoryId: Int? = null,
    val title: String,
    @SerializedName("description")
    val notes: String? = null,
    @SerializedName("from_loc")
    val fromLocation: String,
    @SerializedName("to_loc")
    val toLocation: String,
    val status: String? = null
)

data class UpdateRequestBody(
    @SerializedName("category_id")
    val categoryId: Int? = null,
    val title: String? = null,
    @SerializedName("description")
    val notes: String? = null,
    @SerializedName("from_loc")
    val fromLocation: String? = null,
    @SerializedName("to_loc")
    val toLocation: String? = null,
    val status: String? = null
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
