package com.titipin.app.ui.preloved

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.ui.theme.*

private val CONDITIONS = listOf(
    "NEW"      to "Mulus",
    "LIKE_NEW" to "Baik",
    "GOOD"     to "Normal",
    "FAIR"     to "Layak"
)

private val CATEGORIES = listOf(
    "👟" to "Sepatu",
    "👗" to "Fashion",
    "📱" to "Gadget",
    "📚" to "Buku",
    "💻" to "Elektronik",
    "⚽" to "Olahraga",
    "🪑" to "Furniture",
    "📦" to "Lainnya"
)

private val categoryValues = mapOf(
    "Sepatu"    to "FASHION",
    "Fashion"   to "FASHION",
    "Gadget"    to "GADGET",
    "Buku"      to "BUKU",
    "Elektronik" to "ELEKTRONIK",
    "Olahraga"  to "OLAHRAGA",
    "Furniture" to "FURNITURE",
    "Lainnya"   to "LAINNYA"
)

@Composable
fun PrelovedFormContent(
    viewModel: PrelovedViewModel,
    onDismiss: () -> Unit
) {
    val actionState by viewModel.actionState.collectAsState()
    val isLoading = actionState is PrelovedActionState.Loading

    var title        by remember { mutableStateOf("") }
    var description  by remember { mutableStateOf("") }
    var priceStr     by remember { mutableStateOf("") }
    var selectedCond by remember { mutableStateOf("") }
    var selectedCat  by remember { mutableStateOf("") }

    val formValid = title.isNotEmpty() && priceStr.isNotEmpty() &&
            selectedCond.isNotEmpty() && selectedCat.isNotEmpty()

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
            // ── FOTO UPLOAD ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.lg))
                    .background(CreamDark)
                    .border(2.dp, Charcoal30, RoundedCornerShape(Radius.lg))
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("📷", fontSize = 28.sp)
                    Text("Tambah Foto", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Charcoal, fontFamily = DmSansFamily)
                    Text("Max 5 foto · JPG, PNG", fontSize = 10.sp, color = Charcoal60, fontFamily = DmSansFamily)
                }
            }

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
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement   = Arrangement.spacedBy(6.dp)
            ) {
                CATEGORIES.forEach { (emoji, label) ->
                    val isSelected = selectedCat == label
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.full))
                            .background(if (isSelected) TerracottaPale else CreamDark)
                            .border(
                                1.dp,
                                if (isSelected) Terracotta else CreamDark,
                                RoundedCornerShape(Radius.full)
                            )
                            .clickable { selectedCat = label }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$emoji $label",
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Terracotta else Charcoal60,
                            fontFamily = DmSansFamily
                        )
                    }
                }
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
                onClick = {
                    viewModel.createPreloved(
                        title       = title,
                        description = description.ifEmpty { null },
                        price       = priceStr.toIntOrNull() ?: 0,
                        category    = categoryValues[selectedCat] ?: selectedCat.uppercase(),
                        condition   = selectedCond,
                        imageUrl    = null
                    )
                },
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
}

@Composable
private fun FormFieldLabel(text: String) {
    Text(
        text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp, color = Charcoal60, fontFamily = DmSansFamily
    )
}