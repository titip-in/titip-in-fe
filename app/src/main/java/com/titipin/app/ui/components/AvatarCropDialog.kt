package com.titipin.app.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.ui.theme.Charcoal
import com.titipin.app.ui.theme.Charcoal60
import com.titipin.app.ui.theme.Cream
import com.titipin.app.ui.theme.DmSansFamily
import com.titipin.app.ui.theme.FrauncesFamily
import com.titipin.app.ui.theme.Radius
import com.titipin.app.ui.theme.Spacing
import com.titipin.app.ui.theme.Terracotta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun AvatarCropDialog(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onCrop: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, imageUri) {
        value = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(imageUri)?.use { BitmapFactory.decodeStream(it) }
        }
    }
    val cropSize = 280.dp
    val cropPx = with(LocalDensity.current) { cropSize.toPx() }
    var scale by remember(imageUri) { mutableFloatStateOf(1f) }
    var offset by remember(imageUri) { mutableStateOf(Offset.Zero) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Cream,
        shape = RoundedCornerShape(Radius.xl),
        title = {
            Text(
                "Crop Foto Profil",
                fontFamily = FrauncesFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Charcoal
            )
        },
        text = {
            Column {
                Text(
                    "Geser dan pinch untuk mengatur posisi foto.",
                    fontFamily = DmSansFamily,
                    fontSize = 12.sp,
                    color = Charcoal60
                )
                Spacer(Modifier.height(Spacing.md))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(cropSize)
                            .clip(RoundedCornerShape(Radius.lg))
                            .background(Charcoal)
                            .border(2.dp, Terracotta, RoundedCornerShape(Radius.lg)),
                        contentAlignment = Alignment.Center
                    ) {
                        val source = bitmap
                        if (source == null) {
                            CircularProgressIndicator(color = Terracotta)
                        } else {
                            Image(
                                bitmap = source.asImageBitmap(),
                                contentDescription = "Foto yang akan dipotong",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = offset.x
                                        translationY = offset.y
                                    }
                                    .pointerInput(source, scale) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            val nextScale = (scale * zoom).coerceIn(1f, 4f)
                                            val baseScale = max(cropPx / source.width, cropPx / source.height)
                                            val maxX = ((source.width * baseScale * nextScale - cropPx) / 2f).coerceAtLeast(0f)
                                            val maxY = ((source.height * baseScale * nextScale - cropPx) / 2f).coerceAtLeast(0f)
                                            scale = nextScale
                                            offset = Offset(
                                                x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                                                y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                                            )
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    bitmap?.let { source ->
                        onCrop(cropAvatarBitmap(source, cropPx, scale, offset))
                    }
                },
                enabled = bitmap != null,
                shape = RoundedCornerShape(Radius.full),
                colors = ButtonDefaults.buttonColors(containerColor = Charcoal)
            ) {
                Text("Gunakan", fontFamily = DmSansFamily, color = Cream)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", fontFamily = DmSansFamily, color = Charcoal60)
            }
        }
    )
}

private fun cropAvatarBitmap(source: Bitmap, cropPx: Float, scale: Float, offset: Offset): Bitmap {
    val baseScale = max(cropPx / source.width, cropPx / source.height)
    val effectiveScale = baseScale * scale
    val cropSide = (cropPx / effectiveScale).coerceAtMost(minOf(source.width, source.height).toFloat())
    val left = ((source.width - cropSide) / 2f - offset.x / effectiveScale)
        .coerceIn(0f, source.width - cropSide)
    val top = ((source.height - cropSide) / 2f - offset.y / effectiveScale)
        .coerceIn(0f, source.height - cropSide)

    val cropped = Bitmap.createBitmap(
        source,
        left.roundToInt(),
        top.roundToInt(),
        cropSide.roundToInt().coerceAtLeast(1),
        cropSide.roundToInt().coerceAtLeast(1)
    )
    return Bitmap.createScaledBitmap(cropped, 720, 720, true).also {
        if (it != cropped) cropped.recycle()
    }
}
