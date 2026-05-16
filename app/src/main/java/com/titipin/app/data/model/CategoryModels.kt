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
