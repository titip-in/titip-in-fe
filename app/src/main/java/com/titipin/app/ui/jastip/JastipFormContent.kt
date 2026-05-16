package com.titipin.app.ui.jastip

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.ui.auth.TitipinTextField
import com.titipin.app.ui.theme.*
import java.util.Calendar

@Composable
fun JastipFormContent(
    viewModel: JastipViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val actionState by viewModel.actionState.collectAsState()
    val isLoading = actionState is JastipActionState.Loading
    var showConfirmDialog by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var fromLocation by remember { mutableStateOf("") }
    var toLocation   by remember { mutableStateOf("") }
    var notes        by remember { mutableStateOf("") }
    var imageUrl     by remember { mutableStateOf("") }
    var latitudeStr  by remember { mutableStateOf("-7.9358") }
    var longitudeStr by remember { mutableStateOf("112.6139") }

    // Deadline — simpan terpisah biar enak diformatkan
    val now = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf("") }   // "2026-03-20"
    var selectedTime by remember { mutableStateOf("") }   // "14:00"

    // Gabungkan jadi format yang BE mau: "2026-03-20T14:00:00"
    val deadlineFormatted = if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty())
        "${selectedDate}T${selectedTime}:00" else ""

    // Display label untuk button
    val deadlineDisplay = when {
        selectedDate.isEmpty() -> "Pilih tanggal & waktu"
        selectedTime.isEmpty() -> selectedDate
        else -> "$selectedDate · $selectedTime"
    }

    // DatePickerDialog — ini Android View-based dialog, bukan Compose
    // Cara panggil dari Compose: bikin lambda, panggil .show() saat user tap
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            // month di Calendar mulai dari 0, jadi +1
            selectedDate = "%04d-%02d-%02d".format(year, month + 1, day)
            // Setelah pilih tanggal, langsung buka time picker
            TimePickerDialog(
                context,
                { _, hour, minute -> selectedTime = "%02d:%02d".format(hour, minute) },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true // 24-hour format
            ).show()
        },
        now.get(Calendar.YEAR),
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH)
    ).apply {
        // Tidak bisa pilih tanggal yang sudah lewat
        datePicker.minDate = now.timeInMillis
    }

    val formValid = title.isNotEmpty() &&
            fromLocation.isNotEmpty() &&
            toLocation.isNotEmpty() &&
            deadlineFormatted.isNotEmpty() &&
            imageUrl.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
    ) {
        // ── HEADER ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Spacing.lg, end = Spacing.sm)
                .padding(top = Spacing.xs, bottom = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "● BUKA JASTIP",
                    color = Terracotta, fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp, fontFamily = DmSansFamily
                )
                Text(
                    text = "Isi Detail Jastip",
                    color = Charcoal, fontSize = 22.sp,
                    fontWeight = FontWeight.Medium, fontFamily = FrauncesFamily
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = Charcoal60)
            }
        }

        HorizontalDivider(color = Charcoal10, thickness = 0.5.dp)

        // ── FIELDS ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg)
                .padding(top = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            TitipinTextField(
                value = title,
                onValueChange = { title = it },
                label = "📦 JUDUL JASTIP",
                placeholder = "Titip snack dari Suhat",
                isFocused = title.isNotEmpty()
            )

            TitipinTextField(
                value = fromLocation, onValueChange = { fromLocation = it },
                label = "📍 DARI",
                placeholder = "Giant Dinoyo, Malang",
                isFocused = fromLocation.isNotEmpty()
            )

            TitipinTextField(
                value = toLocation, onValueChange = { toLocation = it },
                label = "🏁 TUJUAN",
                placeholder = "Kampus UB Teknik",
                isFocused = toLocation.isNotEmpty()
            )

            // ── DEADLINE — tap to pick date + time ────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "📅 DEADLINE",
                    fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp, color = Charcoal60, fontFamily = DmSansFamily,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                // Tombol pilih deadline — bukan TextField, tapi Box yang bisa diklik
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .background(CreamDark)
                        .clickable { datePickerDialog.show() }
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = deadlineDisplay,
                            fontSize = 13.sp,
                            fontFamily = DmSansFamily,
                            // Abu-abu kalau belum dipilih, Charcoal kalau sudah
                            color = if (deadlineFormatted.isEmpty()) Charcoal30 else Charcoal
                        )
                        Text(
                            text = if (deadlineFormatted.isEmpty()) "Pilih →" else "Ubah →",
                            fontSize = 11.sp,
                            color = Terracotta,
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                // Tampilkan format final yang akan dikirim ke BE
                if (deadlineFormatted.isNotEmpty()) {
                    Text(
                        text = "Akan dikirim: $deadlineFormatted",
                        fontSize = 10.sp, color = Sage, fontFamily = DmSansFamily,
                        modifier = Modifier.padding(start = 4.dp, top = 3.dp)
                    )
                }
            }

            TitipinTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = "🖼️ URL FOTO",
                placeholder = "https://.../foto.jpg",
                isFocused = imageUrl.isNotEmpty()
            )
            Text(
                text = "Upload gambar native akan ditambahkan di phase media. Untuk sementara pakai URL hasil upload.",
                fontSize = 10.sp, color = Charcoal30, fontFamily = DmSansFamily,
                modifier = Modifier.padding(start = 4.dp)
            )

            // ── KOORDINAT ─────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
                TitipinTextField(
                    value = latitudeStr, onValueChange = { latitudeStr = it },
                    label = "📌 LATITUDE", placeholder = "-7.9358",
                    isFocused = true, keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                TitipinTextField(
                    value = longitudeStr, onValueChange = { longitudeStr = it },
                    label = "📌 LONGITUDE", placeholder = "112.6139",
                    isFocused = true, keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = "💡 Default pusat Malang",
                fontSize = 10.sp, color = Charcoal30, fontFamily = DmSansFamily,
                modifier = Modifier.padding(start = 4.dp)
            )

            // ── CATATAN ───────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "📝 CATATAN (OPSIONAL)",
                    fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp, color = Charcoal60, fontFamily = DmSansFamily,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    placeholder = {
                        Text(
                            "Bisa titip max 5 item, ongkir nego...",
                            fontSize = 13.sp, color = Charcoal30, fontFamily = DmSansFamily
                        )
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 13.sp, fontFamily = DmSansFamily, color = Charcoal
                    ),
                    shape = RoundedCornerShape(Radius.md),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Sage,
                        unfocusedBorderColor = if (notes.isNotEmpty()) Sage else Charcoal10,
                        focusedContainerColor = CreamDark,
                        unfocusedContainerColor = CreamDark,
                        cursorColor = Terracotta
                    )
                )
            }

            if (actionState is JastipActionState.Error) {
                Text(
                    text = "⚠ ${(actionState as JastipActionState.Error).message}",
                    fontSize = 12.sp, color = Terracotta, fontFamily = DmSansFamily
                )
            }

            Spacer(Modifier.height(Spacing.xs))
        }

        // ── TOMBOL SUBMIT — di luar scroll, selalu keliatan ───────
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
                    containerColor         = Charcoal,
                    contentColor           = Cream,
                    disabledContainerColor = Charcoal10,
                    disabledContentColor   = Charcoal30
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Cream, strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Posting Jastip →",
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = DmSansFamily
                    )
                }
            }
        }
    }

    // ── Dialog Konfirmasi ─────────────────────────────────────────
    if (showConfirmDialog) {
        JastipConfirmDialog(
            fromLocation = fromLocation.trim(),
            toLocation   = toLocation.trim(),
            deadline     = deadlineDisplay,
            onConfirm    = {
                showConfirmDialog = false
                viewModel.createJastip(
                    title        = title.trim(),
                    fromLocation = fromLocation,
                    toLocation   = toLocation,
                    deadline     = deadlineFormatted,
                    latitude     = latitudeStr.toDoubleOrNull() ?: -7.9358,
                    longitude    = longitudeStr.toDoubleOrNull() ?: 112.6139,
                    notes        = notes.ifEmpty { null },
                    imageUrl     = imageUrl.trim()
                )
            },
            onDismiss = { showConfirmDialog = false }
        )
    }
}

@Composable
fun FormLabel(text: String) {
    Text(
        text = text,
        fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp, color = Charcoal60, fontFamily = DmSansFamily,
        modifier = Modifier.padding(top = Spacing.xs, bottom = 4.dp)
    )
}
// ── Dialog Konfirmasi Jastip ───────────────────────────────────────
@Composable
fun JastipConfirmDialog(
    fromLocation: String,
    toLocation: String,
    deadline: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
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
                        .background(SagePale),
                    contentAlignment = Alignment.Center
                ) { Text("📦", fontSize = 24.sp) }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Posting Jastip?",
                    fontFamily = FrauncesFamily, fontSize = 20.sp,
                    fontWeight = FontWeight.Medium, color = Charcoal,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Jastipmu akan langsung terlihat\noleh pengguna di sekitar area kamu.",
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
                JastipConfirmRow("Dari",     fromLocation)
                JastipConfirmRow("Ke",       toLocation)
                JastipConfirmRow("Deadline", deadline)
            }
        },
        confirmButton = {
            Button(
                onClick  = onConfirm,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(Radius.full),
                colors   = ButtonDefaults.buttonColors(containerColor = Charcoal)
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
private fun JastipConfirmRow(label: String, value: String) {
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
