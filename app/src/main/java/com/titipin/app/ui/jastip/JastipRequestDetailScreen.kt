package com.titipin.app.ui.jastip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.titipin.app.shared.openWhatsApp
import com.titipin.app.shared.waMessageTakeRequest
import com.titipin.app.ui.components.LimitReachedDialog
import com.titipin.app.ui.components.StatusBadge
import com.titipin.app.ui.components.UserContactPanel
import com.titipin.app.ui.components.SupportPanel
import com.titipin.app.ui.theme.Charcoal
import com.titipin.app.ui.theme.Charcoal10
import com.titipin.app.ui.theme.Charcoal60
import com.titipin.app.ui.theme.ComponentSize
import com.titipin.app.ui.theme.Cream
import com.titipin.app.ui.theme.CreamDark
import com.titipin.app.ui.theme.DmSansFamily
import com.titipin.app.ui.theme.FrauncesFamily
import com.titipin.app.ui.theme.Radius
import com.titipin.app.ui.theme.Sage
import com.titipin.app.ui.theme.Spacing
import com.titipin.app.ui.theme.Terracotta

@Composable
fun JastipRequestDetailScreen(
    requestId: String,
    onBack: () -> Unit = {},
    viewModel: RequestViewModel = hiltViewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val categoryState by viewModel.categoryState.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val context = LocalContext.current

    var limitReachedMsg by remember { mutableStateOf<String?>(null) }
    var showEditSheet by remember { mutableStateOf(false) }

    LaunchedEffect(requestId) { viewModel.loadDetail(requestId) }
    LaunchedEffect(actionState) {
        when (actionState) {
            is RequestActionState.Success -> {
                if ((actionState as RequestActionState.Success).data == null) {
                    viewModel.resetActionState()
                    onBack()
                    return@LaunchedEffect
                }
                showEditSheet = false
                viewModel.resetActionState()
                viewModel.loadDetail(requestId)
            }
            is RequestActionState.LimitReached -> {
                limitReachedMsg = (actionState as RequestActionState.LimitReached).message
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
                text = "DETAIL REQUEST",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                fontFamily = DmSansFamily,
                color = Charcoal60
            )
        }

        when (val state = detailState) {
            is RequestActionState.Loading,
            is RequestActionState.Idle,
            is RequestActionState.FeatureInProgress -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
                }
            }
            is RequestActionState.Error, is RequestActionState.LimitReached -> {
                val errorMsg = if (state is RequestActionState.Error) state.message else (state as RequestActionState.LimitReached).message
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
            is RequestActionState.Success -> {
                val request = state.data ?: return@Column
                val isOwner = currentUserId == request.userId?.toString()
                val message = waMessageTakeRequest(request.fromLocation, request.toLocation)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.lg)
                ) {
                    Spacer(Modifier.height(Spacing.sm))

                    Text(
                        text = request.title,
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
                        StatusBadge(status = request.status)
                        if (request.category != null) {
                            Text(
                                text = listOfNotNull(request.category.icon, request.category.name).joinToString(" "),
                                fontSize = 12.sp,
                                color = Charcoal60,
                                fontFamily = DmSansFamily
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.md))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.lg))
                            .background(Charcoal)
                            .padding(Spacing.lg)
                    ) {
                        Column {
                            Text(
                                text = "RUTE REQUEST",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = Cream.copy(alpha = 0.35f),
                                fontFamily = DmSansFamily
                            )
                            Spacer(Modifier.height(14.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(request.fromLocation, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Cream, fontFamily = DmSansFamily)
                                    Text("Titik pengambilan", fontSize = 10.sp, color = Cream.copy(alpha = 0.4f), fontFamily = DmSansFamily)
                                }
                                Text("→", fontSize = 22.sp, color = Terracotta, modifier = Modifier.padding(horizontal = 8.dp))
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text(request.toLocation, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Cream, fontFamily = DmSansFamily)
                                    Text("Tujuan", fontSize = 10.sp, color = Cream.copy(alpha = 0.4f), fontFamily = DmSansFamily)
                                }
                            }
                        }
                    }

                    if (!request.createdAt.isNullOrBlank()) {
                        Spacer(Modifier.height(Spacing.sm))
                        InfoTile("📅", "DIPOSTING", formatDateDisplay(request.createdAt, includeYear = true))
                    }

                    if (!request.notes.isNullOrEmpty()) {
                        Spacer(Modifier.height(Spacing.sm))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.md))
                                .background(CreamDark)
                                .padding(Spacing.md)
                        ) {
                            Column {
                                Text("DESKRIPSI", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Charcoal60, fontFamily = DmSansFamily)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "\"${request.notes}\"",
                                    fontSize = 13.sp,
                                    color = Charcoal,
                                    fontFamily = DmSansFamily,
                                    fontStyle = FontStyle.Italic,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.sm))

                    UserContactPanel(
                        name = request.user.name,
                        waNumber = request.user.waNumber,
                        avatarUrl = request.user.avatarUrl,
                        status = request.user.status,
                        isOwner = isOwner,
                        ownerLabel = "Ini request Anda",
                        message = message
                    )

                    if (isOwner) {
                        Spacer(Modifier.height(Spacing.sm))
                        OwnerPanel(
                            status = request.status,
                            isLoading = actionState is RequestActionState.Loading,
                            onToggleStatus = {
                                val nextStatus = if (request.status == "OPEN") "CLOSED" else "OPEN"
                                viewModel.updateStatus(request.id, nextStatus)
                            },
                            onEdit = { showEditSheet = true },
                            onDelete = { viewModel.deleteRequest(request.id) }
                        )
                    }

                    Spacer(Modifier.height(Spacing.xl))

                    SupportPanel()

                    Spacer(Modifier.height(100.dp))
                }

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
                                openWhatsApp(context, request.user.waNumber, message)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ComponentSize.buttonHeight),
                        enabled = !isOwner && request.user.waNumber.isNotBlank(),
                        shape = RoundedCornerShape(Radius.full),
                        colors = ButtonDefaults.buttonColors(containerColor = Sage, contentColor = Cream)
                    ) {
                        Text(
                            text = if (isOwner) "Ini request Anda" else "💬 Hubungi via WhatsApp",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = DmSansFamily
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

    if (showEditSheet && detailState is RequestActionState.Success) {
        val request = (detailState as RequestActionState.Success).data
        if (request != null) {
            EditRequestSheet(
                item = request,
                categories = (categoryState as? RequestCategoryState.Success)?.data.orEmpty(),
                onDismiss = { showEditSheet = false },
                onSubmit = { title, fromLoc, toLoc, notes, categoryId ->
                    viewModel.updateRequestFields(request.id, title, fromLoc, toLoc, notes, categoryId)
                }
            )
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
            text = "KELOLA REQUEST",
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
            text = { Text(if (isReopen) "Buka kembali request Jastip ini?" else "Tutup request Jastip ini?", fontFamily = DmSansFamily) },
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
            title = { Text("Hapus request?", fontFamily = FrauncesFamily, color = Charcoal) },
            text = { Text("Request yang dihapus tidak bisa dikembalikan.", fontFamily = DmSansFamily) },
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
