package com.titipin.app.ui.jastip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.ui.theme.*

/**
 * Screen untuk provider "apply" ke sebuah request jastip.
 * UI sudah siap, tapi action button menunggu BE endpoint:
 *   POST /requests/{id}/offer
 *
 * Saat ini tombol "Ambil Request" masih placeholder — akan diaktifkan
 * setelah BE phase 2 (offer system) selesai.
 */
@Composable
fun JastipOfferScreen(
    fromLocation: String,
    toLocation: String,
    requesterName: String,
    notes: String?,
    onBack: () -> Unit,
    // Akan diisi saat BE ready: onSubmitOffer: () -> Unit
) {
    var confirmChecked by remember { mutableStateOf(false) }

    Scaffold(containerColor = Cream) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            // ── HEADER ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali", tint = Charcoal)
                }
                Spacer(Modifier.width(4.dp))
                Column {
                    Text("● JASTIP", color = Terracotta, fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp,
                        fontFamily = DmSansFamily)
                    Text("Ambil Request", color = Charcoal, fontSize = 24.sp,
                        fontWeight = FontWeight.Medium, fontFamily = FrauncesFamily)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Spacer(Modifier.height(4.dp))

                // ── Request detail card ───────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.lg))
                        .background(Charcoal)
                        .padding(Spacing.lg)
                ) {
                    // Decorative blob
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .offset(x = 16.dp, y = (-16).dp)
                            .clip(CircleShape)
                            .background(Sage.copy(alpha = 0.12f))
                            .align(Alignment.TopEnd)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        // Requester info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            val initials = requesterName.trim().split(" ")
                                .filter { it.isNotBlank() }.take(2)
                                .joinToString("") { it.first().uppercase() }
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape)
                                    .background(GoldPale.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(initials, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                    color = Gold, fontFamily = DmSansFamily)
                            }
                            Column {
                                Text(requesterName, fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold, color = Cream,
                                    fontFamily = DmSansFamily)
                                Text("Butuh dititipin", fontSize = 11.sp,
                                    color = Cream.copy(alpha = 0.5f), fontFamily = DmSansFamily)
                            }
                        }

                        // Rute
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.md))
                                .background(Cream.copy(alpha = 0.07f))
                                .padding(Spacing.md),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("RUTE PENGIRIMAN", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp, color = Sage, fontFamily = DmSansFamily)
                            Spacer(Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Dari", fontSize = 9.sp, color = Cream.copy(alpha = 0.4f),
                                        fontFamily = DmSansFamily)
                                    Text(fromLocation, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                        color = Cream, fontFamily = DmSansFamily)
                                }
                                Text("→", fontSize = 16.sp, color = Sage)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Ke", fontSize = 9.sp, color = Cream.copy(alpha = 0.4f),
                                        fontFamily = DmSansFamily)
                                    Text(toLocation, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                        color = Cream, fontFamily = DmSansFamily)
                                }
                            }
                        }

                        // Catatan requester
                        if (!notes.isNullOrEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(Radius.md))
                                    .background(Gold.copy(alpha = 0.12f))
                                    .padding(Spacing.md)
                            ) {
                                Text("CATATAN", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp, color = Gold, fontFamily = DmSansFamily)
                                Spacer(Modifier.height(4.dp))
                                Text("\"$notes\"", fontSize = 13.sp, color = Cream.copy(alpha = 0.85f),
                                    fontFamily = DmSansFamily, lineHeight = 18.sp)
                            }
                        }
                    }
                }

                // ── Info tentang flow ─────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .background(SagePale)
                        .padding(Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("ℹ️", fontSize = 14.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Cara kerja:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = Charcoal, fontFamily = DmSansFamily)
                        listOf(
                            "Kamu apply sebagai provider untuk request ini",
                            "Requester akan melihat daftarmu dan memilih",
                            "Jika dipilih, kamu akan dinotifikasi dan bisa langsung chat via WhatsApp"
                        ).forEachIndexed { i, step ->
                            Text("${i + 1}. $step", fontSize = 11.sp, color = Charcoal60,
                                fontFamily = DmSansFamily, lineHeight = 16.sp)
                        }
                    }
                }

                // ── Checklist konfirmasi ──────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Checkbox(
                        checked         = confirmChecked,
                        onCheckedChange = { confirmChecked = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor   = Sage,
                            checkmarkColor = Color.White
                        )
                    )
                    Text(
                        text = "Saya siap mengambilkan titipan dari $fromLocation ke $toLocation",
                        fontSize = 12.sp, color = Charcoal, fontFamily = DmSansFamily,
                        lineHeight = 17.sp, modifier = Modifier.weight(1f)
                    )
                }

                // ── Coming soon banner ────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .background(GoldPale)
                        .padding(Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🚧", fontSize = 14.sp)
                    Text(
                        "Fitur offer sedang dalam pengembangan. Tombol akan aktif setelah update berikutnya.",
                        fontSize = 11.sp, color = Charcoal, fontFamily = DmSansFamily,
                        lineHeight = 16.sp
                    )
                }

                Spacer(Modifier.height(Spacing.sm))
            }

            // ── SUBMIT BUTTON ─────────────────────────────────────
            HorizontalDivider(color = CreamDark, thickness = 1.dp)
            Column(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)) {
                Button(
                    onClick  = { /* TODO: POST /requests/{id}/offer — BE phase 2 */ },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(Radius.full),
                    enabled  = false, // disabled sampai BE ready
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = Sage,
                        contentColor           = Color.White,
                        disabledContainerColor = CreamDark,
                        disabledContentColor   = Charcoal30
                    )
                ) {
                    Text(
                        "Ambil Request — Segera Hadir",
                        fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Fitur ini akan aktif setelah update backend.",
                    fontSize = 11.sp, color = Charcoal30, fontFamily = DmSansFamily,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}