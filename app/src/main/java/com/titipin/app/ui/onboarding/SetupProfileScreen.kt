package com.titipin.app.ui.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.titipin.app.ui.auth.TitipinTextField
import com.titipin.app.ui.profile.ProfileUiState
import com.titipin.app.ui.profile.ProfileViewModel
import com.titipin.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SetupProfileScreen(
    onFinish: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isUploadingAvatar by viewModel.isUploadingAvatar.collectAsState()
    val isUpdatingProfile by viewModel.isUpdatingProfile.collectAsState()
    val user = (uiState as? ProfileUiState.Success)?.user

    var name by remember(user?.id) { mutableStateOf(user?.name.orEmpty()) }
    var waNumber by remember(user?.id) { mutableStateOf(user?.waNumber.orEmpty()) }
    var status by remember(user?.id) { mutableStateOf(user?.status.orEmpty()) }
    var localAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var showOtpDialog by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }
    var otpCooldown by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf<String?>(null) }
    var shouldFinishAfterSave by remember { mutableStateOf(false) }

    LaunchedEffect(otpCooldown) {
        if (otpCooldown > 0) {
            delay(1000)
            otpCooldown -= 1
        }
    }

    LaunchedEffect(uiState, shouldFinishAfterSave) {
        val current = (uiState as? ProfileUiState.Success)?.user
        if (shouldFinishAfterSave && current != null && !isUpdatingProfile) {
            shouldFinishAfterSave = false
            onFinish()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                localAvatarUri = uri
                viewModel.uploadAvatar(uri)
            }
        }
    )

    val emailVerified = !user?.emailVerifiedAt.isNullOrBlank()
    val waVerified = !user?.waVerifiedAt.isNullOrBlank()
    val canSave = emailVerified && waVerified && name.isNotBlank() && !isUploadingAvatar && !isUpdatingProfile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Spacing.xl))
        Text(
            text = "Lengkapi Profil",
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            color = Charcoal,
            fontFamily = FrauncesFamily,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = "Profil wajib dilengkapi sebelum masuk dashboard.",
            fontSize = 13.sp,
            color = Charcoal60,
            fontFamily = DmSansFamily,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(CreamDark)
                .clickable {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            contentAlignment = Alignment.Center
        ) {
            val displayUrl = localAvatarUri ?: user?.avatarUrl
            if (displayUrl != null) {
                AsyncImage(
                    model = displayUrl,
                    contentDescription = "Foto profil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = name.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Terracotta
                )
            }

            Box(
                modifier = Modifier.fillMaxSize().background(Charcoal.copy(alpha = if (isUploadingAvatar) 0.5f else 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (isUploadingAvatar) {
                    CircularProgressIndicator(color = Cream, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = "Ganti Foto", tint = Cream, modifier = Modifier.size(30.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        TitipinTextField(
            value = name,
            onValueChange = { name = it },
            label = "NAMA",
            placeholder = "Nama lengkap",
            isFocused = name.isNotBlank()
        )

        ReadOnlyField(
            label = "EMAIL",
            value = user?.email.orEmpty(),
            badge = if (emailVerified) "Verified" else "Belum verified",
            badgeColor = if (emailVerified) Sage else Terracotta
        )

        if (!emailVerified) {
            OutlinedButton(
                onClick = {
                    viewModel.resendEmailVerification { success, text ->
                        message = text
                        if (success) viewModel.loadProfile()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUpdatingProfile,
                shape = RoundedCornerShape(Radius.full)
            ) {
                Text("Kirim ulang email verifikasi", fontFamily = DmSansFamily)
            }
        }

        TitipinTextField(
            value = waNumber,
            onValueChange = { waNumber = it },
            label = "NO. WHATSAPP",
            placeholder = "08xxxxxxxxxx",
            keyboardType = KeyboardType.Phone,
            isFocused = waNumber.isNotBlank()
        )

        Button(
            onClick = {
                viewModel.requestWaOtpForNumber(waNumber.trim()) { success, text ->
                    message = text
                    if (success) {
                        otp = ""
                        otpCooldown = 60
                        showOtpDialog = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = waNumber.length >= 10 && !waVerified && otpCooldown == 0 && !isUpdatingProfile,
            shape = RoundedCornerShape(Radius.full),
            colors = ButtonDefaults.buttonColors(containerColor = if (waVerified) Sage else Charcoal)
        ) {
            val label = when {
                waVerified -> "WhatsApp Terverifikasi"
                otpCooldown > 0 -> "Kirim OTP lagi dalam ${otpCooldown}s"
                else -> "Verifikasi WhatsApp"
            }
            Text(label, fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold, color = Cream)
        }

        TitipinTextField(
            value = status,
            onValueChange = { status = it },
            label = "BIO (OPSIONAL)",
            placeholder = "Cth: Suka titip skincare Korea",
            isFocused = status.isNotBlank()
        )

        message?.let {
            Text(it, fontSize = 12.sp, color = Charcoal60, fontFamily = DmSansFamily, modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        Button(
            onClick = {
                shouldFinishAfterSave = true
                viewModel.updateProfile(
                    name = name.trim(),
                    waNumber = waNumber.trim(),
                    status = status.trim().ifEmpty { null }
                )
            },
            modifier = Modifier.fillMaxWidth().height(ComponentSize.buttonHeight),
            enabled = canSave,
            shape = RoundedCornerShape(Radius.full),
            colors = ButtonDefaults.buttonColors(
                containerColor = Charcoal,
                contentColor = Cream,
                disabledContainerColor = Charcoal10,
                disabledContentColor = Charcoal30
            )
        ) {
            if (isUpdatingProfile) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Cream, strokeWidth = 2.dp)
            } else {
                Text("Simpan dan Masuk", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = DmSansFamily)
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))
    }

    if (showOtpDialog) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            title = { Text("Masukkan OTP", fontFamily = FrauncesFamily, color = Charcoal) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text("Kode OTP dikirim ke WhatsApp kamu.", fontFamily = DmSansFamily, color = Charcoal60)
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { otp = it.filter { char -> char.isDigit() }.take(6) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("123456", fontFamily = DmSansFamily) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(Radius.md)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.verifyWaOtp(otp) { success, text ->
                            message = text
                            if (success) {
                                showOtpDialog = false
                                viewModel.loadProfile()
                            }
                        }
                    },
                    enabled = otp.length == 6
                ) {
                    Text("Verifikasi", color = Terracotta, fontFamily = DmSansFamily)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.requestWaOtpForNumber(waNumber.trim()) { success, text ->
                            message = text
                            if (success) otpCooldown = 60
                        }
                    },
                    enabled = otpCooldown == 0
                ) {
                    Text(
                        if (otpCooldown > 0) "Kirim ulang (${otpCooldown}s)" else "Kirim ulang",
                        color = if (otpCooldown > 0) Charcoal30 else Charcoal,
                        fontFamily = DmSansFamily
                    )
                }
            },
            containerColor = Cream
        )
    }
}

@Composable
private fun ReadOnlyField(
    label: String,
    value: String,
    badge: String,
    badgeColor: androidx.compose.ui.graphics.Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = Charcoal60,
            fontFamily = DmSansFamily,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.md))
                .background(CreamDark)
                .padding(horizontal = Spacing.md, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(value.ifBlank { "-" }, fontSize = 13.sp, color = Charcoal, fontFamily = DmSansFamily)
            Text(badge, fontSize = 11.sp, color = badgeColor, fontWeight = FontWeight.SemiBold, fontFamily = DmSansFamily)
        }
    }
}
