package com.titipin.app.ui.preloved

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.data.model.conditionLabel
import com.titipin.app.data.model.formattedPrice
import com.titipin.app.ui.theme.*

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PrelovedDetailScreen(
    prelovedId: String,
    onBack: () -> Unit = {},
    viewModel: PrelovedViewModel = hiltViewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(prelovedId) { viewModel.loadDetail(prelovedId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        when (val state = detailState) {
            is PrelovedActionState.Loading, is PrelovedActionState.Idle -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
                }
            }
            is PrelovedActionState.Error -> {
                Box(Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) {
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
            is PrelovedActionState.Success -> {
                val item = state.data ?: return@Column
                val initials = item.user.name.trim()
                    .split(" ").filter { it.isNotBlank() }
                    .take(2).joinToString("") { it.first().uppercase() }

                val categoryEmoji = categoryEmojiFor(item.category)

                val heroBg = when (item.condition) {
                    "NEW", "LIKE_NEW" -> SagePale
                    "GOOD"            -> GoldPale
                    else              -> TerracottaPale
                }

                // Simulasi beberapa foto (nanti dari imageUrl)
                // Sekarang 1 halaman saja karena BE return 1 imageUrl
                val pageCount = 1
                val pagerState = rememberPagerState { pageCount }

                // ── HERO — CAROUSEL ───────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(heroBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(categoryEmoji, fontSize = 80.sp)
                        }
                    }

                    // Back button
                    Box(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Cream.copy(alpha = 0.9f))
                            .clickable { onBack() }
                            .align(Alignment.TopStart),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("←", fontSize = 16.sp, color = Charcoal)
                    }

                    // Kondisi badge kanan atas
                    val (condBg, condColor) = when (item.condition) {
                        "NEW", "LIKE_NEW" -> SagePale to Sage
                        else -> CreamDark to Charcoal60
                    }
                    Box(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(12.dp)
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(Radius.full))
                            .background(condBg)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            item.conditionLabel().uppercase(),
                            fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            color = condColor, fontFamily = DmSansFamily, letterSpacing = 0.5.sp
                        )
                    }

                    // Dot indicator carousel
                    if (pageCount > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(pageCount) { index ->
                                Box(
                                    modifier = Modifier
                                        .then(
                                            if (pagerState.currentPage == index)
                                                Modifier.width(16.dp).height(4.dp)
                                            else
                                                Modifier.size(4.dp)
                                        )
                                        .clip(RoundedCornerShape(Radius.full))
                                        .background(
                                            if (pagerState.currentPage == index) Terracotta
                                            else Terracotta.copy(alpha = 0.3f)
                                        )
                                )
                            }
                        }
                    }
                }

                // ── KONTEN ────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    // Judul + Harga
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = item.title,
                                fontSize = 20.sp, fontWeight = FontWeight.Medium,
                                color = Charcoal, fontFamily = FrauncesFamily, lineHeight = 26.sp
                            )
                            Text(
                                text = item.formattedPrice(),
                                fontSize = 22.sp, fontWeight = FontWeight.Bold,
                                color = Terracotta, fontFamily = DmSansFamily
                            )
                        }
                    }

                    // Tags
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PrelovedTag("$categoryEmoji ${item.category}")
                        if (item.status == "AVAILABLE") PrelovedTag("✓ Tersedia")
                        else if (item.status == "SOLD")  PrelovedTag("✕ Terjual")
                    }

                    // Seller card + Lihat Profil
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(CreamDark)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp).clip(CircleShape).background(SagePale),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(initials, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Sage, fontFamily = DmSansFamily)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.user.name.trim(), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Charcoal, fontFamily = DmSansFamily)
                            Text("⭐ — · Malang", fontSize = 10.sp, color = Charcoal60, fontFamily = DmSansFamily)
                        }
                        // Lihat Profil — nanti navigate ke ProfileScreen user lain
                        Text(
                            text = "Lihat Profil →",
                            fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                            color = Terracotta, fontFamily = DmSansFamily,
                            modifier = Modifier.clickable { /* TODO: navigate to seller profile */ }
                        )
                    }

                    // Deskripsi
                    if (!item.description.isNullOrEmpty()) {
                        Column {
                            Text(
                                "DESKRIPSI", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp, color = Charcoal60, fontFamily = DmSansFamily,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Text(
                                item.description, fontSize = 13.sp, color = Charcoal,
                                fontFamily = DmSansFamily, lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.md))
                }

                // ── CTA ───────────────────────────────────────────
                HorizontalDivider(color = Charcoal10, thickness = 0.5.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${item.user.waNumber}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(ComponentSize.buttonHeight),
                        enabled = item.status == "AVAILABLE",
                        shape   = RoundedCornerShape(Radius.full),
                        colors  = ButtonDefaults.buttonColors(
                            containerColor         = Terracotta,
                            contentColor           = Cream,
                            disabledContainerColor = Charcoal10,
                            disabledContentColor   = Charcoal30
                        )
                    ) {
                        Text(
                            text = if (item.status == "AVAILABLE") "💬 Chat via WhatsApp" else "Barang tidak tersedia",
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = DmSansFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrelovedTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.full))
            .background(CreamDark)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 11.sp, color = Charcoal60, fontFamily = DmSansFamily)
    }
}