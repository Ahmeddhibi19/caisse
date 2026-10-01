package com.ahmeddhibi.caisse.domain.usecase

import com.ahmeddhibi.caisse.data.cart.InMemoryCartRepository
import com.ahmeddhibi.caisse.testing.FakeSaleRepository
import com.ahmeddhibi.caisse.testing.TestData.croissant
import com.ahmeddhibi.caisse.testing.TestData.espresso
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CheckoutUseCaseTest {

    private val cart = InMemoryCartRepository()
    private val sales = FakeSaleRepository()

    @Test
    fun `an empty cart records nothing`() = runTest {
        val result = checkout()

        assertThat(result).isEqualTo(CheckoutResult.EmptyCart)
        assertThat(sales.recorded).isEmpty()
    }

    @Test
    fun `records what was in the cart and empties it`() = runTest {
        cart.add(croissant)
        cart.add(croissant)
        cart.add(espresso)

        val result = checkout()

        assertThat(result).isInstanceOf(CheckoutResult.Success::class.java)
        assertThat(sales.recorded.single().map { it.product.id to it.quantity })
            .containsExactly("croissant" to 2, "espresso" to 1)
        assertThat(cart.cart.value.isEmpty).isTrue()
    }

    @Test
    fun `a sale that fails to be stored puts the lines back in the cart`() = runTest {
        sales.failure = IllegalStateException("disk full")
        cart.add(croissant)

        val error = runCatching { checkout() }.exceptionOrNull()

        assertThat(error).isInstanceOf(IllegalStateException::class.java)
        assertThat(cart.cart.value.quantityOf("croissant")).isEqualTo(1)
    }

    // Like the real application scope, a supervisor: one failed checkout must not cancel it.
    private suspend fun TestScope.checkout(): CheckoutResult = CheckoutUseCase(
        cartRepository = cart,
        saleRepository = sales,
        appScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
    ).invoke()
}
