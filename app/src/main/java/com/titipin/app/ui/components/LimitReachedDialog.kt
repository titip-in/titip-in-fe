package com.titipin.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.titipin.app.ui.theme.*

/**
 * Dialog cantik yang muncul saat user mencapai batas limit aktif (5/5).
 * Digunakan di JastipScreen, PrelovedScreen, JastipSayaScreen, dan PrelovedSayaScreen.
 */
@Composable
fun LimitReachedDialog(
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.xl))
                .background(Cream)
                .padding(Spacing.lg)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(TerracottaPale),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚧", fontSize = 30.sp)
                }

                // Title
                Text(
                    text = "Limit Tercapai",
                    fontFamily = FrauncesFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = Charcoal,
                    textAlign = TextAlign.Center
                )

                // Message
                Text(
                    text = message.ifBlank {
                        "Kamu sudah mencapai batas 5 item aktif. Tutup atau hapus salah satu item aktif sebelum membuat yang baru."
                    },
                    fontFamily = DmSansFamily,
                    fontSize = 13.sp,
                    color = Charcoal60,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(Spacing.xs))

                // Tombol
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ComponentSize.buttonHeight),
                    shape = RoundedCornerShape(Radius.full),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Charcoal,
                        contentColor = Cream
                    )
                ) {
                    Text(
                        "Mengerti",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
