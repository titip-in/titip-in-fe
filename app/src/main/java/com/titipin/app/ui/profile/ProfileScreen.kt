package com.titipin.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.ui.theme.*

@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    onNavigateToJastipSaya: () -> Unit = {},
    onNavigateToPrelovedSaya: () -> Unit = {},
    onNavigateToReview: () -> Unit = {},
    onNavigateToPengaturan: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
                }
            }
            is ProfileUiState.Success -> {
                val user = state.user
                val initials = user.name.trim()
                    .split(" ").filter { it.isNotBlank() }
                    .take(2).joinToString("") { it.first().uppercase() }

                Column(modifier = Modifier.fillMaxSize()) {
                    // ── HERO CHARCOAL ──────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Charcoal)
                            .padding(horizontal = Spacing.lg)
                            .padding(top = 0.dp, bottom = Spacing.xl)
                            .statusBarsPadding()
                    ) {
                        // Blob dekorasi
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .offset(x = 20.dp, y = (-30).dp)
                                .clip(CircleShape)
                                .background(Terracotta.copy(alpha = 0.1f))
                                .align(Alignment.TopEnd)
                        )
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .offset(x = (-10).dp, y = 10.dp)
                                .clip(CircleShape)
                                .background(Sage.copy(alpha = 0.1f))
                                .align(Alignment.BottomStart)
                        )

                        Column {
                            // Label PROFIL kanan atas
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    "PROFIL",
                                    fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 2.sp,
                                    color = Cream.copy(alpha = 0.4f),
                                    fontFamily = DmSansFamily
                                )
                            }

                            Spacer(Modifier.height(Spacing.md))

                            // Avatar + Info
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Avatar dengan gradient
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                listOf(Sage, Terracotta)
                                            )
                                        )
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        initials,
                                        fontSize = 20.sp, fontWeight = FontWeight.Bold,
                                        color = androidx.compose.ui.graphics.Color.White,
                                        fontFamily = DmSansFamily
                                    )
                                }

                                Column {
                                    Text(
                                        user.name.trim(),
                                        fontSize = 20.sp, fontWeight = FontWeight.Normal,
                                        color = Cream, fontFamily = FrauncesFamily, lineHeight = 24.sp
                                    )
                                    Text(
                                        user.email,
                                        fontSize = 11.sp, color = Cream.copy(alpha = 0.5f),
                                        fontFamily = DmSansFamily
                                    )
                                    if (!user.status.isNullOrBlank()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            user.status,
                                            fontSize = 11.sp, color = Cream.copy(alpha = 0.7f),
                                            fontFamily = DmSansFamily
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(Radius.full))
                                                .background(Sage)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                "✓ TERVERIFIKASI",
                                                fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                                color = androidx.compose.ui.graphics.Color.White,
                                                fontFamily = DmSansFamily, letterSpacing = 0.5.sp
                                            )
                                        }
                                        if (user.waNumber.isNotBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(Radius.full))
                                                    .background(GoldPale)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    "📱 ${maskWaNumber(user.waNumber)}",
                                                    fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                                    color = Gold, fontFamily = DmSansFamily
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(Spacing.md))

                            // Stats grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "—" to "Jastip",
                                    "—" to "Preloved",
                                    "—" to "Review"
                                ).forEach { (count, label) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(Radius.md))
                                            .background(Cream.copy(alpha = 0.07f))
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                count,
                                                fontSize = 20.sp, fontWeight = FontWeight.Light,
                                                color = Cream, fontFamily = FrauncesFamily
                                            )
                                            Text(
                                                label,
                                                fontSize = 9.sp, color = Cream.copy(alpha = 0.4f),
                                                fontFamily = DmSansFamily
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── MENU LIST ─────────────────────────────────
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ProfileMenuItem(emoji = "📦", label = "Jastip Saya",
                            subtitle = "Lihat semua jastip kamu", bgColor = SagePale,
                            onClick = onNavigateToJastipSaya)
                        ProfileMenuItem(emoji = "🛍️", label = "Preloved Saya",
                            subtitle = "Lihat semua barang kamu", bgColor = TerracottaPale,
                            onClick = onNavigateToPrelovedSaya)
                        ProfileMenuItem(emoji = "⭐", label = "Review & Rating",
                            subtitle = "— ulasan diterima", bgColor = GoldPale,
                            onClick = onNavigateToReview)
                        ProfileMenuItem(emoji = "⚙️", label = "Pengaturan",
                            subtitle = "Akun, notifikasi, privasi", bgColor = CreamDark,
                            onClick = onNavigateToPengaturan)

                        // Keluar — card dengan border Terracotta
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.md))
                                .background(TerracottaPale.copy(alpha = 0.6f))
                                .border(1.dp, Terracotta.copy(alpha = 0.4f), RoundedCornerShape(Radius.md))
                                .clickable {
                                    viewModel.logout()
                                    onLogout()
                                }
                                .padding(horizontal = Spacing.md, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TerracottaPale),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🚪", fontSize = 16.sp)
                            }
                            Text(
                                "Keluar",
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                color = Terracotta, fontFamily = DmSansFamily
                            )
                        }
                    }
                }
            }
            is ProfileUiState.Error -> {
                Box(Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("😕", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(state.message, color = Charcoal60, fontFamily = DmSansFamily, fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.loadProfile() }) {
                            Text("Coba lagi", color = Terracotta, fontFamily = DmSansFamily)
                        }
                    }
                }
            }
        }
    }
}

// Sensor WA: 0812-xxxx-8901
private fun maskWaNumber(wa: String): String {
    val digits = wa.filter { it.isDigit() }
    return if (digits.length >= 6) {
        "${digits.take(4)}-xxxx-${digits.takeLast(4)}"
    } else wa
}

// ── MENU ITEM ─────────────────────────────────────────────────────
@Composable
private fun ProfileMenuItem(
    emoji: String,
    label: String,
    subtitle: String,
    bgColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(androidx.compose.ui.graphics.Color.White)
            .clickable { onClick() }
            .padding(horizontal = Spacing.md, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 16.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = Charcoal, fontFamily = DmSansFamily)
            Text(subtitle, fontSize = 10.sp, color = Charcoal60, fontFamily = DmSansFamily)
        }
        Text("›", fontSize = 18.sp, color = Charcoal30, fontFamily = DmSansFamily)
    }
}