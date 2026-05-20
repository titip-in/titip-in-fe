package com.titipin.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.titipin.app.data.model.ListingImageDto
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
    val pagerState = rememberPagerState(pageCount = { maxOf(1, images.size) })

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
            if (images.isEmpty()) {
                Text(
                    text = "Belum ada foto",
                    color = Charcoal60,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = DmSansFamily
                )
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val image = images.getOrNull(page)
                    if (image != null) {
                        AsyncImage(
                            model = image.imageUrl,
                            contentDescription = contentDescription,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(images.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .then(
                                if (pagerState.currentPage == index)
                                    Modifier.width(16.dp).height(4.dp)
                                else
                                    Modifier.size(4.dp)
                            )
                            .clip(RoundedCornerShape(Radius.full))
                            .background(
                                if (pagerState.currentPage == index) Terracotta
                                else Terracotta.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}
