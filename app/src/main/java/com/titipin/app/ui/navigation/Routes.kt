package com.titipin.app.ui.navigation

object Routes {
    // Splash
    const val SPLASH   = "splash"

    // Auth
    const val LOGIN    = "login"
    const val REGISTER = "register"

    // Main tabs
    const val HOME     = "home"
    const val JASTIP   = "jastip"
    const val PRELOVED = "preloved"
    const val PROFILE  = "profile"

    // Jastip sub-screens
    const val JASTIP_DETAIL_PATTERN = "jastip/{id}"
    const val JASTIP_REQUEST_DETAIL_PATTERN = "jastip/request/{id}"
    const val JASTIP_SAYA           = "jastip_saya"

    // Preloved sub-screens
    const val PRELOVED_DETAIL_PATTERN = "preloved/{id}"
    const val PRELOVED_SAYA           = "preloved_saya"

    // Profile sub-screens
    const val REVIEW_RATING = "review_rating"
    const val PENGATURAN    = "pengaturan"

    // Helper
    fun jastipDetail(id: String)   = "jastip/$id"
    fun jastipRequestDetail(id: String) = "jastip/request/$id"
    fun prelovedDetail(id: String) = "preloved/$id"
}
