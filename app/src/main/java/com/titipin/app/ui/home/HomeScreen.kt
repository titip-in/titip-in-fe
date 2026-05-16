package com.titipin.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.data.model.JastipDto
import com.titipin.app.data.model.PrelovedDto
import com.titipin.app.data.model.PrelovedRequestDto
import com.titipin.app.data.model.RequestDto
import com.titipin.app.data.model.formattedPrice
import com.titipin.app.data.model.formattedMaxPrice
import com.titipin.app.shared.TitipinPullRefresh
import com.titipin.app.shared.timeAgo
import com.titipin.app.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToJastip: () -> Unit = {},
    onNavigateToPreloved: () -> Unit = {},
    onNavigateToPengaturan: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Setup Profile Dialog — muncul pertama kali jika WA belum diisi
    if (uiState.showSetupProfile) {
        SetupProfileDialog(
            userName      = uiState.userName,
            onDismiss     = { viewModel.dismissSetupProfile() },
            onSetupNow    = {
                viewModel.dismissSetupProfile()
                onNavigateToPengaturan()
            }
        )
    }

    TitipinPullRefresh(
        isRefreshing = uiState.isRefreshing,
        onRefresh    = { viewModel.refresh() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // ── TOP BAR ───────────────────────────────────────────
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

            // ── SEARCH BAR ────────────────────────────────────────
            HomeSearchBar(
                query         = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                onClear       = { viewModel.clearSearch(); focusManager.clearFocus() },
                onDone        = { focusManager.clearFocus() },
                modifier      = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg)
            )

            Spacer(Modifier.height(Spacing.md))

            // ── SEARCH RESULTS ────────────────────────────────────
            AnimatedVisibility(visible = uiState.isSearchActive, enter = fadeIn(), exit = fadeOut()) {
                SearchResultsContent(
                    jastipResults   = uiState.searchJastip,
                    prelovedResults = uiState.searchPreloved,
                    query           = uiState.searchQuery,
                    onJastipClick   = onNavigateToJastip,
                    onPrelovedClick = onNavigateToPreloved
                )
            }

            // ── HOME CONTENT ──────────────────────────────────────
            AnimatedVisibility(visible = !uiState.isSearchActive, enter = fadeIn(), exit = fadeOut()) {
                Column {
                    // ── STATS GRID ────────────────────────────────
                    Column(
                        modifier = Modifier.padding(horizontal = Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.isLoading) {
                            HomeSkeleton()
                        } else {
                            // Hero card: Jastip Aktif
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(Radius.lg))
                                    .background(Charcoal)
                                    .clickable { onNavigateToJastip() }
                                    .padding(Spacing.md)
                            ) {
                                Box(
                                    modifier = Modifier.size(80.dp).offset(x = 20.dp, y = (-20).dp)
                                        .clip(CircleShape).background(Sage.copy(alpha = 0.15f))
                                        .align(Alignment.TopEnd)
                                )
                                Column {
                                    Text(
                                        "● AKTIF SEKARANG", fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold, color = Sage,
                                        fontFamily = DmSansFamily, letterSpacing = 1.5.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Jastip Aktif\ndi Malang", fontSize = 20.sp,
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
                                                text = "${uiState.jastipCount}",
                                                fontSize = 32.sp, fontWeight = FontWeight.Light,
                                                color = Cream, fontFamily = FrauncesFamily, lineHeight = 36.sp
                                            )
                                            Text(
                                                "jastip tersedia", fontSize = 10.sp,
                                                color = Cream.copy(alpha = 0.5f), fontFamily = DmSansFamily
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(Radius.full))
                                                .background(Sage)
                                                .clickable { onNavigateToJastip() }
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                "Lihat Semua →", fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White, fontFamily = DmSansFamily
                                            )
                                        }
                                    }
                                }
                            }

                            // Row mini stats: Preloved + Request Jastip + Request Preloved
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Preloved
                                MiniStatCard(
                                    emoji     = "🛍️",
                                    label     = "Preloved",
                                    count     = uiState.prelovedCount,
                                    unit      = "barang",
                                    bgColor   = Terracotta,
                                    textColor = Color.White,
                                    onClick   = onNavigateToPreloved,
                                    modifier  = Modifier.weight(1f)
                                )
                                // Jastip Request
                                MiniStatCard(
                                    emoji     = "📍",
                                    label     = "Request\nJastip",
                                    count     = uiState.jastipRequestCount,
                                    unit      = "terbuka",
                                    bgColor   = SagePale,
                                    textColor = Charcoal,
                                    onClick   = onNavigateToJastip,
                                    modifier  = Modifier.weight(1f)
                                )
                            }

                            // Preloved Request card full-width
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(Radius.lg))
                                    .background(GoldPale)
                                    .clickable { onNavigateToPreloved() }
                                    .padding(horizontal = Spacing.md, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("🔍", fontSize = 24.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Barang Dicari", fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium, color = Charcoal, fontFamily = FrauncesFamily
                                    )
                                    Text(
                                        "${uiState.prelovedRequestCount} pencarian aktif · Tab Dicari",
                                        fontSize = 10.sp, color = Charcoal60, fontFamily = DmSansFamily
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(Radius.full))
                                        .background(Gold)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "Lihat →", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                        color = Color.White, fontFamily = DmSansFamily
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.md))

                    // ── AKTIVITAS TERBARU ──────────────────────────
                    Text(
                        "AKTIVITAS TERBARU", fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp,
                        color = Charcoal60, fontFamily = DmSansFamily,
                        modifier = Modifier.padding(horizontal = Spacing.lg)
                    )
                    Spacer(Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.padding(horizontal = Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (uiState.isLoading) {
                            repeat(3) { ActivitySkeleton() }
                        } else {
                            // Merge semua aktivitas dan sort by waktu (terbaru dulu)
                            data class FeedItem(
                                val dotColor: Color,
                                val title: String,
                                val subtitle: String,
                                val time: String,
                                val onClick: () -> Unit
                            )

                            val feedItems = buildList<FeedItem> {
                                uiState.recentJastip.forEach { j ->
                                    add(FeedItem(
                                        dotColor = Sage,
                                        title    = j.title.ifBlank { "${j.fromLocation} → ${j.toLocation}" },
                                        subtitle = "Jastip · ${j.user.name.trim()}",
                                        time     = timeAgo(j.createdAt),
                                        onClick  = onNavigateToJastip
                                    ))
                                }
                                uiState.recentPreloved.forEach { p ->
                                    add(FeedItem(
                                        dotColor = Terracotta,
                                        title    = p.title,
                                        subtitle = "Preloved · ${p.formattedPrice()}",
                                        time     = timeAgo(p.createdAt ?: p.updatedAt.orEmpty()),
                                        onClick  = onNavigateToPreloved
                                    ))
                                }
                                uiState.recentJastipRequests.forEach { r ->
                                    add(FeedItem(
                                        dotColor = Gold,
                                        title    = r.title,
                                        subtitle = "Request · ${r.fromLocation} → ${r.toLocation}",
                                        time     = timeAgo(r.createdAt ?: r.updatedAt.orEmpty()),
                                        onClick  = onNavigateToJastip
                                    ))
                                }
                                uiState.recentPrelovedRequests.forEach { pr ->
                                    add(FeedItem(
                                        dotColor = Gold,
                                        title    = pr.title,
                                        subtitle = "Cari Barang · ${pr.formattedMaxPrice() ?: "Budget fleksibel"}",
                                        time     = timeAgo(pr.createdAt.orEmpty()),
                                        onClick  = onNavigateToPreloved
                                    ))
                                }
                            }

                            if (feedItems.isEmpty()) {
                                ActivityItem(
                                    dotColor = Sage,
                                    title    = "Belum ada aktivitas terbaru",
                                    subtitle = "Jadilah yang pertama buka jastip!",
                                    time     = ""
                                )
                            } else {
                                feedItems.take(6).forEach { item ->
                                    ActivityItem(
                                        dotColor = item.dotColor,
                                        title    = item.title,
                                        subtitle = item.subtitle,
                                        time     = item.time,
                                        onClick  = item.onClick
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(Spacing.xl))
                }
            }
        }
    }
}

// ── MINI STAT CARD ────────────────────────────────────────────────
@Composable
fun MiniStatCard(
    emoji: String,
    label: String,
    count: Int,
    unit: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.lg))
            .background(bgColor)
            .clickable { onClick() }
            .padding(Spacing.md)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(Spacing.sm))
            Text(
                label, fontSize = 15.sp,
                color = textColor, fontFamily = FrauncesFamily, lineHeight = 20.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$count $unit",
                fontSize = 10.sp, color = textColor.copy(alpha = 0.7f), fontFamily = DmSansFamily
            )
            Spacer(Modifier.height(Spacing.sm))
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape)
                    .background(textColor.copy(alpha = 0.15f)).align(Alignment.End),
                contentAlignment = Alignment.Center
            ) {
                Text("→", fontSize = 12.sp, color = textColor)
            }
        }
    }
}

// ── SEARCH BAR ────────────────────────────────────────────────────
@Composable
fun HomeSearchBar(
    query: String, onQueryChange: (String) -> Unit,
    onClear: () -> Unit, onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(Radius.md))
            .background(CreamDark).padding(horizontal = Spacing.md, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🔍", fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))
        BasicTextField(
            value = query, onValueChange = onQueryChange,
            modifier = Modifier.weight(1f).onFocusChanged { },
            textStyle = TextStyle(fontSize = 13.sp, fontFamily = DmSansFamily, color = Charcoal),
            singleLine = true, cursorBrush = SolidColor(Terracotta),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onDone() }),
            decorationBox = { inner ->
                if (query.isEmpty()) Text(
                    "Cari jastip atau barang...", fontSize = 13.sp,
                    color = Charcoal30, fontFamily = DmSansFamily
                )
                inner()
            }
        )
        AnimatedVisibility(visible = query.isNotEmpty()) {
            Box(
                modifier = Modifier.padding(start = 6.dp).size(20.dp).clip(CircleShape)
                    .background(Charcoal30).clickable { onClear() },
                contentAlignment = Alignment.Center
            ) {
                Text("✕", fontSize = 10.sp, color = Cream, fontFamily = DmSansFamily)
            }
        }
    }
}

// ── SEARCH RESULTS ────────────────────────────────────────────────
@Composable
fun SearchResultsContent(
    jastipResults: List<JastipDto>, prelovedResults: List<PrelovedDto>,
    query: String, onJastipClick: () -> Unit, onPrelovedClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        val total = jastipResults.size + prelovedResults.size
        Text(
            text = if (total > 0) "$total hasil untuk \"$query\"" else "Tidak ada hasil untuk \"$query\"",
            fontSize = 11.sp, color = Charcoal60, fontFamily = DmSansFamily,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        if (jastipResults.isNotEmpty()) {
            Text(
                "📦 JASTIP", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp, color = Sage, fontFamily = DmSansFamily
            )
            jastipResults.take(3).forEach { jastip ->
                SearchResultItem(
                    emoji   = "📦",
                    title   = jastip.title.ifBlank { "${jastip.fromLocation} → ${jastip.toLocation}" },
                    subtitle = jastip.user.name.trim(),
                    badge   = if (jastip.status == "ACTIVE") "Aktif" else "Tutup",
                    badgeBg = if (jastip.status == "ACTIVE") SagePale else CreamDark,
                    badgeFg = if (jastip.status == "ACTIVE") Sage else Charcoal30,
                    onClick = onJastipClick
                )
            }
        }
        if (prelovedResults.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                "🛍️ PRELOVED", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp, color = Terracotta, fontFamily = DmSansFamily
            )
            prelovedResults.take(3).forEach { item ->
                SearchResultItem(
                    emoji   = "🛍️", title = item.title, subtitle = item.formattedPrice(),
                    badge   = item.category?.name ?: "Preloved", badgeBg = TerracottaPale, badgeFg = Terracotta,
                    onClick = onPrelovedClick
                )
            }
        }
        if (total == 0) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔍", fontSize = 36.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Coba kata kunci lain", fontFamily = DmSansFamily, color = Charcoal60, fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(Spacing.xl))
    }
}

@Composable
private fun SearchResultItem(
    emoji: String, title: String, subtitle: String,
    badge: String, badgeBg: Color, badgeFg: Color, onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md))
            .background(Color.White).clickable { onClick() }
            .padding(horizontal = Spacing.md, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(emoji, fontSize = 16.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = Charcoal, fontFamily = DmSansFamily,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(subtitle, fontSize = 11.sp, color = Charcoal60, fontFamily = DmSansFamily)
        }
        Box(
            modifier = Modifier.clip(RoundedCornerShape(Radius.full)).background(badgeBg)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(badge, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = badgeFg, fontFamily = DmSansFamily)
        }
    }
}

// ── SKELETON ──────────────────────────────────────────────────────
@Composable
fun HomeSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(Radius.lg))
            .background(Charcoal.copy(alpha = 0.08f)))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f).height(130.dp).clip(RoundedCornerShape(Radius.lg))
                .background(Charcoal.copy(alpha = 0.06f)))
            Box(modifier = Modifier.weight(1f).height(130.dp).clip(RoundedCornerShape(Radius.lg))
                .background(Charcoal.copy(alpha = 0.06f)))
        }
        Box(modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(Radius.lg))
            .background(Charcoal.copy(alpha = 0.05f)))
    }
}

@Composable
fun ActivitySkeleton() {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md))
            .background(Color.White).padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Charcoal.copy(alpha = 0.1f)))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(modifier = Modifier.fillMaxWidth(0.6f).height(11.dp).clip(RoundedCornerShape(4.dp))
                .background(Charcoal.copy(alpha = 0.1f)))
            Box(modifier = Modifier.fillMaxWidth(0.35f).height(9.dp).clip(RoundedCornerShape(4.dp))
                .background(Charcoal.copy(alpha = 0.07f)))
        }
        Box(modifier = Modifier.width(36.dp).height(9.dp).clip(RoundedCornerShape(4.dp))
            .background(Charcoal.copy(alpha = 0.07f)))
    }
}

// ── ACTIVITY ITEM ─────────────────────────────────────────────────
@Composable
fun ActivityItem(
    dotColor: Color, title: String, subtitle: String, time: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md))
            .background(Color.White).clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                color = Charcoal, fontFamily = DmSansFamily,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(subtitle, fontSize = 10.sp, color = Charcoal60, fontFamily = DmSansFamily,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (time.isNotEmpty()) Text(time, fontSize = 10.sp, color = Charcoal30, fontFamily = DmSansFamily)
    }
}

// ── SETUP PROFILE DIALOG ──────────────────────────────────────────────
@Composable
private fun SetupProfileDialog(
    userName: String,
    onDismiss: () -> Unit,
    onSetupNow: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Cream,
        shape            = RoundedCornerShape(Radius.xl),
        title = {
            Column {
                Text("Halo, ${userName.split(" ").firstOrNull() ?: userName}! 👋",
                    fontFamily = FrauncesFamily, fontSize = 22.sp,
                    fontWeight = FontWeight.Medium, color = Charcoal)
            }
        },
        text = {
            Text(
                "Lengkapi nomor WhatsApp kamu agar buyer/seller bisa menghubungimu langsung.\n\nKamu bisa isi sekarang atau nanti di Pengaturan.",
                fontFamily = DmSansFamily, fontSize = 13.sp, color = Charcoal60,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onSetupNow,
                shape  = RoundedCornerShape(Radius.full),
                colors = ButtonDefaults.buttonColors(containerColor = Charcoal)
            ) {
                Text("Isi Sekarang", fontFamily = DmSansFamily, color = Cream,
                    fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Nanti Saja", fontFamily = DmSansFamily, color = Charcoal60)
            }
        }
    )
}
