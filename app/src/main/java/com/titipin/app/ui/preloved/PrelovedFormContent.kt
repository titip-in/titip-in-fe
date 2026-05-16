package com.titipin.app.ui.preloved

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.shared.ImageItem
import com.titipin.app.shared.ListingImagePickerRow
import com.titipin.app.ui.components.CategoryChipRow
import com.titipin.app.ui.theme.*

private val CONDITIONS = listOf(
    "NEW"      to "Mulus",
    "LIKE_NEW" to "Baik",
    "GOOD"     to "Normal",
    "FAIR"     to "Layak"
)

@Composable
fun PrelovedFormContent(
    viewModel: PrelovedViewModel,
    onDismiss: () -> Unit
) {
    val actionState by viewModel.actionState.collectAsState()
    val categoryState by viewModel.categoryState.collectAsState()
    val isLoading = actionState is PrelovedActionState.Loading
    var showConfirmDialog by remember { mutableStateOf(false) }

    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var title        by remember { mutableStateOf("") }
    var description  by remember { mutableStateOf("") }
    var priceStr     by remember { mutableStateOf("") }
    var selectedCond by remember { mutableStateOf("") }
    var imageItems   by remember { mutableStateOf<List<ImageItem>>(emptyList()) }

    val formValid = title.isNotEmpty() && priceStr.isNotEmpty() &&
            selectedCond.isNotEmpty() && imageItems.isNotEmpty()

    // ── Root: Column dengan fillMaxHeight biar tombol tidak hilang ─
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.95f)   // 95% tinggi sheet — tombol selalu keliatan
            .imePadding()
            .navigationBarsPadding()
    ) {
        // ── HEADER ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Spacing.lg, end = Spacing.sm)
                .padding(top = Spacing.xs, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Jual Barang",
                    color = Charcoal, fontSize = 22.sp,
                    fontWeight = FontWeight.Medium, fontFamily = FrauncesFamily
                )
                Text(
                    text = "Isi detail barang preloved kamu",
                    color = Charcoal60, fontSize = 11.sp, fontFamily = DmSansFamily
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = Charcoal60)
            }
        }

        HorizontalDivider(color = Charcoal10, thickness = 0.5.dp)

        // ── SCROLLABLE FIELDS — weight(1f) biar tombol tidak terdorong ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg)
                .padding(top = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // ── IMAGE PICKER ──────────────────────────────────────────
            ListingImagePickerRow(
                images       = imageItems,
                onPickImages = { newUris ->
                    val space = 5 - imageItems.size
                    val toAdd = newUris.take(space).map { ImageItem.Local(it) }
                    imageItems = imageItems + toAdd
                },
                onRemove = { idx ->
                    imageItems = imageItems.toMutableList().also { it.removeAt(idx) }
                }
            )

            // ── NAMA BARANG ───────────────────────────────────────
            FormFieldLabel("NAMA BARANG")
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nike Air Max 90 Size 42", fontSize = 13.sp, color = Charcoal30, fontFamily = DmSansFamily) },
                textStyle = TextStyle(fontSize = 13.sp, fontFamily = DmSansFamily, color = Charcoal),
                singleLine = true,
                shape = RoundedCornerShape(Radius.md),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = if (title.isNotEmpty()) Sage else Terracotta,
                    unfocusedBorderColor = if (title.isNotEmpty()) Sage else Charcoal10,
                    focusedContainerColor   = CreamDark,
                    unfocusedContainerColor = CreamDark,
                    cursorColor = Terracotta
                )
            )

            // ── HARGA ─────────────────────────────────────────────
            FormFieldLabel("HARGA (RP)")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(CreamDark)
                    .border(
                        1.dp,
                        if (priceStr.isNotEmpty()) Sage else Charcoal10,
                        RoundedCornerShape(Radius.md)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rp", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Terracotta, fontFamily = DmSansFamily)
                    Spacer(Modifier.width(8.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it.filter { c -> c.isDigit() } },
                        textStyle = TextStyle(fontSize = 13.sp, fontFamily = DmSansFamily, color = Charcoal),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (priceStr.isEmpty()) {
                                Text("350000", fontSize = 13.sp, color = Charcoal30, fontFamily = DmSansFamily)
                            }
                            inner()
                        }
                    )
                }
            }
            if (priceStr.isNotEmpty()) {
                val formatted = priceStr.toLongOrNull()?.toString()
                    ?.reversed()?.chunked(3)?.joinToString(".")?.reversed()
                Text(
                    "Rp $formatted",
                    fontSize = 11.sp, color = Sage, fontFamily = DmSansFamily,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }

            // ── KONDISI ───────────────────────────────────────────
            FormFieldLabel("KONDISI")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CONDITIONS.forEach { (value, label) ->
                    val isSelected = selectedCond == value
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.full))
                            .background(if (isSelected) Charcoal else CreamDark)
                            .clickable { selectedCond = value }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Cream else Charcoal60,
                            fontFamily = DmSansFamily
                        )
                    }
                }
            }

            // ── KATEGORI ──────────────────────────────────────────
            FormFieldLabel("KATEGORI")
            if (categoryState is PrelovedCategoryState.Success) {
                CategoryChipRow(
                    categories = (categoryState as PrelovedCategoryState.Success).data,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = { selectedCategoryId = it }
                )
            } else {
                Text("Kategori belum termuat", fontSize = 11.sp, color = Charcoal60, fontFamily = DmSansFamily)
            }

            // ── DESKRIPSI ─────────────────────────────────────────
            FormFieldLabel("DESKRIPSI (OPSIONAL)")
            OutlinedTextField(
                value = description, onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth().height(90.dp),
                placeholder = { Text("Jarang dipakai, kondisi mulus...", fontSize = 13.sp, color = Charcoal30, fontFamily = DmSansFamily) },
                textStyle = TextStyle(fontSize = 13.sp, fontFamily = DmSansFamily, color = Charcoal),
                shape = RoundedCornerShape(Radius.md),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Sage,
                    unfocusedBorderColor = if (description.isNotEmpty()) Sage else Charcoal10,
                    focusedContainerColor   = CreamDark,
                    unfocusedContainerColor = CreamDark,
                    cursorColor = Terracotta
                )
            )

            if (actionState is PrelovedActionState.Error) {
                Text(
                    "⚠ ${(actionState as PrelovedActionState.Error).message}",
                    fontSize = 12.sp, color = Terracotta, fontFamily = DmSansFamily
                )
            }

            Spacer(Modifier.height(Spacing.xs))
        }

        // ── TOMBOL SUBMIT — di luar scroll, selalu visible ────────
        HorizontalDivider(color = Charcoal10, thickness = 0.5.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md)
        ) {
            Button(
                onClick = { if (formValid) showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ComponentSize.buttonHeight),
                enabled = formValid && !isLoading,
                shape   = RoundedCornerShape(Radius.full),
                colors  = ButtonDefaults.buttonColors(
                    containerColor         = Terracotta,
                    contentColor           = Cream,
                    disabledContainerColor = Charcoal10,
                    disabledContentColor   = Charcoal30
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Cream, strokeWidth = 2.dp)
                } else {
                    Text("Posting Barang →", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = DmSansFamily)
                }
            }
        }
    }

    // ── Dialog Konfirmasi ─────────────────────────────────────────
    if (showConfirmDialog) {
        val priceFormatted = priceStr.toLongOrNull()?.let {
            "Rp " + it.toString().reversed().chunked(3).joinToString(".").reversed()
        } ?: priceStr
        PrelovedConfirmDialog(
            title     = title.trim(),
            price     = priceFormatted,
            condition = selectedCond,
            category  = (categoryState as? PrelovedCategoryState.Success)
                ?.data
                ?.firstOrNull { it.id == selectedCategoryId }
                ?.name ?: "Tanpa kategori",
            onConfirm = {
                showConfirmDialog = false
                viewModel.createPreloved(
                    categoryId   = selectedCategoryId,
                    title        = title,
                    description  = description.ifEmpty { null },
                    price        = priceStr.toIntOrNull() ?: 0,
                    condition    = selectedCond,
                    imageUris    = imageItems.filterIsInstance<ImageItem.Local>().map { it.uri }
                )
            },
            onDismiss = { showConfirmDialog = false }
        )
    }
}

@Composable
private fun FormFieldLabel(text: String) {
    Text(
        text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp, color = Charcoal60, fontFamily = DmSansFamily
    )
}
// ── Dialog Konfirmasi Preloved ─────────────────────────────────────
@Composable
fun PrelovedConfirmDialog(
    title: String,
    price: String,
    condition: String,
    category: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val conditionLabel = when (condition) {
        "NEW"      -> "Baru"
        "LIKE_NEW" -> "Seperti Baru"
        "GOOD"     -> "Bagus"
        "FAIR"     -> "Layak Pakai"
        else       -> condition
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color.White,
        shape            = RoundedCornerShape(Radius.xl),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(50))
                        .background(TerracottaPale),
                    contentAlignment = Alignment.Center
                ) { Text("🛍️", fontSize = 24.sp) }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Posting Barang?",
                    fontFamily = FrauncesFamily, fontSize = 20.sp,
                    fontWeight = FontWeight.Medium, color = Charcoal,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Barangmu akan langsung muncul\ndi halaman Preloved.",
                    fontFamily = DmSansFamily, fontSize = 12.sp,
                    color = Charcoal60, textAlign = TextAlign.Center,
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
                PrelovedConfirmRow("Nama",    title)
                PrelovedConfirmRow("Harga",   price)
                PrelovedConfirmRow("Kondisi", conditionLabel)
                PrelovedConfirmRow("Kategori",category)
            }
        },
        confirmButton = {
            Button(
                onClick  = onConfirm,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(Radius.full),
                colors   = ButtonDefaults.buttonColors(containerColor = Terracotta)
            ) {
                Text(
                    "Ya, Posting Sekarang",
                    fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp, color = Cream
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick  = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(Radius.full),
                border   = BorderStroke(1.5.dp, CreamDark)
            ) {
                Text(
                    "Batal, Cek Lagi",
                    fontFamily = DmSansFamily, fontWeight = FontWeight.Medium,
                    fontSize = 14.sp, color = Charcoal60
                )
            }
        }
    )
}

@Composable
private fun PrelovedConfirmRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label, fontSize = 11.sp, color = Charcoal60,
            fontFamily = DmSansFamily, modifier = Modifier.weight(0.35f)
        )
        Text(
            value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            color = Charcoal, fontFamily = DmSansFamily,
            modifier = Modifier.weight(0.65f),
            textAlign = TextAlign.End
        )
    }
}
