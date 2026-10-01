package com.ahmeddhibi.caisse.domain.model

import com.ahmeddhibi.caisse.testing.TestData.croissant
import com.ahmeddhibi.caisse.testing.TestData.espresso
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CartTest {

    @Test
    fun `adding the same product twice increases its quantity`() {
        val cart = Cart().add(croissant).add(espresso).add(croissant)

        assertThat(cart.lines.map { it.product.id to it.quantity })
            .containsExactly("croissant" to 2, "espresso" to 1)
            .inOrder()
        assertThat(cart.itemCount).isEqualTo(3)
        assertThat(cart.total).isEqualTo(Money(140 * 2 + 180))
    }

    @Test
    fun `decrementing the last unit removes the line`() {
        val cart = Cart().add(croissant).add(croissant)

        assertThat(cart.decrement("croissant").quantityOf("croissant")).isEqualTo(1)
        assertThat(cart.decrement("croissant").decrement("croissant").isEmpty).isTrue()
    }

    @Test
    fun `decrementing an unknown product changes nothing`() {
        val cart = Cart().add(croissant)

        assertThat(cart.decrement("unknown")).isEqualTo(cart)
    }

    @Test
    fun `merge adds quantities to existing lines and appends new ones`() {
        val cart = Cart().add(espresso)

        val merged = cart.merge(listOf(CartLine(espresso, 2), CartLine(croissant, 1)))

        assertThat(merged.quantityOf("espresso")).isEqualTo(3)
        assertThat(merged.quantityOf("croissant")).isEqualTo(1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a line cannot have a zero quantity`() {
        CartLine(croissant, 0)
    }
}
