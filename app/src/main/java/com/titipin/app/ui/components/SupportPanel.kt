package com.titipin.app.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.ui.theme.*

@Composable
fun SupportPanel() {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(CreamDark)
            .clickable {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:support@titipin.me")
                    putExtra(Intent.EXTRA_SUBJECT, "Bantuan Titipin App")
                }
                context.startActivity(Intent.createChooser(intent, "Kirim Email"))
            }
            .padding(Spacing.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(TerracottaPale),
                contentAlignment = Alignment.Center
            ) {
                Text("🎧", fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Butuh Bantuan?",
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = Charcoal, fontFamily = DmSansFamily
                )
                Text(
                    text = "Hubungi support@titipin.me untuk pelaporan masalah.",
                    fontSize = 11.sp, color = Charcoal60, fontFamily = DmSansFamily,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
