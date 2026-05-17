package com.titipin.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.titipin.app.data.model.ListingImageDto
import com.titipin.app.ui.theme.Charcoal10
import com.titipin.app.ui.theme.Charcoal30
import com.titipin.app.ui.theme.Charcoal60
import com.titipin.app.ui.theme.CreamDark
import com.titipin.app.ui.theme.DmSansFamily
import com.titipin.app.ui.theme.Radius
import com.titipin.app.ui.theme.Spacing
import com.titipin.app.ui.theme.Terracotta

@Composable
fun DetailImageGallery(
    images: List<ListingImageDto>,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    var selectedIndex by remember(images) {
        mutableIntStateOf(images.indexOfFirst { it.isPrimary }.takeIf { it >= 0 } ?: 0)
    }
    val selectedImage = images.getOrNull(selectedIndex)

    androidx.compose.foundation.layout.Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(Radius.lg))
                .background(CreamDark),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImage == null) {
                Text(
                    text = "Belum ada foto",
                    color = Charcoal60,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = DmSansFamily
                )
            } else {
                AsyncImage(
                    model = selectedImage.imageUrl,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (images.size > 1) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                itemsIndexed(images) { index, image ->
                    val selected = selectedIndex == index
                    AsyncImage(
                        model = image.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(Radius.sm))
                            .background(Charcoal10)
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) Terracotta else Charcoal30,
                                shape = RoundedCornerShape(Radius.sm)
                            )
                            .clickable { selectedIndex = index }
                            .padding(if (selected) 2.dp else 0.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
