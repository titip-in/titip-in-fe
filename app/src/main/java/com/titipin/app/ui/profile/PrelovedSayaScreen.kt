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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.data.model.PrelovedDto
import com.titipin.app.data.model.conditionLabel
import com.titipin.app.data.model.formattedPrice
import com.titipin.app.shared.timeAgo
import com.titipin.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrelovedSayaScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    viewModel: PrelovedSayaViewModel = hiltViewModel()
) {
    val listState   by viewModel.listState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }   // 0 = Dijual, 1 = Terjual/Reserved

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(actionState) {
        when (actionState) {
            is PrelovedSayaActionState.Success -> {
                viewModel.resetActionState()
                viewModel.loadData()
            }
            is PrelovedSayaActionState.Error -> {
                val msg = (actionState as PrelovedSayaActionState.Error).message
                viewModel.resetActionState()
                scope.launch { snackbarHostState.showSnackbar(msg) }
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
                .statusBarsPadding()
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

            // ── TAB ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .padding(horizontal = Spacing.lg)
                    .clip(RoundedCornerShape(Radius.full))
                    .background(CreamDark)
                    .padding(4.dp)
            ) {
                listOf("Dijual", "Terjual").forEachIndexed { index, label ->
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

            // ── CONTENT ───────────────────────────────────────────
            when (listState) {
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
                            Text((listState as PrelovedSayaState.Error).message,
                                fontFamily = DmSansFamily, color = Charcoal60,
                                fontSize = 13.sp, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(12.dp))
                            TextButton(onClick = { viewModel.loadData() }) {
                                Text("Coba lagi", color = Terracotta, fontFamily = DmSansFamily)
                            }
                        }
                    }
                }
                is PrelovedSayaState.Success -> {
                    val allData = (listState as PrelovedSayaState.Success).data
                    val filteredData = if (selectedTab == 0)
                        allData.filter { it.status == "AVAILABLE" }
                    else
                        allData.filter { it.status == "SOLD" || it.status == "CLOSED" }

                    val isActionLoading = actionState is PrelovedSayaActionState.Loading

                    if (filteredData.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (selectedTab == 0) "🛍️" else "✅", fontSize = 48.sp)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    if (selectedTab == 0) "Belum ada barang dijual"
                                    else "Belum ada barang terjual",
                                    fontFamily = FrauncesFamily, color = Charcoal,
                                    fontSize = 16.sp, fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    if (selectedTab == 0) "Post barang preloved kamu sekarang!"
                                    else "Barang yang terjual akan muncul di sini.",
                                    fontFamily = DmSansFamily, color = Charcoal60, fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            items(filteredData, key = { it.id }) { item ->
                                PrelovedSayaCard(
                                    item            = item,
                                    isActionLoading = isActionLoading,
                                    onLihatDetail   = { onNavigateToDetail(item.id) },
                                    onMarkSold      = { viewModel.updateStatus(item.id, "SOLD") },
                                    onHapus         = { viewModel.deletePreloved(item.id) }
                                )
                            }
                            item { Spacer(Modifier.height(24.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// ── PRELOVED SAYA CARD ────────────────────────────────────────────
@Composable
private fun PrelovedSayaCard(
    item: PrelovedDto,
    isActionLoading: Boolean,
    onLihatDetail: () -> Unit,
    onMarkSold: () -> Unit,
    onHapus: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isAvailable = item.status == "AVAILABLE"

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onLihatDetail() },
        shape     = RoundedCornerShape(Radius.lg),
        colors    = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(Spacing.md)) {

            // ── Thumbnail / category box ──────────────────────────
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(Radius.md))
                    .background(TerracottaPale),
                contentAlignment = Alignment.Center
            ) {
                Text(categoryEmojiForPreloved(item.category?.name), fontSize = 26.sp)
            }

            Spacer(Modifier.width(Spacing.md))

            // ── Info ──────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = Charcoal, fontFamily = DmSansFamily,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.full))
                            .background(
                                when (item.status) {
                                    "AVAILABLE" -> SagePale
                                    "SOLD"      -> GoldPale
                                    else        -> TerracottaPale
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            when (item.status) {
                                "AVAILABLE" -> "DIJUAL"
                                "SOLD"      -> "TERJUAL"
                                else        -> "DITUTUP"
                            },
                            fontSize = 8.sp, fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = when (item.status) {
                                "AVAILABLE" -> Sage
                                "SOLD"      -> Gold
                                else        -> Terracotta
                            },
                            fontFamily = DmSansFamily
                        )
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

                // ── Aksi ─────────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()) {

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.full))
                            .background(CreamDark)
                            .clickable(enabled = !isActionLoading) { showDeleteDialog = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("🗑 Hapus", fontSize = 11.sp, color = Charcoal60,
                            fontFamily = DmSansFamily, fontWeight = FontWeight.Medium)
                    }

                    if (isAvailable) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.full))
                                .background(Terracotta)
                                .clickable(enabled = !isActionLoading) { onMarkSold() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isActionLoading) {
                                CircularProgressIndicator(color = Cream, strokeWidth = 1.5.dp,
                                    modifier = Modifier.size(12.dp))
                            } else {
                                Text("✓ Tandai Terjual", fontSize = 11.sp, color = Cream,
                                    fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor   = Cream,
            shape            = RoundedCornerShape(Radius.xl),
            title = {
                Text("Hapus Barang?", fontFamily = FrauncesFamily, fontSize = 20.sp,
                    fontWeight = FontWeight.Medium, color = Charcoal)
            },
            text = {
                Text("\"${item.title}\" akan dihapus permanen dari daftar preloved kamu.",
                    fontFamily = DmSansFamily, fontSize = 13.sp, color = Charcoal60,
                    lineHeight = 20.sp)
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; onHapus() },
                    shape = RoundedCornerShape(Radius.full),
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                ) { Text("Hapus", fontFamily = DmSansFamily, color = Cream) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", fontFamily = DmSansFamily, color = Charcoal60)
                }
            }
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
