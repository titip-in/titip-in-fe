package com.titipin.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.ui.theme.*

@Preview(showBackground = true)
@Composable
fun HomeScreen(
    onNavigateToJastip: () -> Unit = {},
    onNavigateToPreloved: () -> Unit = {},
    userName: String = "",
    userInitials: String = ""
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // ── TOP BAR ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Selamat datang,",
                    fontSize = 11.sp, color = Charcoal60,
                    fontWeight = FontWeight.Medium, fontFamily = DmSansFamily,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = if (userName.isNotEmpty()) "${userName.trim().split(" ").first()} 👋"
                    else "Titip.in 👋",
                    fontSize = 20.sp, fontWeight = FontWeight.Medium,
                    color = Charcoal, fontFamily = FrauncesFamily
                )
            }
            // Avatar inisial
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(Sage, Terracotta)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userInitials.ifEmpty { "T" },
                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White, fontFamily = DmSansFamily
                )
            }
        }

        // ── SEARCH BAR ────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg)
                .clip(RoundedCornerShape(Radius.md))
                .background(CreamDark)
                .padding(horizontal = Spacing.md, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔍", fontSize = 14.sp,
                modifier = Modifier.padding(end = 8.dp),
                color = Charcoal30
            )
            Text(
                text = "Cari jastip atau barang...",
                fontSize = 13.sp, color = Charcoal30, fontFamily = DmSansFamily
            )
        }

        Spacer(Modifier.height(Spacing.md))

        // ── BENTO GRID ────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Card 1: Jastip Aktif — full width Charcoal ────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.lg))
                    .background(Charcoal)
                    .clickable { onNavigateToJastip() }
                    .padding(Spacing.md)
            ) {
                // Blob dekorasi
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .offset(x = 20.dp, y = (-20).dp)
                        .clip(CircleShape)
                        .background(Sage.copy(alpha = 0.15f))
                        .align(Alignment.TopEnd)
                )
                Column {
                    Text(
                        text = "● TERSEDIA SEKARANG",
                        fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                        color = Sage, fontFamily = DmSansFamily, letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Jastip Aktif\ndi Malang",
                        fontSize = 20.sp, fontWeight = FontWeight.Normal,
                        color = Cream, fontFamily = FrauncesFamily,
                        fontStyle = FontStyle.Italic, lineHeight = 24.sp
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "—",
                                fontSize = 32.sp, fontWeight = FontWeight.Light,
                                color = Cream, fontFamily = FrauncesFamily, lineHeight = 36.sp
                            )
                            Text(
                                text = "jastip tersedia",
                                fontSize = 10.sp, color = Cream.copy(alpha = 0.5f),
                                fontFamily = DmSansFamily
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.full))
                                .background(Sage)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Lihat Semua →",
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                color = androidx.compose.ui.graphics.Color.White,
                                fontFamily = DmSansFamily
                            )
                        }
                    }
                }
            }

            // ── Card 2 + 3: Preloved & Request — 2 kolom ─────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Preloved Terbaru — Terracotta
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Radius.lg))
                        .background(Terracotta)
                        .clickable { onNavigateToPreloved() }
                        .padding(Spacing.md)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("👗", fontSize = 22.sp)
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            text = "Preloved\nTerbaru",
                            fontSize = 15.sp, fontWeight = FontWeight.Normal,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontFamily = FrauncesFamily, lineHeight = 20.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "— barang aktif",
                            fontSize = 10.sp,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                            fontFamily = DmSansFamily
                        )
                        Spacer(Modifier.height(Spacing.sm))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f))
                                .align(Alignment.End),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("→", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }

                // Request Jastip — SagePale
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Radius.lg))
                        .background(SagePale)
                        .clickable { onNavigateToJastip() }
                        .padding(Spacing.md)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("📍", fontSize = 22.sp)
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            text = "Request\nJastip",
                            fontSize = 15.sp, fontWeight = FontWeight.Normal,
                            color = Charcoal, fontFamily = FrauncesFamily, lineHeight = 20.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "— request terbuka",
                            fontSize = 10.sp, color = Charcoal60, fontFamily = DmSansFamily
                        )
                        Spacer(Modifier.height(Spacing.sm))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Sage)
                                .align(Alignment.End),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("→", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }
            }

            // ── Card 4: Featured Listing — GoldPale ───────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.lg))
                    .background(GoldPale)
                    .padding(horizontal = Spacing.md, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("⭐", fontSize = 24.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Featured Listing",
                        fontSize = 14.sp, fontWeight = FontWeight.Medium,
                        color = Charcoal, fontFamily = FrauncesFamily
                    )
                    Text(
                        "Promosikan jastipmu",
                        fontSize = 10.sp, color = Charcoal60, fontFamily = DmSansFamily
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.full))
                        .background(Gold)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Baru", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White, fontFamily = DmSansFamily)
                }
            }
        }

        Spacer(Modifier.height(Spacing.md))

        // ── AKTIVITAS TERBARU ─────────────────────────────────────
        Text(
            text = "AKTIVITAS TERBARU",
            fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp, color = Charcoal60, fontFamily = DmSansFamily,
            modifier = Modifier.padding(horizontal = Spacing.lg)
        )

        Spacer(Modifier.height(8.dp))

        // Activity items — static untuk sekarang, nanti bisa dari API
        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ActivityItem(
                dotColor = Sage,
                title = "Jastip aktif di sekitarmu",
                subtitle = "Tap untuk lihat semua jastip",
                time = "Sekarang",
                onClick = onNavigateToJastip
            )
            ActivityItem(
                dotColor = Terracotta,
                title = "Barang preloved terbaru",
                subtitle = "Preloved · Lihat semua barang",
                time = "Sekarang",
                onClick = onNavigateToPreloved
            )
        }

        Spacer(Modifier.height(Spacing.xl))
    }
}

// ── ACTIVITY ITEM ─────────────────────────────────────────────────
@Composable
fun ActivityItem(
    dotColor: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    time: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(androidx.compose.ui.graphics.Color.White)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                color = Charcoal, fontFamily = DmSansFamily)
            Text(subtitle, fontSize = 10.sp, color = Charcoal60, fontFamily = DmSansFamily)
        }
        Text(time, fontSize = 10.sp, color = Charcoal30, fontFamily = DmSansFamily)
    }
}