package com.titipin.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.data.model.CategoryDto
import com.titipin.app.ui.theme.Charcoal
import com.titipin.app.ui.theme.Charcoal60
import com.titipin.app.ui.theme.Cream
import com.titipin.app.ui.theme.DmSansFamily
import com.titipin.app.ui.theme.Radius
import com.titipin.app.ui.theme.SagePale
import com.titipin.app.ui.theme.TerracottaPale

@Composable
fun CategoryChipRow(
    categories: List<CategoryDto>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    includeAllChip: Boolean = true
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        if (includeAllChip) {
            item {
                CategoryChip(
                    label = "Semua",
                    selected = selectedCategoryId == null,
                    onClick = { onCategorySelected(null) }
                )
            }
        }

        items(categories, key = { it.id }) { category ->
            CategoryChip(
                label = listOfNotNull(category.icon, category.name).joinToString(" "),
                selected = selectedCategoryId == category.id,
                onClick = { onCategorySelected(category.id) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = DmSansFamily
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = TerracottaPale.copy(alpha = 0.55f),
            selectedContainerColor = SagePale,
            labelColor = Charcoal60,
            selectedLabelColor = Charcoal
        ),
        shape = RoundedCornerShape(Radius.full),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = TerracottaPale.copy(alpha = 0f),
            selectedBorderColor = SagePale.copy(alpha = 0f)
        )
    )
}
