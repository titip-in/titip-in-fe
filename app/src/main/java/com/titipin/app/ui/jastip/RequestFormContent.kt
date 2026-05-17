package com.titipin.app.ui.jastip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.ui.auth.TitipinTextField
import com.titipin.app.ui.components.CategoryChipRow
import com.titipin.app.ui.theme.*

@Composable
fun RequestFormContent(
    viewModel: RequestViewModel,
    onDismiss: () -> Unit
) {
    val actionState by viewModel.actionState.collectAsState()
    val categoryState by viewModel.categoryState.collectAsState()
    val isLoading = actionState is RequestActionState.Loading
    var showConfirmDialog by remember { mutableStateOf(false) }

    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var title        by remember { mutableStateOf("") }
    var fromLocation by remember { mutableStateOf("") }
    var toLocation   by remember { mutableStateOf("") }
    var notes        by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
    ) {
        // ── Handle + Header ───────────────────────────────────────
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(CreamDark)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(Spacing.md))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Request Jastip",
                    fontFamily = FrauncesFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = Charcoal
                )
                Text(
                    text = "Minta tolong titip dari lokasi ke tujuanmu",
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = Charcoal60
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = Charcoal60)
            }
        }

        Spacer(Modifier.height(Spacing.md))
        HorizontalDivider(color = CreamDark, thickness = 1.dp)
        Spacer(Modifier.height(Spacing.md))

        // ── Scrollable form fields ─────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Info banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(GoldPale)
                    .padding(Spacing.md),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Text("💡", fontSize = 14.sp)
                Text(
                    text = "Kamu butuh dititipin? Isi request ini agar provider bisa menghubungi lewat WhatsApp.",
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = Charcoal,
                    lineHeight = 18.sp
                )
            }

            if (categoryState is RequestCategoryState.Success) {
                Text(
                    text = "Kategori",
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal
                )
                CategoryChipRow(
                    categories = (categoryState as RequestCategoryState.Success).data,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = { selectedCategoryId = it }
                )
            }

            TitipinTextField(
                label         = "Judul Request",
                value         = title,
                onValueChange = { title = it },
                placeholder   = "Contoh: Butuh laundry hari ini"
            )

            // Dari
            TitipinTextField(
                label         = "Dari (Titik Pengambilan)",
                value         = fromLocation,
                onValueChange = { fromLocation = it },
                placeholder   = "Contoh: Giant Dinoyo, Matos"
            )

            // Ke
            TitipinTextField(
                label         = "Ke (Tujuan Antar)",
                value         = toLocation,
                onValueChange = { toLocation = it },
                placeholder   = "Contoh: Kos Soekarno Hatta"
            )

            // Catatan
            TitipinTextField(
                label         = "Catatan (opsional)",
                value         = notes,
                onValueChange = { notes = it },
                placeholder   = "Contoh: Indomie 3 bungkus, bayar transfer"
            )

            // Error state
            if (actionState is RequestActionState.Error) {
                Text(
                    text = (actionState as RequestActionState.Error).message,
                    color = Terracotta,
                    fontSize = 12.sp,
                    fontFamily = DmSansFamily
                )
            }

            Spacer(Modifier.height(Spacing.sm))
        }

        // ── Submit button — di luar scroll ────────────────────────
        HorizontalDivider(color = CreamDark, thickness = 1.dp)
        Column(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)) {
            Button(
                onClick = {
                    if (title.isNotBlank() && fromLocation.isNotBlank() && toLocation.isNotBlank()) {
                        showConfirmDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(Radius.full),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Terracotta,
                    disabledContainerColor = Terracotta.copy(alpha = 0.5f)
                ),
                enabled = title.isNotBlank() && fromLocation.isNotBlank() && toLocation.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Cream,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Posting Request →",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Cream
                    )
                }
            }
        }
    }

    // ── Dialog Konfirmasi ─────────────────────────────────────────
    if (showConfirmDialog) {
        RequestConfirmDialog(
            title = title.trim(),
            fromLocation = fromLocation.trim(),
            toLocation   = toLocation.trim(),
            notes        = notes.trim().ifBlank { null },
            onConfirm    = {
                showConfirmDialog = false
                viewModel.createRequest(
                    categoryId    = selectedCategoryId,
                    title         = title.trim(),
                    fromLocation = fromLocation.trim(),
                    toLocation   = toLocation.trim(),
                    notes        = notes.trim().ifBlank { null }
                )
            },
            onDismiss = { showConfirmDialog = false }
        )
    }
}

// ── Dialog Konfirmasi Request ──────────────────────────────────────
@Composable
fun RequestConfirmDialog(
    title: String,
    fromLocation: String,
    toLocation: String,
    notes: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color.White,
        shape            = RoundedCornerShape(Radius.xl),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SagePale),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📍", fontSize = 24.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Posting Request?",
                    fontFamily = FrauncesFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Charcoal,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Requestmu akan terlihat oleh provider\ndi sekitar area kamu.",
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = Charcoal60,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(CreamDark)
                    .padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ConfirmRow(label = "Judul", value = title)
                ConfirmRow(label = "Dari", value = fromLocation)
                ConfirmRow(label = "Ke", value = toLocation)
                if (!notes.isNullOrEmpty()) {
                    ConfirmRow(label = "Catatan", value = notes)
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = onConfirm,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(Radius.full),
                colors   = ButtonDefaults.buttonColors(containerColor = Terracotta)
            ) {
                Text("Ya, Posting Sekarang", fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Cream)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick  = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(Radius.full),
                border   = BorderStroke(1.5.dp, CreamDark)
            ) {
                Text("Batal, Cek Lagi", fontFamily = DmSansFamily,
                    fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Charcoal60)
            }
        }
    )
}

@Composable
private fun ConfirmRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, fontSize = 11.sp, color = Charcoal60, fontFamily = DmSansFamily,
            modifier = Modifier.weight(0.35f))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Charcoal,
            fontFamily = DmSansFamily, modifier = Modifier.weight(0.65f),
            textAlign = TextAlign.End)
    }
}
