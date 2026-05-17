package com.titipin.app.ui.jastip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.shared.formatDateDisplay
import com.titipin.app.shared.formatDeadlineDisplay
import com.titipin.app.shared.openWhatsApp
import com.titipin.app.shared.waMessageJastipDetail
import com.titipin.app.shared.formatTimeDisplay
import com.titipin.app.ui.components.DetailImageGallery
import com.titipin.app.ui.components.LimitReachedDialog
import com.titipin.app.ui.components.StatusBadge
import com.titipin.app.ui.components.UserContactPanel
import com.titipin.app.ui.components.SupportPanel
import com.titipin.app.ui.theme.*

@Composable
fun JastipDetailScreen(
    jastipId: String,
    onBack: () -> Unit = {},
    viewModel: JastipViewModel = hiltViewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val categoryState by viewModel.categoryState.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val context = LocalContext.current

    var limitReachedMsg by remember { mutableStateOf<String?>(null) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showReopenDialog by remember { mutableStateOf(false) }

    LaunchedEffect(jastipId) { viewModel.loadDetail(jastipId) }
    LaunchedEffect(actionState) {
        when (actionState) {
            is JastipActionState.Success -> {
                if ((actionState as JastipActionState.Success).data == null) {
                    viewModel.resetActionState()
                    onBack()
                    return@LaunchedEffect
                }
                showEditSheet = false
                showReopenDialog = false
                viewModel.resetActionState()
                viewModel.loadDetail(jastipId)
            }
            is JastipActionState.LimitReached -> {
                limitReachedMsg = (actionState as JastipActionState.LimitReached).message
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
        // ── TOP BAR ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali", tint = Charcoal)
            }
            Text(
                text = "DETAIL JASTIP",
                fontSize = 11.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp, fontFamily = DmSansFamily, color = Charcoal60
            )
        }

        when (val state = detailState) {
            is JastipActionState.Loading, is JastipActionState.Idle -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
                }
            }
            is JastipActionState.Error, is JastipActionState.LimitReached -> {
                val errorMsg = if (state is JastipActionState.Error) state.message else (state as JastipActionState.LimitReached).message
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("😕", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(errorMsg, color = Charcoal60, fontFamily = DmSansFamily, fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = onBack) {
                            Text("Kembali", color = Terracotta, fontFamily = DmSansFamily)
                        }
                    }
                }
            }
            is JastipActionState.Success -> {
                val jastip = state.data ?: return@Column
                val deadlineTime = formatTimeDisplay(jastip.deadline)
                val deadlineDate = formatDateDisplay(jastip.deadline, includeYear = true)
                val isOwner = currentUserId == jastip.userId

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.lg)
                ) {
                    Spacer(Modifier.height(Spacing.sm))

                    DetailImageGallery(
                        images = jastip.images.orEmpty(),
                        contentDescription = jastip.title.ifBlank { "Foto jastip" }
                    )

                    Spacer(Modifier.height(Spacing.md))

                    Text(
                        text = jastip.title.ifBlank { "${jastip.fromLocation} → ${jastip.toLocation}" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = Charcoal,
                        fontFamily = FrauncesFamily,
                        lineHeight = 30.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(status = jastip.status)
                        if (jastip.category != null) {
                            Text(
                                text = listOfNotNull(jastip.category.icon, jastip.category.name).joinToString(" "),
                                fontSize = 12.sp,
                                color = Charcoal60,
                                fontFamily = DmSansFamily
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.md))

                    // ── RUTE CARD ─────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.lg))
                            .background(Charcoal)
                            .padding(Spacing.lg)
                    ) {
                        Column {
                            Text("RUTE JASTIP", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Cream.copy(alpha = 0.35f), fontFamily = DmSansFamily)
                            Spacer(Modifier.height(14.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(jastip.fromLocation, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Cream, fontFamily = DmSansFamily)
                                    Text("Titik berangkat", fontSize = 10.sp, color = Cream.copy(alpha = 0.4f), fontFamily = DmSansFamily)
                                }
                                Text("→", fontSize = 22.sp, color = Terracotta, modifier = Modifier.padding(horizontal = 8.dp))
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text(jastip.toLocation, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Cream, fontFamily = DmSansFamily)
                                    Text("Tujuan", fontSize = 10.sp, color = Cream.copy(alpha = 0.4f), fontFamily = DmSansFamily)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.sm))

                    // ── INFO TILES ────────────────────────────────
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
                        InfoTile("📅", "TANGGAL", deadlineDate, Modifier.weight(1f))
                        InfoTile("⏰", "BATAS NITIP", deadlineTime, Modifier.weight(1f))
                    }

                    if (!jastip.notes.isNullOrEmpty()) {
                        Spacer(Modifier.height(Spacing.sm))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.md))
                                .background(CreamDark)
                                .padding(Spacing.md)
                        ) {
                            Column {
                                Text("CATATAN", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Charcoal60, fontFamily = DmSansFamily)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "\"${jastip.notes}\"",
                                    fontSize = 13.sp, color = Charcoal,
                                    fontFamily = DmSansFamily, fontStyle = FontStyle.Italic, lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.sm))

                    UserContactPanel(
                        name = jastip.user.name,
                        waNumber = jastip.user.waNumber,
                        avatarUrl = jastip.user.avatarUrl,
                        status = jastip.user.status,
                        isOwner = isOwner,
                        message = waMessageJastipDetail(
                            jastip.fromLocation,
                            jastip.toLocation,
                            formatDeadlineDisplay(jastip.deadline, includeYear = false)
                        )
                    )

                    if (isOwner) {
                        Spacer(Modifier.height(Spacing.sm))
                        OwnerPanel(
                            status = jastip.status,
                            isLoading = actionState is JastipActionState.Loading,
                            onToggleStatus = {
                                if (jastip.status == "CLOSED") {
                                    showReopenDialog = true
                                } else {
                                    viewModel.updateStatus(jastip.id, "CLOSED")
                                }
                            },
                            onEdit = { showEditSheet = true },
                            onDelete = { viewModel.deleteJastip(jastip.id) }
                        )
                    }

                    Spacer(Modifier.height(Spacing.xl))

                    SupportPanel()

                    Spacer(Modifier.height(100.dp))
                }

                // ── BOTTOM CTA ────────────────────────────────────
                HorizontalDivider(color = Charcoal10, thickness = 0.5.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = {
                            if (!isOwner) {
                                openWhatsApp(
                                    context, jastip.user.waNumber,
                                    waMessageJastipDetail(
                                        jastip.fromLocation,
                                        jastip.toLocation,
                                        formatDeadlineDisplay(jastip.deadline, includeYear = false)
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ComponentSize.buttonHeight),
                        enabled = !isOwner && jastip.user.waNumber.isNotBlank(),
                        shape  = RoundedCornerShape(Radius.full),
                        colors = ButtonDefaults.buttonColors(containerColor = Sage, contentColor = Cream)
                    ) {
                        Text(
                            text = if (isOwner) "Ini listing Anda" else "💬 Hubungi via WhatsApp",
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

    if (showReopenDialog && detailState is JastipActionState.Success) {
        val jastip = (detailState as JastipActionState.Success).data
        if (jastip != null) {
            EditDeadlineDialog(
                currentDeadline = jastip.deadline,
                onDismiss = { showReopenDialog = false },
                onSubmit = { newDeadline ->
                    viewModel.reopenJastip(jastip.id, newDeadline)
                }
            )
        }
    }

    if (showEditSheet && detailState is JastipActionState.Success) {
        val jastip = (detailState as JastipActionState.Success).data
        if (jastip != null) {
            EditJastipSheet(
                item = jastip,
                categories = (categoryState as? JastipCategoryState.Success)?.data.orEmpty(),
                onDismiss = { showEditSheet = false },
                onSubmit = { title, fromLoc, toLoc, deadline, notes, categoryId, imageUris, existingUrls ->
                    viewModel.updateJastip(jastip.id, title, fromLoc, toLoc, deadline, notes, categoryId, imageUris, existingUrls)
                }
            )
        }
    }
}

// ── INFO TILE ─────────────────────────────────────────────────────
@Composable
fun InfoTile(emoji: String, label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.md))
            .background(CreamDark)
            .padding(Spacing.md)
    ) {
        Column {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Charcoal60, fontFamily = DmSansFamily)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Charcoal, fontFamily = DmSansFamily)
        }
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
            text = "KELOLA LISTING",
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
                    text = if (status == "ACTIVE") "Tutup" else "Buka",
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
            text = { Text(if (isReopen) "Buka kembali listing Jastip ini?" else "Tutup listing Jastip ini?", fontFamily = DmSansFamily) },
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
            title = { Text("Hapus listing?", fontFamily = FrauncesFamily, color = Charcoal) },
            text = { Text("Listing yang dihapus tidak bisa dikembalikan.", fontFamily = DmSansFamily) },
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
