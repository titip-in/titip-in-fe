package com.titipin.app.navigation

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
    // JASTIP_FORM dihapus — sekarang pakai ModalBottomSheet di dalam JastipScreen

    // Preloved sub-screens
    const val PRELOVED_DETAIL_PATTERN = "preloved/{id}"
    // PRELOVED_FORM juga nanti ModalBottomSheet

    // Helper
    fun jastipDetail(id: String)   = "jastip/$id"
    fun prelovedDetail(id: String) = "preloved/$id"
}