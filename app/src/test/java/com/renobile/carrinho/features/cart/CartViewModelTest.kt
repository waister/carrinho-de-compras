package com.renobile.carrinho.features.cart

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
class CartViewModelTest {

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
            prefsValues[firstArg()] = secondArg()
            Unit
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(Prefs)
    }

    private fun cart(id: Long) = CartEntity(id, "Carrinho $id", 100L, 0L, 0, 0.0, 0.0, "")

    private fun product(id: Long, name: String, quantity: Double) =
        ProductEntity(id, 1L, 0L, name, quantity, 5.0)

    private fun activeCartViewModel(): CartViewModel {
        coEvery { cartRepository.getActiveCart() } returns cart(1)
        return CartViewModel(cartRepository, productRepository)
    }

    @Test
    fun `given no active cart, when init, then state reflects empty cart`() = runTest {
        coEvery { cartRepository.getActiveCart() } returns null

        val vm = CartViewModel(cartRepository, productRepository)

        val state = vm.uiState.value
        assertNull(state.cart)
        assertEquals(emptyList<ProductEntity>(), state.products)
        assertEquals(ProductSortOrder.NEWEST, state.sortOrder)
        assertTrue(!state.isLoading)
    }

    @Test
    fun `given active cart with products, when init, then state is populated`() = runTest {
        val activeCart = cart(1)
        val products = listOf(product(10, "Arroz", 2.0))
        coEvery { cartRepository.getActiveCart() } returns activeCart
        coEvery { productRepository.getProductsByCartId(1) } returns products

        val vm = CartViewModel(cartRepository, productRepository)

        val state = vm.uiState.value
        assertEquals(activeCart, state.cart)
        assertEquals(products, state.products)
        assertEquals(ProductSortOrder.NEWEST, state.sortOrder)
        assertTrue(!state.isLoading)
    }

    @Test
    fun `given load failure, when init, then error is set`() = runTest {
        coEvery { cartRepository.getActiveCart() } throws RuntimeException("boom")

        val vm = CartViewModel(cartRepository, productRepository)

        assertEquals("boom", vm.uiState.value.error)
        assertTrue(!vm.uiState.value.isLoading)
    }

    @Test
    fun `given no active cart, when createCart, then inserts a new cart`() = runTest {
        coEvery { cartRepository.getActiveCart() } returns null
        val vm = CartViewModel(cartRepository, productRepository)

        vm.events.test {
            vm.createCart("Compras do mês")

            assertTrue(awaitItem() is CartEvents.ShowSnackbar)
        }
        coVerify(exactly = 1) {
            cartRepository.insertCart(match { it.name == "Compras do mês" && it.dateClose == 0L })
        }
    }

    @Test
    fun `given active cart with products, when createCart, then closes current cart and opens new one`() = runTest {
        coEvery { cartRepository.getActiveCart() } returns cart(1)
        coEvery { productRepository.getProductsByCartId(1) } returns listOf(product(10, "Arroz", 2.0))
        val vm = CartViewModel(cartRepository, productRepository)

        vm.createCart("Nova compra")

        coVerify(exactly = 1) {
            cartRepository.updateCart(match { it.id == 1L && it.dateClose != 0L && it.products == 1 })
        }
        coVerify(exactly = 1) {
            cartRepository.insertCart(match { it.name == "Nova compra" && it.dateClose == 0L })
        }
    }

    @Test
    fun `given active cart without products, when createCart with name, then renames cart`() = runTest {
        coEvery { cartRepository.getActiveCart() } returns cart(1)
        coEvery { productRepository.getProductsByCartId(1) } returns emptyList()
        val vm = CartViewModel(cartRepository, productRepository)

        vm.createCart("Renomeada")

        coVerify(exactly = 1) {
            cartRepository.updateCart(match { it.id == 1L && it.name == "Renomeada" && it.dateClose == 0L })
        }
        coVerify(exactly = 0) { cartRepository.insertCart(any()) }
    }

    @Test
    fun `when addOrEditProduct, then inserts product and shows snackbar`() = runTest {
        val vm = activeCartViewModel()
        val newProduct = product(0, "Arroz", 1.0)

        vm.events.test {
            vm.addOrEditProduct(newProduct)

            assertTrue(awaitItem() is CartEvents.ShowSnackbar)
        }
        coVerify(exactly = 1) { productRepository.insertProduct(newProduct) }
    }

    @Test
    fun `given product, when deleteProduct, then deletes and shows snackbar`() = runTest {
        val vm = activeCartViewModel()
        val target = product(10, "Arroz", 1.0)

        vm.events.test {
            vm.deleteProduct(target)

            assertTrue(awaitItem() is CartEvents.ShowSnackbar)
        }
        coVerify(exactly = 1) { productRepository.deleteProduct(target) }
    }

    @Test
    fun `given active cart, when clearCart, then deletes all products`() = runTest {
        coEvery { productRepository.getProductsByCartId(1) } returns listOf(product(10, "Arroz", 1.0))
        val vm = activeCartViewModel()

        vm.clearCart()

        coVerify(exactly = 1) { productRepository.deleteProductsByCartId(1) }
    }

    @Test
    fun `given no active cart, when clearCart, then does nothing`() = runTest {
        coEvery { cartRepository.getActiveCart() } returns null
        val vm = CartViewModel(cartRepository, productRepository)

        vm.clearCart()

        coVerify(exactly = 0) { productRepository.deleteProductsByCartId(any()) }
    }

    @Test
    fun `given valid delta, when changeQuantity, then updates quantity`() = runTest {
        coEvery { productRepository.getProductsByCartId(1) } returns listOf(product(10, "Arroz", 2.0))
        val vm = activeCartViewModel()
        val target = product(10, "Arroz", 2.0)

        vm.changeQuantity(target, 1.0)

        coVerify(exactly = 1) { productRepository.insertProduct(match { it.quantity == 3.0 }) }
    }

    @Test
    fun `given negative delta below minimum, when changeQuantity, then shows error snackbar`() = runTest {
        val vm = activeCartViewModel()
        val target = product(10, "Arroz", 1.0)

        vm.events.test {
            vm.changeQuantity(target, -2.0)

            assertTrue(awaitItem() is CartEvents.ShowSnackbar)
        }
        coVerify(exactly = 0) { productRepository.insertProduct(any()) }
    }

    @Test
    fun `given order, when onSortOrderChanged, then persists and reloads`() = runTest {
        val vm = activeCartViewModel()

        vm.onSortOrderChanged(ProductSortOrder.NAME_ASC)

        coVerify(exactly = 1) { Prefs.putValue(PREF_SORT_ORDER, ProductSortOrder.NAME_ASC.name) }
        assertEquals(ProductSortOrder.NAME_ASC, vm.uiState.value.sortOrder)
    }

    @Test
    fun `given search terms, when onSearchTermsChanged, then products are filtered`() = runTest {
        coEvery { cartRepository.getActiveCart() } returns cart(1)
        coEvery { productRepository.getProductsByCartId(1) } returns listOf(
            product(10, "Arroz", 1.0),
            product(20, "Feijão", 1.0)
        )
        val vm = CartViewModel(cartRepository, productRepository)

        vm.onSearchTermsChanged("feij")

        assertEquals(listOf("Feijão"), vm.uiState.value.products.map { it.name })
        assertEquals("feij", vm.uiState.value.searchTerms)
    }
}
