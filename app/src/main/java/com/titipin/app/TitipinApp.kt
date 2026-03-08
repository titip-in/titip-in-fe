package com.titipin.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// @HiltAndroidApp wajib ada di Application class
// Ini "nyalain" Hilt untuk seluruh app
// Tanpa ini, @AndroidEntryPoint di MainActivity ga akan jalan

// Analogi: kalau Hilt itu pabrik yang bikin object,
// @HiltAndroidApp itu tombol ON pabriknya
@HiltAndroidApp
class TitipinApp : Application()
