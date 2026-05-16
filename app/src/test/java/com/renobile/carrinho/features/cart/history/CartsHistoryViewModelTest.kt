package com.renobile.carrinho.features.cart.history

import app.cash.turbine.test
import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.repositories.CartRepository
import com.renobile.carrinho.repositories.ProductRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartsHistoryViewModelTest {

    private val cartRepository = mockk<CartRepository>(relaxed = true)
    private val productRepository = mockk<ProductRepository>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given carts exists, when init, then load and update missing keywords`() = runTest {
        // Given
        val cartWithoutKeywords = CartEntity(
            id = 1, name = "Cart 1", dateOpen = 100, dateClose = 200,
            products = 1, units = 1.0, valueTotal = 10.0, keywords = ""
        )
        val product = ProductEntity(
            id = 1, cartId = 1, listId = 0, name = "Product A", quantity = 1.0, price = 10.0
        )

        coEvery { cartRepository.getAllCarts() } returns listOf(cartWithoutKeywords)
        coEvery { productRepository.getProductsByCartId(1) } returns listOf(product)

        // When
        val viewModel = CartsHistoryViewModel(cartRepository, productRepository)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.carts.size)
            assertEquals("Cart 1", state.carts[0].name)

            coVerify { cartRepository.updateCart(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given search terms, when search, then filter carts by name or keywords`() = runTest {
        // Given
        val cart1 = CartEntity(
            id = 1, name = "Groceries", dateOpen = 100, dateClose = 200,
            products = 1, units = 1.0, valueTotal = 10.0, keywords = "apple, banana"
        )
        val cart2 = CartEntity(
            id = 2, name = "Electronics", dateOpen = 100, dateClose = 200,
            products = 1, units = 1.0, valueTotal = 10.0, keywords = "phone"
        )

        coEvery { cartRepository.getAllCarts() } returns listOf(cart1, cart2)

        val viewModel = CartsHistoryViewModel(cartRepository, productRepository)

        // When
        viewModel.onSearchTermsChanged("apple")

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.carts.size)
            assertEquals("Groceries", state.carts[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
