package com.titipin.app.shared

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.titipin.app.ui.theme.*

/**
 * Satu item gambar: bisa berupa Uri lokal (belum upload) atau URL remote (sudah diupload).
 */
sealed class ImageItem {
    data class Local(val uri: Uri, val isUploading: Boolean = false) : ImageItem()
    data class Remote(val url: String) : ImageItem()
}

/**
 * Row horizontal berisi thumbnail gambar yang sudah dipilih + tombol tambah.
 * Mendukung multi-select dari photo picker bawaan Android 13+.
 *
 * @param images          State list gambar saat ini
 * @param onPickImages    Dipanggil saat user memilih gambar baru
 * @param onRemove        Dipanggil saat user tap × di thumbnail
 * @param maxImages       Max jumlah gambar (default 5 sesuai API limit)
 * @param thumbnailSize   Ukuran thumbnail (default 80dp)
 * @param label           Label section di atas row (opsional)
 */
@Composable
fun ListingImagePickerRow(
    images: List<ImageItem>,
    onPickImages: (List<Uri>) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxImages: Int = 5,
    thumbnailSize: Dp = 80.dp,
    label: String = "FOTO"
) {
    val canAdd = images.size < maxImages

    // Photo picker (Android 13+ Media Picker)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxImages)
    ) { uris ->
        if (uris.isNotEmpty()) onPickImages(uris)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                fontSize = 9.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp, color = Charcoal60, fontFamily = DmSansFamily
            )
            Text(
                "${images.size}/$maxImages",
                fontSize = 10.sp, color = Charcoal30, fontFamily = DmSansFamily
            )
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Existing images
            itemsIndexed(images) { index, item ->
                Box(
                    modifier = Modifier
                        .size(thumbnailSize)
                        .clip(RoundedCornerShape(Radius.md))
                ) {
                    when (item) {
                        is ImageItem.Local -> {
                            AsyncImage(
                                model           = item.uri,
                                contentDescription = "Foto ${index + 1}",
                                contentScale    = ContentScale.Crop,
                                modifier        = Modifier.fillMaxSize()
                            )
                            if (item.isUploading) {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.45f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Cream, strokeWidth = 2.dp,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        is ImageItem.Remote -> {
                            AsyncImage(
                                model           = item.url,
                                contentDescription = "Foto ${index + 1}",
                                contentScale    = ContentScale.Crop,
                                modifier        = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Remove button — × di pojok kanan atas
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { onRemove(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("×", fontSize = 12.sp, color = Color.White, fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Bold, lineHeight = 12.sp)
                    }

                    // Primary badge untuk gambar pertama
                    if (index == 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Terracotta.copy(alpha = 0.85f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("UTAMA", fontSize = 7.sp, color = Color.White,
                                fontFamily = DmSansFamily, fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp)
                        }
                    }
                }
            }

            // Add button
            if (canAdd) {
                item {
                    Box(
                        modifier = Modifier
                            .size(thumbnailSize)
                            .clip(RoundedCornerShape(Radius.md))
                            .border(1.5.dp, Terracotta.copy(alpha = 0.5f), RoundedCornerShape(Radius.md))
                            .background(TerracottaPale.copy(alpha = 0.3f))
                            .clickable {
                                launcher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("+", fontSize = 24.sp, color = Terracotta, fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Light, lineHeight = 24.sp)
                            Text("Tambah", fontSize = 8.sp, color = Terracotta,
                                fontFamily = DmSansFamily)
                        }
                    }
                }
            }
        }

        // Hint text
        Text(
            "Foto pertama jadi sampul utama. Maks $maxImages foto.",
            fontSize = 10.sp, color = Charcoal30, fontFamily = DmSansFamily
        )
    }
}
