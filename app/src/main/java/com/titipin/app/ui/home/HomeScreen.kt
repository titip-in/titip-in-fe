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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.data.model.JastipDto
import com.titipin.app.data.model.PrelovedDto
import com.titipin.app.data.model.formattedPrice
import com.titipin.app.shared.timeAgo
import com.titipin.app.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToJastip: () -> Unit = {},
    onNavigateToPreloved: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    "Selamat datang,",
                    fontSize = 11.sp, color = Charcoal60,
                    fontWeight = FontWeight.Medium, fontFamily = DmSansFamily,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = if (uiState.userName.isNotEmpty())
                        "${uiState.userName.trim().split(" ").first()} 👋"
                    else "Titip.in 👋",
                    fontSize = 20.sp, fontWeight = FontWeight.Medium,
                    color = Charcoal, fontFamily = FrauncesFamily
                )
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Sage, Terracotta))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    uiState.userInitials.ifEmpty { "T" },
                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = Color.White, fontFamily = DmSansFamily
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
            Text("🔍", fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))
            Text("Cari jastip atau barang...", fontSize = 13.sp, color = Charcoal30, fontFamily = DmSansFamily)
        }

        Spacer(Modifier.height(Spacing.md))

        // ── BENTO GRID ────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card 1: Jastip Aktif
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.lg))
                    .background(Charcoal)
                    .clickable { onNavigateToJastip() }
                    .padding(Spacing.md)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .offset(x = 20.dp, y = (-20).dp)
                        .clip(CircleShape)
                        .background(Sage.copy(alpha = 0.15f))
                        .align(Alignment.TopEnd)
                )
                Column {
                    Text("● TERSEDIA SEKARANG", fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold, color = Sage,
                        fontFamily = DmSansFamily, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Jastip Aktif\ndi Malang", fontSize = 20.sp,
                        color = Cream, fontFamily = FrauncesFamily,
                        fontStyle = FontStyle.Italic, lineHeight = 24.sp)
                    Spacer(Modifier.height(Spacing.sm))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            // Pakai total semua jastip yang di-fetch
                            Text(
                                text = if (uiState.allJastip.isNotEmpty())
                                    "${uiState.allJastip.size}" else "—",
                                fontSize = 32.sp, fontWeight = FontWeight.Light,
                                color = Cream, fontFamily = FrauncesFamily, lineHeight = 36.sp
                            )
                            Text("jastip tersedia", fontSize = 10.sp,
                                color = Cream.copy(alpha = 0.5f), fontFamily = DmSansFamily)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.full))
                                .background(Sage)
                                .clickable { onNavigateToJastip() }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("Lihat Semua →", fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White, fontFamily = DmSansFamily)
                        }
                    }
                }
            }

            // Card 2 + 3
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Radius.lg))
                        .background(Terracotta)
                        .clickable { onNavigateToPreloved() }
                        .padding(Spacing.md)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("👗", fontSize = 22.sp)
                        Spacer(Modifier.height(Spacing.sm))
                        Text("Preloved\nTerbaru", fontSize = 15.sp,
                            color = Color.White, fontFamily = FrauncesFamily, lineHeight = 20.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (uiState.allPreloved.isNotEmpty())
                                "${uiState.allPreloved.size} barang aktif" else "— barang aktif",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f), fontFamily = DmSansFamily
                        )
                        Spacer(Modifier.height(Spacing.sm))
                        Box(
                            modifier = Modifier.size(28.dp).clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .align(Alignment.End),
                            contentAlignment = Alignment.Center
                        ) { Text("→", fontSize = 12.sp, color = Color.White) }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Radius.lg))
                        .background(SagePale)
                        .clickable { onNavigateToJastip() }
                        .padding(Spacing.md)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("📍", fontSize = 22.sp)
                        Spacer(Modifier.height(Spacing.sm))
                        Text("Request\nJastip", fontSize = 15.sp,
                            color = Charcoal, fontFamily = FrauncesFamily, lineHeight = 20.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("— request terbuka", fontSize = 10.sp,
                            color = Charcoal60, fontFamily = DmSansFamily)
                        Spacer(Modifier.height(Spacing.sm))
                        Box(
                            modifier = Modifier.size(28.dp).clip(CircleShape)
                                .background(Sage).align(Alignment.End),
                            contentAlignment = Alignment.Center
                        ) { Text("→", fontSize = 12.sp, color = Color.White) }
                    }
                }
            }

            // Card 4: Featured
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
                    Text("Featured Listing", fontSize = 14.sp,
                        fontWeight = FontWeight.Medium, color = Charcoal, fontFamily = FrauncesFamily)
                    Text("Promosikan jastipmu", fontSize = 10.sp,
                        color = Charcoal60, fontFamily = DmSansFamily)
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(Radius.full))
                        .background(Gold).padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Baru", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = Color.White, fontFamily = DmSansFamily)
                }
            }
        }

        Spacer(Modifier.height(Spacing.md))

        // ── AKTIVITAS TERBARU ─────────────────────────────────────
        Text("AKTIVITAS TERBARU", fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp,
            color = Charcoal60, fontFamily = DmSansFamily,
            modifier = Modifier.padding(horizontal = Spacing.lg))

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().padding(Spacing.md), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                }
            } else {
                uiState.recentJastip.forEach { jastip ->
                    ActivityItem(
                        dotColor = Sage,
                        title    = "${jastip.fromLocation} → ${jastip.toLocation}",
                        subtitle = jastip.user.name.trim(),
                        time     = timeAgo(jastip.createdAt),
                        onClick  = onNavigateToJastip
                    )
                }
                uiState.recentPreloved.forEach { preloved ->
                    ActivityItem(
                        dotColor = Terracotta,
                        title    = preloved.title,
                        subtitle = "Preloved · ${preloved.formattedPrice()}",
                        time     = timeAgo(preloved.createdAt),
                        onClick  = onNavigateToPreloved
                    )
                }
                if (uiState.recentJastip.isEmpty() && uiState.recentPreloved.isEmpty()) {
                    ActivityItem(
                        dotColor = Sage,
                        title    = "Belum ada aktivitas terbaru",
                        subtitle = "Jadilah yang pertama buka jastip!",
                        time     = ""
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.xl))
    }
}

@Composable
fun ActivityItem(
    dotColor: Color,
    title: String,
    subtitle: String,
    time: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                color = Charcoal, fontFamily = DmSansFamily,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, fontSize = 10.sp, color = Charcoal60, fontFamily = DmSansFamily)
        }
        if (time.isNotEmpty()) {
            Text(time, fontSize = 10.sp, color = Charcoal30, fontFamily = DmSansFamily)
        }
    }
}