package com.titipin.app.navigation

import java.net.URLEncoder

object Routes {
    const val SPLASH      = "splash"
    const val ONBOARDING  = "onboarding"
    const val LOGIN       = "login"
    const val REGISTER    = "register"
    const val SETUP_PROFILE = "setup_profile"
    const val HOME        = "home"
    const val JASTIP      = "jastip"
    const val PRELOVED    = "preloved"
    const val PROFILE     = "profile"

    const val JASTIP_DETAIL_PATTERN   = "jastip/{id}"
    const val JASTIP_REQUEST_DETAIL_PATTERN = "jastip/request/{id}"
    const val JASTIP_SAYA             = "jastip_saya"
    const val PRELOVED_DETAIL_PATTERN = "preloved/{id}"
    const val PRELOVED_REQUEST_DETAIL_PATTERN = "preloved/request/{id}"
    const val PRELOVED_SAYA           = "preloved_saya"
    const val REVIEW_RATING           = "review_rating"
    const val PENGATURAN              = "pengaturan"
    const val ANALYTICS               = "analytics"

    // Offer screen: from, to, requesterName, notes (optional)
    const val JASTIP_OFFER_PATTERN = "jastip_offer?from={from}&to={to}&name={name}&notes={notes}"

    fun jastipDetail(id: String)   = "jastip/$id"
    fun jastipRequestDetail(id: String) = "jastip/request/$id"
    fun prelovedDetail(id: String) = "preloved/$id"
    fun prelovedRequestDetail(id: String) = "preloved/request/$id"
    fun jastipOffer(
        from: String,
        to: String,
        name: String,
        notes: String = ""
    ): String {
        val enc = { s: String -> URLEncoder.encode(s, "UTF-8") }
        return "jastip_offer?from=${enc(from)}&to=${enc(to)}&name=${enc(name)}&notes=${enc(notes)}"
    }
}
