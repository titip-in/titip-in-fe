package com.titipin.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.ui.theme.Charcoal
import com.titipin.app.ui.theme.Charcoal10
import com.titipin.app.ui.theme.DmSansFamily
import com.titipin.app.ui.theme.Gold
import com.titipin.app.ui.theme.GoldPale
import com.titipin.app.ui.theme.Radius
import com.titipin.app.ui.theme.Sage
import com.titipin.app.ui.theme.SagePale
import com.titipin.app.ui.theme.Terracotta
import com.titipin.app.ui.theme.TerracottaPale

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val spec = statusSpec(status)
    Box(
        modifier = modifier
            .background(spec.background, RoundedCornerShape(Radius.full))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = spec.label,
            color = spec.foreground,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = DmSansFamily,
            letterSpacing = 0.5.sp
        )
    }
}

private data class StatusSpec(
    val label: String,
    val background: Color,
    val foreground: Color
)

private fun statusSpec(status: String): StatusSpec {
    return when (status.uppercase()) {
        "ACTIVE" -> StatusSpec("AKTIF", SagePale, Sage)
        "AVAILABLE" -> StatusSpec("TERSEDIA", SagePale, Sage)
        "OPEN" -> StatusSpec("TERBUKA", SagePale, Sage)
        "SOLD" -> StatusSpec("TERJUAL", TerracottaPale, Terracotta)
        "HIDDEN" -> StatusSpec("DISEMBUNYIKAN", GoldPale, Gold)
        "CLOSED" -> StatusSpec("DITUTUP", Charcoal10, Charcoal)
        else -> StatusSpec(status.uppercase(), Charcoal10, Charcoal)
    }
}
