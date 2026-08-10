package com.renobile.carrinho.repositories

import com.renobile.carrinho.database.dao.CartDao
import com.renobile.carrinho.database.entities.CartEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CartRepositoryImplTest {

    private val cartDao = mockk<CartDao>(relaxed = true)
    private val repository = CartRepositoryImpl(cartDao)

    @Test
    fun `given carts, when getActiveCart, then returns first open cart`() = runTest {
        val closed = CartEntity(1, "Closed", 100, 200, 0, 0.0, 0.0, "")
        val active = CartEntity(2, "Active", 100, 0, 0, 0.0, 0.0, "")
        coEvery { cartDao.getAll() } returns listOf(closed, active)

        val result = repository.getActiveCart()

        assertEquals(active.id, result?.id)
    }

    @Test
    fun `given no open carts, when getActiveCart, then returns null`() = runTest {
        coEvery { cartDao.getAll() } returns emptyList()

        assertNull(repository.getActiveCart())
    }

    @Test
    fun `given cart, when insertCart, then delegates to dao`() = runTest {
        val cart = CartEntity(1, "A", 100, 0, 0, 0.0, 0.0, "")

        repository.insertCart(cart)

        coVerify(exactly = 1) { cartDao.insert(cart) }
    }

    @Test
    fun `given cart, when updateCart, then delegates to dao insert with replace`() = runTest {
        val cart = CartEntity(1, "A", 100, 0, 0, 0.0, 0.0, "")

        repository.updateCart(cart)

        coVerify(exactly = 1) { cartDao.insert(cart) }
    }

    @Test
    fun `given cart, when deleteCart, then delegates to dao`() = runTest {
        val cart = CartEntity(1, "A", 100, 0, 0, 0.0, 0.0, "")

        repository.deleteCart(cart)

        coVerify(exactly = 1) { cartDao.delete(cart) }
    }

    @Test
    fun `when getAllCarts, then returns all carts`() = runTest {
        val carts = listOf(
            CartEntity(1, "A", 100, 0, 0, 0.0, 0.0, ""),
            CartEntity(2, "B", 100, 0, 0, 0.0, 0.0, "")
        )
        coEvery { cartDao.getAll() } returns carts

        assertEquals(carts, repository.getAllCarts())
    }
}
