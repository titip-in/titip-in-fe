package com.titipin.app.ui.jastip

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.data.model.JastipDto
import com.titipin.app.data.model.CategoryDto
import com.titipin.app.data.model.primaryImageUrl
import com.titipin.app.shared.ImageItem
import com.titipin.app.shared.ListingImagePickerRow
import com.titipin.app.ui.auth.TitipinTextField
import com.titipin.app.ui.components.CategoryChipRow
import com.titipin.app.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditJastipSheet(
    item: JastipDto,
    categories: List<CategoryDto> = emptyList(),
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        fromLoc: String,
        toLoc: String,
        deadline: String,
        notes: String?,
        categoryId: Int?,
        imageUris: List<Uri>,
        existingImageUrls: List<String>
    ) -> Unit
) {
    var title by remember { mutableStateOf(item.title) }
    var fromLocation by remember { mutableStateOf(item.fromLocation) }
    var toLocation by remember { mutableStateOf(item.toLocation) }
    var notes by remember { mutableStateOf(item.notes ?: "") }
    var selectedCategoryId by remember(item.id) { mutableStateOf(item.categoryId) }
    var imageItems by remember(item.id) {
        mutableStateOf<List<ImageItem>>(
            item.images.orEmpty()
                .map { it.imageUrl }
                .ifEmpty { listOfNotNull(item.primaryImageUrl()) }
                .distinct()
                .map { ImageItem.Remote(it) }
        )
    }

    val context = LocalContext.current
    val now = Calendar.getInstance()
    
    // Parse existing deadline if possible, otherwise empty
    val initialDate = item.deadline?.substringBefore("T") ?: ""
    val initialTime = item.deadline?.substringAfter("T")?.substringBefore(":")?.let { h ->
        val m = item.deadline.substringAfter("T").substringAfter(":").substringBefore(":")
        "$h:$m"
    } ?: ""

    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedTime by remember { mutableStateOf(initialTime) }

    val deadlineFormatted = if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty())
        "${selectedDate}T${selectedTime}:00" else ""

    val deadlineDisplay = when {
        selectedDate.isEmpty() -> "Pilih tanggal & waktu"
        selectedTime.isEmpty() -> selectedDate
        else -> "$selectedDate · $selectedTime"
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            selectedDate = "%04d-%02d-%02d".format(year, month + 1, day)
            TimePickerDialog(
                context,
                { _, hour, minute -> selectedTime = "%02d:%02d".format(hour, minute) },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
            ).show()
        },
        now.get(Calendar.YEAR),
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = now.timeInMillis
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Cream,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg)
                .padding(bottom = Spacing.xxl)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text("Edit Jastip", fontSize = 20.sp, fontFamily = FrauncesFamily, color = Charcoal)

            TitipinTextField(
                value = title,
                onValueChange = { title = it },
                label = "Judul Jastip",
                placeholder = "Contoh: Jastip Oleh-oleh Bandung"
            )

            if (categories.isNotEmpty()) {
                Text("Kategori", fontSize = 12.sp, fontFamily = DmSansFamily, color = Charcoal)
                CategoryChipRow(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = { selectedCategoryId = it },
                    modifier = Modifier.fillMaxWidth(),
                    includeAllChip = false
                )
            }

            TitipinTextField(
                value = fromLocation,
                onValueChange = { fromLocation = it },
                label = "Dari Mana?",
                placeholder = "Contoh: Bandung"
            )

            TitipinTextField(
                value = toLocation,
                onValueChange = { toLocation = it },
                label = "Ke Mana?",
                placeholder = "Contoh: Jakarta"
            )

            Column {
                Text("Batas Nitip", fontSize = 12.sp, fontFamily = DmSansFamily, color = Charcoal)
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth().height(ComponentSize.inputHeight),
                    shape = RoundedCornerShape(Radius.md),
                    colors = ButtonDefaults.buttonColors(containerColor = CreamDark, contentColor = Charcoal)
                ) {
                    Text(deadlineDisplay, fontFamily = DmSansFamily, fontSize = 14.sp)
                }
            }

            TitipinTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Catatan Tambahan (Opsional)",
                placeholder = "Contoh: Berangkat tgl 20 sore..."
            )

            ListingImagePickerRow(
                images = imageItems,
                onPickImages = { newUris ->
                    val space = 5 - imageItems.size
                    imageItems = imageItems + newUris.take(space).map { ImageItem.Local(it) }
                },
                onRemove = { index ->
                    imageItems = imageItems.toMutableList().also { it.removeAt(index) }
                },
                label = "FOTO JASTIP"
            )

            Spacer(Modifier.height(Spacing.sm))

            val formValid = title.isNotBlank() &&
                fromLocation.isNotBlank() &&
                toLocation.isNotBlank() &&
                deadlineFormatted.isNotBlank() &&
                imageItems.isNotEmpty()
            
            Button(
                onClick = {
                    if (formValid) {
                        onSubmit(
                            title,
                            fromLocation,
                            toLocation,
                            deadlineFormatted,
                            notes.ifBlank { null },
                            selectedCategoryId,
                            imageItems.filterIsInstance<ImageItem.Local>().map { it.uri },
                            imageItems.filterIsInstance<ImageItem.Remote>().map { it.url }
                        )
                    }
                },
                enabled = formValid,
                modifier = Modifier.fillMaxWidth().height(ComponentSize.buttonHeight),
                shape = RoundedCornerShape(Radius.full),
                colors = ButtonDefaults.buttonColors(containerColor = Terracotta, contentColor = Cream)
            ) {
                Text("Simpan Perubahan", fontSize = 14.sp, fontFamily = DmSansFamily, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EditDeadlineDialog(
    currentDeadline: String?,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    val context = LocalContext.current
    val now = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }

    val deadlineFormatted = if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty())
        "${selectedDate}T${selectedTime}:00" else ""

    val deadlineDisplay = when {
        selectedDate.isEmpty() -> "Pilih tanggal & waktu"
        selectedTime.isEmpty() -> selectedDate
        else -> "$selectedDate · $selectedTime"
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            selectedDate = "%04d-%02d-%02d".format(year, month + 1, day)
            TimePickerDialog(
                context,
                { _, hour, minute -> selectedTime = "%02d:%02d".format(hour, minute) },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
            ).show()
        },
        now.get(Calendar.YEAR),
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = now.timeInMillis
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Cream,
        title = {
            Text("Buka Kembali Jastip", fontFamily = FrauncesFamily, color = Charcoal, fontSize = 20.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tentukan batas nitip baru untuk jastip ini.", fontFamily = DmSansFamily, color = Charcoal60)
                Button(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth().height(ComponentSize.inputHeight),
                    shape = RoundedCornerShape(Radius.md),
                    colors = ButtonDefaults.buttonColors(containerColor = CreamDark, contentColor = Charcoal)
                ) {
                    Text(deadlineDisplay, fontFamily = DmSansFamily, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(deadlineFormatted) },
                enabled = deadlineFormatted.isNotBlank(),
                shape = RoundedCornerShape(Radius.full),
                colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
            ) {
                Text("Buka Jastip", color = Cream, fontFamily = DmSansFamily)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Charcoal60, fontFamily = DmSansFamily)
            }
        }
    )
}
