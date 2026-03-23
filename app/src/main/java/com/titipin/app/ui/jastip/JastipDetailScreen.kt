package com.titipin.app.ui.jastip

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.ui.theme.*
import androidx.core.net.toUri

@Composable
fun JastipDetailScreen(
    jastipId: String,
    onBack: () -> Unit = {},
    viewModel: JastipViewModel = hiltViewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(jastipId) { viewModel.loadDetail(jastipId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
    ) {
        // ── TOP BAR ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali", tint = Charcoal)
            }
            Text(
                text = "DETAIL JASTIP",
                fontSize = 11.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp, fontFamily = DmSansFamily, color = Charcoal60
            )
        }

        when (val state = detailState) {
            is JastipActionState.Loading, is JastipActionState.Idle -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
                }
            }
            is JastipActionState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("😕", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(state.message, color = Charcoal60, fontFamily = DmSansFamily, fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = onBack) {
                            Text("Kembali", color = Terracotta, fontFamily = DmSansFamily)
                        }
                    }
                }
            }
            is JastipActionState.Success -> {
                val jastip = state.data ?: return@Column
                val deadlineTime = jastip.deadline.substringAfter("T").take(5)
                val deadlineDate = jastip.deadline.substringBefore("T")
                val initials = jastip.user.name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.lg)
                ) {
                    Spacer(Modifier.height(Spacing.sm))

                    // ── USER ROW ──────────────────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SagePale),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(initials, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Sage, fontFamily = DmSansFamily)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(jastip.user.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Charcoal, fontFamily = DmSansFamily)
                            Text("⭐ — · Malang", fontSize = 12.sp, color = Charcoal60, fontFamily = DmSansFamily)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.full))
                                .background(if (jastip.status == "ACTIVE") SagePale else CreamDark)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (jastip.status == "ACTIVE") "● Aktif" else "✕ Tutup",
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                color = if (jastip.status == "ACTIVE") Sage else Charcoal30,
                                fontFamily = DmSansFamily
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.md))

                    // ── RUTE CARD ─────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.lg))
                            .background(Charcoal)
                            .padding(Spacing.lg)
                    ) {
                        Column {
                            Text("RUTE JASTIP", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Cream.copy(alpha = 0.35f), fontFamily = DmSansFamily)
                            Spacer(Modifier.height(14.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(jastip.fromLocation, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Cream, fontFamily = DmSansFamily)
                                    Text("Titik berangkat", fontSize = 10.sp, color = Cream.copy(alpha = 0.4f), fontFamily = DmSansFamily)
                                }
                                Text("→", fontSize = 22.sp, color = Terracotta, modifier = Modifier.padding(horizontal = 8.dp))
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text(jastip.toLocation, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Cream, fontFamily = DmSansFamily)
                                    Text("Tujuan", fontSize = 10.sp, color = Cream.copy(alpha = 0.4f), fontFamily = DmSansFamily)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.sm))

                    // ── INFO TILES ────────────────────────────────
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
                        InfoTile("📅", "TANGGAL", deadlineDate, Modifier.weight(1f))
                        InfoTile("⏰", "DEADLINE", deadlineTime, Modifier.weight(1f))
                    }

                    // ── CATATAN ───────────────────────────────────
                    if (!jastip.notes.isNullOrEmpty()) {
                        Spacer(Modifier.height(Spacing.sm))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.md))
                                .background(CreamDark)
                                .padding(Spacing.md)
                        ) {
                            Column {
                                Text("CATATAN", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Charcoal60, fontFamily = DmSansFamily)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "\"${jastip.notes}\"",
                                    fontSize = 13.sp, color = Charcoal,
                                    fontFamily = DmSansFamily, fontStyle = FontStyle.Italic, lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.xl))
                }

                // ── BOTTOM CTA ────────────────────────────────────
                HorizontalDivider(color = Charcoal10, thickness = 0.5.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW,
                                "https://wa.me/${jastip.user.waNumber}".toUri())
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ComponentSize.buttonHeight),
                        shape  = RoundedCornerShape(Radius.full),
                        colors = ButtonDefaults.buttonColors(containerColor = Sage, contentColor = Cream)
                    ) {
                        Text(
                            text = "💬 Hubungi via WhatsApp",
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = DmSansFamily
                        )
                    }
                }
            }
        }
    }
}

// ── INFO TILE ─────────────────────────────────────────────────────
@Composable
fun InfoTile(emoji: String, label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.md))
            .background(CreamDark)
            .padding(Spacing.md)
    ) {
        Column {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Charcoal60, fontFamily = DmSansFamily)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Charcoal, fontFamily = DmSansFamily)
        }
    }
}