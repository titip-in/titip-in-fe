package com.titipin.app.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.titipin.app.R
import com.titipin.app.data.model.UserTier
import com.titipin.app.data.model.tierDisplayName
import com.titipin.app.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Bottom sheet multi-step untuk upgrade subscription via QRIS.
 *
 * Step 1: Tampilkan QRIS statis + instruksi bayar
 * Step 2: Upload bukti bayar (photo picker → upload API)
 * Step 3: Kirim POST /me/subscriptions/upgrade
 *
 * @param targetTier Tier yang ingin di-upgrade ("plus" | "pro")
 * @param onDismiss Dipanggil ketika sheet di-dismiss
 * @param onUpload Dipanggil saat user memilih foto resi, return URL hasil upload atau null jika gagal
 * @param onSubmit Dipanggil saat user submit dengan (tier, paymentProofUrl)
 * @param isUploading Loading state saat upload foto resi
 * @param isSubmitting Loading state saat submit upgrade
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeSubscriptionSheet(
    targetTier: String,
    onDismiss: () -> Unit,
    onUpload: suspend (uri: android.net.Uri) -> String?,
    onSubmit: (tier: String, paymentProofUrl: String) -> Unit,
    isUploading: Boolean = false,
    isSubmitting: Boolean = false
) {
    var step by remember { mutableIntStateOf(1) }
    var proofUrl by remember { mutableStateOf<String?>(null) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                uploadError = null
                scope.launch {
                    val url = onUpload(uri)
                    if (url != null) {
                        proofUrl = url
                        step = 3
                    } else {
                        uploadError = "Gagal upload gambar, coba lagi"
                    }
                }
            }
        }
    )

    val tierName = tierDisplayName(targetTier)
    val tierPrice = if (targetTier == UserTier.PRO) "Rp25.000" else "Rp10.000"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Cream,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg)
                .padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Upgrade ke $tierName",
                        fontFamily = FrauncesFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = Charcoal
                    )
                    Text(
                        "$tierPrice / bulan",
                        fontFamily = DmSansFamily,
                        fontSize = 13.sp,
                        color = Charcoal60
                    )
                }
                TierBadge(targetTier)
            }

            // Step indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(1, 2, 3).forEach { s ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(Radius.full))
                            .background(if (step >= s) Terracotta else Charcoal10)
                    )
                }
            }

            when (step) {
                // ── STEP 1: QRIS ─────────────────────────────────────────────
                1 -> {
                    Text(
                        "Scan QR di bawah ini dengan aplikasi pembayaran favoritmu (GoPay, OVO, DANA, dll.)",
                        fontFamily = DmSansFamily,
                        fontSize = 13.sp,
                        color = Charcoal60,
                        lineHeight = 19.sp
                    )

                    // QRIS Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.lg))
                            .border(1.dp, Charcoal10, RoundedCornerShape(Radius.lg))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.qris),
                            contentDescription = "QRIS Titip.in",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp),
                            contentScale = ContentScale.FillWidth
                        )
                    }

                    // Info box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(SagePale)
                            .border(1.dp, Sage.copy(alpha = 0.4f), RoundedCornerShape(Radius.md))
                            .padding(Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("ℹ️", fontSize = 16.sp)
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                "Nominal yang dibayar: $tierPrice",
                                fontFamily = DmSansFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Charcoal
                            )
                            Text(
                                "Setelah bayar, simpan screenshot bukti transfer kamu.",
                                fontFamily = DmSansFamily,
                                fontSize = 11.sp,
                                color = Charcoal60,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Button(
                        onClick = { step = 2 },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.full),
                        colors = ButtonDefaults.buttonColors(containerColor = Charcoal)
                    ) {
                        Text(
                            "Sudah Bayar, Upload Bukti",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Cream
                        )
                    }
                }

                // ── STEP 2: UPLOAD RESI ────────────────────────────────────
                2 -> {
                    Text(
                        "Upload screenshot atau foto bukti pembayaran QRIS kamu.",
                        fontFamily = DmSansFamily,
                        fontSize = 13.sp,
                        color = Charcoal60,
                        lineHeight = 19.sp
                    )

                    // Upload area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(Radius.lg))
                            .background(CreamDark)
                            .border(
                                width = 1.5.dp,
                                color = if (uploadError != null) Terracotta else Charcoal10,
                                shape = RoundedCornerShape(Radius.lg)
                            )
                            .clickable(enabled = !isUploading) {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📸", fontSize = 32.sp)
                                Text(
                                    "Ketuk untuk pilih foto",
                                    fontFamily = DmSansFamily,
                                    fontSize = 13.sp,
                                    color = Charcoal60
                                )
                            }
                        }
                    }

                    if (uploadError != null) {
                        Text(
                            "⚠️ $uploadError",
                            fontFamily = DmSansFamily,
                            fontSize = 12.sp,
                            color = Terracotta
                        )
                    }

                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUploading,
                        shape = RoundedCornerShape(Radius.full),
                        colors = ButtonDefaults.buttonColors(containerColor = Charcoal)
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                color = Cream,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                "Pilih Foto Bukti",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = Cream
                            )
                        }
                    }

                    TextButton(
                        onClick = { step = 1 },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("← Kembali ke QR", fontFamily = DmSansFamily, color = Charcoal60)
                    }
                }

                // ── STEP 3: KONFIRMASI & SUBMIT ────────────────────────────
                3 -> {
                    Text(
                        "Bukti pembayaran berhasil diupload. Konfirmasi untuk mengirim permintaan upgrade.",
                        fontFamily = DmSansFamily,
                        fontSize = 13.sp,
                        color = Charcoal60,
                        lineHeight = 19.sp
                    )

                    // Preview resi
                    proofUrl?.let { url ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(Radius.lg))
                                .background(CreamDark),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Bukti pembayaran",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Summary card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(SagePale)
                            .border(1.dp, Sage.copy(alpha = 0.4f), RoundedCornerShape(Radius.md))
                            .padding(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("✅ Bukti terupload", fontFamily = DmSansFamily, fontSize = 12.sp, color = Charcoal)
                        Text("📦 Tier: $tierName", fontFamily = DmSansFamily, fontSize = 12.sp, color = Charcoal)
                        Text("💰 Nominal: $tierPrice/bulan", fontFamily = DmSansFamily, fontSize = 12.sp, color = Charcoal)
                        Text(
                            "Admin akan memverifikasi dan mengaktifkan plan kamu dalam 1×24 jam.",
                            fontFamily = DmSansFamily,
                            fontSize = 11.sp,
                            color = Charcoal60,
                            lineHeight = 16.sp
                        )
                    }

                    Button(
                        onClick = {
                            proofUrl?.let { url -> onSubmit(targetTier, url) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = proofUrl != null && !isSubmitting,
                        shape = RoundedCornerShape(Radius.full),
                        colors = ButtonDefaults.buttonColors(containerColor = Charcoal)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Cream,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                "Kirim Permintaan Upgrade",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = Cream
                            )
                        }
                    }

                    TextButton(
                        onClick = { step = 2 },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("← Upload ulang foto", fontFamily = DmSansFamily, color = Charcoal60)
                    }
                }
            }
        }
    }
}
