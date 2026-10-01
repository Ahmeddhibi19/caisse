package com.ahmeddhibi.caisse.data.cart

import com.ahmeddhibi.caisse.testing.TestData.croissant
import com.ahmeddhibi.caisse.testing.TestData.espresso
import com.ahmeddhibi.caisse.testing.TestData.sandwich
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class InMemoryCartRepositoryTest {

    private val repository = InMemoryCartRepository()

    @Test
    fun `drain returns the lines and leaves an empty cart`() {
        repository.add(croissant)
        repository.add(croissant)
        repository.add(espresso)

        val drained = repository.drain()

        assertThat(drained.map { it.product.id to it.quantity })
            .containsExactly("croissant" to 2, "espresso" to 1)
        assertThat(repository.cart.value.isEmpty).isTrue()
    }

    @Test
    fun `restore keeps what was added after the drain`() {
        repository.add(croissant)
        val drained = repository.drain()
        repository.add(sandwich)
        repository.add(croissant)

        repository.restore(drained)

        assertThat(repository.cart.value.quantityOf("croissant")).isEqualTo(2)
        assertThat(repository.cart.value.quantityOf("sandwich")).isEqualTo(1)
    }

    @Test
    fun `remove deletes the whole line`() {
        repository.add(espresso)
        repository.add(espresso)

        repository.remove("espresso")

        assertThat(repository.cart.value.isEmpty).isTrue()
    }
}
