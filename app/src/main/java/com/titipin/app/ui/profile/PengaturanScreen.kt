package com.titipin.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengaturanScreen(
    onBack: () -> Unit,
    viewModel: PengaturanViewModel = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsState()
    val notifJastip by viewModel.notifJastip.collectAsState()
    val notifPesan  by viewModel.notifPesan.collectAsState()

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
                    Text("Pengaturan", color = Charcoal, fontSize = 24.sp,
                        fontWeight = FontWeight.Medium, fontFamily = FrauncesFamily)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Spacer(Modifier.height(4.dp))

                // ── SECTION: AKUN ─────────────────────────────────
                PengaturanSectionLabel("AKUN")

                when (val state = uiState) {
                    is PengaturanUiState.Ready -> {
                        val user = state.user

                        // User info display card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.lg))
                                .background(Charcoal)
                                .padding(Spacing.lg)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                // Avatar
                                val initials = user.name.trim()
                                    .split(" ").filter { it.isNotBlank() }
                                    .take(2).joinToString("") { it.first().uppercase() }
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                listOf(Sage, Terracotta)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(initials, fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold, color = Color.White,
                                        fontFamily = DmSansFamily)
                                }
                                Column {
                                    Text(user.name.trim(), fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold, color = Cream,
                                        fontFamily = DmSansFamily)
                                    Text(user.email, fontSize = 12.sp,
                                        color = Cream.copy(alpha = 0.5f), fontFamily = DmSansFamily)
                                }
                            }
                        }

                        // Edit Profil — placeholder, nunggu BE endpoint
                        PengaturanMenuItem(
                            emoji    = "👤",
                            label    = "Edit Profil",
                            subtitle = "Ubah nama dan foto profil",
                            badge    = "Segera",
                            onClick  = { /* TODO: nunggu BE PUT /users/{id} */ }
                        )

                        // No. WhatsApp
                        PengaturanMenuItem(
                            emoji    = "📱",
                            label    = "No. WhatsApp",
                            subtitle = user.waNumber.ifEmpty { "Belum diisi" },
                            badge    = "Segera",
                            onClick  = { /* TODO: nunggu BE */ }
                        )

                        // Ubah Password
                        PengaturanMenuItem(
                            emoji    = "🔒",
                            label    = "Ubah Password",
                            subtitle = "Ganti password akun kamu",
                            badge    = "Segera",
                            onClick  = { /* TODO: nunggu BE */ }
                        )
                    }

                    is PengaturanUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
                        }
                    }

                    is PengaturanUiState.Error -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.md))
                                .background(TerracottaPale)
                                .padding(Spacing.md),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 14.sp)
                            Text((uiState as PengaturanUiState.Error).message,
                                fontFamily = DmSansFamily, fontSize = 12.sp, color = Terracotta)
                        }
                    }
                }

                // ── SECTION: NOTIFIKASI ───────────────────────────
                PengaturanSectionLabel("NOTIFIKASI")

                // Jastip baru di area
                Surface(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(Radius.md),
                    color     = Cream,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SagePale),
                                contentAlignment = Alignment.Center
                            ) { Text("🔔", fontSize = 16.sp) }
                            Column {
                                Text("Jastip baru di area saya", fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium, color = Charcoal,
                                    fontFamily = DmSansFamily)
                                Text("Notifikasi saat ada jastip baru", fontSize = 11.sp,
                                    color = Charcoal60, fontFamily = DmSansFamily)
                            }
                        }
                        TitipinSwitch(checked = notifJastip, onCheckedChange = { viewModel.toggleNotifJastip() })
                    }
                }

                // Pesan masuk
                Surface(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(Radius.md),
                    color     = Cream,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TerracottaPale),
                                contentAlignment = Alignment.Center
                            ) { Text("💬", fontSize = 16.sp) }
                            Column {
                                Text("Pesan WhatsApp masuk", fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium, color = Charcoal,
                                    fontFamily = DmSansFamily)
                                Text("Pengingat pesan yang belum dibalas", fontSize = 11.sp,
                                    color = Charcoal60, fontFamily = DmSansFamily)
                            }
                        }
                        TitipinSwitch(checked = notifPesan, onCheckedChange = { viewModel.toggleNotifPesan() })
                    }
                }

                // ── SECTION: LAINNYA ──────────────────────────────
                PengaturanSectionLabel("LAINNYA")

                PengaturanMenuItem(
                    emoji    = "📄",
                    label    = "Syarat & Ketentuan",
                    subtitle = "Kebijakan penggunaan Titip.in",
                    onClick  = { /* TODO: buka browser/webview */ }
                )

                PengaturanMenuItem(
                    emoji    = "🛡️",
                    label    = "Kebijakan Privasi",
                    subtitle = "Bagaimana data kamu digunakan",
                    onClick  = { /* TODO */ }
                )

                // Versi app — tidak clickable
                Surface(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(Radius.md),
                    color     = Cream,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CreamDark),
                                contentAlignment = Alignment.Center
                            ) { Text("ℹ️", fontSize = 16.sp) }
                            Column {
                                Text("Versi Aplikasi", fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium, color = Charcoal,
                                    fontFamily = DmSansFamily)
                                Text("v1.0.0 MVP", fontSize = 11.sp,
                                    color = Charcoal60, fontFamily = DmSansFamily)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.lg))
            }
        }
    }
}

// ── Reusable components ────────────────────────────────────────────

@Composable
private fun PengaturanSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = Charcoal60,
        fontFamily = DmSansFamily
    )
}

@Composable
private fun PengaturanMenuItem(
    emoji: String,
    label: String,
    subtitle: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier        = Modifier.fillMaxWidth().clickable { onClick() },
        shape           = RoundedCornerShape(Radius.md),
        color           = Cream,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CreamDark),
                    contentAlignment = Alignment.Center
                ) { Text(emoji, fontSize = 16.sp) }
                Column {
                    Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = Charcoal, fontFamily = DmSansFamily)
                    Text(subtitle, fontSize = 11.sp, color = Charcoal60,
                        fontFamily = DmSansFamily)
                }
            }
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.full))
                        .background(GoldPale)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(badge, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        color = Gold, fontFamily = DmSansFamily, letterSpacing = 0.5.sp)
                }
            } else {
                Text("›", fontSize = 18.sp, color = Charcoal30, fontFamily = DmSansFamily)
            }
        }
    }
}

@Composable
private fun TitipinSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor       = Cream,
            checkedTrackColor       = Sage,
            uncheckedThumbColor     = Cream,
            uncheckedTrackColor     = CreamDark,
            uncheckedBorderColor    = CreamDark
        )
    )
}