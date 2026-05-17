package com.titipin.app.ui.preloved

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
import com.titipin.app.data.model.PrelovedRequestDto
import com.titipin.app.ui.auth.TitipinTextField
import com.titipin.app.ui.components.CategoryChipRow
import com.titipin.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPrelovedRequestSheet(
    item: PrelovedRequestDto,
    categories: List<CategoryDto> = emptyList(),
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String?, maxPrice: Int?, categoryId: Int?) -> Unit
) {
    var title by remember { mutableStateOf(item.title) }
    var description by remember { mutableStateOf(item.description ?: "") }
    var maxPriceStr by remember { mutableStateOf(item.maxPrice?.toString() ?: "") }
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
            Text("Edit Pencarian Barang", fontSize = 20.sp, fontFamily = FrauncesFamily, color = Charcoal)

            TitipinTextField(
                value = title,
                onValueChange = { title = it },
                label = "Judul Pencarian",
                placeholder = "Contoh: Dicari iPhone 13 Pro"
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
                value = maxPriceStr,
                onValueChange = { maxPriceStr = it },
                label = "Budget Maksimal (Rp) - Opsional",
                placeholder = "Contoh: 15000000"
            )

            TitipinTextField(
                value = description,
                onValueChange = { description = it },
                label = "Deskripsi Tambahan (Opsional)",
                placeholder = "Contoh: Diutamakan ex-inter warna sierra blue..."
            )

            Spacer(Modifier.height(Spacing.sm))

            val formValid = title.isNotBlank()
            
            Button(
                onClick = {
                    if (formValid) {
                        val maxPriceNum = maxPriceStr.toIntOrNull()
                        onSubmit(title, description.ifBlank { null }, maxPriceNum, selectedCategoryId)
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
