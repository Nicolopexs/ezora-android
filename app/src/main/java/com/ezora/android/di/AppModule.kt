package com.ezora.android.di

import android.content.Context
import androidx.room.Room
import com.ezora.android.BuildConfig
import com.ezora.android.core.Brand
import com.ezora.android.data.local.AppDatabase
import com.ezora.android.data.local.FarmaciaDao
import com.ezora.android.data.local.KvDao
import com.ezora.android.data.local.MedicoDao
import com.ezora.android.data.local.StockDao
import com.ezora.android.data.remote.ApiService
import com.ezora.android.data.remote.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttp(authInterceptor: AuthInterceptor): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(authInterceptor)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideApi(client: OkHttpClient, json: Json): ApiService = Retrofit.Builder()
        .baseUrl(Brand.apiBaseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "ezora.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideMedicoDao(db: AppDatabase): MedicoDao = db.medicoDao()
    @Provides fun provideFarmaciaDao(db: AppDatabase): FarmaciaDao = db.farmaciaDao()
    @Provides fun provideStockDao(db: AppDatabase): StockDao = db.stockDao()
    @Provides fun provideKvDao(db: AppDatabase): KvDao = db.kvDao()
}
