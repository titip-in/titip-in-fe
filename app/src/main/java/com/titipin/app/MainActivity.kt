package com.titipin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.titipin.app.navigation.TitipinNavGraph
import com.titipin.app.ui.theme.TitipinTheme
import dagger.hilt.android.AndroidEntryPoint

// @AndroidEntryPoint → wajib ada di semua Activity yang pakai Hilt
// Ini kasih tau Hilt bahwa class ini butuh dependency injection
// Nanti ViewModel, Repository, dll bisa di-inject otomatis
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enableEdgeToEdge = konten melebar sampai ke balik status bar & nav bar
        // Bikin tampilan lebih immersive, sesuai design system kita
        enableEdgeToEdge()

        // setContent = pengganti setContentView(R.layout.activity_main)
        // Semua UI Compose hidup di dalam block ini
        setContent {
            TitipinTheme {
                // TitipinNavGraph mengurus semua navigasi
                // Dari sini satu baris ini, seluruh app sudah jalan
                TitipinNavGraph()
            }
        }
    }
}

// Catatan: Greeting() dan GreetingPreview() yang default tadi
// udah dihapus — itu cuma boilerplate dari Android Studio
