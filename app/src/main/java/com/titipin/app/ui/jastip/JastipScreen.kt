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
import com.titipin.app.data.model.primaryImageUrl
import com.titipin.app.data.model.tierActiveLimit
import com.titipin.app.data.model.tierImageLimit
import com.titipin.app.shared.formatDeadlineDisplay
import com.titipin.app.shared.openWhatsApp
import com.titipin.app.shared.TitipinPullRefresh
import com.titipin.app.shared.timeAgo
import com.titipin.app.shared.waMessageJastip
import com.titipin.app.shared.waMessageTakeRequest
import com.titipin.app.ui.components.CategoryChipRow
import com.titipin.app.ui.components.LimitReachedDialog
import com.titipin.app.ui.components.StatusBadge
import com.titipin.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JastipScreen(
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToRequestDetail: (String) -> Unit = {},
    onNavigateToOffer: (from: String, to: String, name: String, notes: String) -> Unit = { _, _, _, _ -> },
    viewModel: JastipViewModel = hiltViewModel(),
    requestViewModel: RequestViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val categoryState by viewModel.categoryState.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUserTier by viewModel.currentUserTier.collectAsState()

    val requestListState by requestViewModel.listState.collectAsState()
    val requestActionState by requestViewModel.actionState.collectAsState()
    val isRequestRefreshing by requestViewModel.isRefreshing.collectAsState()
    val requestCategoryState by requestViewModel.categoryState.collectAsState()
    val selectedRequestCategoryId by requestViewModel.selectedCategoryId.collectAsState()
    val requestCurrentUserId by requestViewModel.currentUserId.collectAsState()
    val requestCurrentUserTier by requestViewModel.currentUserTier.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    // Sheet untuk Buka Jastip (tab 0)
    val jastipSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showJastipSheet by remember { mutableStateOf(false) }

    // Sheet untuk Request Jastip (tab 1)
    val requestSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showRequestSheet by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var limitDialogMessage by remember { mutableStateOf<String?>(null) }

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
            is RequestActionState.FeatureInProgress -> {
                requestViewModel.resetActionState()
                scope.launch {
                    snackbarHostState.showSnackbar("Fitur ambil request masih dalam pengembangan. Untuk sementara gunakan WhatsApp.")
                }
            }
            is RequestActionState.Error -> {
                val msg = (requestActionState as RequestActionState.Error).message
                requestViewModel.resetActionState()
                scope.launch { snackbarHostState.showSnackbar(msg) }
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                            currentUserId = currentUserId,
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
                            listState = requestListState,
                            actionState = requestActionState,
                            categoryState = requestCategoryState,
                            selectedCategoryId = selectedRequestCategoryId,
                            onCategorySelected = requestViewModel::selectCategory,
                            currentUserId = requestCurrentUserId,
                            onCardClick = { request -> onNavigateToRequestDetail(request.id) },
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
                        if (selectedTab == 0) {
                            val activeLimit = tierActiveLimit(currentUserTier)
                            val activeMine = (listState as? JastipListState.Success)
                                ?.data
                                ?.count { it.userId == currentUserId && it.status == "ACTIVE" } ?: 0
                            if (activeMine >= activeLimit) {
                                limitDialogMessage = "Kamu sudah punya $activeLimit jastip aktif sesuai limit plan kamu. Tutup salah satu jastip dulu atau upgrade plan."
                            } else {
                                showJastipSheet = true
                            }
                        } else {
                            val activeLimit = tierActiveLimit(requestCurrentUserTier)
                            val activeMine = (requestListState as? RequestListState.Success)
                                ?.data
                                ?.count { it.userId?.toString() == requestCurrentUserId && it.status == "OPEN" } ?: 0
                            if (activeMine >= activeLimit) {
                                limitDialogMessage = "Kamu sudah punya $activeLimit request jastip aktif sesuai limit plan kamu. Tutup salah satu request dulu atau upgrade plan."
                            } else {
                                showRequestSheet = true
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("＋", color = Cream, fontSize = 22.sp, fontWeight = FontWeight.Light)
            }

        if (limitDialogMessage != null) {
            LimitReachedDialog(
                message = limitDialogMessage.orEmpty(),
                onDismiss = { limitDialogMessage = null }
            )
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
                    maxImages = tierImageLimit(currentUserTier),
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

        // ── SNACKBAR HOST ──────────────────────────────────────────
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        ) { data ->
            Snackbar(
                snackbarData    = data,
                containerColor  = Charcoal,
                contentColor    = Cream,
                actionColor     = Terracotta,
                shape           = androidx.compose.foundation.shape.RoundedCornerShape(Radius.md)
            )
        }
    }
}

// ── TAB TERSEDIA ──────────────────────────────────────────────────
@Composable
fun JastipTersediaContent(
    listState: JastipListState,
    categoryState: JastipCategoryState,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
    currentUserId: String?,
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
                        JastipCard(
                            jastip = jastip,
                            currentUserId = currentUserId,
                            onClick = { onCardClick(jastip.id) }
                        )
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
    categoryState: RequestCategoryState,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
    currentUserId: String?,
    onCardClick: (RequestDto) -> Unit,
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
            val filteredData = selectedCategoryId?.let { selected ->
                listState.data.filter { it.categoryId == selected }
            } ?: listState.data

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
                if (categoryState is RequestCategoryState.Success) {
                    item {
                        CategoryChipRow(
                            categories = categoryState.data,
                            selectedCategoryId = selectedCategoryId,
                            onCategorySelected = onCategorySelected,
                            modifier = Modifier.padding(vertical = Spacing.sm)
                        )
                    }
                }
                if (filteredData.isEmpty()) {
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
                    items(filteredData, key = { it.id }) { request ->
                        RequestCard(
                            request       = request,
                            isTakeLoading = isTakeLoading,
                            currentUserId = currentUserId,
                            onClick = { onCardClick(request) }
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

// ── REQUEST CARD (redesigned) ─────────────────────────────────────
@Composable
fun RequestCard(
    request: RequestDto,
    isTakeLoading: Boolean,
    currentUserId: String?,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val createdAtLabel = timeAgo(request.createdAt ?: request.updatedAt.orEmpty())
    val initials = request.user.name.trim().split(" ")
        .filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
    val isMine = currentUserId == request.userId?.toString()

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
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
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GoldPale),
                    contentAlignment = Alignment.Center
                ) {
                    if (!request.user.avatarUrl.isNullOrBlank()) {
                        coil.compose.AsyncImage(
                            model              = request.user.avatarUrl,
                            contentDescription = request.user.name,
                            modifier           = Modifier.fillMaxSize(),
                            contentScale       = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(initials, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gold, fontFamily = DmSansFamily)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.user.name,
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = Charcoal, fontFamily = DmSansFamily,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = createdAtLabel,
                        fontSize = 10.sp, color = Charcoal60, fontFamily = DmSansFamily
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (isMine) {
                        OwnerMiniBadge()
                        Spacer(Modifier.height(4.dp))
                    }
                    StatusBadge(status = request.status)
                }
            }

            Spacer(Modifier.height(Spacing.sm))

            // ── Title ───────────────
            Text(
                text = request.title,
                fontSize = 16.sp, fontWeight = FontWeight.Medium,
                color = Charcoal, fontFamily = FrauncesFamily,
                lineHeight = 21.sp, maxLines = 2, overflow = TextOverflow.Ellipsis
            )

            if (!request.notes.isNullOrEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = request.notes,
                    fontSize = 12.sp, color = Charcoal60, fontFamily = DmSansFamily,
                    lineHeight = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(Spacing.md))

            // ── Rute (Dari -> Ke) ───────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(CreamDark)
                    .padding(horizontal = Spacing.sm, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("DARI", fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Charcoal60, fontFamily = DmSansFamily)
                    Text(request.fromLocation, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Charcoal, fontFamily = DmSansFamily, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("→", fontSize = 16.sp, color = Terracotta, modifier = Modifier.padding(horizontal = 8.dp))
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("KE", fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Charcoal60, fontFamily = DmSansFamily)
                    Text(request.toLocation, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Charcoal, fontFamily = DmSansFamily, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                }
            }

            Spacer(Modifier.height(Spacing.sm))
            HorizontalDivider(color = Charcoal10, thickness = 0.5.dp)
            Spacer(Modifier.height(Spacing.sm))

            // ── Footer ────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                if (request.category != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.full))
                            .background(CreamDark)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = listOfNotNull(request.category.icon, request.category.name).joinToString(" "),
                            fontSize = 10.sp, color = Charcoal60, fontFamily = DmSansFamily
                        )
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.full))
                        .background(if (request.user.waNumber.isNotBlank()) Sage else CreamDark)
                        .clickable(enabled = !isTakeLoading && request.user.waNumber.isNotBlank()) {
                            openWhatsApp(
                                context, request.user.waNumber,
                                waMessageTakeRequest(request.fromLocation, request.toLocation)
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isTakeLoading) {
                        CircularProgressIndicator(color = Cream, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            text = "💬 Hubungi",
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = Cream, fontFamily = DmSansFamily
                        )
                    }
                }
            }
        }
    }
}

// ── JASTIP CARD ───────────────────────────────────────────────────

@Composable
fun JastipCard(
    jastip: JastipDto,
    currentUserId: String?,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val deadlineShort = formatDeadlineDisplay(jastip.deadline, includeYear = false)
    val deadlineFull = formatDeadlineDisplay(jastip.deadline, includeYear = true)
    val createdAtLabel = timeAgo(jastip.createdAt)
    // Inisial dari userId — nanti ganti nama user kalau BE udah return user object
    val initials = jastip.user.name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    val name = jastip.user.name
    val primaryImageUrl = jastip.primaryImageUrl()
    val isMine = currentUserId == jastip.userId

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape     = RoundedCornerShape(Radius.lg),
        colors    = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            if (!primaryImageUrl.isNullOrBlank()) {
                coil.compose.AsyncImage(
                    model = primaryImageUrl,
                    contentDescription = jastip.title.ifBlank { "Foto jastip" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(Radius.md)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Spacer(Modifier.height(Spacing.sm))
            }

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
                    if (!jastip.user.avatarUrl.isNullOrBlank()) {
                        coil.compose.AsyncImage(
                            model = jastip.user.avatarUrl,
                            contentDescription = jastip.user.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = initials,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Sage,
                            fontFamily = DmSansFamily
                        )
                    }
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
                    if (isMine) {
                        OwnerMiniBadge()
                        Spacer(Modifier.height(4.dp))
                    }
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

            // ── Row bawah: Batas Nitip kiri, tombol WA kanan ─────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Batas Nitip $deadlineFull",
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

@Composable
private fun OwnerMiniBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.full))
            .background(TerracottaPale)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "Milik Saya",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Terracotta,
            fontFamily = DmSansFamily
        )
    }
}
