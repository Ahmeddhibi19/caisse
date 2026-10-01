package com.ahmeddhibi.caisse.core.di

import com.ahmeddhibi.caisse.BuildConfig
import com.ahmeddhibi.caisse.core.config.AppConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppConfig(): AppConfig = AppConfig(
        storeId = BuildConfig.STORE_ID,
        firebaseDatabaseUrl = BuildConfig.FIREBASE_DATABASE_URL,
        useFirebaseEmulator = BuildConfig.USE_FIREBASE_EMULATOR,
    )

    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
