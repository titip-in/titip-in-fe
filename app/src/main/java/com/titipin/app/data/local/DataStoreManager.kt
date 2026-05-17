package com.titipin.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.titipin.app.data.model.UserData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "titipin_prefs"
)

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ACCESS_TOKEN  = stringPreferencesKey("access_token")
        private val KEY_TOKEN_TYPE    = stringPreferencesKey("token_type")
        private val KEY_USER_ID       = stringPreferencesKey("user_id")
        private val KEY_USER_NAME     = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL    = stringPreferencesKey("user_email")
        private val KEY_USER_EMAIL_VERIFIED_AT = stringPreferencesKey("user_email_verified_at")
        private val KEY_USER_WA       = stringPreferencesKey("user_wa")
        private val KEY_USER_WA_VERIFIED_AT = stringPreferencesKey("user_wa_verified_at")
        private val KEY_USER_AVATAR   = stringPreferencesKey("user_avatar")
        private val KEY_USER_STATUS   = stringPreferencesKey("user_status")
        private val KEY_ONBOARDING    = androidx.datastore.preferences.core.booleanPreferencesKey("has_seen_onboarding")
    }

    suspend fun saveAuthData(
        accessToken: String,
        tokenType: String,
        user: UserData
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN]  = accessToken
            prefs[KEY_TOKEN_TYPE]    = tokenType
            prefs[KEY_USER_ID]       = user.id.toString()
            prefs[KEY_USER_NAME]     = user.name
            prefs[KEY_USER_EMAIL]    = user.email
            user.emailVerifiedAt?.let { prefs[KEY_USER_EMAIL_VERIFIED_AT] = it } ?: prefs.remove(KEY_USER_EMAIL_VERIFIED_AT)
            user.waNumber?.let { prefs[KEY_USER_WA] = it } ?: prefs.remove(KEY_USER_WA)
            user.waVerifiedAt?.let { prefs[KEY_USER_WA_VERIFIED_AT] = it } ?: prefs.remove(KEY_USER_WA_VERIFIED_AT)
            user.avatarUrl?.let { prefs[KEY_USER_AVATAR] = it } ?: prefs.remove(KEY_USER_AVATAR)
            user.status?.let { prefs[KEY_USER_STATUS] = it } ?: prefs.remove(KEY_USER_STATUS)
        }
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACCESS_TOKEN]
    }

    val userId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ID]
    }

    val userName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_EMAIL]
    }

    val userEmailVerifiedAt: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_EMAIL_VERIFIED_AT]
    }

    val tokenType: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_TOKEN_TYPE]
    }

    val userWaNumber: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_WA]
    }

    val userWaVerifiedAt: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_WA_VERIFIED_AT]
    }

    val userAvatarUrl: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_AVATAR]
    }

    val userStatus: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_STATUS]
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACCESS_TOKEN] != null
    }

    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING] == true
    }

    suspend fun markOnboardingSeen() {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING] = true
        }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_TOKEN_TYPE)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_NAME)
            prefs.remove(KEY_USER_EMAIL)
            prefs.remove(KEY_USER_EMAIL_VERIFIED_AT)
            prefs.remove(KEY_USER_WA)
            prefs.remove(KEY_USER_WA_VERIFIED_AT)
            prefs.remove(KEY_USER_AVATAR)
            prefs.remove(KEY_USER_STATUS)
        }
    }
}
