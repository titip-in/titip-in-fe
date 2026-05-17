package com.titipin.app.shared

import org.junit.Assert.assertEquals
import org.junit.Test

class WhatsappUtilsTest {

    @Test
    fun waMessageJastip_includesRoute() {
        val message = waMessageJastip("Calf Coffee", "Sumbersari")

        assertEquals("Halo, aku mau nitip dari Calf Coffee ke Sumbersari. Masih bisa? 😊", message)
    }

    @Test
    fun waMessageJastipDetail_includesRouteAndDeadline() {
        val message = waMessageJastipDetail(
            fromLocation = "Malang",
            toLocation = "Jakarta",
            deadline = "17 Mei, 18:52"
        )

        assertEquals(
            "Halo! Aku tertarik dengan jastipmu dari Malang ke Jakarta (deadline 17 Mei, 18:52). Bisa nitip?",
            message
        )
    }

    @Test
    fun waMessagePreloved_includesTitleAndPrice() {
        val message = waMessagePreloved("Sepatu", "Rp 125.000")

        assertEquals("Halo, aku tertarik dengan Sepatu seharga Rp 125.000. Masih available?", message)
    }

    @Test
    fun waMessageWanted_includesTitle() {
        val message = waMessageWanted("Kalkulator")

        assertEquals("Halo! Aku punya barang yang kamu cari: Kalkulator. Mau lihat detailnya?", message)
    }
}
