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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.titipin.app.data.model.conditionLabel
import com.titipin.app.data.model.formattedPrice
import com.titipin.app.data.model.primaryImageUrl
import com.titipin.app.shared.openWhatsApp
import com.titipin.app.shared.waMessagePreloved
import com.titipin.app.ui.components.LimitReachedDialog
import com.titipin.app.ui.components.UserContactPanel
import com.titipin.app.ui.components.SupportPanel
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
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
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
                val primaryImageUrl = item.primaryImageUrl()
                val isOwner = currentUserId == item.userId?.toString()

                val heroBg = when (item.condition) {
                    "NEW", "LIKE_NEW" -> SagePale
                    "GOOD"            -> GoldPale
                    else              -> TerracottaPale
                }

                // Simulasi beberapa foto (nanti dari imageUrl)
                // Sekarang 1 halaman saja karena BE return 1 imageUrl
                val pageCount = 1
                val pagerState = rememberPagerState { pageCount }

                // ── HERO — CAROUSEL ───────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(heroBg),
                            contentAlignment = Alignment.Center
                        ) {
                            if (primaryImageUrl.isNullOrBlank()) {
                                Text(categoryEmoji, fontSize = 80.sp)
                            } else {
                                AsyncImage(
                                    model = primaryImageUrl,
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // Back button
                    Box(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Cream.copy(alpha = 0.9f))
                            .clickable { onBack() }
                            .align(Alignment.TopStart),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("←", fontSize = 16.sp, color = Charcoal)
                    }

                    // Kondisi badge kanan atas
                    val (condBg, condColor) = when (item.condition) {
                        "NEW", "LIKE_NEW" -> SagePale to Sage
                        else -> CreamDark to Charcoal60
                    }
                    Box(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(12.dp)
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(Radius.full))
                            .background(condBg)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            item.conditionLabel().uppercase(),
                            fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            color = condColor, fontFamily = DmSansFamily, letterSpacing = 0.5.sp
                        )
                    }

                    // Dot indicator carousel
                    if (pageCount > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(pageCount) { index ->
                                Box(
                                    modifier = Modifier
                                        .then(
                                            if (pagerState.currentPage == index)
                                                Modifier.width(16.dp).height(4.dp)
                                            else
                                                Modifier.size(4.dp)
                                        )
                                        .clip(RoundedCornerShape(Radius.full))
                                        .background(
                                            if (pagerState.currentPage == index) Terracotta
                                            else Terracotta.copy(alpha = 0.3f)
                                        )
                                )
                            }
                        }
                    }
                }

                // ── KONTEN ────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
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
                        if (item.status == "AVAILABLE") PrelovedTag("✓ Tersedia")
                        else if (item.status == "SOLD")  PrelovedTag("✕ Terjual")
                        else if (item.status == "CLOSED") PrelovedTag("Ditutup")
                    }

                    UserContactPanel(
                        name = item.user.name,
                        waNumber = item.user.waNumber,
                        avatarUrl = item.user.avatarUrl,
                        status = item.user.status,
                        isOwner = isOwner,
                        ownerLabel = "Ini barang Anda",
                        message = waMessagePreloved(item.title, item.formattedPrice())
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
    onMarkSold: () -> Unit,
    onToggleClosed: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showStatusConfirm by remember { mutableStateOf<String?>(null) } // null, "SOLD", "CLOSED", "AVAILABLE"

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
