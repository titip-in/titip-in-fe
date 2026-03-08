package com.titipin.app.ui.theme

import androidx.compose.ui.unit.dp

// ── SPACING ───────────────────────────────────────────────────────
// Gunakan ini konsisten di semua screen — jangan hardcode angka
object Spacing {
    val xs   = 4.dp
    val sm   = 8.dp
    val md   = 16.dp
    val lg   = 24.dp
    val xl   = 32.dp
    val xxl  = 48.dp

    // Screen horizontal padding — pakai ini di semua screen
    val screenHorizontal = 20.dp
    val screenVertical   = 24.dp
}

// ── RADIUS ────────────────────────────────────────────────────────
object Radius {
    val sm   = 8.dp
    val md   = 16.dp
    val lg   = 24.dp
    val xl   = 32.dp
    val full = 999.dp   // pill / capsule shape (button, badge)
}

// ── ELEVATION / SHADOW ────────────────────────────────────────────
object Elevation {
    val none   = 0.dp
    val low    = 2.dp
    val medium = 4.dp
    val high   = 8.dp
}

// ── ICON SIZE ─────────────────────────────────────────────────────
object IconSize {
    val sm  = 16.dp
    val md  = 20.dp
    val lg  = 24.dp
    val xl  = 32.dp
}

// ── COMPONENT SIZES ───────────────────────────────────────────────
object ComponentSize {
    val buttonHeight     = 52.dp
    val buttonHeightSm   = 40.dp
    val inputHeight      = 56.dp
    val fabSize          = 56.dp
    val bottomNavHeight  = 64.dp
    val cardImageHeight  = 180.dp
    val avatarSm         = 32.dp
    val avatarMd         = 40.dp
    val avatarLg         = 64.dp
}
