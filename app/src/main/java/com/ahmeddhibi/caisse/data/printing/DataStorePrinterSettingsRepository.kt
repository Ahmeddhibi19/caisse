package com.ahmeddhibi.caisse.data.printing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ahmeddhibi.caisse.domain.printing.PrinterMode
import com.ahmeddhibi.caisse.domain.printing.PrinterSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStorePrinterSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PrinterSettingsRepository {

    override val mode: Flow<PrinterMode> = dataStore.data.map { preferences ->
        preferences[MODE]?.let { stored -> PrinterMode.entries.firstOrNull { it.name == stored } } ?: PrinterMode.NORMAL
    }

    override suspend fun setMode(mode: PrinterMode) {
        dataStore.edit { it[MODE] = mode.name }
    }

    private companion object {
        val MODE = stringPreferencesKey("printer_mode")
    }
}
