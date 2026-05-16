package com.titipin.app.data.model

import com.google.gson.annotations.SerializedName

data class CategoryDto(
    val id: Int,
    val name: String,
    val icon: String? = null,
    val type: String,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class ListingImageDto(
    val id: Int? = null,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("is_primary")
    val isPrimary: Boolean = false,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class PaginatedResponse<T>(
    @SerializedName("current_page")
    val currentPage: Int? = null,
    val data: List<T> = emptyList(),
    @SerializedName("per_page")
    val perPage: Int? = null,
    val total: Int? = null,
    @SerializedName("next_page_url")
    val nextPageUrl: String? = null,
    @SerializedName("prev_page_url")
    val prevPageUrl: String? = null
)
