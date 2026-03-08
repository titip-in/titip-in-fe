package com.titipin.app.navigation

object Routes {
    const val LOGIN     = "login"
    const val REGISTER  = "register"

    const val HOME      = "home"
    const val JASTIP    = "jastip"
    const val PRELOVED  = "preloved"
    const val PROFILE   = "profile"

    const val JASTIP_DETAIL     = "jastip_detail/{jastipId}"
    const val JASTIP_FORM       = "jastip_form"

    const val PRELOVED_DETAIL   = "preloved_detail/{prelovedId}"
    const val PRELOVED_FORM     = "preloved_form"

    const val JASTIP_SAYA       = "jastip_saya"
    const val PRELOVED_SAYA     = "preloved_saya"
    const val PENGATURAN        = "pengaturan"
    const val REVIEW            = "review"

    fun jastipDetail(id: String)   = "jastip_detail/$id"
    fun prelovedDetail(id: String) = "preloved_detail/$id"
}
