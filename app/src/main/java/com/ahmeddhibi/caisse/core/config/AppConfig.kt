package com.ahmeddhibi.caisse.core.config

data class AppConfig(
    val storeId: String,
    val firebaseDatabaseUrl: String,
    val useFirebaseEmulator: Boolean,
)
