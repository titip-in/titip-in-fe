package com.titipin.app.ui.auth

import androidx.compose.foundation.background
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
    onLoginSuccess: () -> Unit = {}
) {
    // ── STATE ──────────────────────────────────────────────────────
    // Ini pengganti getText() dari EditText di XML
    // Setiap kali user ketik, variabel ini otomatis update → UI ikut update

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // State untuk show/hide password (icon mata)
    var passwordVisible by remember { mutableStateOf(false) }

    // State loading — nanti dipake waktu hit API
    var isLoading by remember { mutableStateOf(false) }

    // ── UI ─────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()                    // padding dari status bar di level Column
            .verticalScroll(rememberScrollState())
    ) {
        // Gap kecil di atas — biar hero ga nempel ke status bar
        Spacer(modifier = Modifier.height(Spacing.sm))

        // ── HERO SECTION ──────────────────────────────────────────
        // Sekarang ada padding horizontal — ga fullwidth, sejajar form field
        Box(
            modifier = Modifier
                .padding(horizontal = Spacing.md)   // sejajar dengan form di bawah
                .fillMaxWidth()
                .background(
                    color = Charcoal,
                    shape = RoundedCornerShape(Radius.xl)  // full rounded semua sudut
                )
                .padding(horizontal = Spacing.lg, vertical = Spacing.xl)
        ) {
            // Blob dekoratif kiri atas (lingkaran sage transparan)
            // Ini efek visual dari design system — blob-blob warna
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = (-30).dp, y = (-30).dp) // geser keluar batas
                    .clip(CircleShape)
                    .background(Sage.copy(alpha = 0.15f)) // transparan 15%
                    .align(Alignment.TopEnd)
            )

            // Blob dekoratif kanan bawah (lingkaran terracotta)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Terracotta.copy(alpha = 0.2f))
                    .align(Alignment.BottomCenter)
                    .offset(x = 60.dp, y = 20.dp)
            )

            // Konten hero — Column di dalam Box
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Tag kecil di atas
                Text(
                    text = "● PLATFORM MAHASISWA MALANG",
                    color = Sage,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    fontFamily = DmSansFamily
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                // Nama app — Fraunces Italic besar
                // Ini yang bikin feel "luxury" dari design system
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

                // Tagline
                Text(
                    text = "Jastip & preloved terdekat,\nlangsung di genggamanmu.",
                    color = Cream.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    fontFamily = DmSansFamily,
                    lineHeight = 20.sp
                )
            }
        }

        // ── FORM SECTION ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm) // jarak antar elemen
        ) {

            // ── EMAIL FIELD ───────────────────────────────────────
            // Ini pengganti EditText di XML
            // Di Compose pakai OutlinedTextField atau TextField
            TitipinTextField(
                value = email,
                onValueChange = { email = it },       // lambda — dipanggil tiap user ketik
                label = "EMAIL / NO. HP",
                placeholder = "rizky@student.ub.ac.id",
                keyboardType = KeyboardType.Email,
                isFocused = email.isNotEmpty()
            )

            // ── PASSWORD FIELD ────────────────────────────────────
            TitipinTextField(
                value = password,
                onValueChange = { password = it },
                label = "PASSWORD",
                placeholder = "min. 8 karakter",
                keyboardType = KeyboardType.Password,
                isFocused = password.isNotEmpty(),
                // VisualTransformation = cara Compose sembunyikan teks password
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None           // tampilkan teks asli
                else
                    PasswordVisualTransformation(),     // tampilkan ●●●●●●●●
                trailingIcon = {
                    // Icon mata untuk show/hide password
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Rounded.Visibility
                            else
                                Icons.Rounded.VisibilityOff,
                            contentDescription = "Toggle password",
                            tint = Charcoal30
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            // ── TOMBOL MASUK ──────────────────────────────────────
            // Button di Compose — ga perlu xml drawable untuk background
            Button(
                onClick = {
                    // Nanti di sini kita panggil ViewModel untuk hit API
                    // Sekarang langsung navigate ke home dulu (dummy)
                    onLoginSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ComponentSize.buttonHeight),
                enabled = email.isNotEmpty() && password.isNotEmpty() && !isLoading,
                // enabled = false → button otomatis greyed out
                // Ini yang dulu kamu lakuin manual dengan button.isEnabled = false
                shape = RoundedCornerShape(Radius.full),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Charcoal,
                    contentColor   = Cream,
                    disabledContainerColor = Charcoal10,
                    disabledContentColor   = Charcoal30
                )
            ) {
                if (isLoading) {
                    // Tampilkan loading spinner kalau lagi proses
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color    = Cream,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text       = "Masuk →",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = DmSansFamily
                    )
                }
            }

            // ── DIVIDER "atau" ────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                // Garis kiri
                HorizontalDivider(
                    modifier = Modifier.weight(1f), // weight = layout_weight di XML
                    color = Charcoal10
                )
                Text(
                    text     = "atau",
                    fontSize = 11.sp,
                    color    = Charcoal30,
                    fontFamily = DmSansFamily
                )
                // Garis kanan
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color    = Charcoal10
                )
            }

            // ── TOMBOL GOOGLE ─────────────────────────────────────
            OutlinedButton(
                onClick = { /* nanti implementasi Google Sign In */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ComponentSize.buttonHeight),
                shape  = RoundedCornerShape(Radius.md),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SagePale,
                    contentColor   = Charcoal
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = SageLight
                )
            ) {
                Text(
                    text       = "🔍  Lanjut dengan Google",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = DmSansFamily
                )
            }

            // ── LINK REGISTER ─────────────────────────────────────
            // TextButton = teks yang bisa diklik, tanpa background
            // Row dengan wrapContentWidth biar dua teks ini sejajar horizontal
            // Pakai wrapContentWidth + Center agar ga wrap ke baris baru
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.sm),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text     = "Belum punya akun?",
                    fontSize = 12.sp,
                    color    = Charcoal60,
                    fontFamily = DmSansFamily
                )
                // Spacer 4dp antara teks dan link — biar ga nempel
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text       = "Daftar sekarang",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Terracotta,
                    fontFamily = DmSansFamily,
                    modifier   = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}

// ── REUSABLE COMPONENT: TitipinTextField ──────────────────────────
// Ini contoh "komponen" — composable yang bisa dipakai berulang
// Mirip kayak custom view di XML, tapi jauh lebih simpel
// Kita pakai di LoginScreen dan RegisterScreen (biar konsisten)
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
    trailingIcon: @Composable (() -> Unit)? = null // nullable — kadang ada icon, kadang tidak
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Label kecil di atas field
        Text(
            text     = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color    = Charcoal60,
            fontFamily = DmSansFamily,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // TextField — inti dari input field
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            modifier      = Modifier.fillMaxWidth(),
            placeholder   = {
                Text(
                    text     = placeholder,
                    fontSize = 13.sp,
                    color    = Charcoal30,
                    fontFamily = DmSansFamily
                )
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize   = 13.sp,
                fontFamily = DmSansFamily,
                color      = Charcoal
            ),
            trailingIcon  = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine    = true,
            shape         = RoundedCornerShape(Radius.md),
            colors        = OutlinedTextFieldDefaults.colors(
                // Border hijau (sage) kalau ada isi, transparan kalau kosong
                focusedBorderColor   = if (isFocused) Sage else Terracotta,
                unfocusedBorderColor = if (isFocused) Sage else Charcoal10,
                focusedContainerColor   = CreamDark,
                unfocusedContainerColor = CreamDark,
                cursorColor = Terracotta
            )
        )
    }
}

// ── PREVIEW ───────────────────────────────────────────────────────
// @Preview = bisa lihat tampilan di panel kanan Android Studio
// tanpa perlu run di emulator/device
// Ini pengganti Design tab di XML editor
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    TitipinTheme {
        LoginScreen()
    }
}