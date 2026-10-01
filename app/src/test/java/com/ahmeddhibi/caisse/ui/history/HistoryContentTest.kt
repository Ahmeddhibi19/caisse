package com.ahmeddhibi.caisse.ui.history

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ahmeddhibi.caisse.domain.model.PrintStatus
import com.ahmeddhibi.caisse.domain.model.Sale
import com.ahmeddhibi.caisse.domain.model.SyncStatus
import com.ahmeddhibi.caisse.testing.sale
import com.ahmeddhibi.caisse.ui.theme.CaisseTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, qualifiers = "w1280dp-h800dp")
class HistoryContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var reprinted: String? = null
    private var resynced: String? = null

    @Test
    fun `lists each sale with its number and print state`() {
        show(sale(sequence = 1, printStatus = PrintStatus.PRINTED), sale(sequence = 2, printStatus = PrintStatus.PENDING))

        composeRule.onNodeWithText("C01-000001").assertExists()
        composeRule.onNodeWithText("Imprimé").assertExists()
        composeRule.onNodeWithText("C01-000002").assertExists()
        composeRule.onNodeWithText("En attente").assertExists()
    }

    @Test
    fun `only failed tickets offer a reprint`() {
        show(sale(sequence = 1, printStatus = PrintStatus.FAILED))

        composeRule.onNodeWithText("Échec").assertExists()
        composeRule.onNodeWithText("Réimprimer").performClick()

        assertThat(reprinted).isEqualTo("sale-1")
    }

    @Test
    fun `a sale rejected by the server can be sent again`() {
        show(sale(sequence = 1, printStatus = PrintStatus.PRINTED, syncStatus = SyncStatus.CONFLICT))

        composeRule.onNodeWithText("Conflit").assertExists()
        composeRule.onNodeWithText("Relancer la synchro").performClick()

        assertThat(resynced).isEqualTo("sale-1")
    }

    @Test
    fun `explains the empty history`() {
        show()

        composeRule.onNodeWithText("Aucune vente pour le moment", substring = true).assertExists()
    }

    private fun show(vararg sales: Sale) {
        composeRule.setContent {
            CaisseTheme {
                HistoryContent(
                    uiState = HistoryUiState(isLoading = false, sales = sales.toList()),
                    onBack = {},
                    onSaleClick = {},
                    onReprint = { reprinted = it },
                    onRetrySync = { resynced = it },
                    onPreviewDismissed = {},
                )
            }
        }
    }
}
