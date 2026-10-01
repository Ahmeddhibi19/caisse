package com.ahmeddhibi.caisse.data.printing

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.ahmeddhibi.caisse.domain.printing.PrinterMode
import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStorePrinterSettingsRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `the printer works normally until told otherwise`() = runTest {
        val repository = repository()

        assertThat(repository.mode.first()).isEqualTo(PrinterMode.NORMAL)
    }

    @Test
    fun `the chosen mode is kept`() = runTest {
        val repository = repository()

        repository.setMode(PrinterMode.OFFLINE)

        assertThat(repository.mode.first()).isEqualTo(PrinterMode.OFFLINE)
    }

    private fun TestScope.repository() = DataStorePrinterSettingsRepository(
        PreferenceDataStoreFactory.create(scope = backgroundScope) { File(folder.root, "settings.preferences_pb") },
    )
}
