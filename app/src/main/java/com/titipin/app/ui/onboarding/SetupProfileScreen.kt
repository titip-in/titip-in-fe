package com.titipin.app.ui.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.titipin.app.ui.auth.TitipinTextField
import com.titipin.app.ui.profile.ProfileUiState
import com.titipin.app.ui.profile.ProfileViewModel
import com.titipin.app.ui.theme.*

@Composable
fun SetupProfileScreen(
    onFinish: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isUploadingAvatar by viewModel.isUploadingAvatar.collectAsState()
    val isUpdatingProfile by viewModel.isUpdatingProfile.collectAsState()

    var status by remember { mutableStateOf("") }
    var localAvatarUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                localAvatarUri = uri
                viewModel.uploadAvatar(uri)
            }
        }
    )

    // Optional: we can extract user info from state if loaded
    val user = (uiState as? ProfileUiState.Success)?.user

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
            text = "Hai, ${user?.name?.split(" ")?.firstOrNull() ?: "Titipers"}! 👋",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Charcoal,
            fontFamily = DmSansFamily,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = "Ayo lengkapi profilmu biar Titipers lain\nmudah mengenali kamu.",
            fontSize = 14.sp,
            color = Charcoal60,
            fontFamily = DmSansFamily,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Avatar Picker
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(CreamDark)
                .clickable {
                    photoPickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val displayUrl = localAvatarUri ?: user?.avatarUrl
            if (displayUrl != null) {
                AsyncImage(
                    model = displayUrl,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = user?.name?.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Terracotta
                )
            }

            if (isUploadingAvatar) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Charcoal.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Cream, modifier = Modifier.size(24.dp))
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Charcoal.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = "Ganti Foto",
                        tint = Cream.copy(alpha = 0.8f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = "Ganti Foto Profil",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Terracotta,
            fontFamily = DmSansFamily,
            modifier = Modifier.clickable {
                photoPickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Status Input
        TitipinTextField(
            value = status,
            onValueChange = { status = it },
            label = "STATUS BIO (Opsional)",
            placeholder = "Cth: Suka titip skincare Korea",
            isFocused = status.isNotEmpty()
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(Spacing.xl))

        val isProcessing = isUploadingAvatar || isUpdatingProfile
        Button(
            onClick = {
                if (status.isNotBlank()) {
                    viewModel.updateProfile(status = status)
                } else {
                    onFinish() // if just uploading photo and skipping status, or just clicking continue
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(ComponentSize.buttonHeight),
            enabled = !isProcessing,
            shape = RoundedCornerShape(Radius.full),
            colors = ButtonDefaults.buttonColors(
                containerColor = Charcoal,
                contentColor = Cream,
                disabledContainerColor = Charcoal10,
                disabledContentColor = Charcoal30
            )
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Cream, strokeWidth = 2.dp)
            } else {
                Text(text = "Lanjutkan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = DmSansFamily)
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        TextButton(
            onClick = { onFinish() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isProcessing
        ) {
            Text(
                text = "Lewati",
                color = Charcoal60,
                fontSize = 14.sp,
                fontFamily = DmSansFamily
            )
        }
        
        Spacer(modifier = Modifier.height(Spacing.lg))
    }

    // Effect for handling the status update completion
    LaunchedEffect(uiState) {
        if (!isUploadingAvatar && !isUpdatingProfile && status.isNotBlank()) {
            // we should wait for success
            if (uiState is ProfileUiState.Success) {
                val returnedStatus = (uiState as ProfileUiState.Success).user.status
                if (returnedStatus == status) {
                     onFinish()
                }
            }
        }
    }
}
