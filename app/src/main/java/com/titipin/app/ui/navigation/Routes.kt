package com.titipin.app.navigation

// Semua route navigasi didefinisiin di sini sebagai konstanta
// Tujuannya: biar ga typo waktu manggil navController.navigate("...")
// Mirip kayak R.id.fragment_home di XML dulu, tapi lebih simpel

object Routes {
    // Auth
    const val LOGIN     = "login"
    const val REGISTER  = "register"

    // Main screens (bottom nav)
    const val HOME      = "home"
    const val JASTIP    = "jastip"
    const val PRELOVED  = "preloved"
    const val PROFILE   = "profile"

    // Jastip sub-screens
    const val JASTIP_DETAIL     = "jastip_detail/{jastipId}"
    const val JASTIP_FORM       = "jastip_form"

    // Preloved sub-screens
    const val PRELOVED_DETAIL   = "preloved_detail/{prelovedId}"
    const val PRELOVED_FORM     = "preloved_form"

    // Profile sub-screens
    const val JASTIP_SAYA       = "jastip_saya"
    const val PRELOVED_SAYA     = "preloved_saya"
    const val PENGATURAN        = "pengaturan"
    const val REVIEW            = "review"

    // Helper untuk navigasi ke detail dengan ID
    // Contoh: Routes.jastipDetail("abc-123") → "jastip_detail/abc-123"
    fun jastipDetail(id: String)   = "jastip_detail/$id"
    fun prelovedDetail(id: String) = "preloved_detail/$id"
}
