package com.titipin.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.data.model.UserTier
import com.titipin.app.data.model.normalizedTier
import com.titipin.app.data.model.tierActiveLimit
import com.titipin.app.data.model.tierBoostLimit
import com.titipin.app.data.model.tierDisplayName
import com.titipin.app.data.model.tierImageLimit
import com.titipin.app.ui.theme.Charcoal
import com.titipin.app.ui.theme.Charcoal60
import com.titipin.app.ui.theme.Cream
import com.titipin.app.ui.theme.CreamDark
import com.titipin.app.ui.theme.DmSansFamily
import com.titipin.app.ui.theme.Gold
import com.titipin.app.ui.theme.GoldPale
import com.titipin.app.ui.theme.Radius
import com.titipin.app.ui.theme.Sage
import com.titipin.app.ui.theme.SagePale
import com.titipin.app.ui.theme.Spacing
import com.titipin.app.ui.theme.Terracotta
import com.titipin.app.ui.theme.TerracottaPale

@Composable
fun TierBadge(
    tier: String?,
    modifier: Modifier = Modifier,
    showBasic: Boolean = true
) {
    val normalized = tier.normalizedTier()
    if (!showBasic && normalized == UserTier.BASIC) return

    val (label, colors, textColor) = when (normalized) {
        UserTier.PLUS -> Triple("Titip Plus", listOf(Color(0xFF6D5BD0), Color(0xFF9D7AEA)), Cream)
        UserTier.PRO -> Triple("Titip Pro", listOf(Gold, Color(0xFFE8C66A)), Charcoal)
        else -> Triple("Titip Basic", listOf(CreamDark, CreamDark), Charcoal60)
    }

    Text(
        text = label,
        modifier = modifier
            .clip(RoundedCornerShape(Radius.full))
            .background(Brush.horizontalGradient(colors))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = textColor,
        fontFamily = DmSansFamily
    )
}

@Composable
fun BoostedBadge(modifier: Modifier = Modifier) {
    Text(
        text = "Dipromosikan",
        modifier = modifier
            .clip(RoundedCornerShape(Radius.full))
            .background(Terracotta)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Cream,
        fontFamily = DmSansFamily
    )
}

@Composable
fun PlanSummaryPanel(
    tier: String?,
    boostQuota: Int,
    modifier: Modifier = Modifier
) {
    val normalized = tier.normalizedTier()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(if (normalized == UserTier.BASIC) TerracottaPale else SagePale)
            .border(
                1.dp,
                if (normalized == UserTier.BASIC) Terracotta.copy(alpha = 0.35f) else Sage.copy(alpha = 0.35f),
                RoundedCornerShape(Radius.md)
            )
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = tierDisplayName(tier),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    fontFamily = DmSansFamily
                )
                Text(
                    text = if (normalized == UserTier.BASIC) "Upgrade untuk boost dan limit lebih besar" else "Plan aktif kamu saat ini",
                    fontSize = 11.sp,
                    color = Charcoal60,
                    fontFamily = DmSansFamily
                )
            }
            TierBadge(tier)
        }
        Spacer(Modifier.height(2.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            PlanMetric("Item aktif", tierActiveLimit(tier).toString(), Modifier.weight(1f))
            PlanMetric("Boost", "$boostQuota/${tierBoostLimit(tier)}", Modifier.weight(1f))
            PlanMetric("Foto", tierImageLimit(tier).toString(), Modifier.weight(1f))
        }
        if (normalized == UserTier.BASIC) {
            Text(
                text = "Plus Rp10rb/bulan: 10 item aktif + 1 boost. Pro Rp25rb/bulan: 20 item aktif + 5 boost.",
                fontSize = 11.sp,
                color = Charcoal60,
                lineHeight = 16.sp,
                fontFamily = DmSansFamily
            )
        }
    }
}

@Composable
private fun PlanMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.sm))
            .background(Cream.copy(alpha = 0.65f))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Charcoal, fontFamily = DmSansFamily)
        Text(label, fontSize = 9.sp, color = Charcoal60, fontFamily = DmSansFamily)
    }
}
