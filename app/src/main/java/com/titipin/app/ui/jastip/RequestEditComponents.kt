package com.titipin.app.ui.jastip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.data.model.CategoryDto
import com.titipin.app.data.model.RequestDto
import com.titipin.app.ui.auth.TitipinTextField
import com.titipin.app.ui.components.CategoryChipRow
import com.titipin.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRequestSheet(
    item: RequestDto,
    categories: List<CategoryDto> = emptyList(),
    onDismiss: () -> Unit,
    onSubmit: (title: String, fromLoc: String, toLoc: String, notes: String?, categoryId: Int?) -> Unit
) {
    var title by remember { mutableStateOf(item.title) }
    var fromLocation by remember { mutableStateOf(item.fromLocation) }
    var toLocation by remember { mutableStateOf(item.toLocation) }
    var notes by remember { mutableStateOf(item.notes ?: "") }
    var selectedCategoryId by remember(item.id) { mutableStateOf(item.categoryId) }

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
            Text("Edit Permintaan Jastip", fontSize = 20.sp, fontFamily = FrauncesFamily, color = Charcoal)

            TitipinTextField(
                value = title,
                onValueChange = { title = it },
                label = "Judul Request",
                placeholder = "Contoh: Titip Sepatu Cibaduyut"
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

            TitipinTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Catatan Tambahan (Opsional)",
                placeholder = "Contoh: Ukuran 42 warna hitam ya..."
            )

            Spacer(Modifier.height(Spacing.sm))

            val formValid = title.isNotBlank() && fromLocation.isNotBlank() && toLocation.isNotBlank()
            
            Button(
                onClick = {
                    if (formValid) {
                        onSubmit(title, fromLocation, toLocation, notes.ifBlank { null }, selectedCategoryId)
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
