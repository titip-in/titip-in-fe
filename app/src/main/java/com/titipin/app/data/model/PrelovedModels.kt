package com.titipin.app.data.model

import com.google.gson.annotations.SerializedName

// ── PRELOVED ───────────────────────────────────────────────────────
data class PrelovedDto(
    val id: String,
    @SerializedName("user_id")
    val userId: Int? = null,
    val user: UserSummary = UserSummary(name = "Pengguna Titip.in", waNumber = ""),
    @SerializedName("category_id")
    val categoryId: Int? = null,
    val category: CategoryDto? = null,
    val title: String,
    val description: String?,
    val price: Int,
    val condition: String,   // "NEW" | "LIKE_NEW" | "GOOD" | "FAIR"
    val images: List<ListingImageDto> = emptyList(),
    val status: String,      // "AVAILABLE" | "SOLD" | "CLOSED"
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class CreatePrelovedRequest(
    @SerializedName("category_id")
    val categoryId: Int? = null,
    val title: String,
    val description: String? = null,
    val price: Int,          // IDR, integer
    val condition: String,
    @SerializedName("primary_image_url")
    val primaryImageUrl: String? = null,
    val status: String? = null,
    val images: List<String>
)

data class UpdatePrelovedStatusRequest(
    val status: String       // "AVAILABLE" | "SOLD" | "CLOSED"
)

data class UpdatePrelovedListingRequest(
    @SerializedName("category_id")
    val categoryId: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val price: Int? = null,
    val condition: String? = null,
    @SerializedName("primary_image_url")
    val primaryImageUrl: String? = null,
    val status: String? = null,
    val images: List<String>? = null
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
    val formatted = price.toString()
        .reversed().chunked(3).joinToString(".").reversed()
    return "Rp $formatted"
}

fun PrelovedDto.primaryImageUrl(): String? =
    images.firstOrNull { it.isPrimary }?.imageUrl ?: images.firstOrNull()?.imageUrl
