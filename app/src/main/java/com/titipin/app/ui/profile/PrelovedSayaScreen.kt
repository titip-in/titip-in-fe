package com.titipin.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.data.model.PrelovedDto
import com.titipin.app.data.model.PrelovedRequestDto
import com.titipin.app.data.model.conditionLabel
import com.titipin.app.data.model.formattedMaxPrice
import com.titipin.app.data.model.formattedPrice
import com.titipin.app.data.model.primaryImageUrl
import com.titipin.app.shared.timeAgo
import com.titipin.app.ui.components.LimitReachedDialog
import com.titipin.app.ui.preloved.EditPrelovedRequestSheet
import com.titipin.app.ui.preloved.EditPrelovedSheet
import com.titipin.app.ui.theme.*
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrelovedSayaScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToRequestDetail: (String) -> Unit = {},
    viewModel: PrelovedSayaViewModel = hiltViewModel()
) {
    val listState   by viewModel.listState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val itemForEdit by viewModel.itemForEdit.collectAsState()
    val fetchingEditId by viewModel.fetchingEditId.collectAsState()
    // 0=Dijual, 1=Dicari (Request), 2=Arsip
    var selectedTab by remember { mutableStateOf(0) }
    var editRequest by remember { mutableStateOf<PrelovedRequestDto?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var limitDialogMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(actionState) {
        when (actionState) {
            is PrelovedSayaActionState.Success -> {
                viewModel.resetActionState()
                viewModel.loadData()
            }
            is PrelovedSayaActionState.LimitReached -> {
                limitDialogMessage = (actionState as PrelovedSayaActionState.LimitReached).message
                viewModel.resetActionState()
            }
            is PrelovedSayaActionState.Error -> {
                scope.launch { snackbarHostState.showSnackbar((actionState as PrelovedSayaActionState.Error).message) }
                viewModel.resetActionState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Cream,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(data, containerColor = Charcoal, contentColor = Cream, actionColor = Terracotta,
                    shape = RoundedCornerShape(Radius.md))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp, fontFamily = DmSansFamily)
                    Text("Preloved Saya", color = Charcoal, fontSize = 24.sp,
                        fontWeight = FontWeight.Medium, fontFamily = FrauncesFamily)
                }
            }

            // ── TAB (3 tab) ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .padding(horizontal = Spacing.lg)
                    .clip(RoundedCornerShape(Radius.full))
                    .background(CreamDark)
                    .padding(4.dp)
            ) {
                listOf("Dijual", "Dicari", "Arsip").forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Radius.full))
                            .background(if (selectedTab == index) Charcoal else CreamDark)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            fontFamily = DmSansFamily,
                            color = if (selectedTab == index) Cream else Charcoal60)
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))

            // ── CONTENT ───────────────────────────────────────────
            when (val state = listState) {
                is PrelovedSayaState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
                    }
                }
                is PrelovedSayaState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(Spacing.lg)) {
                            Text("😕", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(state.message, fontFamily = DmSansFamily, color = Charcoal60, fontSize = 13.sp)
                            Spacer(Modifier.height(12.dp))
                            TextButton(onClick = { viewModel.loadData() }) {
                                Text("Coba lagi", color = Terracotta, fontFamily = DmSansFamily)
                            }
                        }
                    }
                }
                is PrelovedSayaState.Success -> {
                    val isActionLoading = actionState is PrelovedSayaActionState.Loading

                    when (selectedTab) {
                        // ── Tab 0: Dijual (AVAILABLE) ─────────────
                        0 -> {
                            val data = state.listings.filter { it.status == "AVAILABLE" }
                            if (data.isEmpty()) {
                                SayaEmptyState(emoji = "🛍️", title = "Belum ada barang dijual",
                                    subtitle = "Post barang preloved kamu dari tab Preloved!")
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(horizontal = Spacing.lg),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    items(data, key = { it.id }) { item ->
                                        PrelovedSayaCard(
                                            item            = item,
                                            isActionLoading = isActionLoading,
                                            isEditLoading   = fetchingEditId == item.id,
                                            onLihatDetail   = { onNavigateToDetail(item.id) },
                                            onEdit          = { viewModel.fetchPrelovedForEdit(item.id) },
                                            onMarkSold      = { viewModel.updateListingStatus(item.id, "SOLD") },
                                            onHapus         = { viewModel.deleteListing(item.id) }
                                        )
                                    }
                                    item { Spacer(Modifier.height(24.dp)) }
                                }
                            }
                        }

                        // ── Tab 1: Dicari (OPEN request) ──────────
                        1 -> {
                            val data = state.requests.filter { it.status == "OPEN" }
                            if (data.isEmpty()) {
                                SayaEmptyState(emoji = "🔍", title = "Belum ada pencarian aktif",
                                    subtitle = "Post permintaan barang dari tab Preloved → Dicari!")
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(horizontal = Spacing.lg),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    items(data, key = { it.id }) { req ->
                                        PrelovedRequestSayaCard(
                                            request         = req,
                                            isActionLoading = isActionLoading,
                                            onClick         = { onNavigateToRequestDetail(req.id) },
                                            onEdit          = { editRequest = req },
                                            onTutup         = { viewModel.updateRequestStatus(req.id, "CLOSED") },
                                            onHapus         = { viewModel.deleteRequest(req.id) }
                                        )
                                    }
                                    item { Spacer(Modifier.height(24.dp)) }
                                }
                            }
                        }

                        // ── Tab 2: Arsip (SOLD/CLOSED) ────────────
                        2 -> {
                            val soldListings    = state.listings.filter { it.status == "SOLD" || it.status == "CLOSED" }
                            val closedRequests  = state.requests.filter { it.status == "CLOSED" }
                            if (soldListings.isEmpty() && closedRequests.isEmpty()) {
                                SayaEmptyState(emoji = "📦", title = "Belum ada arsip",
                                    subtitle = "Barang terjual & pencarian ditutup muncul di sini.")
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(horizontal = Spacing.lg),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    if (soldListings.isNotEmpty()) {
                                        item { SayaSectionLabel("🛍️ BARANG DIJUAL") }
                                        items(soldListings, key = { "l_${it.id}" }) { item ->
                                            PrelovedSayaCard(
                                                item            = item,
                                                isActionLoading = isActionLoading,
                                                isEditLoading   = fetchingEditId == item.id,
                                                onLihatDetail   = { onNavigateToDetail(item.id) },
                                                onEdit          = { viewModel.fetchPrelovedForEdit(item.id) },
                                                onMarkSold      = null,
                                                onBuka          = { viewModel.updateListingStatus(item.id, "AVAILABLE") },
                                                onHapus         = { viewModel.deleteListing(item.id) }
                                            )
                                        }
                                    }
                                    if (closedRequests.isNotEmpty()) {
                                        item { SayaSectionLabel("🔍 PENCARIAN DITUTUP") }
                                        items(closedRequests, key = { "r_${it.id}" }) { req ->
                                            PrelovedRequestSayaCard(
                                                request         = req,
                                                isActionLoading = isActionLoading,
                                                onClick         = { onNavigateToRequestDetail(req.id) },
                                                onEdit          = { editRequest = req },
                                                onTutup         = null,
                                                onBuka          = { viewModel.updateRequestStatus(req.id, "OPEN") },
                                                onHapus         = { viewModel.deleteRequest(req.id) }
                                            )
                                        }
                                    }
                                    item { Spacer(Modifier.height(24.dp)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (limitDialogMessage != null) {
        LimitReachedDialog(
            message = limitDialogMessage.orEmpty(),
            onDismiss = { limitDialogMessage = null }
        )
    }

    itemForEdit?.let { item ->
        EditPrelovedSheet(
            item = item,
            categories = categories,
            onDismiss = { viewModel.clearItemForEdit() },
            onSubmit = { title, price, condition, description, categoryId, imageUris, existingUrls ->
                viewModel.updateListing(item.id, title, price, condition, description, categoryId, imageUris, existingUrls)
                viewModel.clearItemForEdit()
            }
        )
    }

    editRequest?.let { item ->
        EditPrelovedRequestSheet(
            item = item,
            categories = categories,
            onDismiss = { editRequest = null },
            onSubmit = { title, description, maxPrice, categoryId ->
                viewModel.updateRequest(item.id, title, description, maxPrice, categoryId)
                editRequest = null
            }
        )
    }
}

// ── PRELOVED LISTING CARD ─────────────────────────────────────────
@Composable
private fun PrelovedSayaCard(
    item: PrelovedDto,
    isActionLoading: Boolean,
    isEditLoading: Boolean = false,
    onLihatDetail: () -> Unit,
    onEdit: () -> Unit,
    onMarkSold: (() -> Unit)?,
    onBuka: (() -> Unit)? = null,
    onHapus: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSoldDialog by remember { mutableStateOf(false) }
    var showReopenDialog by remember { mutableStateOf(false) }
    val isAvailable = item.status == "AVAILABLE"

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onLihatDetail() },
        shape     = RoundedCornerShape(Radius.lg),
        colors    = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(Spacing.md)) {
            val imageUrl = item.primaryImageUrl()
            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(Radius.md))
                .background(TerracottaPale), contentAlignment = Alignment.Center) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(categoryEmojiForPreloved(item.category?.name), fontSize = 26.sp)
                }
            }
            Spacer(Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = Charcoal, fontFamily = DmSansFamily,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(6.dp))
                    Box(modifier = Modifier.clip(RoundedCornerShape(Radius.full))
                        .background(when (item.status) {
                            "AVAILABLE" -> SagePale; "SOLD" -> GoldPale; else -> TerracottaPale
                        }).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text(when (item.status) {
                            "AVAILABLE" -> "DIJUAL"; "SOLD" -> "TERJUAL"; else -> "DITUTUP"
                        }, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp,
                            color = when (item.status) {
                                "AVAILABLE" -> Sage; "SOLD" -> Gold; else -> Terracotta
                            }, fontFamily = DmSansFamily)
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(item.formattedPrice(), fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = Terracotta, fontFamily = DmSansFamily)
                Spacer(Modifier.height(2.dp))
                Text("${item.conditionLabel()} · ${timeAgo(item.createdAt ?: item.updatedAt.orEmpty())}",
                    fontSize = 11.sp, color = Charcoal60, fontFamily = DmSansFamily)
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Charcoal10, thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(Radius.full)).background(CreamDark)
                        .clickable(enabled = !isActionLoading) { showDeleteDialog = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("🗑 Hapus", fontSize = 11.sp, color = Charcoal60, fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Medium)
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(Radius.full)).background(GoldPale)
                        .clickable(enabled = !isActionLoading && !isEditLoading) { onEdit() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center) {
                        if (isEditLoading) {
                            CircularProgressIndicator(
                                color = Gold, strokeWidth = 1.5.dp,
                                modifier = Modifier.size(12.dp)
                            )
                        } else {
                            Text("✎ Edit", fontSize = 11.sp, color = Gold, fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Medium)
                        }
                    }
                    if (isAvailable && onMarkSold != null) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(Radius.full)).background(Terracotta)
                            .clickable(enabled = !isActionLoading) { showSoldDialog = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center) {
                            if (isActionLoading) {
                                CircularProgressIndicator(color = Cream, strokeWidth = 1.5.dp, modifier = Modifier.size(12.dp))
                            } else {
                                Text("✓ Tandai Terjual", fontSize = 11.sp, color = Cream,
                                    fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    if (onBuka != null) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(Radius.full)).background(Sage)
                            .clickable(enabled = !isActionLoading) { showReopenDialog = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text("Buka Lagi", fontSize = 11.sp, color = Cream,
                                fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
    if (showDeleteDialog) {
        SayaDeleteDialog(
            title   = "Hapus Barang?",
            message = "\"${item.title}\" akan dihapus permanen.",
            onConfirm = { showDeleteDialog = false; onHapus() },
            onDismiss = { showDeleteDialog = false }
        )
    }
    if (showSoldDialog && onMarkSold != null) {
        SayaDeleteDialog(
            title = "Tandai Terjual?",
            message = "Barang ini akan masuk arsip dan tidak tampil sebagai barang tersedia.",
            confirmLabel = "Tandai",
            onConfirm = { showSoldDialog = false; onMarkSold() },
            onDismiss = { showSoldDialog = false }
        )
    }
    if (showReopenDialog && onBuka != null) {
        SayaDeleteDialog(
            title = "Buka Lagi Barang?",
            message = "Barang ini akan tersedia lagi jika limit aktif kamu masih tersedia.",
            confirmLabel = "Buka Lagi",
            onConfirm = { showReopenDialog = false; onBuka() },
            onDismiss = { showReopenDialog = false }
        )
    }
}

// ── PRELOVED REQUEST CARD ─────────────────────────────────────────
@Composable
private fun PrelovedRequestSayaCard(
    request: PrelovedRequestDto,
    isActionLoading: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onTutup: (() -> Unit)?,
    onBuka: (() -> Unit)? = null,
    onHapus: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCloseDialog by remember { mutableStateOf(false) }
    var showReopenDialog by remember { mutableStateOf(false) }
    val isOpen = request.status == "OPEN"

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(Radius.lg),
        colors    = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(request.title, fontSize = 15.sp, fontWeight = FontWeight.Medium,
                    color = Charcoal, fontFamily = FrauncesFamily,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.clip(RoundedCornerShape(Radius.full))
                    .background(if (isOpen) SagePale else CreamDark)
                    .padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(if (isOpen) "OPEN" else "CLOSED", fontSize = 9.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                        color = if (isOpen) Sage else Charcoal30, fontFamily = DmSansFamily)
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val budget = request.formattedMaxPrice()
                if (budget != null) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(Radius.full)).background(CreamDark)
                        .padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("💰 $budget", fontSize = 10.sp, color = Charcoal60, fontFamily = DmSansFamily)
                    }
                }
                if (request.category != null) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(Radius.full)).background(CreamDark)
                        .padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("${request.category.icon ?: ""} ${request.category.name}",
                            fontSize = 10.sp, color = Charcoal60, fontFamily = DmSansFamily)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(timeAgo(request.createdAt.orEmpty()), fontSize = 11.sp, color = Charcoal60, fontFamily = DmSansFamily)
            Spacer(Modifier.height(Spacing.sm))
            HorizontalDivider(color = Charcoal10, thickness = 0.5.dp)
            Spacer(Modifier.height(Spacing.sm))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                Box(modifier = Modifier.clip(RoundedCornerShape(Radius.full)).background(CreamDark)
                    .clickable(enabled = !isActionLoading) { showDeleteDialog = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("🗑 Hapus", fontSize = 12.sp, color = Charcoal60, fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Medium)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(Radius.full)).background(GoldPale)
                    .clickable(enabled = !isActionLoading) { onEdit() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("✎ Edit", fontSize = 12.sp, color = Gold, fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Medium)
                }
                if (onTutup != null && isOpen) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(Radius.full)).background(Charcoal)
                        .clickable(enabled = !isActionLoading) { showCloseDialog = true }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center) {
                        if (isActionLoading) {
                            CircularProgressIndicator(color = Cream, strokeWidth = 1.5.dp, modifier = Modifier.size(14.dp))
                        } else {
                            Text("✓ Tutup", fontSize = 12.sp, color = Cream,
                                fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                if (onBuka != null) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(Radius.full)).background(Sage)
                        .clickable(enabled = !isActionLoading) { showReopenDialog = true }
                        .padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Buka Lagi", fontSize = 12.sp, color = Cream,
                            fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
    if (showDeleteDialog) {
        SayaDeleteDialog(
            title   = "Hapus Pencarian?",
            message = "Pencarian \"${request.title}\" akan dihapus permanen.",
            onConfirm = { showDeleteDialog = false; onHapus() },
            onDismiss = { showDeleteDialog = false }
        )
    }
    if (showCloseDialog && onTutup != null) {
        SayaDeleteDialog(
            title = "Tutup Pencarian?",
            message = "Pencarian ini akan masuk arsip dan tidak tampil sebagai pencarian aktif.",
            confirmLabel = "Tutup",
            onConfirm = { showCloseDialog = false; onTutup() },
            onDismiss = { showCloseDialog = false }
        )
    }
    if (showReopenDialog && onBuka != null) {
        SayaDeleteDialog(
            title = "Buka Lagi Pencarian?",
            message = "Pencarian ini akan aktif lagi jika limit aktif kamu masih tersedia.",
            confirmLabel = "Buka Lagi",
            onConfirm = { showReopenDialog = false; onBuka() },
            onDismiss = { showReopenDialog = false }
        )
    }
}

private fun categoryEmojiForPreloved(category: String?): String = when (category.orEmpty().uppercase().trim()) {
    "SEPATU"     -> "👟"
    "FASHION"    -> "👗"
    "GADGET"     -> "📱"
    "BUKU"       -> "📚"
    "ELEKTRONIK" -> "💻"
    "OLAHRAGA"   -> "⚽"
    "FURNITURE"  -> "🪑"
    else         -> "📦"
}
