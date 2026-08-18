package com.renobile.carrinho.features.cart.detail

import app.cash.turbine.test
import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.repositories.CartRepository
import com.renobile.carrinho.repositories.ProductRepository
import com.renobile.carrinho.util.PREF_SORT_ORDER
import com.renobile.carrinho.util.Prefs
import com.renobile.carrinho.util.ProductSortOrder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartDetailsViewModelTest {

    private val cartRepository = mockk<CartRepository>(relaxed = true)
    private val productRepository = mockk<ProductRepository>(relaxed = true)
    private val dispatcher = UnconfinedTestDispatcher()
    private val prefsValues = mutableMapOf<String, Any>()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        prefsValues.clear()
        mockkObject(Prefs)
        every { Prefs.getValue<String>(any(), any()) } answers {
            prefsValues[firstArg()] as? String ?: secondArg()
        }
        every { Prefs.putValue(any(), any<Any>()) } answers {
            prefsValues.put(firstArg(), secondArg<Any>())
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(Prefs)
    }

    private fun cart(id: Long) = CartEntity(id, "Carrinho $id", 100L, 0L, 0, 0.0, 0.0, "")

    private fun product(id: Long, name: String, quantity: Double, price: Double) =
        ProductEntity(id, 1L, 0L, name, quantity, price)

    private fun setupViewModelWithCart(cartId: Long = 1L): CartDetailsViewModel {
        coEvery { cartRepository.getAllCarts() } returns listOf(cart(cartId))
        return CartDetailsViewModel(cartRepository, productRepository)
    }

    @Test
    fun `given existing cart with products, when init, then state is populated sorted by newest`() = runTest {
        val products = listOf(
            product(10, "Arroz", 1.0, 5.0),
            product(20, "Feijão", 2.0, 4.0),
            product(30, "Carne", 0.5, 8.0),
        )
        coEvery { cartRepository.getAllCarts() } returns listOf(cart(1))
        coEvery { productRepository.getProductsByCartId(1) } returns products

        val vm = CartDetailsViewModel(cartRepository, productRepository)
        vm.init(1)

        val state = vm.uiState.value
        assertEquals(cart(1), state.cart)
        assertEquals(listOf("Carne", "Feijão", "Arroz"), state.products.map { it.name })
        assertEquals(ProductSortOrder.NEWEST, state.sortOrder)
        assertTrue(!state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `given cart not found, when init, then error is set`() = runTest {
        coEvery { cartRepository.getAllCarts() } returns listOf(cart(2))

        val vm = CartDetailsViewModel(cartRepository, productRepository)
        vm.init(1)

        val state = vm.uiState.value
        assertEquals("Cart not found", state.error)
        assertNull(state.cart)
        assertEquals(emptyList<ProductEntity>(), state.products)
        assertTrue(!state.isLoading)
    }

    @Test
    fun `given repository failure, when init, then error message is set`() = runTest {
        coEvery { cartRepository.getAllCarts() } throws RuntimeException("boom")

        val vm = CartDetailsViewModel(cartRepository, productRepository)
        vm.init(1)

        val state = vm.uiState.value
        assertEquals("boom", state.error)
        assertTrue(!state.isLoading)
    }

    @Test
    fun `given stored sort price ascending, when init, then products sorted by price`() = runTest {
        prefsValues[PREF_SORT_ORDER] = ProductSortOrder.PRICE_ASC.name
        coEvery { cartRepository.getAllCarts() } returns listOf(cart(1))
        coEvery { productRepository.getProductsByCartId(1) } returns listOf(
            product(10, "Arroz", 1.0, 5.0),
            product(20, "Feijão", 1.0, 3.0),
            product(30, "Carne", 1.0, 8.0),
        )

        val vm = CartDetailsViewModel(cartRepository, productRepository)
        vm.init(1)

        assertEquals(
            listOf("Feijão", "Arroz", "Carne"),
            vm.uiState.value.products.map { it.name },
        )
        assertEquals(ProductSortOrder.PRICE_ASC, vm.uiState.value.sortOrder)
    }

    @Test
    fun `given stored sort name ascending, when init, then products sorted by name`() = runTest {
        prefsValues[PREF_SORT_ORDER] = ProductSortOrder.NAME_ASC.name
        coEvery { cartRepository.getAllCarts() } returns listOf(cart(1))
        coEvery { productRepository.getProductsByCartId(1) } returns listOf(
            product(10, "banana", 1.0, 5.0),
            product(20, "Abacaxi", 1.0, 3.0),
            product(30, "Arroz", 1.0, 8.0),
        )

        val vm = CartDetailsViewModel(cartRepository, productRepository)
        vm.init(1)

        assertEquals(
            listOf("Abacaxi", "Arroz", "banana"),
            vm.uiState.value.products.map { it.name },
        )
    }

    @Test
    fun `given stored sort oldest, when init, then products sorted by id ascending`() = runTest {
        prefsValues[PREF_SORT_ORDER] = ProductSortOrder.OLDEST.name
        coEvery { cartRepository.getAllCarts() } returns listOf(cart(1))
        coEvery { productRepository.getProductsByCartId(1) } returns listOf(
            product(30, "Carne", 1.0, 8.0),
            product(10, "Arroz", 1.0, 5.0),
            product(20, "Feijão", 1.0, 3.0),
        )

        val vm = CartDetailsViewModel(cartRepository, productRepository)
        vm.init(1)

        assertEquals(
            listOf("Arroz", "Feijão", "Carne"),
            vm.uiState.value.products.map { it.name },
        )
    }

    @Test
    fun `given initial search terms, when init, then products filtered and terms set`() = runTest {
        coEvery { cartRepository.getAllCarts() } returns listOf(cart(1))
        coEvery { productRepository.getProductsByCartId(1) } returns listOf(
            product(10, "Arroz", 1.0, 5.0),
            product(20, "Feijão", 1.0, 4.0),
        )

        val vm = CartDetailsViewModel(cartRepository, productRepository)
        vm.init(1, "FEIJ")

        val state = vm.uiState.value
        assertEquals(listOf("Feijão"), state.products.map { it.name })
        assertEquals("FEIJ", state.searchTerms)
    }

    @Test
    fun `given products, when init, then volumes and total are computed`() = runTest {
        coEvery { cartRepository.getAllCarts() } returns listOf(cart(1))
        coEvery { productRepository.getProductsByCartId(1) } returns listOf(
            product(10, "Arroz", 2.0, 5.0),
            product(20, "Feijão", 1.5, 4.0),
        )

        val vm = CartDetailsViewModel(cartRepository, productRepository)
        vm.init(1)

        val state = vm.uiState.value
        assertEquals(3.5, state.volumes, 0.0)
        assertEquals(16.0, state.total, 0.0)
    }

    @Test
    fun `given order, when onSortOrderChanged, then persists preference and reloads`() = runTest {
        coEvery { cartRepository.getAllCarts() } returns listOf(cart(1))
        coEvery { productRepository.getProductsByCartId(1) } returns listOf(
            product(10, "Arroz", 1.0, 5.0),
            product(20, "Feijão", 1.0, 3.0),
        )
        val vm = CartDetailsViewModel(cartRepository, productRepository)
        vm.init(1)

        vm.onSortOrderChanged(1, ProductSortOrder.PRICE_ASC)

        coVerify(exactly = 1) { Prefs.putValue(PREF_SORT_ORDER, ProductSortOrder.PRICE_ASC.name) }
        assertEquals(ProductSortOrder.PRICE_ASC, vm.uiState.value.sortOrder)
        assertEquals(listOf("Feijão", "Arroz"), vm.uiState.value.products.map { it.name })
    }

    @Test
    fun `given search terms, when onSearchTermsChanged, then products filtered and terms updated`() = runTest {
        coEvery { cartRepository.getAllCarts() } returns listOf(cart(1))
        coEvery { productRepository.getProductsByCartId(1) } returns listOf(
            product(10, "Arroz", 1.0, 5.0),
            product(20, "Feijão", 1.0, 4.0),
        )
        val vm = CartDetailsViewModel(cartRepository, productRepository)
        vm.init(1)

        vm.onSearchTermsChanged(1, "arroz")

        val state = vm.uiState.value
        assertEquals("arroz", state.searchTerms)
        assertEquals(listOf("Arroz"), state.products.map { it.name })
    }

    @Test
    fun `given empty search terms, when onSearchTermsChanged, then all products shown`() = runTest {
        coEvery { cartRepository.getAllCarts() } returns listOf(cart(1))
        coEvery { productRepository.getProductsByCartId(1) } returns listOf(
            product(10, "Arroz", 1.0, 5.0),
            product(20, "Feijão", 1.0, 4.0),
        )
        val vm = CartDetailsViewModel(cartRepository, productRepository)
        vm.init(1, "Feijão")

        vm.onSearchTermsChanged(1, "")

        assertEquals(2, vm.uiState.value.products.size)
        assertEquals("", vm.uiState.value.searchTerms)
    }

    @Test
    fun `when deleteCart, then deletes products and cart and emits CartDeleted`() = runTest {
        val products = listOf(product(10, "Arroz", 1.0, 5.0), product(20, "Feijão", 1.0, 4.0))
        coEvery { cartRepository.getAllCarts() } returns listOf(cart(1))
        coEvery { productRepository.getProductsByCartId(1) } returns products
        val vm = CartDetailsViewModel(cartRepository, productRepository)
        vm.init(1)

        vm.events.test {
            vm.deleteCart(1)

            assertTrue(awaitItem() is CartDetailsEvents.CartDeleted)
        }
        coVerify(exactly = 1) { productRepository.deleteProduct(products[0]) }
        coVerify(exactly = 1) { productRepository.deleteProduct(products[1]) }
        coVerify(exactly = 1) { cartRepository.deleteCart(cart(1)) }
    }

    @Test
    fun `given delete failure, when deleteCart, then error is set and cart is not deleted`() = runTest {
        coEvery { cartRepository.getAllCarts() } returns listOf(cart(1))
        coEvery { productRepository.getProductsByCartId(1) } throws RuntimeException("delete boom")
        val vm = CartDetailsViewModel(cartRepository, productRepository)
        vm.init(1)

        vm.deleteCart(1)

        assertEquals("delete boom", vm.uiState.value.error)
        coVerify(exactly = 0) { cartRepository.deleteCart(any()) }
    }
}
