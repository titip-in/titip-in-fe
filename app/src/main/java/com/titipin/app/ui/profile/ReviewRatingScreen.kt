package com.titipin.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewRatingScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    Text("● PROFIL", color = Terracotta, fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp,
                        fontFamily = DmSansFamily)
                    Text("Review & Rating", color = Charcoal, fontSize = 24.sp,
                        fontWeight = FontWeight.Medium, fontFamily = FrauncesFamily)
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // ── Rating summary card ───────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.lg))
                            .background(Charcoal)
                            .padding(Spacing.lg)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Angka rating besar
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(90.dp)
                            ) {
                                val rating = if (uiState is ProfileUiState.Success) "—" else "—"
                                Text(
                                    text = rating,
                                    fontFamily = FrauncesFamily,
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Light,
                                    color = Cream,
                                    lineHeight = 52.sp
                                )
                                Text("★★★★★", color = Gold, fontSize = 13.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("— ulasan", fontSize = 10.sp,
                                    color = Cream.copy(alpha = 0.4f), fontFamily = DmSansFamily)
                            }

                            Spacer(Modifier.width(Spacing.lg))

                            // Bar chart rating
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(5, 4, 3, 2, 1).forEach { star ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("$star", fontSize = 10.sp,
                                            color = Cream.copy(alpha = 0.5f),
                                            fontFamily = DmSansFamily,
                                            modifier = Modifier.width(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Cream.copy(alpha = 0.1f))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Info banner — nunggu BE ───────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(GoldPale)
                            .padding(Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⭐", fontSize = 16.sp)
                        Text(
                            "Review dari transaksi selesai akan muncul di sini. Fitur rating sedang dalam pengembangan.",
                            fontFamily = DmSansFamily, fontSize = 12.sp,
                            color = Charcoal, lineHeight = 18.sp
                        )
                    }
                }

                // ── Empty state ───────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(GoldPale),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⭐", fontSize = 32.sp)
                            }
                            Text(
                                "Belum ada ulasan",
                                fontFamily = FrauncesFamily, fontSize = 18.sp,
                                fontWeight = FontWeight.Medium, color = Charcoal
                            )
                            Text(
                                "Selesaikan transaksi jastip atau\npreloved untuk mendapat ulasan.",
                                fontFamily = DmSansFamily, fontSize = 13.sp,
                                color = Charcoal60, textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}