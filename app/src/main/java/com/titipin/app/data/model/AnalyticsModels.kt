package com.titipin.app.data.model

import com.google.gson.annotations.SerializedName

data class AnalyticsData(
    @SerializedName("total_views")
    val totalViews: Int = 0,
    @SerializedName("total_clicks")
    val totalClicks: Int = 0,
    @SerializedName("item_details")
    val itemDetails: List<AnalyticsItemDetail> = emptyList(),
    @SerializedName("conversion_rate")
    val conversionRate: Double = 0.0,
    @SerializedName("best_item")
    val bestItem: AnalyticsItemDetail? = null
)

data class AnalyticsItemDetail(
    val id: String,
    val title: String,
    val type: String,       // "jastip_listing" | "jastip_request" | "preloved_listing" | "preloved_request"
    val views: Int = 0,
    val clicks: Int = 0
) {
    val typeLabel: String get() = when (type) {
        "jastip_listing"   -> "Jastip Listing"
        "jastip_request"   -> "Jastip Request"
        "preloved_listing" -> "Preloved Listing"
        "preloved_request" -> "Preloved Request"
        else               -> type
    }

    val typeEmoji: String get() = when (type) {
        "jastip_listing"   -> "📦"
        "jastip_request"   -> "📍"
        "preloved_listing" -> "🛍️"
        "preloved_request" -> "🔍"
        else               -> "📄"
    }
}

data class UpgradeSubscriptionRequest(
    val tier: String,
    @SerializedName("payment_proof_url")
    val paymentProofUrl: String
)
