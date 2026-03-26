package com.titipin.app.ui.preloved

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.data.model.PrelovedDto
import com.titipin.app.data.model.WantedDto
import com.titipin.app.data.model.conditionLabel
import com.titipin.app.data.model.formattedMaxPrice
import com.titipin.app.data.model.formattedPrice
import com.titipin.app.shared.timeAgo
import com.titipin.app.ui.theme.*
import kotlinx.coroutines.launch

private val cardBgColors = listOf(TerracottaPale, SagePale, GoldPale, CreamDark)

// Filter pills sesuai design system
private val categoryFilters = listOf("Semua", "👟 Sepatu", "👗 Fashion", "📱 Gadget", "📚 Buku", "💻 Elektronik", "⚽ Olahraga", "🪑 Furniture", "📦 Lainnya")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrelovedScreen(
    onNavigateToDetail: (String) -> Unit = {},
    viewModel: PrelovedViewModel = hiltViewModel(),
    wantedViewModel: WantedViewModel = hiltViewModel()
) {
    val listState   by viewModel.listState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    val wantedListState   by wantedViewModel.listState.collectAsState()
    val wantedActionState by wantedViewModel.actionState.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    // Sheet tab Dijual
    val prelovedSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPrelovedSheet  by remember { mutableStateOf(false) }

    // Sheet tab Dicari
    val wantedSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showWantedSheet  by remember { mutableStateOf(false) }

    val scope   = rememberCoroutineScope()
    val context = LocalContext.current

    // Tutup sheet preloved setelah sukses post
    LaunchedEffect(actionState) {
        if (actionState is PrelovedActionState.Success) {
            scope.launch { prelovedSheetState.hide() }.invokeOnCompletion {
                showPrelovedSheet = false
                viewModel.resetActionState()
                viewModel.loadPrelovedList()
            }
        }
    }

    // Handle wanted action states
    LaunchedEffect(wantedActionState) {
        when (wantedActionState) {
            is WantedActionState.Success -> {
                scope.launch { wantedSheetState.hide() }.invokeOnCompletion {
                    showWantedSheet = false
                    wantedViewModel.resetActionState()
                    wantedViewModel.loadWantedList()
                }
            }
            is WantedActionState.FulfillSuccess -> {
                // Refresh list + buka WA ke pencari
                val waNumber = (wantedActionState as WantedActionState.FulfillSuccess)
                    .result.wantedItem.user.waNumber
                wantedViewModel.resetActionState()
                wantedViewModel.loadWantedList()
                val intent = Intent(Intent.ACTION_VIEW, "https://wa.me/$waNumber".toUri())
                context.startActivity(intent)
            }
            else -> {}
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
                1 -> PrelovedDicariContent(
                    listState   = wantedListState,
                    actionState = wantedActionState,
                    onFulfill   = { id -> wantedViewModel.fulfillWanted(id) },
                    onRetry     = { wantedViewModel.loadWantedList() }
                )
            }
        }

        // ── FAB — sadar tab ───────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Spacing.lg, bottom = Spacing.lg)
                .size(52.dp)
                .shadow(elevation = 8.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Terracotta)
                .clickable {
                    if (selectedTab == 0) showPrelovedSheet = true
                    else showWantedSheet = true
                },
            contentAlignment = Alignment.Center
        ) {
            Text("＋", color = Cream, fontSize = 22.sp, fontWeight = FontWeight.Light)
        }
    }

    // ── BOTTOM SHEET: Jual Preloved ───────────────────────────────
    if (showPrelovedSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPrelovedSheet = false; viewModel.resetActionState() },
            sheetState = prelovedSheetState,
            containerColor = Cream
        ) {
            PrelovedFormContent(
                viewModel = viewModel,
                onDismiss = {
                    scope.launch { prelovedSheetState.hide() }.invokeOnCompletion {
                        showPrelovedSheet = false
                        viewModel.resetActionState()
                    }
                }
            )
        }
    }

    // ── BOTTOM SHEET: Cari Barang ─────────────────────────────────
    if (showWantedSheet) {
        ModalBottomSheet(
            onDismissRequest = { showWantedSheet = false; wantedViewModel.resetActionState() },
            sheetState = wantedSheetState,
            containerColor = Cream
        ) {
            WantedFormContent(
                viewModel = wantedViewModel,
                onDismiss = {
                    scope.launch { wantedSheetState.hide() }.invokeOnCompletion {
                        showWantedSheet = false
                        wantedViewModel.resetActionState()
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
                    "👟 Sepatu"     to "SEPATU",
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
fun PrelovedDicariContent(
    listState: WantedListState,
    actionState: WantedActionState,
    onFulfill: (String) -> Unit,
    onRetry: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Semua") }

    when (listState) {
        is WantedListState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
            }
        }
        is WantedListState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(Spacing.lg)
                ) {
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
        is WantedListState.Success -> {
            val isFulfillLoading = actionState is WantedActionState.Loading
            // Filter data berdasarkan kategori yang dipilih
            val filteredData = if (selectedCategory == "Semua") {
                listState.data
            } else {
                // categoryFilters pakai format "👟 Sepatu" — ambil label saja lalu map ke BE value
                val label     = selectedCategory.substringAfter(" ") // "Sepatu"
                val filterKey = when (label.uppercase()) {
                    "SEPATU"     -> "SEPATU"
                    "FASHION"    -> "FASHION"
                    "GADGET"     -> "GADGET"
                    "BUKU"       -> "BUKU"
                    "ELEKTRONIK" -> "ELEKTRONIK"
                    "OLAHRAGA"   -> "OLAHRAGA"
                    "FURNITURE"  -> "FURNITURE"
                    else         -> label.uppercase()
                }
                listState.data.filter { it.category?.uppercase() == filterKey }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                // ── Category filter pills ─────────────────────────
                item(span = { GridItemSpan(1) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoryFilters.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Radius.full))
                                    .background(if (isSelected) Charcoal else CreamDark)
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = DmSansFamily,
                                    color = if (isSelected) Cream else Charcoal60
                                )
                            }
                        }
                    }
                }

                // Info banner
                item(span = { GridItemSpan(1) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(TerracottaPale)
                            .padding(Spacing.md),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Text("🔍", fontSize = 14.sp)
                        Text(
                            text = "Punya barang yang orang cari? Tekan \"Hubungi\" dan langsung chat via WA!",
                            fontFamily = DmSansFamily,
                            fontSize = 12.sp,
                            color = Charcoal,
                            lineHeight = 18.sp
                        )
                    }
                }

                if (filteredData.isEmpty()) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔍", fontSize = 48.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("Belum ada yang mencari", fontFamily = FrauncesFamily, color = Charcoal,
                                    fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(4.dp))
                                Text("Jadilah yang pertama cari barang!", fontFamily = DmSansFamily,
                                    color = Charcoal60, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    items(filteredData.size) { index ->
                        WantedCard(
                            wanted          = filteredData[index],
                            isFulfillLoading = isFulfillLoading,
                            onFulfill       = { onFulfill(filteredData[index].id) }
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

// ── WANTED CARD ───────────────────────────────────────────────────
@Composable
fun WantedCard(
    wanted: WantedDto,
    isFulfillLoading: Boolean,
    onFulfill: () -> Unit
) {
    val createdAtLabel = timeAgo(wanted.createdAt)
    val initials = wanted.user.name.trim().split(" ")
        .filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
    val formattedBudget = wanted.formattedMaxPrice()

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(Radius.lg),
        colors    = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {

            // ── Avatar + nama + badge ──────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(TerracottaPale),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Terracotta, fontFamily = DmSansFamily)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(wanted.user.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Charcoal, fontFamily = DmSansFamily)
                    Text("Mencari · $createdAtLabel", fontSize = 11.sp, color = Charcoal60, fontFamily = DmSansFamily)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.full))
                        .background(TerracottaPale)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("CARI", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Terracotta, fontFamily = DmSansFamily)
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Judul barang ──────────────────────────────────────
            Text(
                text = wanted.title,
                fontFamily = FrauncesFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Charcoal,
                lineHeight = 22.sp
            )

            // ── Deskripsi ─────────────────────────────────────────
            if (!wanted.description.isNullOrEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = wanted.description,
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = Charcoal60,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Chips: budget + kategori ──────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (formattedBudget != null) {
                    PrelovedInfoChip("💰 $formattedBudget")
                }
                if (!wanted.category.isNullOrEmpty()) {
                    PrelovedInfoChip("${categoryEmojiFor(wanted.category)} ${wanted.category}")
                }
            }

            Spacer(Modifier.height(Spacing.sm))
            HorizontalDivider(color = Charcoal10, thickness = 0.5.dp)
            Spacer(Modifier.height(Spacing.sm))

            // ── Tombol Hubungi ────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.full))
                        .background(if (isFulfillLoading) Terracotta.copy(alpha = 0.5f) else Terracotta)
                        .clickable(enabled = !isFulfillLoading) { onFulfill() }
                        .padding(horizontal = 20.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFulfillLoading) {
                        CircularProgressIndicator(color = Cream, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    } else {
                        Text("💬 Hubungi", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Cream, fontFamily = DmSansFamily)
                    }
                }
            }
        }
    }
}

@Composable
private fun PrelovedInfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.full))
            .background(CreamDark)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, fontSize = 11.sp, color = Charcoal60, fontFamily = DmSansFamily)
    }
}

// ── HELPER ────────────────────────────────────────────────────────
fun categoryEmojiFor(category: String): String = when (category.uppercase().trim()) {
    "SEPATU"     -> "👟"
    "FASHION"    -> "👗"
    "GADGET"     -> "📱"
    "BUKU"       -> "📚"
    "ELEKTRONIK" -> "💻"
    "OLAHRAGA"   -> "⚽"
    "FURNITURE"  -> "🪑"
    else         -> "📦"
}