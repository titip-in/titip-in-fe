package com.titipin.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property — cara setup DataStore di Kotlin
// Ini bikin DataStore instance yang tied ke Application context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "titipin_prefs"
)

// @Singleton = Hilt hanya buat satu instance ini untuk seluruh app
// @Inject constructor = Hilt tau cara bikin class ini otomatis
@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Keys untuk tiap data yang disimpan
    // Mirip kayak key di SharedPreferences, tapi type-safe
    companion object {
        private val KEY_ACCESS_TOKEN  = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER_ID       = stringPreferencesKey("user_id")
        private val KEY_USER_NAME     = stringPreferencesKey("user_name")
    }

    // ── SIMPAN TOKEN ───────────────────────────────────────────────
    // suspend = fungsi ini harus dipanggil dari coroutine (bukan main thread)
    // Di XML dulu kamu mungkin pakai SharedPreferences.edit().putString()
    // DataStore lebih aman karena async dan tidak block UI thread
    suspend fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        userId: String,
        userName: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN]  = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_USER_ID]       = userId
            prefs[KEY_USER_NAME]     = userName
        }
    }

    // ── BACA TOKEN ─────────────────────────────────────────────────
    // Flow = stream data yang bisa di-observe
    // Kalau data berubah, semua yang observe Flow ini otomatis dapat nilai baru
    // Ini konsep reactive programming — UI otomatis update tanpa polling
    val accessToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACCESS_TOKEN]
    }

    val userId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ID]
    }

    val userName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME]
    }

    // Cek apakah user sudah login — untuk SplashScreen nanti
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACCESS_TOKEN] != null
    }

    // ── HAPUS TOKEN (Logout) ───────────────────────────────────────
    suspend fun clearAuthData() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_NAME)
        }
    }
}
