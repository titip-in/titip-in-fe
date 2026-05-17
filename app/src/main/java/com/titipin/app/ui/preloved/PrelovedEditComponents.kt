package com.titipin.app.ui.preloved

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.data.model.CategoryDto
import com.titipin.app.data.model.PrelovedDto
import com.titipin.app.data.model.primaryImageUrl
import com.titipin.app.shared.ImageItem
import com.titipin.app.shared.ListingImagePickerRow
import com.titipin.app.ui.auth.TitipinTextField
import com.titipin.app.ui.components.CategoryChipRow
import com.titipin.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPrelovedSheet(
    item: PrelovedDto,
    categories: List<CategoryDto> = emptyList(),
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        price: Int,
        condition: String,
        description: String?,
        categoryId: Int?,
        imageUris: List<Uri>,
        existingImageUrls: List<String>
    ) -> Unit
) {
    var title by remember { mutableStateOf(item.title) }
    var priceStr by remember { mutableStateOf(item.price.toString()) }
    var condition by remember { mutableStateOf(item.condition) }
    var description by remember { mutableStateOf(item.description ?: "") }
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
            Text("Edit Barang Preloved", fontSize = 20.sp, fontFamily = FrauncesFamily, color = Charcoal)

            TitipinTextField(
                value = title,
                onValueChange = { title = it },
                label = "Nama Barang",
                placeholder = "Contoh: Sepatu Compass Gazelle"
            )

            TitipinTextField(
                value = priceStr,
                onValueChange = { priceStr = it },
                label = "Harga (Rp)",
                placeholder = "Contoh: 350000"
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

            Column {
                Text("Kondisi Barang", fontSize = 12.sp, fontFamily = DmSansFamily, color = Charcoal)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("NEW" to "Baru", "LIKE_NEW" to "Seperti Baru", "GOOD" to "Bagus", "FAIR" to "Layak Pakai").forEach { (value, label) ->
                        val isSelected = condition == value
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.full))
                                .background(if (isSelected) Charcoal else TerracottaPale.copy(alpha = 0.6f))
                                .clickable { condition = value }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                fontFamily = DmSansFamily,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) Cream else Terracotta
                            )
                        }
                    }
                }
            }

            TitipinTextField(
                value = description,
                onValueChange = { description = it },
                label = "Deskripsi",
                placeholder = "Jelaskan kondisi barang, minus, kelengkapan, dll..."
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
                label = "FOTO BARANG"
            )

            Spacer(Modifier.height(Spacing.sm))

            val priceNum = priceStr.toIntOrNull() ?: 0
            val formValid = title.isNotBlank() && priceNum > 0 && condition.isNotBlank() && imageItems.isNotEmpty()
            
            Button(
                onClick = {
                    if (formValid) {
                        onSubmit(
                            title,
                            priceNum,
                            condition,
                            description.ifBlank { null },
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
