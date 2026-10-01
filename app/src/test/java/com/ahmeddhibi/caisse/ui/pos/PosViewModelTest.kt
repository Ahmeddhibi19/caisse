package com.ahmeddhibi.caisse.ui.pos

import com.ahmeddhibi.caisse.data.cart.InMemoryCartRepository
import com.ahmeddhibi.caisse.data.catalog.HardcodedProductRepository
import com.ahmeddhibi.caisse.domain.usecase.CheckoutUseCase
import com.ahmeddhibi.caisse.testing.FakeRegisterRepository
import com.ahmeddhibi.caisse.testing.FakeSaleRepository
import com.ahmeddhibi.caisse.testing.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cart = InMemoryCartRepository()
    private val sales = FakeSaleRepository()
    private lateinit var appScope: CoroutineScope
    private lateinit var viewModel: PosViewModel

    @Before
    fun setUp() {
        appScope = CoroutineScope(SupervisorJob() + mainDispatcherRule.dispatcher)
        viewModel = PosViewModel(
            productRepository = HardcodedProductRepository(),
            cartRepository = cart,
            registerRepository = FakeRegisterRepository(),
            checkout = CheckoutUseCase(cart, sales, appScope),
        )
    }

    @After
    fun tearDown() {
        appScope.cancel()
    }

    @Test
    fun `exposes the eight products and the register`() = runTest {
        collectState()

        assertThat(viewModel.uiState.value.products).hasSize(8)
        assertThat(viewModel.uiState.value.registerKey).isEqualTo("C01")
    }

    @Test
    fun `tapping products fills the cart`() = runTest {
        collectState()
        val product = viewModel.uiState.value.products.first()

        viewModel.onProductClick(product)
        viewModel.onProductClick(product)

        assertThat(viewModel.uiState.value.cart.quantityOf(product.id)).isEqualTo(2)
        assertThat(viewModel.uiState.value.canCheckout).isTrue()
    }

    @Test
    fun `decrement and remove update the cart`() = runTest {
        collectState()
        val (first, second) = viewModel.uiState.value.products

        viewModel.onProductClick(first)
        viewModel.onProductClick(first)
        viewModel.onProductClick(second)
        viewModel.onDecrement(first.id)
        viewModel.onRemove(second.id)

        assertThat(viewModel.uiState.value.cart.quantityOf(first.id)).isEqualTo(1)
        assertThat(viewModel.uiState.value.cart.quantityOf(second.id)).isEqualTo(0)
    }

    @Test
    fun `checkout empties the cart and confirms the ticket`() = runTest {
        collectState()
        val product = viewModel.uiState.value.products.first()
        viewModel.onProductClick(product)

        viewModel.onCheckoutClick()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.cart.isEmpty).isTrue()
        assertThat(state.isCheckingOut).isFalse()
        assertThat(state.message).isEqualTo(PosMessage.TicketRecorded("C01-000001", product.price))
    }

    @Test
    fun `a double tap records a single sale`() = runTest {
        collectState()
        sales.gate = CompletableDeferred()
        viewModel.onProductClick(viewModel.uiState.value.products.first())

        viewModel.onCheckoutClick()
        viewModel.onCheckoutClick()
        assertThat(viewModel.uiState.value.isCheckingOut).isTrue()
        assertThat(viewModel.uiState.value.canCheckout).isFalse()

        sales.gate?.complete(Unit)
        advanceUntilIdle()

        assertThat(sales.recorded).hasSize(1)
    }

    @Test
    fun `a failed checkout keeps the cart and reports the error`() = runTest {
        collectState()
        sales.failure = IllegalStateException("disk full")
        val product = viewModel.uiState.value.products.first()
        viewModel.onProductClick(product)

        viewModel.onCheckoutClick()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.cart.quantityOf(product.id)).isEqualTo(1)
        assertThat(viewModel.uiState.value.message).isEqualTo(PosMessage.CheckoutFailed)
    }

    @Test
    fun `checkout with an empty cart does nothing`() = runTest {
        collectState()

        viewModel.onCheckoutClick()
        advanceUntilIdle()

        assertThat(sales.recorded).isEmpty()
        assertThat(viewModel.uiState.value.message).isNull()
    }

    @Test
    fun `the confirmation is cleared once shown`() = runTest {
        collectState()
        viewModel.onProductClick(viewModel.uiState.value.products.first())
        viewModel.onCheckoutClick()
        advanceUntilIdle()

        viewModel.onMessageShown()

        assertThat(viewModel.uiState.value.message).isNull()
    }

    private fun TestScope.collectState() {
        backgroundScope.launch(mainDispatcherRule.dispatcher) { viewModel.uiState.collect() }
    }
}
