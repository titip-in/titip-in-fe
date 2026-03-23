package com.titipin.app.ui.preloved

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.data.model.PrelovedDto
import com.titipin.app.data.model.conditionLabel
import com.titipin.app.data.model.formattedPrice
import com.titipin.app.ui.theme.*
import kotlinx.coroutines.launch

private val cardBgColors = listOf(TerracottaPale, SagePale, GoldPale, CreamDark)

// Filter pills sesuai design system
private val categoryFilters = listOf("Semua", "👟 Sepatu", "👗 Fashion", "📱 Gadget", "📚 Buku", "💻 Elektronik", "⚽ Olahraga", "🪑 Furniture", "📦 Lainnya")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrelovedScreen(
    onNavigateToDetail: (String) -> Unit = {},
    viewModel: PrelovedViewModel = hiltViewModel()
) {
    val listState   by viewModel.listState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet  by remember { mutableStateOf(false) }
    val scope      = rememberCoroutineScope()

    LaunchedEffect(actionState) {
        if (actionState is PrelovedActionState.Success) {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                showSheet = false
                viewModel.resetActionState()
                viewModel.loadPrelovedList()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Cream)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

            // ── HEADER ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("● PRELOVED", color = Terracotta, fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp, fontFamily = DmSansFamily)
                    Text("Jual & Cari", color = Charcoal, fontSize = 26.sp,
                        fontWeight = FontWeight.Medium, fontFamily = FrauncesFamily)
                }
            }

            // ── TAB ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .padding(horizontal = Spacing.lg)
                    .clip(RoundedCornerShape(Radius.full))
                    .background(CreamDark)
                    .padding(4.dp)
            ) {
                listOf("Dijual", "Dicari").forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Radius.full))
                            .background(if (selectedTab == index) Charcoal else CreamDark)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            fontFamily = DmSansFamily,
                            color = if (selectedTab == index) Cream else Charcoal60)
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))

            when (selectedTab) {
                0 -> PrelovedDijualContent(listState, onNavigateToDetail) { viewModel.loadPrelovedList() }
                1 -> PrelovedDicariContent()
            }
        }

        // ── FAB — mepet ke bottom nav ─────────────────────────────
        // 74dp = tinggi bottom nav, 16dp = margin bawah nav, 8dp = gap antara FAB dan nav
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Spacing.lg, bottom = Spacing.lg)
                .size(52.dp)
                .shadow(elevation = 8.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Terracotta)
                .clickable { showSheet = true },
            contentAlignment = Alignment.Center
        ) {
            Text("＋", color = Cream, fontSize = 22.sp, fontWeight = FontWeight.Light)
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false; viewModel.resetActionState() },
            sheetState = sheetState,
            containerColor = Cream
        ) {
            PrelovedFormContent(
                viewModel = viewModel,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSheet = false
                        viewModel.resetActionState()
                    }
                }
            )
        }
    }
}

// ── TAB DIJUAL ────────────────────────────────────────────────────
@Composable
fun PrelovedDijualContent(
    listState: PrelovedListState,
    onCardClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Semua") }

    when (listState) {
        is PrelovedListState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
            }
        }
        is PrelovedListState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(Spacing.lg)) {
                    Text("😕", fontSize = 40.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(listState.message, fontFamily = DmSansFamily, color = Charcoal60, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = onRetry) {
                        Text("Coba lagi", color = Terracotta, fontFamily = DmSansFamily)
                    }
                }
            }
        }
        is PrelovedListState.Success -> {
            Column {
                // ── CATEGORY FILTER PILLS ─────────────────────────
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.lg)
                        .padding(bottom = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoryFilters.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.full))
                                .background(if (isSelected) Charcoal else CreamDark)
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                filter, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                                fontFamily = DmSansFamily,
                                color = if (isSelected) Cream else Charcoal60
                            )
                        }
                    }
                }

                // Filter client-side — mapping pill label -> BE category value
                val filterMapping = mapOf(
                    "👟 Sepatu"     to "FASHION",
                    "👗 Fashion"    to "FASHION",
                    "📱 Gadget"     to "GADGET",
                    "📚 Buku"       to "BUKU",
                    "💻 Elektronik" to "ELEKTRONIK",
                    "⚽ Olahraga"   to "OLAHRAGA",
                    "🪑 Furniture"  to "FURNITURE",
                    "📦 Lainnya"    to "LAINNYA"
                )
                val filteredData = if (selectedFilter == "Semua") {
                    listState.data
                } else {
                    val beCategory = filterMapping[selectedFilter]
                    listState.data.filter { it.category.uppercase() == beCategory }
                }

                if (filteredData.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🛍️", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("Belum ada barang dijual", fontFamily = FrauncesFamily, color = Charcoal, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Text("Jadilah yang pertama jual barang!", fontFamily = DmSansFamily, color = Charcoal60, fontSize = 12.sp)
                        }
                    }
                } else {
                    // ── BENTO GRID 2 kolom ────────────────────────
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = Spacing.lg, end = Spacing.lg, bottom = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = filteredData,
                            key   = { _, item -> item.id },
                            span  = { index, _ ->
                                if (index == 0) GridItemSpan(2) else GridItemSpan(1)
                            }
                        ) { index, item ->
                            PrelovedBentoCard(
                                item    = item,
                                isWide  = index == 0,
                                bgColor = cardBgColors[index % cardBgColors.size],
                                onClick = { onCardClick(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── BENTO CARD ────────────────────────────────────────────────────
@Composable
fun PrelovedBentoCard(
    item: PrelovedDto,
    isWide: Boolean,
    bgColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val categoryEmoji = categoryEmojiFor(item.category)

    // Kondisi label singkat — max 5 karakter biar tidak wrap
    val condShort = when (item.condition) {
        "NEW"      -> "BARU"
        "LIKE_NEW" -> "MULUS"
        "GOOD"     -> "BAIK"
        "FAIR"     -> "LAYAK"
        else       -> item.condition.take(5)
    }

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(Radius.lg),
        colors    = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // ── FOTO AREA ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isWide) Modifier.height(130.dp) else Modifier.aspectRatio(1f))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(categoryEmoji, fontSize = if (isWide) 52.sp else 36.sp)
            }

            // ── INFO ──────────────────────────────────────────────
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.title,
                    fontSize = if (isWide) 14.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Charcoal, fontFamily = FrauncesFamily,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.formattedPrice(),
                        fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = Terracotta, fontFamily = DmSansFamily
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.full))
                            .background(SagePale)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = condShort,
                            fontSize = 8.sp, fontWeight = FontWeight.Bold,
                            color = Sage, fontFamily = DmSansFamily, letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

// ── TAB DICARI ────────────────────────────────────────────────────
@Composable
fun PrelovedDicariContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(Spacing.lg)) {
            Text("🔍", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text("Fitur Dicari segera hadir", fontFamily = FrauncesFamily, color = Charcoal,
                fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Text("Kamu bisa posting barang\nyang kamu cari di sini.",
                fontFamily = DmSansFamily, color = Charcoal60,
                fontSize = 13.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
        }
    }
}

// ── HELPER ────────────────────────────────────────────────────────
fun categoryEmojiFor(category: String): String = when (category.uppercase()) {
    "FASHION"    -> "👗"
    "GADGET"     -> "📱"
    "BUKU"       -> "📚"
    "ELEKTRONIK" -> "💻"
    "OLAHRAGA"   -> "⚽"
    "FURNITURE"  -> "🪑"
    else         -> "📦"
}