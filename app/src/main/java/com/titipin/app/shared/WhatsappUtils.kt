package com.titipin.app.shared

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

/**
 * Buka WhatsApp dengan pesan pre-filled.
 * waNumber format: "628xxx" (format internasional tanpa +)
 */
fun openWhatsApp(context: Context, waNumber: String, message: String = "") {
    val encoded = if (message.isNotEmpty())
        URLEncoder.encode(message, "UTF-8")
    else ""
    val url = if (encoded.isNotEmpty())
        "https://wa.me/$waNumber?text=$encoded"
    else
        "https://wa.me/$waNumber"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

/** Pre-filled message untuk jastip — dari list card */
fun waMessageJastip(fromLocation: String, toLocation: String): String =
    "Halo, aku mau nitip dari $fromLocation ke $toLocation. Masih bisa? 😊"

/** Pre-filled message untuk jastip detail */
fun waMessageJastipDetail(fromLocation: String, toLocation: String, deadline: String): String =
    "Halo! Aku tertarik dengan jastipmu dari $fromLocation ke $toLocation (deadline $deadline). Bisa nitip?"

/** Pre-filled message untuk request — provider ambil */
fun waMessageTakeRequest(fromLocation: String, toLocation: String): String =
    "Halo! Aku bisa antar titipanmu dari $fromLocation ke $toLocation. Masih butuh? 😊"

/** Pre-filled message untuk preloved */
fun waMessagePreloved(itemTitle: String, price: String): String =
    "Halo, aku tertarik dengan $itemTitle seharga $price. Masih available?"

/** Pre-filled message untuk wanted — seller offer */
fun waMessageWanted(itemTitle: String): String =
    "Halo! Aku punya barang yang kamu cari: $itemTitle. Mau lihat detailnya?"