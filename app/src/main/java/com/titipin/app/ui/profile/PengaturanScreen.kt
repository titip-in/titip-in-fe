package com.titipin.app.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.titipin.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengaturanScreen(
    onBack: () -> Unit,
    viewModel: PengaturanViewModel = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val notifJastip by viewModel.notifJastip.collectAsState()
    val notifPesan  by viewModel.notifPesan.collectAsState()

    var showEditProfileSheet by remember { mutableStateOf(false) }
    var showEditWaSheet      by remember { mutableStateOf(false) }
    var showVerifyWaSheet    by remember { mutableStateOf(false) }
    var showChangePasswordSheet by remember { mutableStateOf(false) }
    var showResetPasswordSheet by remember { mutableStateOf(false) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) viewModel.uploadAvatar(uri) }
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(actionState) {
        when (actionState) {
            is PengaturanActionState.Success -> {
                val message = (actionState as PengaturanActionState.Success).message
                viewModel.resetActionState()
                showEditProfileSheet = false
                showEditWaSheet = false
                showVerifyWaSheet = false
                showChangePasswordSheet = false
                showResetPasswordSheet = false
                scope.launch { snackbarHostState.showSnackbar(message) }
            }
            is PengaturanActionState.Error -> {
                scope.launch { snackbarHostState.showSnackbar((actionState as PengaturanActionState.Error).message) }
                viewModel.resetActionState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Cream,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(data, containerColor = Charcoal, contentColor = Cream,
                    actionColor = Terracotta, shape = RoundedCornerShape(Radius.md))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            // ── HEADER ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali", tint = Charcoal)
                }
                Spacer(Modifier.width(4.dp))
                Column {
                    Text("● PROFIL", color = Terracotta, fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp,
                        fontFamily = DmSansFamily)
                    Text("Pengaturan", color = Charcoal, fontSize = 24.sp,
                        fontWeight = FontWeight.Medium, fontFamily = FrauncesFamily)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Spacer(Modifier.height(4.dp))

                // ── SECTION: AKUN ─────────────────────────────────
                PengaturanSectionLabel("AKUN")

                when (val state = uiState) {
                    is PengaturanUiState.Ready -> {
                        val user = state.user

                        // User info display card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.lg))
                                .background(Charcoal)
                                .padding(Spacing.lg)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                // Avatar
                                val initials = user.name.trim()
                                    .split(" ").filter { it.isNotBlank() }
                                    .take(2).joinToString("") { it.first().uppercase() }
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                listOf(Sage, Terracotta)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!user.avatarUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = user.avatarUrl,
                                            contentDescription = "Foto profil",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(initials, fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold, color = Color.White,
                                            fontFamily = DmSansFamily)
                                    }
                                }
                                Column {
                                    Text(user.name.trim(), fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold, color = Cream,
                                        fontFamily = DmSansFamily)
                                    Text(user.email, fontSize = 12.sp,
                                        color = Cream.copy(alpha = 0.5f), fontFamily = DmSansFamily)
                                }
                            }
                        }

                        // Edit Profil
                        PengaturanMenuItem(
                            emoji   = "👤",
                            label   = "Edit Profil",
                            subtitle = "Ubah nama dan status profil",
                            onClick = { showEditProfileSheet = true }
                        )

                        PengaturanMenuItem(
                            emoji   = "🖼️",
                            label   = "Edit Foto Profil",
                            subtitle = if (user.avatarUrl.isNullOrBlank()) "Tambahkan foto agar profil lebih jelas" else "Ganti foto profil akun",
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )

                        PengaturanMenuItem(
                            emoji   = if (user.emailVerifiedAt.isNullOrBlank()) "✉️" else "✅",
                            label   = "Verifikasi Email",
                            subtitle = if (user.emailVerifiedAt.isNullOrBlank())
                                "Belum terverifikasi — kirim ulang email"
                            else
                                "Email sudah terverifikasi",
                            badge = if (user.emailVerifiedAt.isNullOrBlank()) "Perlu" else null,
                            onClick = {
                                if (user.emailVerifiedAt.isNullOrBlank()) viewModel.resendEmailVerification()
                            }
                        )

                        // No. WhatsApp
                        PengaturanMenuItem(
                            emoji   = "📱",
                            label   = "No. WhatsApp",
                            subtitle = when {
                                user.waNumber.isNullOrBlank() -> "Belum diisi — ketuk untuk mengisi"
                                user.waVerifiedAt.isNullOrBlank() -> "${user.waNumber} — belum verified"
                                else -> "${user.waNumber} — verified"
                            },
                            badge = if (!user.waNumber.isNullOrBlank() && user.waVerifiedAt.isNullOrBlank()) "OTP" else null,
                            onClick = { showEditWaSheet = true }
                        )

                        if (!user.waNumber.isNullOrBlank() && user.waVerifiedAt.isNullOrBlank()) {
                            PengaturanMenuItem(
                                emoji = "🔐",
                                label = "Verifikasi WhatsApp",
                                subtitle = "Kirim OTP dan verifikasi nomor WA",
                                badge = "Perlu",
                                onClick = {
                                    showVerifyWaSheet = true
                                    viewModel.requestWaOtp()
                                }
                            )
                        }

                        // Ubah Password
                        PengaturanMenuItem(
                            emoji    = "🔒",
                            label    = "Ubah Password",
                            subtitle = "Ganti password atau kirim link reset",
                            onClick  = { showChangePasswordSheet = true }
                        )
                    }

                    is PengaturanUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp)
                        }
                    }

                    is PengaturanUiState.Error -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.md))
                                .background(TerracottaPale)
                                .padding(Spacing.md),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 14.sp)
                            Text((uiState as PengaturanUiState.Error).message,
                                fontFamily = DmSansFamily, fontSize = 12.sp, color = Terracotta)
                        }
                    }
                }

                // ── SECTION: NOTIFIKASI ───────────────────────────
                PengaturanSectionLabel("NOTIFIKASI")

                // Jastip baru di area
                Surface(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(Radius.md),
                    color     = Cream,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SagePale),
                                contentAlignment = Alignment.Center
                            ) { Text("🔔", fontSize = 16.sp) }
                            Column {
                                Text("Jastip baru di area saya", fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium, color = Charcoal,
                                    fontFamily = DmSansFamily)
                                Text("Notifikasi saat ada jastip baru", fontSize = 11.sp,
                                    color = Charcoal60, fontFamily = DmSansFamily)
                            }
                        }
                        TitipinSwitch(checked = notifJastip, onCheckedChange = { viewModel.toggleNotifJastip() })
                    }
                }

                // Pesan masuk
                Surface(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(Radius.md),
                    color     = Cream,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TerracottaPale),
                                contentAlignment = Alignment.Center
                            ) { Text("💬", fontSize = 16.sp) }
                            Column {
                                Text("Pesan WhatsApp masuk", fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium, color = Charcoal,
                                    fontFamily = DmSansFamily)
                                Text("Pengingat pesan yang belum dibalas", fontSize = 11.sp,
                                    color = Charcoal60, fontFamily = DmSansFamily)
                            }
                        }
                        TitipinSwitch(checked = notifPesan, onCheckedChange = { viewModel.toggleNotifPesan() })
                    }
                }

                // ── SECTION: LAINNYA ──────────────────────────────
                PengaturanSectionLabel("LAINNYA")

                PengaturanMenuItem(
                    emoji    = "📄",
                    label    = "Syarat & Ketentuan",
                    subtitle = "Kebijakan penggunaan Titip.in",
                    onClick  = { /* TODO: buka browser/webview */ }
                )

                PengaturanMenuItem(
                    emoji    = "🛡️",
                    label    = "Kebijakan Privasi",
                    subtitle = "Bagaimana data kamu digunakan",
                    onClick  = { /* TODO */ }
                )

                // Versi app — tidak clickable
                Surface(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(Radius.md),
                    color     = Cream,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CreamDark),
                                contentAlignment = Alignment.Center
                            ) { Text("ℹ️", fontSize = 16.sp) }
                            Column {
                                Text("Versi Aplikasi", fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium, color = Charcoal,
                                    fontFamily = DmSansFamily)
                                Text("v1.0.0 MVP", fontSize = 11.sp,
                                    color = Charcoal60, fontFamily = DmSansFamily)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.lg))
            }
        }
    }

    // ── Edit Profil Bottom Sheet ───────────────────────────────────
    if (showEditProfileSheet) {
        val currentUser = (uiState as? PengaturanUiState.Ready)?.user
        EditProfileSheet(
            currentName   = currentUser?.name.orEmpty(),
            currentStatus = currentUser?.status.orEmpty(),
            isSaving      = actionState is PengaturanActionState.Loading,
            onDismiss     = { showEditProfileSheet = false },
            onSave        = { name, status ->
                viewModel.updateProfile(name = name.trim(), status = status.trim().ifEmpty { null })
            }
        )
    }

    // ── Edit WA Bottom Sheet ───────────────────────────────────────
    if (showEditWaSheet) {
        val currentUser = (uiState as? PengaturanUiState.Ready)?.user
        EditWaSheet(
            currentWa = currentUser?.waNumber.orEmpty(),
            isSaving  = actionState is PengaturanActionState.Loading,
            onDismiss = { showEditWaSheet = false },
            onSave    = { wa -> viewModel.updateProfile(waNumber = wa.trim()) }
        )
    }

    if (showVerifyWaSheet) {
        VerifyWaSheet(
            isSaving = actionState is PengaturanActionState.Loading,
            onDismiss = { showVerifyWaSheet = false },
            onResend = { viewModel.requestWaOtp() },
            onVerify = { otp -> viewModel.verifyWaOtp(otp.trim()) }
        )
    }

    if (showChangePasswordSheet) {
        val currentUser = (uiState as? PengaturanUiState.Ready)?.user
        ChangePasswordSheet(
            isSaving = actionState is PengaturanActionState.Loading,
            onDismiss = { showChangePasswordSheet = false },
            onResetByEmail = {
                showChangePasswordSheet = false
                showResetPasswordSheet = true
            },
            onSave = { oldPassword, newPassword ->
                viewModel.changePassword(oldPassword, newPassword)
            }
        )

        if (showResetPasswordSheet) {
            ResetPasswordSheet(
                currentEmail = currentUser?.email.orEmpty(),
                isSaving = actionState is PengaturanActionState.Loading,
                onDismiss = { showResetPasswordSheet = false },
                onSend = { email -> viewModel.requestPasswordReset(email.trim()) }
            )
        }
    } else if (showResetPasswordSheet) {
        val currentUser = (uiState as? PengaturanUiState.Ready)?.user
        ResetPasswordSheet(
            currentEmail = currentUser?.email.orEmpty(),
            isSaving = actionState is PengaturanActionState.Loading,
            onDismiss = { showResetPasswordSheet = false },
            onSend = { email -> viewModel.requestPasswordReset(email.trim()) }
        )
    }
}

// ── Reusable components ────────────────────────────────────────────

@Composable
private fun PengaturanSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = Charcoal60,
        fontFamily = DmSansFamily
    )
}

@Composable
private fun PengaturanMenuItem(
    emoji: String,
    label: String,
    subtitle: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier        = Modifier.fillMaxWidth().clickable { onClick() },
        shape           = RoundedCornerShape(Radius.md),
        color           = Cream,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CreamDark),
                    contentAlignment = Alignment.Center
                ) { Text(emoji, fontSize = 16.sp) }
                Column {
                    Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = Charcoal, fontFamily = DmSansFamily)
                    Text(subtitle, fontSize = 11.sp, color = Charcoal60,
                        fontFamily = DmSansFamily)
                }
            }
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.full))
                        .background(GoldPale)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(badge, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        color = Gold, fontFamily = DmSansFamily, letterSpacing = 0.5.sp)
                }
            } else {
                Text("›", fontSize = 18.sp, color = Charcoal30, fontFamily = DmSansFamily)
            }
        }
    }
}

@Composable
private fun TitipinSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor       = Cream,
            checkedTrackColor       = Sage,
            uncheckedThumbColor     = Cream,
            uncheckedTrackColor     = CreamDark,
            uncheckedBorderColor    = CreamDark
        )
    )
}

// ── EDIT PROFILE SHEET ────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileSheet(
    currentName: String,
    currentStatus: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, status: String) -> Unit
) {
    var name   by remember { mutableStateOf(currentName) }
    var status by remember { mutableStateOf(currentStatus) }
    val focusManager = LocalFocusManager.current

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Cream) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg).padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text("Edit Profil", fontFamily = FrauncesFamily, fontSize = 22.sp,
                fontWeight = FontWeight.Medium, color = Charcoal)
            Text("NAMA", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp,
                color = Charcoal60, fontFamily = DmSansFamily)
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nama lengkap", fontFamily = DmSansFamily) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Terracotta,
                    unfocusedBorderColor = Charcoal30,
                    cursorColor          = Terracotta
                ),
                shape = RoundedCornerShape(Radius.md)
            )
            Text("STATUS / BIO", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp,
                color = Charcoal60, fontFamily = DmSansFamily)
            OutlinedTextField(
                value = status, onValueChange = { status = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Contoh: Siap nitip dari Jakarta!", fontFamily = DmSansFamily) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Terracotta,
                    unfocusedBorderColor = Charcoal30,
                    cursorColor          = Terracotta
                ),
                shape = RoundedCornerShape(Radius.md)
            )
            Button(
                onClick = { onSave(name, status) },
                modifier = Modifier.fillMaxWidth(),
                enabled  = name.isNotBlank() && !isSaving,
                shape    = RoundedCornerShape(Radius.full),
                colors   = ButtonDefaults.buttonColors(containerColor = Charcoal)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Cream, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                } else {
                    Text("Simpan", fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold, color = Cream)
                }
            }
        }
    }
}

// ── EDIT WA SHEET ─────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditWaSheet(
    currentWa: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (wa: String) -> Unit
) {
    var wa by remember { mutableStateOf(currentWa) }
    val focusManager = LocalFocusManager.current

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Cream) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg).padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text("Nomor WhatsApp", fontFamily = FrauncesFamily, fontSize = 22.sp,
                fontWeight = FontWeight.Medium, color = Charcoal)
            Text(
                "Nomor WA kamu digunakan untuk dihubungi pembeli/penjual. Hanya ditampilkan secara tersensor.",
                fontFamily = DmSansFamily, fontSize = 12.sp, color = Charcoal60, lineHeight = 18.sp
            )
            OutlinedTextField(
                value = wa, onValueChange = { wa = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("08xxxxxxxxxx", fontFamily = DmSansFamily) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Terracotta,
                    unfocusedBorderColor = Charcoal30,
                    cursorColor          = Terracotta
                ),
                shape = RoundedCornerShape(Radius.md)
            )
            Button(
                onClick = { onSave(wa) },
                modifier = Modifier.fillMaxWidth(),
                enabled  = wa.length >= 10 && !isSaving,
                shape    = RoundedCornerShape(Radius.full),
                colors   = ButtonDefaults.buttonColors(containerColor = Charcoal)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Cream, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                } else {
                    Text("Simpan", fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold, color = Cream)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerifyWaSheet(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onResend: () -> Unit,
    onVerify: (otp: String) -> Unit
) {
    var otp by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )

    ModalBottomSheet(
        onDismissRequest = {},
        sheetState = sheetState,
        containerColor = Cream
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg).padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Verifikasi WhatsApp", fontFamily = FrauncesFamily, fontSize = 22.sp,
                    fontWeight = FontWeight.Medium, color = Charcoal)
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup",
                        tint = Charcoal
                    )
                }
            }
            Text("Masukkan kode OTP 6 digit yang dikirim ke nomor WhatsApp kamu.",
                fontFamily = DmSansFamily, fontSize = 12.sp, color = Charcoal60, lineHeight = 18.sp)
            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it.filter { char -> char.isDigit() }.take(6) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("123456", fontFamily = DmSansFamily) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Terracotta,
                    unfocusedBorderColor = Charcoal30,
                    cursorColor = Terracotta
                ),
                shape = RoundedCornerShape(Radius.md)
            )
            Button(
                onClick = { onVerify(otp) },
                modifier = Modifier.fillMaxWidth(),
                enabled = otp.length == 6 && !isSaving,
                shape = RoundedCornerShape(Radius.full),
                colors = ButtonDefaults.buttonColors(containerColor = Charcoal)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Cream, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                } else {
                    Text("Verifikasi", fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold, color = Cream)
                }
            }
            TextButton(onClick = onResend, enabled = !isSaving, modifier = Modifier.fillMaxWidth()) {
                Text("Kirim ulang OTP", fontFamily = DmSansFamily, color = Terracotta)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePasswordSheet(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onResetByEmail: () -> Unit,
    onSave: (oldPassword: String, newPassword: String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Cream) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg).padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text("Ubah Password", fontFamily = FrauncesFamily, fontSize = 22.sp,
                fontWeight = FontWeight.Medium, color = Charcoal)
            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Password lama", fontFamily = DmSansFamily) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Terracotta,
                    unfocusedBorderColor = Charcoal30,
                    cursorColor = Terracotta
                ),
                shape = RoundedCornerShape(Radius.md)
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Password baru min. 8 karakter", fontFamily = DmSansFamily) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Terracotta,
                    unfocusedBorderColor = Charcoal30,
                    cursorColor = Terracotta
                ),
                shape = RoundedCornerShape(Radius.md)
            )
            Button(
                onClick = { onSave(oldPassword, newPassword) },
                modifier = Modifier.fillMaxWidth(),
                enabled = oldPassword.isNotBlank() && newPassword.length >= 8 && !isSaving,
                shape = RoundedCornerShape(Radius.full),
                colors = ButtonDefaults.buttonColors(containerColor = Charcoal)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Cream, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                } else {
                    Text("Simpan Password", fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold, color = Cream)
                }
            }
            TextButton(onClick = onResetByEmail, enabled = !isSaving, modifier = Modifier.fillMaxWidth()) {
                Text("Set/Reset password via email", fontFamily = DmSansFamily, color = Terracotta)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResetPasswordSheet(
    currentEmail: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSend: (email: String) -> Unit
) {
    var email by remember { mutableStateOf(currentEmail) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Cream) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg).padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text("Reset Password", fontFamily = FrauncesFamily, fontSize = 22.sp,
                fontWeight = FontWeight.Medium, color = Charcoal)
            Text("Link reset password akan dikirim lewat email dan dibuka di web.",
                fontFamily = DmSansFamily, fontSize = 12.sp, color = Charcoal60, lineHeight = 18.sp)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("email@example.com", fontFamily = DmSansFamily) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Terracotta,
                    unfocusedBorderColor = Charcoal30,
                    cursorColor = Terracotta
                ),
                shape = RoundedCornerShape(Radius.md)
            )
            Button(
                onClick = { onSend(email) },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank() && !isSaving,
                shape = RoundedCornerShape(Radius.full),
                colors = ButtonDefaults.buttonColors(containerColor = Charcoal)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Cream, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                } else {
                    Text("Kirim Link Reset", fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold, color = Cream)
                }
            }
        }
    }
}
