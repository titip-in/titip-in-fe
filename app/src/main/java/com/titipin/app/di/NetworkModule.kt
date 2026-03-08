package com.titipin.app.di

import com.titipin.app.BuildConfig
import com.titipin.app.data.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// @Module = ini "pabrik" yang kasih tau Hilt cara bikin object
// @InstallIn(SingletonComponent) = instance ini hidup selama app hidup
//
// Analogi: kalau Hilt itu toko, Module itu resep cara bikin barangnya
// Hilt baca resep ini → bikin object → inject ke tempat yang butuh
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // @Provides = fungsi ini adalah "resep" bikin satu object
    // @Singleton = bikin sekali, pakai terus (tidak bikin ulang tiap dipanggil)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // Logging interceptor — log semua request/response di Logcat
        // Sangat berguna saat development untuk debug masalah API
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY  // debug: log full body
            else
                HttpLoggingInterceptor.Level.NONE  // release: tidak ada log
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            // Timeout — kalau BE tidak response dalam 30 detik, anggap gagal
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        // BASE_URL dari BuildConfig — otomatis beda untuk debug/release
        // Debug  → "http://10.0.2.2:8080/" (localhost emulator)
        // Release → "https://api.titip.in/v1/"
        //
        // 10.0.2.2 adalah alamat khusus emulator untuk akses localhost komputer
        // Kalau pakai device fisik, ganti dengan IP komputer di jaringan yang sama
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // JSON ↔ data class
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        // Retrofit generate implementasi interface ApiService secara otomatis
        // Kita cukup define interface-nya, Retrofit yang urus HTTP-nya
        return retrofit.create(ApiService::class.java)
    }
}
