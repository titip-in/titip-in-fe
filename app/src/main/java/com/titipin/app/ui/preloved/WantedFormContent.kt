package com.titipin.app.ui.preloved

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.ui.auth.TitipinTextField
import com.titipin.app.ui.jastip.FormLabel
import com.titipin.app.ui.theme.*

// Label pill → value BE (tanpa emoji)
private val wantedCategoryValues = mapOf(
    "Sepatu"     to "SEPATU",
    "Fashion"    to "FASHION",
    "Gadget"     to "GADGET",
    "Buku"       to "BUKU",
    "Elektronik" to "ELEKTRONIK",
    "Olahraga"   to "OLAHRAGA",
    "Furniture"  to "FURNITURE",
    "Lainnya"    to "LAINNYA"
)

// Pasangan emoji + label untuk ditampilkan di pill
private val wantedCategories = listOf(
    "👟" to "Sepatu",
    "👗" to "Fashion",
    "📱" to "Gadget",
    "📚" to "Buku",
    "💻" to "Elektronik",
    "⚽" to "Olahraga",
    "🪑" to "Furniture",
    "📦" to "Lainnya"
)

@Composable
fun WantedFormContent(
    viewModel: WantedViewModel,
    onDismiss: () -> Unit
) {
    val actionState by viewModel.actionState.collectAsState()
    val isLoading = actionState is WantedActionState.Loading
    var showConfirmDialog by remember { mutableStateOf(false) }

    var title          by remember { mutableStateOf("") }
    var description    by remember { mutableStateOf("") }
    var maxPriceStr    by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
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
                    text = "Cari Barang",
                    fontFamily = FrauncesFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = Charcoal
                )
                Text(
                    text = "Post barang yang kamu cari",
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

        // ── Scrollable fields ─────────────────────────────────────
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
                    .background(TerracottaPale)
                    .padding(Spacing.md),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Text("🔍", fontSize = 14.sp)
                Text(
                    text = "Punya barang yang cocok dengan request orang lain? Kamu bisa langsung hubungi mereka!",
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = Charcoal,
                    lineHeight = 18.sp
                )
            }

            // Nama barang
            TitipinTextField(
                label         = "Nama Barang yang Dicari",
                value         = title,
                onValueChange = { title = it },
                placeholder   = "Contoh: iPhone 12, Buku Kalkulus Purcell"
            )

            // Deskripsi
            TitipinTextField(
                label         = "Deskripsi (opsional)",
                value         = description,
                onValueChange = { description = it },
                placeholder   = "Contoh: kondisi minimal bagus, RAM 8GB"
            )

            // Budget
            TitipinTextField(
                label         = "Budget Maksimal (opsional)",
                value         = maxPriceStr,
                onValueChange = { maxPriceStr = it.filter { c -> c.isDigit() } },
                placeholder   = "Contoh: 3000000",
                keyboardType  = KeyboardType.Number
            )

            // Kategori
            FormLabel("Kategori (opsional)")
            FlowRowCategories(
                categories = wantedCategories,
                selected   = selectedCategory,
                onSelect   = { label ->
                    selectedCategory = if (selectedCategory == label) "" else label
                }
            )

            if (actionState is WantedActionState.Error) {
                Text(
                    text = (actionState as WantedActionState.Error).message,
                    color = Terracotta,
                    fontSize = 12.sp,
                    fontFamily = DmSansFamily
                )
            }

            Spacer(Modifier.height(Spacing.sm))
        }

        // ── Submit ────────────────────────────────────────────────
        HorizontalDivider(color = CreamDark, thickness = 1.dp)
        Column(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)) {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
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
                enabled = title.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Cream,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Posting Pencarian →",
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
        WantedConfirmDialog(
            title       = title.trim(),
            maxPriceStr = maxPriceStr.ifBlank { null },
            category    = selectedCategory.ifBlank { null },
            onConfirm   = {
                showConfirmDialog = false
                viewModel.createWanted(
                    title       = title.trim(),
                    description = description.trim().ifBlank { null },
                    maxPrice    = maxPriceStr.toDoubleOrNull(),
                    // Translate label → BE value, fallback uppercase kalau tidak ada di map
                    category    = selectedCategory.ifBlank { null }
                        ?.let { wantedCategoryValues[it] ?: it.uppercase() }
                )
            },
            onDismiss = { showConfirmDialog = false }
        )
    }
}

// ── Reusable category pill row ─────────────────────────────────────
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun FlowRowCategories(
    categories: List<Pair<String, String>>,  // emoji to label
    selected: String,                         // hanya label (tanpa emoji)
    onSelect: (String) -> Unit               // returns label saja
) {
    @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        categories.forEach { (emoji, label) ->
            val isSelected = selected == label
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.full))
                    .background(if (isSelected) Charcoal else CreamDark)
                    .clickable { onSelect(label) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "$emoji $label",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = DmSansFamily,
                    color = if (isSelected) Cream else Charcoal60
                )
            }
        }
    }
}

// ── Dialog Konfirmasi Wanted ───────────────────────────────────────
@Composable
fun WantedConfirmDialog(
    title: String,
    maxPriceStr: String?,
    category: String?,
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
                        .background(TerracottaPale),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔍", fontSize = 24.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Posting Pencarian?",
                    fontFamily = FrauncesFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Charcoal,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Penjual yang punya barang ini\nbisa langsung menghubungimu.",
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
                WantedConfirmRow(label = "Barang", value = title)
                if (!maxPriceStr.isNullOrEmpty()) {
                    val formatted = maxPriceStr.toLongOrNull()?.let {
                        "Rp " + it.toString().reversed().chunked(3).joinToString(".").reversed()
                    } ?: maxPriceStr
                    WantedConfirmRow(label = "Budget", value = "~$formatted")
                }
                if (!category.isNullOrEmpty()) {
                    WantedConfirmRow(label = "Kategori", value = category)
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
private fun WantedConfirmRow(label: String, value: String) {
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