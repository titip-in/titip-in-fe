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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.data.model.JastipDto
import com.titipin.app.data.model.RequestDto
import com.titipin.app.data.model.primaryImageUrl
import com.titipin.app.shared.formatDeadlineDisplay
import com.titipin.app.shared.timeAgo
import com.titipin.app.ui.components.LimitReachedDialog
import com.titipin.app.ui.jastip.EditDeadlineDialog
import com.titipin.app.ui.jastip.EditJastipSheet
import com.titipin.app.ui.jastip.EditRequestSheet
import com.titipin.app.ui.theme.*
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JastipSayaScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToRequestDetail: (String) -> Unit = {},
    viewModel: JastipSayaViewModel = hiltViewModel()
) {
    val listState   by viewModel.listState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val itemForEdit by viewModel.itemForEdit.collectAsState()
    val fetchingEditId by viewModel.fetchingEditId.collectAsState()
    // 0=Listing Aktif, 1=Request Saya, 2=Selesai
    var selectedTab by remember { mutableStateOf(0) }
    var editRequest by remember { mutableStateOf<RequestDto?>(null) }
    var reopenJastip by remember { mutableStateOf<JastipDto?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var limitDialogMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(actionState) {
        when (actionState) {
            is JastipSayaActionState.Success -> {
                viewModel.resetActionState()
                viewModel.loadData()
            }
            is JastipSayaActionState.LimitReached -> {
                limitDialogMessage = (actionState as JastipSayaActionState.LimitReached).message
                viewModel.resetActionState()
            }
            is JastipSayaActionState.Error -> {
                scope.launch { snackbarHostState.showSnackbar((actionState as JastipSayaActionState.Error).message) }
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
                    Text("Jastip Saya", color = Charcoal, fontSize = 24.sp,
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
                listOf("Listing", "Request", "Selesai").forEachIndexed { index, label ->
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
                is JastipSayaState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
                    }
                }
                is JastipSayaState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(Spacing.lg)) {
                            Text("😕", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(state.message, fontFamily = DmSansFamily, color = Charcoal60,
                                fontSize = 13.sp, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(12.dp))
                            TextButton(onClick = { viewModel.loadData() }) {
                                Text("Coba lagi", color = Terracotta, fontFamily = DmSansFamily)
                            }
                        }
                    }
                }
                is JastipSayaState.Success -> {
                    val isActionLoading = actionState is JastipSayaActionState.Loading

                    when (selectedTab) {
                        // ── Tab 0: Listing Aktif ──────────────────
                        0 -> {
                            val data = state.listings.filter { it.status == "ACTIVE" }
                            if (data.isEmpty()) {
                                SayaEmptyState(emoji = "📦", title = "Belum ada listing aktif",
                                    subtitle = "Buka jastip baru dari tab Jastip!")
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(horizontal = Spacing.lg),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    items(data, key = { it.id }) { jastip ->
                                        JastipSayaCard(
                                            jastip          = jastip,
                                            isActionLoading = isActionLoading,
                                            onClick         = { onNavigateToDetail(jastip.id) },
                                            onEdit          = { viewModel.fetchJastipForEdit(jastip.id) },
                                            isEditLoading   = fetchingEditId == jastip.id,
                                            onTutup         = { viewModel.updateJastipStatus(jastip.id, "CLOSED") },
                                            onHapus         = { viewModel.deleteJastip(jastip.id) }
                                        )
                                    }
                                    item { Spacer(Modifier.height(24.dp)) }
                                }
                            }
                        }

                        // ── Tab 1: Request Saya ───────────────────
                        1 -> {
                            val data = state.requests.filter { it.status == "OPEN" }
                            if (data.isEmpty()) {
                                SayaEmptyState(emoji = "📍", title = "Belum ada request aktif",
                                    subtitle = "Buka request dari tab Jastip → Request!")
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(horizontal = Spacing.lg),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    items(data, key = { it.id }) { req ->
                                        JastipRequestSayaCard(
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

                        // ── Tab 2: Selesai / Closed ───────────────
                        2 -> {
                            val listings = state.listings.filter { it.status == "CLOSED" }
                            val requests = state.requests.filter { it.status == "CLOSED" }
                            if (listings.isEmpty() && requests.isEmpty()) {
                                SayaEmptyState(emoji = "✅", title = "Belum ada yang selesai",
                                    subtitle = "Listing & request yang ditutup muncul di sini.")
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(horizontal = Spacing.lg),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    if (listings.isNotEmpty()) {
                                        item {
                                            SayaSectionLabel("📦 LISTING")
                                        }
                                        items(listings, key = { "l_${it.id}" }) { jastip ->
                                            JastipSayaCard(
                                                jastip          = jastip,
                                                isActionLoading = isActionLoading,
                                                onClick         = { onNavigateToDetail(jastip.id) },
                                                onEdit          = { viewModel.fetchJastipForEdit(jastip.id) },
                                                isEditLoading   = fetchingEditId == jastip.id,
                                                onTutup         = null,
                                                onBuka          = { reopenJastip = jastip },
                                                onHapus         = { viewModel.deleteJastip(jastip.id) }
                                            )
                                        }
                                    }
                                    if (requests.isNotEmpty()) {
                                        item {
                                            SayaSectionLabel("📍 REQUEST")
                                        }
                                        items(requests, key = { "r_${it.id}" }) { req ->
                                            JastipRequestSayaCard(
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
        EditJastipSheet(
            item = item,
            categories = categories,
            onDismiss = { viewModel.clearItemForEdit() },
            onSubmit = { title, fromLoc, toLoc, deadline, notes, categoryId, imageUris, existingUrls ->
                viewModel.updateJastip(item.id, title, fromLoc, toLoc, deadline, notes, categoryId, imageUris, existingUrls)
                viewModel.clearItemForEdit()
            }
        )
    }

    editRequest?.let { item ->
        EditRequestSheet(
            item = item,
            categories = categories,
            onDismiss = { editRequest = null },
            onSubmit = { title, fromLoc, toLoc, notes, categoryId ->
                viewModel.updateRequest(item.id, title, fromLoc, toLoc, notes, categoryId)
                editRequest = null
            }
        )
    }

    reopenJastip?.let { item ->
        EditDeadlineDialog(
            currentDeadline = item.deadline,
            onDismiss = { reopenJastip = null },
            onSubmit = { deadline ->
                viewModel.reopenJastip(item.id, deadline)
                reopenJastip = null
            }
        )
    }
}

// ── JASTIP LISTING CARD ───────────────────────────────────────────
@Composable
private fun JastipSayaCard(
    jastip: JastipDto,
    isActionLoading: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    isEditLoading: Boolean = false,
    onTutup: (() -> Unit)?,
    onBuka: (() -> Unit)? = null,
    onHapus: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCloseDialog by remember { mutableStateOf(false) }
    var showReopenDialog by remember { mutableStateOf(false) }
    val deadlineDisplay  = formatDeadlineDisplay(jastip.deadline, includeYear = false)
    val createdAtLabel   = timeAgo(jastip.createdAt)
    val isActive         = jastip.status == "ACTIVE"
    val imageUrl = jastip.primaryImageUrl()

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(Radius.lg),
        colors    = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = jastip.title.ifBlank { "Foto jastip" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(Radius.md)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(Spacing.sm))
            }
            // Rute + status
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(jastip.fromLocation, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = Charcoal, fontFamily = DmSansFamily,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text("  →  ", fontSize = 14.sp, color = Terracotta, fontFamily = DmSansFamily)
                    Text(jastip.toLocation, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = Charcoal, fontFamily = DmSansFamily,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.clip(RoundedCornerShape(Radius.full))
                    .background(if (isActive) SagePale else CreamDark)
                    .padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(if (isActive) "AKTIF" else "TUTUP", fontSize = 9.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                        color = if (isActive) Sage else Charcoal30, fontFamily = DmSansFamily)
                }
            }
            Spacer(Modifier.height(6.dp))
            // Title jika ada
            if (jastip.title.isNotBlank()) {
                Text(jastip.title, fontSize = 13.sp, color = Charcoal60, fontFamily = DmSansFamily,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                JastipSayaChip("⏰ $deadlineDisplay")
                JastipSayaChip("🕐 $createdAtLabel")
            }
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
                    .clickable(enabled = !isActionLoading && !isEditLoading) { onEdit() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center) {
                    if (isEditLoading) {
                        CircularProgressIndicator(
                            color = Gold, strokeWidth = 1.5.dp,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Text("✎ Edit", fontSize = 12.sp, color = Gold, fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Medium)
                    }
                }
                if (onTutup != null) {
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
            title   = "Hapus Jastip?",
            message = "Listing ${jastip.fromLocation} → ${jastip.toLocation} akan dihapus permanen.",
            onConfirm = { showDeleteDialog = false; onHapus() },
            onDismiss = { showDeleteDialog = false }
        )
    }
    if (showCloseDialog && onTutup != null) {
        SayaDeleteDialog(
            title = "Tutup Jastip?",
            message = "Listing ini akan dipindahkan ke selesai dan tidak tampil sebagai jastip aktif.",
            confirmLabel = "Tutup",
            onConfirm = { showCloseDialog = false; onTutup() },
            onDismiss = { showCloseDialog = false }
        )
    }
    if (showReopenDialog && onBuka != null) {
        SayaDeleteDialog(
            title = "Buka Lagi Jastip?",
            message = "Listing ini akan aktif lagi jika limit aktif kamu masih tersedia.",
            confirmLabel = "Buka Lagi",
            onConfirm = { showReopenDialog = false; onBuka() },
            onDismiss = { showReopenDialog = false }
        )
    }
}

// ── JASTIP REQUEST CARD ───────────────────────────────────────────
@Composable
private fun JastipRequestSayaCard(
    request: RequestDto,
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
    val createdAtLabel = timeAgo(request.createdAt ?: request.updatedAt.orEmpty())

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(Radius.lg),
        colors    = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            // Title + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    request.title,
                    fontSize = 15.sp, fontWeight = FontWeight.Medium,
                    color = Charcoal, fontFamily = FrauncesFamily,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.full))
                        .background(if (isOpen) SagePale else CreamDark)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (isOpen) "AKTIF" else "TUTUP",
                        fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                        color = if (isOpen) Sage else Charcoal30, fontFamily = DmSansFamily
                    )
                }
            }
            Spacer(Modifier.height(6.dp))

            // Rute dalam kotak CreamDark (konsisten dengan card lain)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(CreamDark)
                    .padding(horizontal = Spacing.sm, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("DARI", fontSize = 8.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp, color = Charcoal60, fontFamily = DmSansFamily)
                    Text(request.fromLocation, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = Charcoal, fontFamily = DmSansFamily,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("→", fontSize = 16.sp, color = Terracotta,
                    modifier = Modifier.padding(horizontal = 8.dp))
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("KE", fontSize = 8.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp, color = Charcoal60, fontFamily = DmSansFamily)
                    Text(request.toLocation, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = Charcoal, fontFamily = DmSansFamily,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End)
                }
            }
            Spacer(Modifier.height(6.dp))

            // Chips kategori + waktu
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (request.category != null) {
                    JastipSayaChip("${request.category.icon ?: ""} ${request.category.name}".trim())
                }
                JastipSayaChip("🕐 $createdAtLabel")
            }

            Spacer(Modifier.height(Spacing.sm))
            HorizontalDivider(color = Charcoal10, thickness = 0.5.dp)
            Spacer(Modifier.height(Spacing.sm))

            // Action buttons
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
            title   = "Hapus Request?",
            message = "Request \"${request.title}\" akan dihapus permanen.",
            onConfirm = { showDeleteDialog = false; onHapus() },
            onDismiss = { showDeleteDialog = false }
        )
    }
    if (showCloseDialog && onTutup != null) {
        SayaDeleteDialog(
            title = "Tutup Request?",
            message = "Request ini akan dipindahkan ke selesai dan tidak tampil sebagai request aktif.",
            confirmLabel = "Tutup",
            onConfirm = { showCloseDialog = false; onTutup() },
            onDismiss = { showCloseDialog = false }
        )
    }
    if (showReopenDialog && onBuka != null) {
        SayaDeleteDialog(
            title = "Buka Lagi Request?",
            message = "Request ini akan aktif lagi jika limit aktif kamu masih tersedia.",
            confirmLabel = "Buka Lagi",
            onConfirm = { showReopenDialog = false; onBuka() },
            onDismiss = { showReopenDialog = false }
        )
    }
}

// ── SHARED COMPONENTS ─────────────────────────────────────────────
@Composable
internal fun SayaEmptyState(emoji: String, title: String, subtitle: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(Spacing.lg)) {
            Text(emoji, fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(title, fontFamily = FrauncesFamily, color = Charcoal,
                fontSize = 16.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontFamily = DmSansFamily, color = Charcoal60,
                fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
internal fun SayaSectionLabel(text: String) {
    Text(text, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp,
        color = Charcoal60, fontFamily = DmSansFamily,
        modifier = Modifier.padding(top = Spacing.sm, bottom = 4.dp))
}

@Composable
internal fun SayaDeleteDialog(
    title: String, message: String,
    confirmLabel: String = "Hapus",
    onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Cream,
        shape            = RoundedCornerShape(Radius.xl),
        title = { Text(title, fontFamily = FrauncesFamily, fontSize = 20.sp,
            fontWeight = FontWeight.Medium, color = Charcoal) },
        text  = { Text(message, fontFamily = DmSansFamily, fontSize = 13.sp,
            color = Charcoal60, lineHeight = 20.sp) },
        confirmButton = {
            Button(onClick = onConfirm, shape = RoundedCornerShape(Radius.full),
                colors = ButtonDefaults.buttonColors(containerColor = Terracotta)) {
                Text(confirmLabel, fontFamily = DmSansFamily, color = Cream)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", fontFamily = DmSansFamily, color = Charcoal60)
            }
        }
    )
}

@Composable
private fun JastipSayaChip(text: String) {
    Box(modifier = Modifier.clip(RoundedCornerShape(Radius.full)).background(CreamDark)
        .padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(text, fontSize = 11.sp, color = Charcoal60, fontFamily = DmSansFamily)
    }
}
