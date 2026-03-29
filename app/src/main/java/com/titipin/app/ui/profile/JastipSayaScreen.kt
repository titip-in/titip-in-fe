package com.titipin.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.titipin.app.data.model.JastipDto
import com.titipin.app.shared.formatDeadlineDisplay
import com.titipin.app.shared.timeAgo
import com.titipin.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JastipSayaScreen(
    onBack: () -> Unit,
    viewModel: JastipSayaViewModel = hiltViewModel()
) {
    val listState   by viewModel.listState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }   // 0 = Aktif, 1 = Selesai

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle action results
    LaunchedEffect(actionState) {
        when (actionState) {
            is JastipSayaActionState.Success -> {
                viewModel.resetActionState()
                viewModel.loadData()
            }
            is JastipSayaActionState.Error -> {
                val msg = (actionState as JastipSayaActionState.Error).message
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
                    Text("Jastip Saya", color = Charcoal, fontSize = 24.sp,
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
                listOf("Aktif", "Selesai").forEachIndexed { index, label ->
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
                            Text((listState as JastipSayaState.Error).message,
                                fontFamily = DmSansFamily, color = Charcoal60,
                                fontSize = 13.sp, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(12.dp))
                            TextButton(onClick = { viewModel.loadData() }) {
                                Text("Coba lagi", color = Terracotta, fontFamily = DmSansFamily)
                            }
                        }
                    }
                }
                is JastipSayaState.Success -> {
                    val allData = (listState as JastipSayaState.Success).data
                    val filteredData = if (selectedTab == 0)
                        allData.filter { it.status == "ACTIVE" }
                    else
                        allData.filter { it.status == "CLOSED" }

                    val isActionLoading = actionState is JastipSayaActionState.Loading

                    if (filteredData.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (selectedTab == 0) "📦" else "✅", fontSize = 48.sp)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    if (selectedTab == 0) "Belum ada jastip aktif"
                                    else "Belum ada jastip selesai",
                                    fontFamily = FrauncesFamily, color = Charcoal,
                                    fontSize = 16.sp, fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    if (selectedTab == 0) "Buka jastip baru dari tab Jastip!"
                                    else "Jastip yang ditutup akan muncul di sini.",
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
                            items(filteredData, key = { it.id }) { jastip ->
                                JastipSayaCard(
                                    jastip          = jastip,
                                    isActionLoading = isActionLoading,
                                    onTutup         = { viewModel.updateStatus(jastip.id, "CLOSED") },
                                    onHapus         = { viewModel.deleteJastip(jastip.id) }
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

// ── JASTIP SAYA CARD ──────────────────────────────────────────────
@Composable
private fun JastipSayaCard(
    jastip: JastipDto,
    isActionLoading: Boolean,
    onTutup: () -> Unit,
    onHapus: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val deadlineDisplay = formatDeadlineDisplay(jastip.deadline, includeYear = false)
    val createdAtLabel  = timeAgo(jastip.createdAt)
    val isActive        = jastip.status == "ACTIVE"

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(Radius.lg),
        colors    = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {

            // ── Rute + status badge ───────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(jastip.fromLocation, fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold, color = Charcoal,
                        fontFamily = DmSansFamily, maxLines = 1,
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text("  →  ", fontSize = 14.sp, color = Terracotta, fontFamily = DmSansFamily)
                    Text(jastip.toLocation, fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold, color = Charcoal,
                        fontFamily = DmSansFamily, maxLines = 1,
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.full))
                        .background(if (isActive) SagePale else CreamDark)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (isActive) "AKTIF" else "TUTUP",
                        fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (isActive) Sage else Charcoal30,
                        fontFamily = DmSansFamily
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // ── Info chips ────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                JastipSayaChip("⏰ $deadlineDisplay")
                JastipSayaChip("🕐 $createdAtLabel")
                if (!jastip.notes.isNullOrEmpty()) JastipSayaChip("📝 Catatan")
            }

            Spacer(Modifier.height(Spacing.sm))
            HorizontalDivider(color = Charcoal10, thickness = 0.5.dp)
            Spacer(Modifier.height(Spacing.sm))

            // ── Aksi ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                // Hapus (selalu tersedia)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.full))
                        .background(CreamDark)
                        .clickable(enabled = !isActionLoading) { showDeleteDialog = true }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("🗑 Hapus", fontSize = 12.sp, color = Charcoal60, fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Medium)
                }

                // Tutup (hanya kalau masih ACTIVE)
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.full))
                            .background(Charcoal)
                            .clickable(enabled = !isActionLoading) { onTutup() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isActionLoading) {
                            CircularProgressIndicator(color = Cream, strokeWidth = 1.5.dp,
                                modifier = Modifier.size(14.dp))
                        } else {
                            Text("✓ Tutup", fontSize = 12.sp, color = Cream,
                                fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    // ── Confirm delete dialog ──────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor   = Cream,
            shape            = RoundedCornerShape(Radius.xl),
            title = {
                Text("Hapus Jastip?", fontFamily = FrauncesFamily, fontSize = 20.sp,
                    fontWeight = FontWeight.Medium, color = Charcoal)
            },
            text = {
                Text(
                    "Jastip ${jastip.fromLocation} → ${jastip.toLocation} akan dihapus permanen.",
                    fontFamily = DmSansFamily, fontSize = 13.sp, color = Charcoal60,
                    lineHeight = 20.sp
                )
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

@Composable
private fun JastipSayaChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.full))
            .background(CreamDark)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, fontSize = 11.sp, color = Charcoal60, fontFamily = DmSansFamily)
    }
}