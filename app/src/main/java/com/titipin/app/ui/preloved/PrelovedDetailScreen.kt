package com.titipin.app.ui.preloved

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.data.model.UserTier
import coil.compose.AsyncImage
import com.titipin.app.data.model.conditionLabel
import com.titipin.app.data.model.formattedPrice
import com.titipin.app.data.model.normalizedTier
import com.titipin.app.data.model.primaryImageUrl
import com.titipin.app.data.model.tierBoostLimit
import com.titipin.app.shared.openWhatsApp
import com.titipin.app.shared.waMessagePreloved
import com.titipin.app.ui.components.BoostedBadge
import com.titipin.app.ui.components.LimitReachedDialog
import com.titipin.app.ui.components.UserContactPanel
import com.titipin.app.ui.components.SupportPanel
import com.titipin.app.ui.components.DetailImageGallery
import com.titipin.app.ui.theme.*

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PrelovedDetailScreen(
    prelovedId: String,
    onBack: () -> Unit = {},
    viewModel: PrelovedViewModel = hiltViewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val categoryState by viewModel.categoryState.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUserTier by viewModel.currentUserTier.collectAsState()
    val currentBoostQuota by viewModel.currentBoostQuota.collectAsState()
    val context = LocalContext.current

    var limitReachedMsg by remember { mutableStateOf<String?>(null) }
    var showEditSheet by remember { mutableStateOf(false) }

    LaunchedEffect(prelovedId) { viewModel.loadDetail(prelovedId) }
    LaunchedEffect(actionState) {
        when (actionState) {
            is PrelovedActionState.Success -> {
                if ((actionState as PrelovedActionState.Success).data == null) {
                    viewModel.resetActionState()
                    onBack()
                    return@LaunchedEffect
                }
                showEditSheet = false
                viewModel.resetActionState()
                viewModel.loadDetail(prelovedId)
            }
            is PrelovedActionState.LimitReached -> {
                limitReachedMsg = (actionState as PrelovedActionState.LimitReached).message
                viewModel.resetActionState()
            }
            is PrelovedActionState.Error -> {
                limitReachedMsg = (actionState as PrelovedActionState.Error).message
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
                text = "DETAIL PRELOVED",
                fontSize = 11.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp, fontFamily = DmSansFamily, color = Charcoal60
            )
        }

        when (val state = detailState) {
            is PrelovedActionState.Loading, is PrelovedActionState.Idle -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
                }
            }
            is PrelovedActionState.Error, is PrelovedActionState.LimitReached -> {
                val errorMsg = if (state is PrelovedActionState.Error) state.message else (state as PrelovedActionState.LimitReached).message
                Box(Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) {
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
            is PrelovedActionState.Success -> {
                val item = state.data ?: return@Column
                val categoryLabel = item.category?.name ?: "Lainnya"
                val categoryEmoji = item.category?.icon ?: categoryEmojiFor(categoryLabel)
                val isOwner = currentUserId == item.userId?.toString()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Spacer(Modifier.height(Spacing.sm))

                    DetailImageGallery(
                        images = item.images.orEmpty(),
                        contentDescription = item.title
                    )

                    Spacer(Modifier.height(Spacing.sm))
                    // Judul + Harga
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = item.title,
                                fontSize = 20.sp, fontWeight = FontWeight.Medium,
                                color = Charcoal, fontFamily = FrauncesFamily, lineHeight = 26.sp
                            )
                            Text(
                                text = item.formattedPrice(),
                                fontSize = 22.sp, fontWeight = FontWeight.Bold,
                                color = Terracotta, fontFamily = DmSansFamily
                            )
                        }
                    }

                    // Tags
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PrelovedTag("$categoryEmoji $categoryLabel")
                        PrelovedTag(item.conditionLabel().uppercase())
                        if (item.status == "AVAILABLE") PrelovedTag("✓ Tersedia")
                        else if (item.status == "SOLD")  PrelovedTag("✕ Terjual")
                        else if (item.status == "CLOSED") PrelovedTag("Ditutup")
                        if (!item.boostedAt.isNullOrBlank()) BoostedBadge()
                    }

                    UserContactPanel(
                        name = item.user.name,
                        waNumber = item.user.waNumber,
                        avatarUrl = item.user.avatarUrl,
                        status = item.user.status,
                        isOwner = isOwner,
                        ownerLabel = "Ini barang Anda",
                        message = waMessagePreloved(item.title, item.formattedPrice()),
                        onChatWaClick = { viewModel.trackPrelovedClick(item.id) }
                    )

                    // Deskripsi
                    if (!item.description.isNullOrEmpty()) {
                        Column {
                            Text(
                                "DESKRIPSI", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp, color = Charcoal60, fontFamily = DmSansFamily,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Text(
                                item.description, fontSize = 13.sp, color = Charcoal,
                                fontFamily = DmSansFamily, lineHeight = 20.sp
                            )
                        }
                    }

                    if (isOwner) {
                        PrelovedOwnerPanel(
                            status = item.status,
                            isLoading = actionState is PrelovedActionState.Loading,
                            tier = currentUserTier,
                            boostQuota = currentBoostQuota,
                            isBoosted = !item.boostedAt.isNullOrBlank(),
                            onBoost = { viewModel.boostPreloved(item.id) },
                            onMarkSold = { viewModel.updateStatus(item.id, "SOLD") },
                            onToggleClosed = {
                                val nextStatus = if (item.status == "AVAILABLE") "CLOSED" else "AVAILABLE"
                                viewModel.updateStatus(item.id, nextStatus)
                            },
                            onEdit = { showEditSheet = true },
                            onDelete = { viewModel.deletePreloved(item.id) }
                        )
                    }

                    Spacer(Modifier.height(Spacing.xl))

                    SupportPanel()

                    Spacer(Modifier.height(100.dp))
                }

                // ── CTA ───────────────────────────────────────────
                HorizontalDivider(color = Charcoal10, thickness = 0.5.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = {
                            viewModel.trackPrelovedClick(item.id)
                            openWhatsApp(
                                context, item.user.waNumber,
                                waMessagePreloved(item.title, item.formattedPrice())
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(ComponentSize.buttonHeight),
                        enabled = !isOwner && item.status == "AVAILABLE" && item.user.waNumber.isNotBlank(),
                        shape   = RoundedCornerShape(Radius.full),
                        colors  = ButtonDefaults.buttonColors(
                            containerColor         = Terracotta,
                            contentColor           = Cream,
                            disabledContainerColor = Charcoal10,
                            disabledContentColor   = Charcoal30
                        )
                    ) {
                        Text(
                            text = if (isOwner) "Ini barang Anda" else if (item.status == "AVAILABLE") "💬 Chat via WhatsApp" else "Barang tidak tersedia",
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

    if (showEditSheet && detailState is PrelovedActionState.Success) {
        val item = (detailState as PrelovedActionState.Success).data
        if (item != null) {
            EditPrelovedSheet(
                item = item,
                categories = (categoryState as? PrelovedCategoryState.Success)?.data.orEmpty(),
                onDismiss = { showEditSheet = false },
                onSubmit = { title, price, condition, description, categoryId, imageUris, existingUrls ->
                    viewModel.updatePreloved(item.id, title, price, condition, description, categoryId, imageUris, existingUrls)
                }
            )
        }
    }
}

@Composable
private fun PrelovedTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.full))
            .background(CreamDark)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 11.sp, color = Charcoal60, fontFamily = DmSansFamily)
    }
}

@Composable
private fun PrelovedOwnerPanel(
    status: String,
    isLoading: Boolean,
    tier: String,
    boostQuota: Int,
    isBoosted: Boolean,
    onBoost: () -> Unit,
    onMarkSold: () -> Unit,
    onToggleClosed: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showStatusConfirm by remember { mutableStateOf<String?>(null) } // null, "SOLD", "CLOSED", "AVAILABLE"
    var showBoostConfirm by remember { mutableStateOf(false) }
    var showUpgradeInfo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(CreamDark)
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "KELOLA BARANG",
            fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp,
            color = Charcoal60, fontFamily = DmSansFamily
        )
        Button(
            onClick = {
                if (tier.normalizedTier() == UserTier.BASIC || tierBoostLimit(tier) == 0 || boostQuota <= 0) {
                    showUpgradeInfo = true
                } else {
                    showBoostConfirm = true
                }
            },
            enabled = !isLoading && status == "AVAILABLE",
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Radius.full),
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Cream)
        ) {
            Text(
                text = if (isBoosted) "Boost Ulang Barang" else "Boost Barang",
                fontFamily = DmSansFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (status == "AVAILABLE") {
                Button(
                    onClick = { showStatusConfirm = "SOLD" },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Radius.full),
                    colors = ButtonDefaults.buttonColors(containerColor = Sage, contentColor = Cream)
                ) {
                    Text("Tandai Terjual", fontFamily = DmSansFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            if (status != "AVAILABLE") {
                Button(
                    onClick = { showStatusConfirm = "AVAILABLE" },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Radius.full),
                    colors = ButtonDefaults.buttonColors(containerColor = Sage, contentColor = Cream)
                ) {
                    Text("Buka Lagi", fontFamily = DmSansFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Button(
                    onClick = { showStatusConfirm = "CLOSED" },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Radius.full),
                    colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Cream)
                ) {
                    Text("Tutup", fontFamily = DmSansFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

    if (showBoostConfirm) {
        AlertDialog(
            onDismissRequest = { showBoostConfirm = false },
            containerColor = Cream,
            title = { Text(if (isBoosted) "Boost Ulang Barang?" else "Boost Barang?", fontFamily = FrauncesFamily, color = Charcoal) },
            text = { Text("Aksi ini memakai 1 kuota boost dan menaikkan barang ke prioritas atas.", fontFamily = DmSansFamily) },
            confirmButton = {
                TextButton(onClick = {
                    showBoostConfirm = false
                    onBoost()
                }) {
                    Text("Boost", color = Terracotta, fontFamily = DmSansFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBoostConfirm = false }) {
                    Text("Batal", color = Charcoal60, fontFamily = DmSansFamily)
                }
            }
        )
    }

    if (showUpgradeInfo) {
        AlertDialog(
            onDismissRequest = { showUpgradeInfo = false },
            containerColor = Cream,
            title = { Text("Upgrade untuk Boost", fontFamily = FrauncesFamily, color = Charcoal) },
            text = { Text("Boost tersedia untuk Titip Plus dan Pro. Cek halaman Profil untuk upgrade via WhatsApp admin.", fontFamily = DmSansFamily) },
            confirmButton = {
                TextButton(onClick = { showUpgradeInfo = false }) {
                    Text("Mengerti", color = Terracotta, fontFamily = DmSansFamily)
                }
            }
        )
    }

    if (showStatusConfirm != null) {
        val actionText = when (showStatusConfirm) {
            "SOLD" -> "Tandai barang ini sudah terjual?"
            "CLOSED" -> "Tutup sementara listing barang ini?"
            else -> "Buka kembali listing barang ini?"
        }
        AlertDialog(
            onDismissRequest = { showStatusConfirm = null },
            containerColor = Cream,
            title = { Text("Ubah Status?", fontFamily = FrauncesFamily, color = Charcoal) },
            text = { Text(actionText, fontFamily = DmSansFamily) },
            confirmButton = {
                TextButton(onClick = {
                    val target = showStatusConfirm
                    showStatusConfirm = null
                    if (target == "SOLD") onMarkSold() else onToggleClosed()
                }) {
                    Text("Ya, Lanjutkan", color = Terracotta, fontFamily = DmSansFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusConfirm = null }) { Text("Batal", color = Charcoal60, fontFamily = DmSansFamily) }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Barang?", fontFamily = FrauncesFamily, color = Charcoal) },
            text = { Text("Barang yang dihapus tidak bisa dikembalikan.", fontFamily = DmSansFamily) },
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
