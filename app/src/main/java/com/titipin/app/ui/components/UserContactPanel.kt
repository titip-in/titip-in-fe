package com.titipin.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.titipin.app.shared.openWhatsApp
import com.titipin.app.ui.theme.Charcoal
import com.titipin.app.ui.theme.Charcoal60
import com.titipin.app.ui.theme.Cream
import com.titipin.app.ui.theme.CreamDark
import com.titipin.app.ui.theme.DmSansFamily
import com.titipin.app.ui.theme.Radius
import com.titipin.app.ui.theme.Sage
import com.titipin.app.ui.theme.Spacing
import com.titipin.app.ui.theme.Terracotta
import com.titipin.app.ui.theme.WarmWhite

@Composable
fun UserContactPanel(
    name: String,
    waNumber: String?,
    modifier: Modifier = Modifier,
    status: String? = null,
    avatarUrl: String? = null,
    isOwner: Boolean = false,
    ownerLabel: String = "Ini listing Anda",
    message: String = ""
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(WarmWhite)
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        UserAvatar(name = name, avatarUrl = avatarUrl)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Charcoal,
                fontFamily = DmSansFamily
            )
            Text(
                text = if (isOwner) ownerLabel else status?.takeIf { it.isNotBlank() } ?: "Pengguna Titip.in",
                fontSize = 11.sp,
                color = Charcoal60,
                fontFamily = DmSansFamily
            )
        }

        if (!isOwner && !waNumber.isNullOrBlank()) {
            Button(
                onClick = { openWhatsApp(context, waNumber, message) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Terracotta,
                    contentColor = Cream
                ),
                shape = RoundedCornerShape(Radius.full)
            ) {
                Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = DmSansFamily)
            }
        }
    }
}

@Composable
private fun UserAvatar(
    name: String,
    avatarUrl: String?
) {
    val initials = name.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "T" }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(CreamDark),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl.isNullOrBlank()) {
            Text(
                text = initials,
                color = Sage,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DmSansFamily
            )
        } else {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Avatar $name",
                modifier = Modifier.size(44.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}
