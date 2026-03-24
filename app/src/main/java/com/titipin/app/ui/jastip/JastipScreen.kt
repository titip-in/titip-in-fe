package com.titipin.app.ui.jastip

import android.content.Intent
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.titipin.app.data.model.JastipDto
import com.titipin.app.shared.formatDeadlineDisplay
import com.titipin.app.shared.timeAgo
import com.titipin.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JastipScreen(
    onNavigateToDetail: (String) -> Unit = {},
    viewModel: JastipViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(actionState) {
        if (actionState is JastipActionState.Success) {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                showSheet = false
                viewModel.resetActionState()
                viewModel.loadJastipList()
            }
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

            Spacer(modifier = Modifier.height(Spacing.md))

            when (selectedTab) {
                0 -> JastipTersediaContent(
                    listState   = listState,
                    onCardClick = onNavigateToDetail,
                    onRetry     = { viewModel.loadJastipList() }
                )
                1 -> JastipRequestContent()
            }
        }

        // ── FAB ───────────────────────────────────────────────────
        // Floating di pojok kanan bawah, sesuai design system
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Spacing.lg, bottom = Spacing.lg)
                .size(52.dp)
                .shadow(elevation = 8.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Terracotta)
                .clickable { showSheet = true },
            contentAlignment = Alignment.Center
        ) {
            Text("＋", color = Cream, fontSize = 22.sp, fontWeight = FontWeight.Light)
        }
    }

    // ── BOTTOM SHEET ──────────────────────────────────────────────
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                viewModel.resetActionState()
            },
            sheetState     = sheetState,
            containerColor = Cream,
        ) {
            JastipFormContent(
                viewModel = viewModel,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSheet = false
                        viewModel.resetActionState()
                    }
                }
            )
        }
    }
}

// ── TAB TERSEDIA ──────────────────────────────────────────────────
@Composable
fun JastipTersediaContent(
    listState: JastipListState,
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
            if (listState.data.isEmpty()) {
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
                    items(listState.data, key = { it.id }) { jastip ->
                        JastipCard(jastip = jastip, onClick = { onCardClick(jastip.id) })
                    }
                    // Extra space di bawah buat FAB
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// ── TAB REQUEST (COMING SOON) ─────────────────────────────────────
@Composable
fun JastipRequestContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(Spacing.lg)) {
            Text("🚧", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Fitur Request segera hadir",
                fontFamily = FrauncesFamily, color = Charcoal,
                fontSize = 18.sp, fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Kamu bisa request jastip ke teman\ndi sekitarmu.",
                fontFamily = DmSansFamily, color = Charcoal60,
                fontSize = 13.sp, lineHeight = 20.sp, textAlign = TextAlign.Center
            )
        }
    }
}

// ── JASTIP CARD ───────────────────────────────────────────────────
@Composable
fun JastipCard(jastip: JastipDto, onClick: () -> Unit) {
    val context = LocalContext.current
    val deadlineShort = formatDeadlineDisplay(jastip.deadline, includeYear = false)
    val deadlineFull = formatDeadlineDisplay(jastip.deadline, includeYear = true)
    val createdAtLabel = timeAgo(jastip.createdAt)
    // Inisial dari userId — nanti ganti nama user kalau BE udah return user object
    val initials = jastip.user.name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    val name = jastip.user.name

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SagePale),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Sage,
                        fontFamily = DmSansFamily
                    )
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
                    // Status badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.full))
                            .background(if (jastip.status == "ACTIVE") SagePale else CreamDark)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (jastip.status == "ACTIVE") "AKTIF" else "TUTUP",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = if (jastip.status == "ACTIVE") Sage else Charcoal30,
                            fontFamily = DmSansFamily
                        )
                    }
                }
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

            // ── Row bawah: Deadline kiri, tombol WA kanan ─────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Deadline $deadlineFull",
                    fontSize = 11.sp,
                    color = Charcoal60,
                    fontFamily = DmSansFamily
                )

                // Tombol WhatsApp
                // TODO: ganti nomor WA dengan data user dari BE
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.full))
                        .background(Terracotta)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, "https://wa.me/${jastip.user.waNumber}".toUri())
                            context.startActivity(intent)
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