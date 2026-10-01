package com.ahmeddhibi.caisse.ui.pos

import android.app.Application
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ahmeddhibi.caisse.data.catalog.HardcodedProductRepository
import com.ahmeddhibi.caisse.domain.model.Cart
import com.ahmeddhibi.caisse.domain.model.Product
import com.ahmeddhibi.caisse.ui.theme.CaisseTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, qualifiers = "w1280dp-h800dp")
class PosContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val products = HardcodedProductRepository().products
    private var clickedProduct: Product? = null
    private var checkouts = 0

    @Test
    fun `checkout is disabled while the cart is empty`() {
        show(PosUiState(products = products, registerKey = "C01"))

        composeRule.onNodeWithTag(CHECKOUT_BUTTON_TAG).assertIsNotEnabled()
        composeRule.onNodeWithText("Caisse C01").assertExists()
    }

    @Test
    fun `tapping a product hands it to the view model`() {
        show(PosUiState(products = products))

        composeRule.onNodeWithText("Espresso").performClick()

        assertThat(clickedProduct?.id).isEqualTo("espresso")
    }

    @Test
    fun `the checkout button shows the total and triggers the sale`() {
        val cart = Cart().add(products.first { it.id == "croissant" }, quantity = 2)
        show(PosUiState(products = products, cart = cart))

        composeRule.onNodeWithTag(CHECKOUT_BUTTON_TAG)
            .assertIsEnabled()
            .assertTextContains("2,80", substring = true)
            .performClick()

        assertThat(checkouts).isEqualTo(1)
    }

    @Test
    fun `checkout is disabled while a sale is being recorded`() {
        val cart = Cart().add(products.first())
        show(PosUiState(products = products, cart = cart, isCheckingOut = true))

        composeRule.onNodeWithTag(CHECKOUT_BUTTON_TAG).assertIsNotEnabled()
    }

    private fun show(state: PosUiState) {
        composeRule.setContent {
            CaisseTheme {
                PosContent(
                    uiState = state,
                    onProductClick = { clickedProduct = it },
                    onDecrement = {},
                    onRemove = {},
                    onCheckout = { checkouts++ },
                    onMessageShown = {},
                )
            }
        }
    }
}
