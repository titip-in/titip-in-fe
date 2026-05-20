package com.titipin.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.data.model.UserTier
import com.titipin.app.data.model.normalizedTier
import com.titipin.app.data.model.tierActiveLimit
import com.titipin.app.data.model.tierBoostLimit
import com.titipin.app.data.model.tierDisplayName
import com.titipin.app.data.model.tierImageLimit
import com.titipin.app.shared.formatDateDisplay
import com.titipin.app.shared.openWhatsApp
import com.titipin.app.ui.theme.Charcoal
import com.titipin.app.ui.theme.Charcoal10
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

private const val ADMIN_WA_NUMBER = "085750583867"

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
        fontFamily = DmSansFamily,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
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
        fontFamily = DmSansFamily,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun PlanSummaryPanel(
    tier: String?,
    boostQuota: Int,
    tierExpiredAt: String? = null,
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
                    text = if (normalized == UserTier.BASIC) {
                        "Upgrade untuk boost dan limit lebih besar"
                    } else {
                        tierExpiredAt?.let { "Aktif sampai ${formatDateDisplay(it, includeYear = true)}" } ?: "Plan aktif kamu saat ini"
                    },
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
fun HomePlanBanner(
    tier: String?,
    boostQuota: Int,
    activeLimit: Int,
    activeCount: Int,
    tierExpiredAt: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val normalized = tier.normalizedTier()
    val isBasic = normalized == UserTier.BASIC
    val title = if (isBasic) "Dapatkan Akses Tanpa Batas" else tierDisplayName(tier)
    val subtitle = if (isBasic) {
        "Upgrade ke Plus atau Pro untuk menaikkan limit, badge tier, dan kuota boost bulanan."
    } else {
        val expiry = tierExpiredAt?.let { "Aktif sampai ${formatDateDisplay(it, includeYear = true)}. " }.orEmpty()
        "${expiry}Limit $activeLimit item aktif per kategori dan sisa boost $boostQuota."
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.lg))
            .background(Charcoal)
            .clickable { onClick() }
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TierBadge(if (isBasic) UserTier.BASIC else normalized)
            Text(
                text = if (isBasic) "Limit: $activeLimit item aktif" else "${activeCount}/$activeLimit aktif",
                fontSize = 10.sp,
                color = Cream.copy(alpha = 0.65f),
                fontFamily = DmSansFamily
            )
        }
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Cream, fontFamily = DmSansFamily)
        Text(subtitle, fontSize = 11.sp, color = Cream.copy(alpha = 0.72f), lineHeight = 16.sp, fontFamily = DmSansFamily)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.full))
                .background(if (isBasic) Gold else Cream.copy(alpha = 0.12f))
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isBasic) "Upgrade Sekarang" else "Kelola Keanggotaan",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isBasic) Cream else Cream,
                fontFamily = DmSansFamily
            )
        }
    }
}

@Composable
fun UpgradePlanComparison(
    currentTier: String?,
    modifier: Modifier = Modifier,
    onUpgradeClick: ((tier: String) -> Unit)? = null
) {
    val current = currentTier.normalizedTier()
    val plans = listOf(
        PlanOption(
            tier = UserTier.BASIC,
            price = "Gratis",
            features = listOf("3 item aktif per kategori", "Fitur dasar listing & request", "Chat via WhatsApp"),
            disabledFeatures = listOf("Badge tier", "Boost listing", "Analitik"),
            isCurrent = current == UserTier.BASIC,
            canUpgrade = false
        ),
        PlanOption(
            tier = UserTier.PLUS,
            price = "Rp10rb/bulan",
            features = listOf("10 item aktif per kategori", "1 boost quota per bulan", "Badge Plus di listing", "Analitik views & klik WA"),
            disabledFeatures = listOf("Listing terbaik & konversi"),
            isCurrent = current == UserTier.PLUS,
            canUpgrade = current == UserTier.BASIC
        ),
        PlanOption(
            tier = UserTier.PRO,
            price = "Rp25rb/bulan",
            features = listOf("20 item aktif per kategori", "5 boost quota per bulan", "Badge Pro di listing", "Analitik lengkap + konversi", "Kuota foto lebih banyak"),
            disabledFeatures = emptyList(),
            isCurrent = current == UserTier.PRO,
            canUpgrade = current != UserTier.PRO
        )
    )

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Upgrade Plan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Charcoal,
                fontFamily = com.titipin.app.ui.theme.FrauncesFamily,
                modifier = Modifier.weight(1f)
            )
            if (current == UserTier.BASIC) {
                Text(
                    "Buka fitur",
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.full))
                        .background(Terracotta)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cream,
                    fontFamily = DmSansFamily
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cardWidth = (maxWidth * 0.86f).coerceAtMost(330.dp)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(end = Spacing.sm)
            ) {
                items(plans) { plan ->
                    PlanCard(
                        plan = plan,
                        modifier = Modifier.width(cardWidth),
                        onUpgradeClick = onUpgradeClick
                    )
                }
            }
        }
        Text(
            "Bayar via QRIS lalu upload bukti transfer. Admin akan memproses upgrade dalam 1×24 jam.",
            fontSize = 10.sp,
            color = Charcoal60,
            lineHeight = 15.sp,
            fontFamily = DmSansFamily
        )
    }
}

private data class PlanOption(
    val tier: String,
    val price: String,
    val features: List<String>,
    val disabledFeatures: List<String>,
    val isCurrent: Boolean,
    val canUpgrade: Boolean
)

@Composable
private fun PlanCard(
    plan: PlanOption,
    modifier: Modifier = Modifier,
    onUpgradeClick: ((tier: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val normalized = plan.tier.normalizedTier()
    val headerBrush = when (normalized) {
        UserTier.PLUS -> Brush.horizontalGradient(listOf(Color(0xFF6D5BD0), Color(0xFF8A5CF6)))
        UserTier.PRO  -> Brush.horizontalGradient(listOf(Color(0xFFFFA51E), Color(0xFFFF7A1A)))
        else          -> Brush.horizontalGradient(listOf(CreamDark, CreamDark))
    }
    val borderColor = when {
        plan.isCurrent            -> Sage
        normalized == UserTier.PLUS -> Color(0xFF8A5CF6)
        normalized == UserTier.PRO  -> Color(0xFFFFA51E)
        else                      -> Charcoal10
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.md))
            .background(Cream)
            .border(1.dp, borderColor, RoundedCornerShape(Radius.md))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBrush)
                .padding(Spacing.md)
        ) {
            Text(tierDisplayName(plan.tier), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (normalized == UserTier.BASIC) Charcoal else Cream, fontFamily = DmSansFamily)
            Text(plan.price, fontSize = 11.sp, color = if (normalized == UserTier.BASIC) Charcoal60 else Cream.copy(alpha = 0.8f), fontFamily = DmSansFamily)
        }
        Column(modifier = Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            plan.features.forEach {
                Text("✓ $it", fontSize = 12.sp, color = Charcoal, lineHeight = 16.sp, fontFamily = DmSansFamily)
            }
            plan.disabledFeatures.forEach {
                Text(it, fontSize = 12.sp, color = Charcoal60.copy(alpha = 0.55f), fontFamily = DmSansFamily)
            }
            Button(
                onClick = {
                    if (plan.canUpgrade) {
                        if (onUpgradeClick != null) {
                            onUpgradeClick(plan.tier)
                        } else {
                            // Fallback ke WA jika tidak ada handler
                            openWhatsApp(context, ADMIN_WA_NUMBER, "Halo admin Titip.in, saya ingin upgrade ke ${tierDisplayName(plan.tier)}.")
                        }
                    }
                },
                enabled = plan.canUpgrade,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.full),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (normalized == UserTier.PRO) Color(0xFFFFA51E) else if (normalized == UserTier.PLUS) Color(0xFF6D5BD0) else SagePale,
                    contentColor = Cream,
                    disabledContainerColor = if (plan.isCurrent) SagePale else CreamDark,
                    disabledContentColor = if (plan.isCurrent) Sage else Charcoal60
                )
            ) {
                Text(
                    if (plan.isCurrent) "Plan Saat Ini" else if (plan.canUpgrade) "Upgrade" else "Tersedia",
                    fontSize = 12.sp,
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
