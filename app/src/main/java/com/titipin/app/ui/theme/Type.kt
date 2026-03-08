package com.titipin.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.titipin.app.R

// ── FONT FAMILIES ─────────────────────────────────────────────────
// Pastikan file .ttf sudah ada di res/font/
val FrauncesFamily = FontFamily(
    Font(R.font.fraunces_light,         FontWeight.Light),
    Font(R.font.fraunces_regular,       FontWeight.Normal),
    Font(R.font.fraunces_medium,        FontWeight.Medium),
    Font(R.font.fraunces_light_italic,  FontWeight.Light,  FontStyle.Italic),
    Font(R.font.fraunces_regular_italic,FontWeight.Normal, FontStyle.Italic),
)

val DmSansFamily = FontFamily(
    Font(R.font.dm_sans_regular,   FontWeight.Normal),
    Font(R.font.dm_sans_medium,    FontWeight.Medium),
    Font(R.font.dm_sans_semibold,  FontWeight.SemiBold),
)

// ── TYPOGRAPHY SCALE ──────────────────────────────────────────────
// Mapping design system → Material 3 typography roles
val TitipinTypography = Typography(

    // Display — Fraunces Italic Light — hero text, branding
    // Contoh: "Jastip & Preloved" di splash/home header
    displayLarge = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Light,
        fontStyle  = FontStyle.Italic,
        fontSize   = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Light,
        fontStyle  = FontStyle.Italic,
        fontSize   = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.3).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Light,
        fontStyle  = FontStyle.Italic,
        fontSize   = 22.sp,
        lineHeight = 28.sp,
    ),

    // Headline — Fraunces Regular/Medium — section titles, screen titles
    // Contoh: "Temukan di Malang", "Jastip Terdekat"
    headlineLarge = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 18.sp,
        lineHeight = 24.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 22.sp,
    ),

    // Title — DM Sans SemiBold — card titles, list item titles
    titleLarge = TextStyle(
        fontFamily = DmSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 16.sp,
        lineHeight = 22.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = DmSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = DmSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
    ),

    // Body — DM Sans Regular — deskripsi, konten utama
    bodyLarge = TextStyle(
        fontFamily = DmSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = DmSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 13.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = DmSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 18.sp,
    ),

    // Label — DM Sans Medium — badge, chip, caption, metadata
    // Contoh: "2 km · 45 menit lagi · Aktif"
    labelLarge = TextStyle(
        fontFamily = DmSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = DmSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = DmSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.sp,
    ),
)
