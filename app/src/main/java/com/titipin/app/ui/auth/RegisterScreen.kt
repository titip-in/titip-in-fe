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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titipin.app.ui.theme.*

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var noWa by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var konfirmasiPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var konfirmasiVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                viewModel.resetState()
                onRegisterSuccess()
            }
            else -> Unit
        }
    }

    val passwordMatch = password == konfirmasiPassword
    val formValid = nama.isNotEmpty() && email.isNotEmpty() &&
            noWa.isNotEmpty() && password.length >= 8 &&
            konfirmasiPassword.isNotEmpty() && passwordMatch

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
                .padding(horizontal = Spacing.lg, vertical = Spacing.lg)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Sage.copy(alpha = 0.15f))
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp)
            )
            Column {
                Text(
                    text = "● DAFTAR SEKARANG",
                    color = Terracotta,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    fontFamily = DmSansFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Titip.in",
                    color = Cream,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FrauncesFamily
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Buat akunmu dan mulai jastip\nataupun jual preloved.",
                    color = Cream.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontFamily = DmSansFamily,
                    lineHeight = 18.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {

            TitipinTextField(
                value         = nama,
                onValueChange = { nama = it },
                label         = "NAMA LENGKAP",
                placeholder   = "Rizky Pratama",
                isFocused     = nama.isNotEmpty()
            )

            TitipinTextField(
                value         = email,
                onValueChange = { email = it },
                label         = "EMAIL",
                placeholder   = "rizky@student.ub.ac.id",
                keyboardType  = KeyboardType.Email,
                isFocused     = email.isNotEmpty()
            )

            // No WhatsApp — prefix +62
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "NO. WHATSAPP",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = Charcoal60,
                    fontFamily = DmSansFamily,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value         = noWa,
                    onValueChange = { noWa = it },
                    modifier      = Modifier.fillMaxWidth(),
                    prefix = {
                        Text("+62 ", fontSize = 13.sp, fontFamily = DmSansFamily, color = Charcoal60)
                    },
                    placeholder = {
                        Text("8123456789", fontSize = 13.sp, color = Charcoal30, fontFamily = DmSansFamily)
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 13.sp, fontFamily = DmSansFamily, color = Charcoal
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape  = RoundedCornerShape(Radius.md),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = if (noWa.isNotEmpty()) Sage else Terracotta,
                        unfocusedBorderColor    = if (noWa.isNotEmpty()) Sage else Charcoal10,
                        focusedContainerColor   = CreamDark,
                        unfocusedContainerColor = CreamDark,
                        cursorColor             = Terracotta
                    )
                )
            }

            TitipinTextField(
                value                = password,
                onValueChange        = { password = it },
                label                = "PASSWORD",
                placeholder          = "min. 8 karakter",
                keyboardType         = KeyboardType.Password,
                isFocused            = password.isNotEmpty(),
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
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

            Column(modifier = Modifier.fillMaxWidth()) {
                TitipinTextField(
                    value                = konfirmasiPassword,
                    onValueChange        = { konfirmasiPassword = it },
                    label                = "KONFIRMASI PASSWORD",
                    placeholder          = "ulangi password",
                    keyboardType         = KeyboardType.Password,
                    isFocused            = konfirmasiPassword.isNotEmpty() && passwordMatch,
                    visualTransformation = if (konfirmasiVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { konfirmasiVisible = !konfirmasiVisible }) {
                            Icon(
                                imageVector = if (konfirmasiVisible)
                                    Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                contentDescription = "Toggle konfirmasi",
                                tint = Charcoal30
                            )
                        }
                    }
                )
                if (konfirmasiPassword.isNotEmpty() && !passwordMatch) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚠ Password tidak cocok",
                        fontSize = 11.sp,
                        color = Terracotta,
                        fontFamily = DmSansFamily,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            if (authState is AuthState.Error) {
                Text(
                    text = "⚠ ${(authState as? AuthState.Error)?.message ?: ""}",
                    fontSize = 12.sp,
                    color = Terracotta,
                    fontFamily = DmSansFamily,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            Button(
                onClick  = { viewModel.register(nama, email, password, noWa) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ComponentSize.buttonHeight),
                enabled = formValid && !isLoading,
                shape   = RoundedCornerShape(Radius.full),
                colors  = ButtonDefaults.buttonColors(
                    containerColor         = Charcoal,
                    contentColor           = Cream,
                    disabledContainerColor = Charcoal10,
                    disabledContentColor   = Charcoal30
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Cream, strokeWidth = 2.dp)
                } else {
                    Text(text = "Buat Akun →", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = DmSansFamily)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.sm, bottom = Spacing.md),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Sudah punya akun?", fontSize = 12.sp, color = Charcoal60, fontFamily = DmSansFamily)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Masuk",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Terracotta,
                    fontFamily = DmSansFamily,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    TitipinTheme {
        RegisterScreen()
    }
}