package com.titipin.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ── COLOR SCHEME ──────────────────────────────────────────────────
// Titip.in hanya punya light theme untuk MVP
private val LightColorScheme = lightColorScheme(
    primary             = md_primary,
    onPrimary           = md_onPrimary,
    primaryContainer    = md_primaryContainer,
    onPrimaryContainer  = md_onPrimaryContainer,

    secondary           = md_secondary,
    onSecondary         = md_onSecondary,
    secondaryContainer  = md_secondaryContainer,
    onSecondaryContainer= md_onSecondaryContainer,

    tertiary            = md_tertiary,
    onTertiary          = md_onTertiary,
    tertiaryContainer   = md_tertiaryContainer,
    onTertiaryContainer = md_onTertiaryContainer,

    background          = md_background,
    onBackground        = md_onBackground,
    surface             = md_surface,
    onSurface           = md_onSurface,
    surfaceVariant      = md_surfaceVariant,
    onSurfaceVariant    = md_onSurfaceVariant,
    outline             = md_outline,
    outlineVariant      = md_outlineVariant,
)

// ── THEME COMPOSABLE ──────────────────────────────────────────────
@Composable
fun TitipinTheme(
    // Dark theme dimatiin dulu untuk MVP
    // bisa dihidupkan lagi nanti dengan darkColorScheme
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = TitipinTypography,
        content     = content
    )
}

// ── USAGE GUIDE ───────────────────────────────────────────────────
// Di MainActivity.kt:
//
//   setContent {
//       TitipinTheme {
//           // semua screen kamu di sini
//       }
//   }
//
// Di composable manapun, akses via:
//   MaterialTheme.colorScheme.primary     → Terracotta
//   MaterialTheme.colorScheme.secondary   → Sage
//   MaterialTheme.colorScheme.background  → Cream
//   MaterialTheme.typography.displayLarge → Fraunces Italic
//   MaterialTheme.typography.bodyLarge    → DM Sans Regular
//
// Untuk warna custom (yang tidak ada di M3 roles), import langsung:
//   import com.titipin.app.ui.theme.Terracotta
//   import com.titipin.app.ui.theme.Spacing
//   import com.titipin.app.ui.theme.Radius
