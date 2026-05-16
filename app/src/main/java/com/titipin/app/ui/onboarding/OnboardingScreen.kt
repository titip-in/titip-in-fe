package com.titipin.app.ui.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.ui.theme.*
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel

private data class OnboardingPage(
    val emoji: String,
    val emojiSecondary: String,
    val title: String,
    val subtitle: String,
    val bg: androidx.compose.ui.graphics.Color,
    val accentColor: androidx.compose.ui.graphics.Color
)

private val pages = listOf(
    OnboardingPage(
        emoji          = "📦",
        emojiSecondary = "🛵",
        title          = "Jastip Hyperlocal",
        subtitle       = "Titip belanja ke teman sekampus yang lagi lewat. Hemat waktu, hemat ongkos.",
        bg             = Charcoal,
        accentColor    = Sage
    ),
    OnboardingPage(
        emoji          = "🛍️",
        emojiSecondary = "✨",
        title          = "Preloved Berkualitas",
        subtitle       = "Jual dan temukan barang preloved dari mahasiswa Malang. Kondisi terawat, harga bersahabat.",
        bg             = Terracotta,
        accentColor    = Color(0xFFF5E4DB)
    ),
    OnboardingPage(
        emoji          = "💬",
        emojiSecondary = "🤝",
        title          = "Langsung via WhatsApp",
        subtitle       = "Tidak ribet. Semua transaksi langsung dikoordinasikan via WhatsApp — cepat dan personal.",
        bg             = Color(0xFF2D5A3D),
        accentColor    = SagePale
    ),
)



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope      = rememberCoroutineScope()

    fun finish() {
        viewModel.markSeen()
        onFinish()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val page = pages[pageIndex]
            OnboardingPage(page = page)
        }

        // ── Bottom controls ────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Dot indicators
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(pages.size) { i ->
                    val isSelected = pagerState.currentPage == i
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        animationSpec = tween(300),
                        label = "dot_width"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = if (isSelected) 1f else 0.4f))
                    )
                }
            }

            // CTA Button
            val isLast = pagerState.currentPage == pages.size - 1
            Button(
                onClick = {
                    if (isLast) {
                        finish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(Radius.full),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor   = Charcoal
                )
            ) {
                Text(
                    text       = if (isLast) "Mulai Sekarang →" else "Lanjut →",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp
                )
            }

            // Skip
            if (!isLast) {
                Text(
                    text     = "Lewati",
                    fontSize = 13.sp,
                    color    = Color.White.copy(alpha = 0.6f),
                    fontFamily = DmSansFamily,
                    modifier = Modifier.clickable { finish() }
                )
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPage) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(
                listOf(page.bg, page.bg.copy(alpha = 0.85f)),
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end   = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY)
            ))
    ) {
        // Background decorative blobs
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-40).dp, y = 60.dp)
                .clip(CircleShape)
                .background(page.accentColor.copy(alpha = 0.12f))
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = 240.dp, y = 120.dp)
                .clip(CircleShape)
                .background(page.accentColor.copy(alpha = 0.08f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = Spacing.xl)
                .padding(top = 80.dp, bottom = 200.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            // Emoji pair
            Row(
                horizontalArrangement = Arrangement.spacedBy((-8).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(page.accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) { Text(page.emoji, fontSize = 36.sp) }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(page.accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text(page.emojiSecondary, fontSize = 24.sp) }
            }

            Spacer(Modifier.height(Spacing.xl))

            // Overline
            Text(
                text       = "● TITIP.IN",
                fontSize   = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color      = page.accentColor,
                fontFamily = DmSansFamily
            )

            Spacer(Modifier.height(Spacing.sm))

            // Title
            Text(
                text       = page.title,
                fontSize   = 34.sp,
                fontWeight = FontWeight.Medium,
                color      = Color.White,
                fontFamily = FrauncesFamily,
                lineHeight = 40.sp
            )

            Spacer(Modifier.height(Spacing.md))

            // Subtitle
            Text(
                text       = page.subtitle,
                fontSize   = 15.sp,
                color      = Color.White.copy(alpha = 0.75f),
                fontFamily = DmSansFamily,
                lineHeight = 22.sp
            )
        }
    }
}