package com.titipin.app.data.model

import com.google.gson.annotations.SerializedName


data class UserSummary(
    val id: Int? = null,
    val name: String,
    @SerializedName("wa_number")
    val waNumber: String,
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,
    val status: String? = null
)
data class JastipDto(
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    val user: UserSummary = UserSummary(name = "Pengguna Titip.in", waNumber = ""),
    @SerializedName("category_id")
    val categoryId: Int? = null,
    val category: CategoryDto? = null,
    val title: String = "",
    @SerializedName("description")
    val notes: String?,
    @SerializedName("from_loc")
    val fromLocation: String,
    @SerializedName("to_loc")
    val toLocation: String,
    val deadline: String,           // "2026-03-20T14:00:00"
    @SerializedName("lat")
    val latitude: Double? = null,
    @SerializedName("lng")
    val longitude: Double? = null,
    val images: List<ListingImageDto>? = emptyList(),
    @SerializedName("primary_image_url")
    val primaryImageUrlRaw: String? = null,  // field langsung dari API list
    val status: String,             // "ACTIVE" | "CLOSED"
    @SerializedName("created_at")
    val createdAt: String
)

fun JastipDto.primaryImageUrl(): String? =
    images.orEmpty().firstOrNull { it.isPrimary }?.imageUrl
        ?: images.orEmpty().firstOrNull()?.imageUrl
        ?: primaryImageUrlRaw  // fallback ke field direct jika images tidak direturn API

// ── REQUEST MODELS ─────────────────────────────────────────────────

data class CreateJastipRequest(
    @SerializedName("category_id")
    val categoryId: Int? = null,
    val title: String,
    @SerializedName("description")
    val notes: String? = null,
    @SerializedName("from_loc")
    val fromLocation: String,
    @SerializedName("to_loc")
    val toLocation: String,
    val deadline: String,       // format: "2026-03-20T14:00:00"
    @SerializedName("lat")
    val latitude: Double? = null,
    @SerializedName("lng")
    val longitude: Double? = null,
    val status: String? = null,
    @SerializedName("primary_image_url")
    val primaryImageUrl: String? = null,
    val images: List<String>
)

data class UpdateJastipStatusRequest(
    val status: String          // "ACTIVE" | "CLOSED"
)

data class UpdateJastipListingRequest(
    @SerializedName("category_id")
    val categoryId: Int? = null,
    val title: String? = null,
    @SerializedName("description")
    val notes: String? = null,
    @SerializedName("from_loc")
    val fromLocation: String? = null,
    @SerializedName("to_loc")
    val toLocation: String? = null,
    val deadline: String? = null,
    val status: String? = null,
    @SerializedName("primary_image_url")
    val primaryImageUrl: String? = null,
    val images: List<String>? = null
)





















