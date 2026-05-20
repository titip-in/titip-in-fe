package com.titipin.app.ui.preloved

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.titipin.app.data.model.PrelovedRequestDto
import com.titipin.app.data.model.formattedMaxPrice
import com.titipin.app.shared.formatDateDisplay
import com.titipin.app.shared.openWhatsApp
import com.titipin.app.shared.waMessageWanted
import com.titipin.app.ui.components.StatusBadge
import com.titipin.app.ui.components.UserContactPanel
import com.titipin.app.ui.components.SupportPanel
import com.titipin.app.ui.components.LimitReachedDialog
import com.titipin.app.ui.theme.*

@Composable
fun PrelovedRequestDetailScreen(
    requestId: String,
    onBack: () -> Unit = {},
    viewModel: PrelovedRequestViewModel = hiltViewModel()
) {
    val actionState    by viewModel.actionState.collectAsState()
    val listState      by viewModel.listState.collectAsState()
    val categoryState  by viewModel.categoryState.collectAsState()
    val currentUserId  by viewModel.currentUserId.collectAsState()
    val context        = LocalContext.current

    var limitReachedMsg by remember { mutableStateOf<String?>(null) }
    var showEditSheet by remember { mutableStateOf(false) }

    // Cari item dari list state (detail endpoint diakses via list / cache)
    var request by remember { mutableStateOf<PrelovedRequestDto?>(null) }

    LaunchedEffect(requestId, listState) {
        if (listState is PrelovedRequestListState.Success) {
            request = (listState as PrelovedRequestListState.Success).data.find { it.id == requestId }
        }
        // Jika list kosong, trigger load
        if (listState is PrelovedRequestListState.Error || request == null) {
            viewModel.loadPrelovedRequestList()
        }
    }

    LaunchedEffect(actionState) {
        when (actionState) {
            is PrelovedRequestActionState.Success -> {
                val success = actionState as PrelovedRequestActionState.Success
                if (success.data == null) {
                    // Delete success → kembali
                    viewModel.resetActionState()
                    onBack()
                } else {
                    showEditSheet = false
                    viewModel.resetActionState()
                    viewModel.loadPrelovedRequestList()
                }
            }
            is PrelovedRequestActionState.LimitReached -> {
                limitReachedMsg = (actionState as PrelovedRequestActionState.LimitReached).message
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
    ) {
        // ── Toolbar ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text("←", fontSize = 20.sp, color = Charcoal)
            }
            Text(
                text = "DETAIL PENCARIAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                fontFamily = DmSansFamily,
                color = Charcoal60
            )
        }

        when {
            request == null && listState is PrelovedRequestListState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
                }
            }
            request == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("😕", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Data tidak ditemukan", color = Charcoal60, fontFamily = DmSansFamily, fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = onBack) {
                            Text("Kembali", color = Terracotta, fontFamily = DmSansFamily)
                        }
                    }
                }
            }
            else -> {
                val item = request!!
                val formattedBudget = item.formattedMaxPrice()
                val initials = item.user.name.trim().split(" ")
                    .filter { it.isNotBlank() }.take(2)
                    .joinToString("") { it.first().uppercase() }
                val waMessage = waMessageWanted(item.title)

                // ── Hero box (kategori / budget info) ─────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerracottaPale)
                        .padding(Spacing.lg),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar besar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Cream.copy(alpha = 0.8f)),
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
                                Text(
                                    initials,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Terracotta,
                                    fontFamily = DmSansFamily
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.user.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Charcoal,
                                fontFamily = DmSansFamily
                            )
                            Text(
                                text = "sedang mencari barang",
                                fontSize = 11.sp,
                                color = Charcoal60,
                                fontFamily = DmSansFamily
                            )
                            Spacer(Modifier.height(4.dp))
                            StatusBadge(status = item.status)
                        }
                    }
                }

                // ── Scrollable konten ──────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    // ── Judul ───────────────────────────────────────
                    Text(
                        text = item.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = Charcoal,
                        fontFamily = FrauncesFamily,
                        lineHeight = 30.sp
                    )

                    // ── Chips info ──────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (formattedBudget != null) {
                            PrelovedRequestDetailChip("💰 $formattedBudget")
                        }
                        if (item.category != null) {
                            val emoji = item.category.icon ?: categoryEmojiFor(item.category.name)
                            PrelovedRequestDetailChip("$emoji ${item.category.name}")
                        }
                        if (!item.createdAt.isNullOrBlank()) {
                            PrelovedRequestDetailChip("📅 ${formatDateDisplay(item.createdAt, includeYear = true)}")
                        }
                    }

                    // ── Deskripsi ───────────────────────────────────
                    if (!item.description.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.md))
                                .background(CreamDark)
                                .padding(Spacing.md)
                        ) {
                            Column {
                                Text(
                                    "DESKRIPSI",
                                    fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp, color = Charcoal60, fontFamily = DmSansFamily
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "\"${item.description}\"",
                                    fontSize = 13.sp, color = Charcoal,
                                    fontFamily = DmSansFamily, fontStyle = FontStyle.Italic,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    // ── Info banner ─────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(TerracottaPale)
                            .padding(Spacing.md),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Text("💡", fontSize = 14.sp)
                    Text(
                        text = "Kamu punya barang yang cocok? Hubungi langsung lewat WhatsApp!",
                        fontFamily = DmSansFamily,
                        fontSize = 12.sp, color = Charcoal, lineHeight = 18.sp
                    )
                }

                // Call UserContactPanel to show the user info and chat button
                UserContactPanel(
                    name = item.user.name,
                    waNumber = item.user.waNumber,
                    avatarUrl = item.user.avatarUrl,
                    status = item.user.status,
                    isOwner = currentUserId == item.userId?.toString(),
                    ownerLabel = "Ini request Anda",
                    message = waMessage,
                    onChatWaClick = { viewModel.trackPrelovedRequestClick(item.id) }
                )

                val isOwner = currentUserId == item.userId?.toString()

                if (isOwner) {
                        Spacer(Modifier.height(Spacing.sm))
                        OwnerPanel(
                            status = item.status,
                            isLoading = actionState is PrelovedRequestActionState.Loading,
                            onToggleStatus = {
                                viewModel.toggleStatus(item.id, item.status)
                            },
                            onEdit = { showEditSheet = true },
                            onDelete = { viewModel.deletePrelovedRequest(item.id) }
                        )
                    }

                    Spacer(Modifier.height(Spacing.xl))

                    SupportPanel()

                    Spacer(Modifier.height(100.dp))
                }

                // ── CTA bar ────────────────────────────────────────
                HorizontalDivider(color = Charcoal10, thickness = 0.5.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = {
                            viewModel.trackPrelovedRequestClick(item.id)
                            openWhatsApp(context, item.user.waNumber, waMessage)
                        },
                        modifier = Modifier.fillMaxWidth().height(ComponentSize.buttonHeight),
                        enabled  = item.user.waNumber.isNotBlank() && item.status == "OPEN",
                        shape    = RoundedCornerShape(Radius.full),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor         = Terracotta,
                            contentColor           = Cream,
                            disabledContainerColor = Charcoal10,
                            disabledContentColor   = Charcoal30
                        )
                    ) {
                        Text(
                            text = when {
                                item.status == "CLOSED" -> "Pencarian ditutup"
                                item.user.waNumber.isBlank() -> "Kontak tidak tersedia"
                                else -> "💬 Hubungi via WhatsApp"
                            },
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = DmSansFamily
                        )
                    }
                }
            }
        }
    }

    if (limitReachedMsg != null) {
        LimitReachedDialog(
            message = limitReachedMsg.orEmpty(),
            onDismiss = { limitReachedMsg = null }
        )
    }

    if (showEditSheet && request != null) {
        EditPrelovedRequestSheet(
            item = request!!,
            categories = (categoryState as? PrelovedRequestCategoryState.Success)?.data.orEmpty(),
            onDismiss = { showEditSheet = false },
            onSubmit = { title, description, maxPrice, categoryId ->
                viewModel.updatePrelovedRequestFields(request!!.id, title, description, maxPrice, categoryId)
            }
        )
    }
}

@Composable
private fun PrelovedRequestDetailChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.full))
            .background(CreamDark)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 12.sp, color = Charcoal60, fontFamily = DmSansFamily)
    }
}

@Composable
private fun OwnerPanel(
    status: String,
    isLoading: Boolean,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showStatusConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(CreamDark)
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "KELOLA PENCARIAN",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = Charcoal60,
            fontFamily = DmSansFamily
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { showStatusConfirm = true },
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(Radius.full),
                colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Cream)
            ) {
                Text(
                    text = if (status == "OPEN") "Tutup" else "Buka",
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            OutlinedButton(
                onClick = onEdit,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(Radius.full)
            ) {
                Text("Edit", fontFamily = DmSansFamily, fontSize = 12.sp, color = Charcoal)
            }
            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(Radius.full)
            ) {
                Text("Hapus", fontFamily = DmSansFamily, fontSize = 12.sp, color = Terracotta)
            }
        }
    }

    if (showStatusConfirm) {
        val isReopen = status == "CLOSED"
        AlertDialog(
            onDismissRequest = { showStatusConfirm = false },
            containerColor = Cream,
            title = { Text("Ubah Status?", fontFamily = FrauncesFamily, color = Charcoal) },
            text = { Text(if (isReopen) "Buka kembali pencarian barang ini?" else "Tutup pencarian barang ini?", fontFamily = DmSansFamily) },
            confirmButton = {
                TextButton(onClick = {
                    showStatusConfirm = false
                    onToggleStatus()
                }) {
                    Text("Ya, Lanjutkan", color = Terracotta, fontFamily = DmSansFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusConfirm = false }) { Text("Batal", color = Charcoal60, fontFamily = DmSansFamily) }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Pencarian?", fontFamily = FrauncesFamily, color = Charcoal) },
            text = { Text("Pencarian yang dihapus tidak bisa dikembalikan.", fontFamily = DmSansFamily) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text("Hapus", color = Terracotta, fontFamily = DmSansFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal", color = Charcoal60, fontFamily = DmSansFamily)
                }
            }
        )
    }
}
