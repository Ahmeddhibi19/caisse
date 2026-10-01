package com.ahmeddhibi.caisse.ui.pos

import com.ahmeddhibi.caisse.data.cart.InMemoryCartRepository
import com.ahmeddhibi.caisse.data.catalog.HardcodedProductRepository
import com.ahmeddhibi.caisse.testing.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: PosViewModel

    @Before
    fun setUp() {
        viewModel = PosViewModel(HardcodedProductRepository(), InMemoryCartRepository())
    }

    @Test
    fun `exposes the eight products of the catalog`() = runTest {
        backgroundScope.launch(mainDispatcherRule.dispatcher) { viewModel.uiState.collect() }

        assertThat(viewModel.uiState.value.products).hasSize(8)
    }

    @Test
    fun `tapping products fills the cart`() = runTest {
        backgroundScope.launch(mainDispatcherRule.dispatcher) { viewModel.uiState.collect() }
        val product = viewModel.uiState.value.products.first()

        viewModel.onProductClick(product)
        viewModel.onProductClick(product)

        assertThat(viewModel.uiState.value.cart.quantityOf(product.id)).isEqualTo(2)
    }

    @Test
    fun `decrement and remove update the cart`() = runTest {
        backgroundScope.launch(mainDispatcherRule.dispatcher) { viewModel.uiState.collect() }
        val (first, second) = viewModel.uiState.value.products

        viewModel.onProductClick(first)
        viewModel.onProductClick(first)
        viewModel.onProductClick(second)
        viewModel.onDecrement(first.id)
        viewModel.onRemove(second.id)

        assertThat(viewModel.uiState.value.cart.quantityOf(first.id)).isEqualTo(1)
        assertThat(viewModel.uiState.value.cart.quantityOf(second.id)).isEqualTo(0)
    }
}
