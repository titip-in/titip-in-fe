package com.titipin.app.ui.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.data.model.AnalyticsData
import com.titipin.app.data.model.AnalyticsItemDetail
import com.titipin.app.data.model.UserTier
import com.titipin.app.ui.components.TierBadge
import com.titipin.app.ui.theme.*

@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    onNavigateToUpgrade: () -> Unit = {},
    currentTier: String = UserTier.BASIC,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Cream,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali", tint = Charcoal)
                }
                Spacer(Modifier.width(4.dp))
                Column {
                    Text(
                        "● PROFIL", color = Terracotta, fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp,
                        fontFamily = DmSansFamily
                    )
                    Text(
                        "Analitik", color = Charcoal, fontSize = 24.sp,
                        fontWeight = FontWeight.Medium, fontFamily = FrauncesFamily
                    )
                }
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is AnalyticsUiState.Loading -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
                }
            }

            is AnalyticsUiState.Paywall -> {
                AnalyticsPaywall(
                    message = state.message,
                    modifier = Modifier.fillMaxSize().padding(padding),
                    onUpgrade = onNavigateToUpgrade
                )
            }

            is AnalyticsUiState.Error -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("😕", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(state.message, color = Charcoal60, fontFamily = DmSansFamily, fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.loadAnalytics() }) {
                            Text("Coba lagi", color = Terracotta, fontFamily = DmSansFamily)
                        }
                    }
                }
            }

            is AnalyticsUiState.Success -> {
                AnalyticsDashboard(
                    data = state.data,
                    currentTier = state.tier,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

// ── PAYWALL ───────────────────────────────────────────────────────────
@Composable
private fun AnalyticsPaywall(
    message: String,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(Spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Lock icon area
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(listOf(Color(0xFF6D5BD0), Color(0xFF9D7AEA)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("🔒", fontSize = 36.sp)
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Analitik Eksklusif",
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            color = Charcoal,
            fontFamily = FrauncesFamily
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Pantau performa listing kamu — berapa kali dilihat, berapa klik WA, dan item terbaikmu.",
            fontSize = 13.sp,
            color = Charcoal60,
            fontFamily = DmSansFamily,
            lineHeight = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        // Feature previews
        listOf(
            "📊" to "Total views & klik" to UserTier.PLUS,
            "📈" to "Grafik conversion rate" to UserTier.PRO,
            "🏆" to "Item terbaik kamu" to UserTier.PRO,
        ).forEach { (pair, tier) ->
            val (emoji, label) = pair
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(Cream)
                    .border(1.dp, Charcoal10, RoundedCornerShape(Radius.md))
                    .padding(horizontal = Spacing.md, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(emoji, fontSize = 20.sp)
                    Text(label, fontSize = 13.sp, color = Charcoal, fontFamily = DmSansFamily)
                }
                TierBadge(tier)
            }
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onUpgrade,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Radius.full),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6D5BD0),
                contentColor = Cream
            )
        ) {
            Text(
                "Upgrade Sekarang",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DmSansFamily
            )
        }
    }
}

// ── DASHBOARD ─────────────────────────────────────────────────────────
@Composable
private fun AnalyticsDashboard(
    data: AnalyticsData,
    currentTier: String,
    modifier: Modifier = Modifier
) {
    val isPro = currentTier.lowercase() == UserTier.PRO

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Spacer(Modifier.height(4.dp))

        // ── Summary cards ─────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnalyticsSummaryCard(
                emoji = "👁️",
                label = "Total Views",
                value = data.totalViews.toString(),
                color = Sage,
                modifier = Modifier.weight(1f)
            )
            AnalyticsSummaryCard(
                emoji = "💬",
                label = "Total Klik WA",
                value = data.totalClicks.toString(),
                color = Terracotta,
                modifier = Modifier.weight(1f)
            )
        }

        // ── Conversion Rate (Pro only) ─────────────────────────────
        if (isPro) {
            val ratePercent = (data.conversionRate).let {
                if (it == it.toLong().toDouble()) "${it.toLong()}%" else "${"%.1f".format(it)}%"
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyticsSummaryCard(
                    emoji = "📈",
                    label = "Conversion Rate",
                    value = ratePercent,
                    color = Gold,
                    modifier = Modifier.weight(1f)
                )
                data.bestItem?.let { best ->
                    AnalyticsSummaryCard(
                        emoji = best.typeEmoji,
                        label = "Item Terbaik",
                        value = best.title,
                        color = Color(0xFF6D5BD0),
                        modifier = Modifier.weight(1f),
                        valueSmall = true
                    )
                }
            }
        }

        // ── Item Details Table ─────────────────────────────────────
        Text(
            "DETAIL PER ITEM",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = Charcoal60,
            fontFamily = DmSansFamily
        )

        if (data.itemDetails.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(Cream)
                    .border(1.dp, Charcoal10, RoundedCornerShape(Radius.md))
                    .padding(Spacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 32.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Belum ada data listing",
                        fontSize = 13.sp,
                        color = Charcoal60,
                        fontFamily = DmSansFamily
                    )
                }
            }
        } else {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = Radius.md, topEnd = Radius.md))
                    .background(Charcoal)
                    .padding(horizontal = Spacing.md, vertical = 10.dp)
            ) {
                Text(
                    "Item",
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Cream,
                    fontFamily = DmSansFamily, modifier = Modifier.weight(1f)
                )
                Text(
                    "Views",
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Cream,
                    fontFamily = DmSansFamily, modifier = Modifier.width(48.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    "Klik",
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Cream,
                    fontFamily = DmSansFamily, modifier = Modifier.width(36.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            data.itemDetails.forEachIndexed { index, item ->
                AnalyticsItemRow(
                    item = item,
                    isLast = index == data.itemDetails.lastIndex
                )
            }
        }

        // ── Pro chart hint ──────────────────────────────────────────
        if (!isPro) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(GoldPale)
                    .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(Radius.md))
                    .padding(Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📈", fontSize = 20.sp)
                Column {
                    Text(
                        "Upgrade ke Pro untuk grafik conversion rate",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Charcoal,
                        fontFamily = DmSansFamily
                    )
                    Text(
                        "Lihat item terbaik & performa keseluruhan",
                        fontSize = 11.sp,
                        color = Charcoal60,
                        fontFamily = DmSansFamily
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.lg))
    }
}

@Composable
private fun AnalyticsSummaryCard(
    emoji: String,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    valueSmall: Boolean = false
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.md))
            .background(Cream)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(Radius.md))
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 14.sp)
            }
            Text(
                label, fontSize = 10.sp, color = Charcoal60,
                fontFamily = DmSansFamily, lineHeight = 14.sp
            )
        }
        Text(
            value,
            fontSize = if (valueSmall) 15.sp else 26.sp,
            fontWeight = if (valueSmall) FontWeight.SemiBold else FontWeight.Light,
            color = Charcoal,
            fontFamily = if (valueSmall) DmSansFamily else FrauncesFamily,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AnalyticsItemRow(
    item: AnalyticsItemDetail,
    isLast: Boolean
) {
    val maxViews = item.views.coerceAtLeast(1)
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val barProgress by animateFloatAsState(
        targetValue = if (appeared) (item.views.toFloat() / maxViews.toFloat()).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(600),
        label = "barProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Cream)
            .border(
                width = 1.dp,
                color = Charcoal10,
                shape = if (isLast) RoundedCornerShape(
                    bottomStart = Radius.md,
                    bottomEnd = Radius.md
                ) else RoundedCornerShape(0.dp)
            )
            .padding(horizontal = Spacing.md, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.typeEmoji, fontSize = 11.sp)
                    Text(
                        item.typeLabel,
                        fontSize = 9.sp,
                        color = Charcoal60,
                        fontFamily = DmSansFamily
                    )
                }
                Text(
                    item.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal,
                    fontFamily = DmSansFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                item.views.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Sage,
                fontFamily = DmSansFamily,
                modifier = Modifier.width(48.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                item.clicks.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Terracotta,
                fontFamily = DmSansFamily,
                modifier = Modifier.width(36.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        // Progress bar mini
        if (item.views > 0) {
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { barProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(Radius.full)),
                color = Sage,
                trackColor = Charcoal10
            )
        }
    }
}
