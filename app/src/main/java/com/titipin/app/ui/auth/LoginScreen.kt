package com.titipin.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.ui.theme.*

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    onSetupRequired: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                val user = (authState as AuthState.Success).auth.user
                viewModel.resetState()
                if (user.emailVerifiedAt.isNullOrBlank() || user.waVerifiedAt.isNullOrBlank()) {
                    onSetupRequired()
                } else {
                    onLoginSuccess()
                }
            }
            is AuthState.PasswordResetSent -> {
                showForgotPasswordDialog = true
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(Spacing.sm))

        Box(
            modifier = Modifier
                .padding(horizontal = Spacing.md)
                .fillMaxWidth()
                .background(color = Charcoal, shape = RoundedCornerShape(Radius.xl))
                .padding(horizontal = Spacing.lg, vertical = Spacing.xl)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = (-30).dp, y = (-30).dp)
                    .clip(CircleShape)
                    .background(Sage.copy(alpha = 0.15f))
                    .align(Alignment.TopEnd)
            )
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Terracotta.copy(alpha = 0.2f))
                    .align(Alignment.BottomCenter)
                    .offset(x = 60.dp, y = 20.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "● PLATFORM MAHASISWA MALANG",
                    color = Sage,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    fontFamily = DmSansFamily
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = "Titip.in",
                    color = Cream,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Light,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FrauncesFamily,
                    lineHeight = 44.sp
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = "Jastip & preloved terdekat,\nlangsung di genggamanmu.",
                    color = Cream.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    fontFamily = DmSansFamily,
                    lineHeight = 20.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            TitipinTextField(
                value = email,
                onValueChange = { email = it },
                label = "EMAIL / NO. HP",
                placeholder = "rizky@student.ub.ac.id",
                keyboardType = KeyboardType.Email,
                isFocused = email.isNotEmpty()
            )

            TitipinTextField(
                value = password,
                onValueChange = { password = it },
                label = "PASSWORD",
                placeholder = "min. 8 karakter",
                keyboardType = KeyboardType.Password,
                isFocused = password.isNotEmpty(),
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            contentDescription = "Toggle password",
                            tint = Charcoal30
                        )
                    }
                }
            )

            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "Lupa Password?",
                    fontSize = 11.sp,
                    color = Terracotta,
                    fontFamily = DmSansFamily,
                    modifier = Modifier.clickable { showForgotPasswordDialog = true }
                )
            }

            if (authState is AuthState.Error) {
                Text(
                    text = "⚠ ${(authState as AuthState.Error).message}",
                    fontSize = 12.sp,
                    color = Terracotta,
                    fontFamily = DmSansFamily,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ComponentSize.buttonHeight),
                enabled = email.isNotEmpty() && password.isNotEmpty() && !isLoading,
                shape = RoundedCornerShape(Radius.full),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = Charcoal,
                    contentColor           = Cream,
                    disabledContainerColor = Charcoal10,
                    disabledContentColor   = Charcoal30
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Cream,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Masuk →",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = DmSansFamily
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Charcoal10)
                Text(text = "atau", fontSize = 11.sp, color = Charcoal30, fontFamily = DmSansFamily)
                HorizontalDivider(modifier = Modifier.weight(1f), color = Charcoal10)
            }

            OutlinedButton(
                onClick = { showGoogleDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ComponentSize.buttonHeight),
                shape = RoundedCornerShape(Radius.md),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SagePale,
                    contentColor   = Charcoal
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SageLight)
            ) {
                Text(
                    text = "🔍  Lanjut dengan Google",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = DmSansFamily
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.sm),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Belum punya akun?", fontSize = 12.sp, color = Charcoal60, fontFamily = DmSansFamily)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Daftar sekarang",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Terracotta,
                    fontFamily = DmSansFamily,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }

    if (showGoogleDialog) {
        AlertDialog(
            onDismissRequest = { showGoogleDialog = false },
            title = { Text("Dalam Pengembangan", fontFamily = FrauncesFamily, color = Charcoal) },
            text = { Text("Login via Google sedang dalam pengembangan. Silakan gunakan Email / No. HP untuk saat ini.", fontFamily = DmSansFamily, color = Charcoal60) },
            confirmButton = {
                TextButton(onClick = { showGoogleDialog = false }) {
                    Text("OK", color = Terracotta, fontFamily = DmSansFamily)
                }
            },
            containerColor = Cream
        )
    }

    if (showForgotPasswordDialog) {
        val resetSentMessage = (authState as? AuthState.PasswordResetSent)?.message
        val errorMessage = (authState as? AuthState.Error)?.message
        AlertDialog(
            onDismissRequest = {
                showForgotPasswordDialog = false
                forgotPasswordEmail = ""
                viewModel.resetState()
            },
            title = { Text("Lupa Password?", fontFamily = FrauncesFamily, color = Charcoal) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(
                        "Masukkan email akunmu. Kalau email terdaftar, link reset password akan dikirim ke inbox.",
                        fontFamily = DmSansFamily,
                        color = Charcoal60
                    )
                    OutlinedTextField(
                        value = forgotPasswordEmail,
                        onValueChange = { forgotPasswordEmail = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("email@example.com", fontFamily = DmSansFamily) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(Radius.md),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Terracotta,
                            unfocusedBorderColor = Charcoal10,
                            cursorColor = Terracotta
                        )
                    )
                    if (resetSentMessage != null) {
                        Text(resetSentMessage, fontSize = 12.sp, color = Sage, fontFamily = DmSansFamily)
                    } else if (errorMessage != null) {
                        Text(errorMessage, fontSize = 12.sp, color = Terracotta, fontFamily = DmSansFamily)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (resetSentMessage != null) {
                            showForgotPasswordDialog = false
                            forgotPasswordEmail = ""
                            viewModel.resetState()
                        } else {
                            viewModel.forgotPassword(forgotPasswordEmail.trim())
                        }
                    },
                    enabled = resetSentMessage != null || (forgotPasswordEmail.isNotBlank() && !isLoading)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Terracotta, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    } else {
                        Text(if (resetSentMessage != null) "Tutup" else "Kirim Link", color = Terracotta, fontFamily = DmSansFamily)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showForgotPasswordDialog = false
                    forgotPasswordEmail = ""
                    viewModel.resetState()
                }) {
                    Text("Batal", color = Charcoal, fontFamily = DmSansFamily)
                }
            },
            containerColor = Cream
        )
    }
}

@Composable
fun TitipinTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isFocused: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = Charcoal60,
            fontFamily = DmSansFamily,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = placeholder, fontSize = 13.sp, color = Charcoal30, fontFamily = DmSansFamily)
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 13.sp,
                fontFamily = DmSansFamily,
                color = Charcoal
            ),
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            shape = RoundedCornerShape(Radius.md),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = if (isFocused) Sage else Terracotta,
                unfocusedBorderColor    = if (isFocused) Sage else Charcoal10,
                focusedContainerColor   = CreamDark,
                unfocusedContainerColor = CreamDark,
                cursorColor             = Terracotta
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    TitipinTheme {
        LoginScreen()
    }
}
