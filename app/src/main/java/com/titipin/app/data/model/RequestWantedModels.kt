package com.titipin.app.data.model

import com.google.gson.annotations.SerializedName

// ── JASTIP REQUEST (Cari Jastip) ──────────────────────────────────────────
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
    val status: String,      // "OPEN" | "CLOSED"
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

// ── PRELOVED REQUEST (Barang Dicari) ──────────────────────────────────────
data class PrelovedRequestDto(
    val id: String,
    @SerializedName("user_id")
    val userId: Int? = null,
    val user: UserSummary = UserSummary(name = "Pengguna Titip.in", waNumber = ""),
    @SerializedName("category_id")
    val categoryId: Int? = null,
    val category: CategoryDto? = null,
    val title: String,
    val description: String?,
    @SerializedName("max_price")
    val maxPrice: Int?,          // IDR, integer (nullable — opsional)
    val status: String,          // "OPEN" | "CLOSED"
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class CreatePrelovedRequestBody(
    @SerializedName("category_id")
    val categoryId: Int? = null,
    val title: String,
    val description: String? = null,
    @SerializedName("max_price")
    val maxPrice: Int? = null,
    val status: String? = null
)

data class UpdatePrelovedRequestBody(
    @SerializedName("category_id")
    val categoryId: Int? = null,
    val title: String? = null,
    val description: String? = null,
    @SerializedName("max_price")
    val maxPrice: Int? = null,
    val status: String? = null
)

// ── HELPERS ────────────────────────────────────────────────────────────────
fun PrelovedRequestDto.formattedMaxPrice(): String? {
    val price = maxPrice ?: return null
    val formatted = price.toString()
        .reversed().chunked(3).joinToString(".").reversed()
    return "~Rp $formatted"
}
