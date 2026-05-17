plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
//    alias(libs.plugins.google.services)
}

android {
    namespace   = "com.titipin.app"
    compileSdk  = 35

    defaultConfig {
        applicationId   = "com.titipin.app"
        minSdk          = 26
        targetSdk       = 35
        versionCode     = 1
        versionName     = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val releaseKeystorePath = providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull
    val releaseKeystorePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull
    val releaseKeyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").orNull
    val releaseKeyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull

    if (
        releaseKeystorePath != null &&
        releaseKeystorePassword != null &&
        releaseKeyAlias != null &&
        releaseKeyPassword != null
    ) {
        signingConfigs {
            create("release") {
                storeFile = file(releaseKeystorePath)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"https://titipin-api.bccdev.id/api/\"")
            isDebuggable = true
        }
        release {
            buildConfigField("String", "BASE_URL", "\"https://titipin-api.bccdev.id/api/\"")
            isMinifyEnabled = true
            signingConfigs.findByName("release")?.let {
                signingConfig = it
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose     = true
        buildConfig = true  // biar BASE_URL bisa diakses dari kode
    }
}

dependencies {
    // ── CORE ──────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.activity.compose)

    // ── COMPOSE ───────────────────────────────────────────────────
    // BOM ngatur semua versi Compose sekaligus — ga perlu tulis versi satu-satu
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons) // ribuan icon gratis
    implementation(libs.androidx.compose.material.icons.extended)

    // ── NAVIGATION ────────────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)

    // ── NETWORKING ────────────────────────────────────────────────
    // Retrofit sama persis kayak di XML — interface API, converter JSON
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging) // biar bisa liat log request/response di Logcat

    // ── HILT ──────────────────────────────────────────────────────
    // Dependency injection — nanti aku jelasin waktu kita pakai
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // ── ROOM ──────────────────────────────────────────────────────
    // Local database — untuk caching data biar ga reload terus
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ── DATASTORE ─────────────────────────────────────────────────
    // Untuk simpan JWT token — pengganti SharedPreferences yang lebih modern
    implementation(libs.datastore.preferences)

    // ── IMAGE LOADING ─────────────────────────────────────────────
    // Coil = Glide-nya Compose, load gambar dari URL
    implementation(libs.coil.compose)

    // ── COROUTINES ────────────────────────────────────────────────
    implementation(libs.coroutines.android)

    // ── MAPS & LOCATION ───────────────────────────────────────────
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // ── FIREBASE (FCM) ────────────────────────────────────────────
//    val firebaseBom = platform(libs.firebase.bom)
//    implementation(firebaseBom)
//    implementation(libs.firebase.messaging)

    // ── TESTING ───────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
