package com.ahmeddhibi.caisse.core.di

import com.ahmeddhibi.caisse.core.config.AppConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    // Host loopback as seen from the Android emulator.
    private const val EMULATOR_HOST = "10.0.2.2"
    private const val AUTH_EMULATOR_PORT = 9099
    private const val DATABASE_EMULATOR_PORT = 9000

    @Provides
    @Singleton
    fun provideFirebaseAuth(config: AppConfig): FirebaseAuth = FirebaseAuth.getInstance().apply {
        if (config.useFirebaseEmulator) useEmulator(EMULATOR_HOST, AUTH_EMULATOR_PORT)
    }

    // Disk persistence stays off: Room is the source of truth, Firebase only receives the outbox.
    @Provides
    @Singleton
    fun provideFirebaseDatabase(config: AppConfig): FirebaseDatabase =
        FirebaseDatabase.getInstance(config.firebaseDatabaseUrl).apply {
            if (config.useFirebaseEmulator) useEmulator(EMULATOR_HOST, DATABASE_EMULATOR_PORT)
        }
}
