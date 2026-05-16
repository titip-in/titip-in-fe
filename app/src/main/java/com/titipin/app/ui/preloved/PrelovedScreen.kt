package com.titipin.app.ui.preloved

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.titipin.app.data.model.PrelovedDto
import com.titipin.app.data.model.PrelovedRequestDto
import com.titipin.app.data.model.conditionLabel
import com.titipin.app.data.model.formattedMaxPrice
import com.titipin.app.data.model.formattedPrice
import com.titipin.app.data.model.primaryImageUrl
import com.titipin.app.shared.TitipinPullRefresh
import com.titipin.app.shared.openWhatsApp
import com.titipin.app.shared.waMessageWanted
import com.titipin.app.ui.components.CategoryChipRow
import com.titipin.app.ui.theme.*
import kotlinx.coroutines.launch

private val cardBgColors = listOf(TerracottaPale, SagePale, GoldPale, CreamDark)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrelovedScreen(
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToRequestDetail: (String) -> Unit = {},
    viewModel: PrelovedViewModel = hiltViewModel(),
    prelovedRequestViewModel: PrelovedRequestViewModel = hiltViewModel()
) {
    val listState              by viewModel.listState.collectAsState()
    val actionState            by viewModel.actionState.collectAsState()
    val isPrelovedRefreshing   by viewModel.isRefreshing.collectAsState()
    val categoryState          by viewModel.categoryState.collectAsState()
    val selectedCategoryId     by viewModel.selectedCategoryId.collectAsState()

    val requestListState       by prelovedRequestViewModel.listState.collectAsState()
    val requestActionState     by prelovedRequestViewModel.actionState.collectAsState()
    val isRequestRefreshing    by prelovedRequestViewModel.isRefreshing.collectAsState()
    val requestCategoryState   by prelovedRequestViewModel.categoryState.collectAsState()
    val requestSelectedCatId   by prelovedRequestViewModel.selectedCategoryId.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    // Sheet tab Dijual
    val prelovedSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPrelovedSheet  by remember { mutableStateOf(false) }

    // Sheet tab Dicari
    val requestSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showRequestSheet  by remember { mutableStateOf(false) }

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

    // Tutup sheet dicari setelah sukses post
    LaunchedEffect(requestActionState) {
        if (requestActionState is PrelovedRequestActionState.Success) {
            scope.launch { requestSheetState.hide() }.invokeOnCompletion {
                showRequestSheet = false
                prelovedRequestViewModel.resetActionState()
                prelovedRequestViewModel.loadPrelovedRequestList()
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
                0 -> TitipinPullRefresh(
                    isRefreshing = isPrelovedRefreshing,
                    onRefresh    = { viewModel.refresh() },
                    modifier     = Modifier.weight(1f)
                ) {
                    PrelovedDijualContent(
                        listState          = listState,
                        categoryState      = categoryState,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = viewModel::selectCategory,
                        onCardClick        = onNavigateToDetail,
                        onRetry            = { viewModel.loadPrelovedList() }
                    )
                }
                1 -> TitipinPullRefresh(
                    isRefreshing = isRequestRefreshing,
                    onRefresh    = { prelovedRequestViewModel.refresh() },
                    modifier     = Modifier.weight(1f)
                ) {
                    PrelovedDicariContent(
                        listState          = requestListState,
                        categoryState      = requestCategoryState,
                        selectedCategoryId = requestSelectedCatId,
                        onCategorySelected = prelovedRequestViewModel::selectCategory,
                        onRetry            = { prelovedRequestViewModel.loadPrelovedRequestList() },
                        onCardClick        = onNavigateToRequestDetail,
                        onContactWhatsApp  = { waNumber, title ->
                            openWhatsApp(context, waNumber, waMessageWanted(title))
                        }
                    )
                }
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
                    else showRequestSheet = true
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
    if (showRequestSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRequestSheet = false; prelovedRequestViewModel.resetActionState() },
            sheetState = requestSheetState,
            containerColor = Cream
        ) {
            PrelovedRequestFormContent(
                viewModel = prelovedRequestViewModel,
                onDismiss = {
                    scope.launch { requestSheetState.hide() }.invokeOnCompletion {
                        showRequestSheet = false
                        prelovedRequestViewModel.resetActionState()
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
    categoryState: PrelovedCategoryState,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
    onCardClick: (String) -> Unit,
    onRetry: () -> Unit
) {
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
            val filteredData = selectedCategoryId?.let { selected ->
                listState.data.filter { it.categoryId == selected }
            } ?: listState.data

            Column {
                if (categoryState is PrelovedCategoryState.Success) {
                    CategoryChipRow(
                        categories         = categoryState.data,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = onCategorySelected,
                        modifier           = Modifier.padding(bottom = Spacing.sm)
                    )
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
    val categoryLabel = item.category?.name ?: "Lainnya"
    val categoryEmoji = item.category?.icon ?: categoryEmojiFor(categoryLabel)
    val primaryImageUrl = item.primaryImageUrl()

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
                if (primaryImageUrl.isNullOrBlank()) {
                    Text(categoryEmoji, fontSize = if (isWide) 52.sp else 36.sp)
                } else {
                    AsyncImage(
                        model              = primaryImageUrl,
                        contentDescription = item.title,
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop
                    )
                }
            }

            // ── INFO ──────────────────────────────────────────────
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text       = item.title,
                    fontSize   = if (isWide) 14.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Charcoal, fontFamily = FrauncesFamily,
                    maxLines   = 2, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = "$categoryEmoji $categoryLabel",
                    fontSize = 10.sp,
                    color    = Charcoal60,
                    fontFamily = DmSansFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = item.formattedPrice(),
                        fontSize   = 13.sp, fontWeight = FontWeight.Bold,
                        color      = Terracotta, fontFamily = DmSansFamily
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.full))
                            .background(SagePale)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text       = condShort,
                            fontSize   = 8.sp, fontWeight = FontWeight.Bold,
                            color      = Sage, fontFamily = DmSansFamily, letterSpacing = 0.5.sp
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
    listState: PrelovedRequestListState,
    categoryState: PrelovedRequestCategoryState,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
    onRetry: () -> Unit,
    onCardClick: (String) -> Unit = {},
    onContactWhatsApp: (String, String) -> Unit
) {
    when (listState) {
        is PrelovedRequestListState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
            }
        }
        is PrelovedRequestListState.Error -> {
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
        is PrelovedRequestListState.Success -> {
            val filteredData = selectedCategoryId?.let { selected ->
                listState.data.filter { it.categoryId == selected }
            } ?: listState.data

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                // ── Category filter chips ─────────────────────────
                if (categoryState is PrelovedRequestCategoryState.Success) {
                    item(span = { GridItemSpan(1) }) {
                        CategoryChipRow(
                            categories         = categoryState.data,
                            selectedCategoryId = selectedCategoryId,
                            onCategorySelected = onCategorySelected,
                            modifier           = Modifier.padding(bottom = 4.dp)
                        )
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
                    items(
                        count = filteredData.size,
                        key   = { filteredData[it].id }
                    ) { index ->
                        PrelovedRequestCard(
                            item    = filteredData[index],
                            onClick = { onCardClick(filteredData[index].id) },
                            onContactWhatsApp = { onContactWhatsApp(filteredData[index].user.waNumber, filteredData[index].title) }
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

// ── PRELOVED REQUEST CARD ─────────────────────────────────────────
@Composable
fun PrelovedRequestCard(
    item: PrelovedRequestDto,
    onClick: () -> Unit = {},
    onContactWhatsApp: () -> Unit
) {
    val initials = item.user.name.trim().split(" ")
        .filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
    val formattedBudget = item.formattedMaxPrice()
    val categoryLabel   = item.category?.name

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
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
                    if (!item.user.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model              = item.user.avatarUrl,
                            contentDescription = item.user.name,
                            modifier           = Modifier.fillMaxSize(),
                            contentScale       = ContentScale.Crop
                        )
                    } else {
                        Text(initials, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Terracotta, fontFamily = DmSansFamily)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.user.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Charcoal, fontFamily = DmSansFamily)
                    Text("Mencari", fontSize = 11.sp, color = Charcoal60, fontFamily = DmSansFamily)
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
                text       = item.title,
                fontFamily = FrauncesFamily,
                fontSize   = 17.sp,
                fontWeight = FontWeight.Medium,
                color      = Charcoal,
                lineHeight = 22.sp
            )

            // ── Deskripsi ─────────────────────────────────────────
            if (!item.description.isNullOrEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = item.description,
                    fontFamily = DmSansFamily,
                    fontSize   = 12.sp,
                    color      = Charcoal60,
                    lineHeight = 17.sp,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Chips: budget + kategori ──────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (formattedBudget != null) {
                    PrelovedInfoChip("💰 $formattedBudget")
                }
                if (!categoryLabel.isNullOrEmpty()) {
                    val emoji = item.category?.icon ?: categoryEmojiFor(categoryLabel)
                    PrelovedInfoChip("$emoji $categoryLabel")
                }
            }

            Spacer(Modifier.height(Spacing.sm))
            HorizontalDivider(color = Charcoal10, thickness = 0.5.dp)
            Spacer(Modifier.height(Spacing.sm))

            // ── Tombol Hubungi via WhatsApp ───────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.full))
                        .background(
                            if (item.user.waNumber.isBlank())
                                Terracotta.copy(alpha = 0.4f)
                            else Terracotta
                        )
                        .clickable(enabled = item.user.waNumber.isNotBlank()) { onContactWhatsApp() }
                        .padding(horizontal = 20.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "💬 Hubungi via WA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Cream,
                        fontFamily = DmSansFamily
                    )
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
