package com.titipin.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── PRIMARY PALETTE ──────────────────────────────────────────────
val Cream        = Color(0xFFF5F2EC)
val CreamDark    = Color(0xFFEDE9DF)
val WarmWhite    = Color(0xFFFAFAF8)
val Charcoal     = Color(0xFF1A1A18)
val Charcoal60   = Color(0x991A1A18)
val Charcoal30   = Color(0x4D1A1A18)
val Charcoal10   = Color(0x141A1A18)

// ── SAGE (Success / Active / Open) ───────────────────────────────
val Sage         = Color(0xFF8FAF8A)
val SageLight    = Color(0xFFC8DBC5)
val SagePale     = Color(0xFFE8F0E7)

// ── TERRACOTTA (Primary CTA / Preloved) ──────────────────────────
val Terracotta       = Color(0xFFC8714A)
val TerracottaLight  = Color(0xFFE8A98C)
val TerracottaPale   = Color(0xFFF5E4DB)

// ── GOLD (Featured / Premium) ────────────────────────────────────
val Gold         = Color(0xFFC4A862)
val GoldPale     = Color(0xFFF0E8D4)

// ── Inactive Text for NavBar ────────────────────────────────────
val InactiveText = Color(0x6FFFFFFF)

// ── SEMANTIC (mapped to roles) ───────────────────────────────────
val StatusOpen   = Sage           // jastip aktif / available
val StatusClosed = Charcoal30     // jastip closed / taken
val StatusSold   = TerracottaLight // preloved terjual
val Featured     = Gold           // featured badge

// ── MATERIAL 3 ROLE MAPPING ──────────────────────────────────────
// dipakai di Theme.kt untuk lightColorScheme()
val md_primary             = Terracotta
val md_onPrimary           = WarmWhite
val md_primaryContainer    = TerracottaPale
val md_onPrimaryContainer  = Charcoal

val md_secondary           = Sage
val md_onSecondary         = WarmWhite
val md_secondaryContainer  = SagePale
val md_onSecondaryContainer= Charcoal

val md_tertiary            = Gold
val md_onTertiary          = WarmWhite
val md_tertiaryContainer   = GoldPale
val md_onTertiaryContainer = Charcoal

val md_background          = Cream
val md_onBackground        = Charcoal
val md_surface             = WarmWhite
val md_onSurface           = Charcoal
val md_surfaceVariant      = CreamDark
val md_onSurfaceVariant    = Charcoal60
val md_outline             = Charcoal30
val md_outlineVariant      = Charcoal10
