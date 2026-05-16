package com.titipin.app.ui.jastip

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.data.model.JastipDto
import com.titipin.app.data.model.RequestDto
import com.titipin.app.shared.formatDeadlineDisplay
import com.titipin.app.shared.openWhatsApp
import com.titipin.app.shared.TitipinPullRefresh
import com.titipin.app.shared.timeAgo
import com.titipin.app.shared.waMessageJastip
import com.titipin.app.shared.waMessageTakeRequest
import com.titipin.app.ui.components.CategoryChipRow
import com.titipin.app.ui.components.StatusBadge
import com.titipin.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JastipScreen(
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToOffer: (from: String, to: String, name: String, notes: String) -> Unit = { _, _, _, _ -> },
    viewModel: JastipViewModel = hiltViewModel(),
    requestViewModel: RequestViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val categoryState by viewModel.categoryState.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()

    val requestListState by requestViewModel.listState.collectAsState()
    val requestActionState by requestViewModel.actionState.collectAsState()
    val isRequestRefreshing by requestViewModel.isRefreshing.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    // Sheet untuk Buka Jastip (tab 0)
    val jastipSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showJastipSheet by remember { mutableStateOf(false) }

    // Sheet untuk Request Jastip (tab 1)
    val requestSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showRequestSheet by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Tutup sheet jastip setelah sukses post
    LaunchedEffect(actionState) {
        if (actionState is JastipActionState.Success) {
            scope.launch { jastipSheetState.hide() }.invokeOnCompletion {
                showJastipSheet = false
                viewModel.resetActionState()
                viewModel.loadJastipList()
            }
        }
    }

    // Tutup sheet request setelah sukses post, refresh list
    LaunchedEffect(requestActionState) {
        when (requestActionState) {
            is RequestActionState.Success -> {
                scope.launch { requestSheetState.hide() }.invokeOnCompletion {
                    showRequestSheet = false
                    requestViewModel.resetActionState()
                    requestViewModel.loadRequestList()
                }
            }
            is RequestActionState.TakeSuccess -> {
                val result   = (requestActionState as RequestActionState.TakeSuccess).takenResult
                val waNumber = result.request.user.waNumber
                val msg      = waMessageTakeRequest(result.request.fromLocation, result.request.toLocation)
                requestViewModel.resetActionState()
                requestViewModel.loadRequestList()
                openWhatsApp(context, waNumber, msg)
            }
            is RequestActionState.Error -> {
                val msg = (requestActionState as RequestActionState.Error).message
                requestViewModel.resetActionState()
                scope.launch { snackbarHostState.showSnackbar(msg) }
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Cream,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData    = data,
                    containerColor  = Charcoal,
                    contentColor    = Cream,
                    actionColor     = Terracotta,
                    shape           = androidx.compose.foundation.shape.RoundedCornerShape(Radius.md)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Cream)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // ── HEADER ────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "● JASTIP",
                            color = Terracotta,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp,
                            fontFamily = DmSansFamily
                        )
                        Text(
                            text = "Titip & Antar",
                            color = Charcoal,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FrauncesFamily
                        )
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
                    listOf("Tersedia", "Request").forEachIndexed { index, label ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(Radius.full))
                                .background(if (selectedTab == index) Charcoal else CreamDark)
                                .clickable { selectedTab = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = DmSansFamily,
                                color = if (selectedTab == index) Cream else Charcoal60
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                when (selectedTab) {
                    0 -> TitipinPullRefresh(
                        isRefreshing = isRefreshing,
                        onRefresh    = { viewModel.refresh() },
                        modifier     = Modifier.weight(1f)
                    ) {
                        JastipTersediaContent(
                            listState = listState,
                            categoryState = categoryState,
                            selectedCategoryId = selectedCategoryId,
                            onCategorySelected = viewModel::selectCategory,
                            onCardClick = onNavigateToDetail,
                            onRetry = { viewModel.loadJastipList() }
                        )
                    }
                    1 -> TitipinPullRefresh(
                        isRefreshing = isRequestRefreshing,
                        onRefresh    = { requestViewModel.refresh() },
                        modifier     = Modifier.weight(1f)
                    ) {
                        JastipRequestContent(
                            listState   = requestListState,
                            actionState = requestActionState,
                            onTake      = { request ->
                                // Navigate ke offer screen dulu biar user konfirmasi
                                // Saat BE phase 2 siap, di offer screen baru POST /requests/{id}/offer
                                onNavigateToOffer(
                                    request.fromLocation,
                                    request.toLocation,
                                    request.user.name,
                                    request.notes ?: ""
                                )
                            },
                            onRetry = { requestViewModel.loadRequestList() }
                        )
                    }
                }
            }

            // ── FAB ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = Spacing.lg, bottom = Spacing.lg)
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Terracotta)
                    .clickable {
                        if (selectedTab == 0) showJastipSheet = true
                        else showRequestSheet = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("＋", color = Cream, fontSize = 22.sp, fontWeight = FontWeight.Light)
            }
        }

        // ── BOTTOM SHEET: Buka Jastip ─────────────────────────────────
        if (showJastipSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showJastipSheet = false
                    viewModel.resetActionState()
                },
                sheetState     = jastipSheetState,
                containerColor = Cream,
            ) {
                JastipFormContent(
                    viewModel = viewModel,
                    onDismiss = {
                        scope.launch { jastipSheetState.hide() }.invokeOnCompletion {
                            showJastipSheet = false
                            viewModel.resetActionState()
                        }
                    }
                )
            }
        }

        // ── BOTTOM SHEET: Request Jastip ──────────────────────────────
        if (showRequestSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showRequestSheet = false
                    requestViewModel.resetActionState()
                },
                sheetState     = requestSheetState,
                containerColor = Cream,
            ) {
                RequestFormContent(
                    viewModel = requestViewModel,
                    onDismiss = {
                        scope.launch { requestSheetState.hide() }.invokeOnCompletion {
                            showRequestSheet = false
                            requestViewModel.resetActionState()
                        }
                    }
                )
            }
        }
    } // end Scaffold
}

// ── TAB TERSEDIA ──────────────────────────────────────────────────
@Composable
fun JastipTersediaContent(
    listState: JastipListState,
    categoryState: JastipCategoryState,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
    onCardClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    when (listState) {
        is JastipListState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
            }
        }
        is JastipListState.Error -> {
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
        is JastipListState.Success -> {
            val filteredData = selectedCategoryId?.let { selected ->
                listState.data.filter { it.categoryId == selected }
            } ?: listState.data

            Column(modifier = Modifier.fillMaxSize()) {
                if (categoryState is JastipCategoryState.Success) {
                    CategoryChipRow(
                        categories = categoryState.data,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = onCategorySelected,
                        modifier = Modifier.padding(bottom = Spacing.sm)
                    )
                }

            if (filteredData.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📦", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Belum ada jastip aktif", fontFamily = FrauncesFamily, color = Charcoal, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Text("Jadilah yang pertama buka jastip!", fontFamily = DmSansFamily, color = Charcoal60, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(filteredData, key = { it.id }) { jastip ->
                        JastipCard(jastip = jastip, onClick = { onCardClick(jastip.id) })
                    }
                    // Extra space di bawah buat FAB
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
            }
        }
    }
}

// ── TAB REQUEST ───────────────────────────────────────────────────
@Composable
fun JastipRequestContent(
    listState: RequestListState,
    actionState: RequestActionState,
    onTake: (RequestDto) -> Unit,
    onRetry: () -> Unit
) {
    when (listState) {
        is RequestListState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
            }
        }
        is RequestListState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(Spacing.lg)
                ) {
                    Text("😕", fontSize = 40.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        listState.message, fontFamily = DmSansFamily,
                        color = Charcoal60, fontSize = 13.sp, textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = onRetry) {
                        Text("Coba lagi", color = Terracotta, fontFamily = DmSansFamily)
                    }
                }
            }
        }
        is RequestListState.Success -> {
            val isTakeLoading = actionState is RequestActionState.Loading
            LazyColumn(
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                // Info banner
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(GoldPale)
                            .padding(Spacing.md),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Text("💡", fontSize = 14.sp)
                        Text(
                            text = "Kamu butuh dititipin? Tekan ＋ untuk post request. Provider terverifikasi di sekitarmu bisa ambil!",
                            fontFamily = DmSansFamily,
                            fontSize = 12.sp,
                            color = Charcoal,
                            lineHeight = 18.sp
                        )
                    }
                }
                if (listState.data.isEmpty()) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📭", fontSize = 48.sp)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Belum ada request",
                                    fontFamily = FrauncesFamily, color = Charcoal,
                                    fontSize = 16.sp, fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Jadilah yang pertama request jastip!",
                                    fontFamily = DmSansFamily, color = Charcoal60, fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    items(listState.data, key = { it.id }) { request ->
                        RequestCard(
                            request       = request,
                            isTakeLoading = isTakeLoading,
                            onTake        = { onTake(request) }
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

// ── REQUEST CARD ──────────────────────────────────────────────────
@Composable
fun RequestCard(
    request: RequestDto,
    isTakeLoading: Boolean,
    onTake: () -> Unit
) {
    val createdAtLabel = timeAgo(request.createdAt)
    val initials = request.user.name.trim().split(" ")
        .filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(Radius.lg),
        colors    = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {

            // ── Avatar + nama + badge Open ─────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(GoldPale),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Gold, fontFamily = DmSansFamily)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(request.user.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Charcoal, fontFamily = DmSansFamily)
                    Text("Request · $createdAtLabel", fontSize = 11.sp, color = Charcoal60, fontFamily = DmSansFamily)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.full))
                        .background(GoldPale)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("OPEN", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Gold, fontFamily = DmSansFamily)
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Highlight box rute ────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(GoldPale)
                    .padding(Spacing.md)
            ) {
                Text("BUTUH DITITIPIN", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp, color = Gold, fontFamily = DmSansFamily)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = request.fromLocation,
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Charcoal,
                        fontFamily = DmSansFamily, modifier = Modifier.weight(1f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text("  →  ", fontSize = 14.sp, color = Gold, fontFamily = DmSansFamily)
                    Text(
                        text = request.toLocation,
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Charcoal,
                        fontFamily = DmSansFamily, modifier = Modifier.weight(1f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ── Catatan ───────────────────────────────────────────
            if (!request.notes.isNullOrEmpty()) {
                Spacer(Modifier.height(8.dp))
                InfoChip("📝 ${request.notes}")
            }

            Spacer(Modifier.height(Spacing.sm))
            HorizontalDivider(color = Charcoal10, thickness = 0.5.dp)
            Spacer(Modifier.height(Spacing.sm))

            // ── Tombol Ambil ──────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.full))
                        .background(if (isTakeLoading) Terracotta.copy(alpha = 0.5f) else Terracotta)
                        .clickable(enabled = !isTakeLoading) { onTake() }
                        .padding(horizontal = 20.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isTakeLoading) {
                        CircularProgressIndicator(color = Cream, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Ambil →", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Cream, fontFamily = DmSansFamily)
                    }
                }
            }
        }
    }
}

// ── JASTIP CARD ───────────────────────────────────────────────────
@Composable
fun JastipCard(jastip: JastipDto, onClick: () -> Unit) {
    val context = LocalContext.current
    val deadlineShort = formatDeadlineDisplay(jastip.deadline, includeYear = false)
    val deadlineFull = formatDeadlineDisplay(jastip.deadline, includeYear = true)
    val createdAtLabel = timeAgo(jastip.createdAt)
    // Inisial dari userId — nanti ganti nama user kalau BE udah return user object
    val initials = jastip.user.name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    val name = jastip.user.name

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape     = RoundedCornerShape(Radius.lg),
        colors    = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {

            // ── Row 1: Avatar + Nama + Status badge ───────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SagePale),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Sage,
                        fontFamily = DmSansFamily
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        // Nanti ganti dengan nama user dari BE
                        text = name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Charcoal,
                        fontFamily = DmSansFamily
                    )
                    Text(
                        text = "⭐ — · Malang",
                        fontSize = 11.sp,
                        color = Charcoal60,
                        fontFamily = DmSansFamily
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.full))
                            .background(CreamDark)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = createdAtLabel,
                            fontSize = 9.sp,
                            color = Charcoal60,
                            fontFamily = DmSansFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    StatusBadge(status = jastip.status)
                }
            }

            Spacer(Modifier.height(Spacing.sm))

            Text(
                text = jastip.title.ifBlank { "${jastip.fromLocation} → ${jastip.toLocation}" },
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Charcoal,
                fontFamily = DmSansFamily,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (jastip.category != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = listOfNotNull(jastip.category.icon, jastip.category.name).joinToString(" "),
                    fontSize = 11.sp,
                    color = Charcoal60,
                    fontFamily = DmSansFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(Spacing.sm))

            // ── Rute from → to ────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = jastip.fromLocation,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal,
                    fontFamily = DmSansFamily,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "  →  ",
                    fontSize = 14.sp,
                    color = Terracotta,
                    fontFamily = DmSansFamily
                )
                Text(
                    text = jastip.toLocation,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal,
                    fontFamily = DmSansFamily,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Chips info ────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoChip("⏰ $deadlineShort")
                if (!jastip.notes.isNullOrEmpty()) InfoChip("📝 Ada catatan")
            }

            Spacer(Modifier.height(Spacing.sm))

            // ── Divider ───────────────────────────────────────────
            HorizontalDivider(color = Charcoal10, thickness = 0.5.dp)

            Spacer(Modifier.height(Spacing.sm))

            // ── Row bawah: Deadline kiri, tombol WA kanan ─────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Deadline $deadlineFull",
                    fontSize = 11.sp,
                    color = Charcoal60,
                    fontFamily = DmSansFamily
                )

                // Tombol WhatsApp
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.full))
                        .background(Terracotta)
                        .clickable {
                            openWhatsApp(
                                context, jastip.user.waNumber,
                                waMessageJastip(jastip.fromLocation, jastip.toLocation)
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💬 ", fontSize = 12.sp)
                        Text(
                            text = "WhatsApp",
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
}


// ── INFO CHIP ─────────────────────────────────────────────────────
@Composable
fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.full))
            .background(CreamDark)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, fontSize = 11.sp, color = Charcoal60, fontFamily = DmSansFamily)
    }
}
