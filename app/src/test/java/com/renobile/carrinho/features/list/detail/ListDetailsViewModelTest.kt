package com.renobile.carrinho.features.list.detail

import app.cash.turbine.test
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.database.entities.PurchaseListEntity
import com.renobile.carrinho.repositories.CartRepository
import com.renobile.carrinho.repositories.ProductRepository
import com.renobile.carrinho.repositories.PurchaseListRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListDetailsViewModelTest {

    private val purchaseListRepository = mockk<PurchaseListRepository>(relaxed = true)
    private val productRepository = mockk<ProductRepository>(relaxed = true)
    private val cartRepository = mockk<CartRepository>(relaxed = true)
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

    private fun list(id: Long) = PurchaseListEntity(id, "Lista $id", 100L, 0L, 0, 0.0, 0.0)

    private fun product(id: Long, name: String, listId: Long) =
        ProductEntity(id, 0L, listId, name, 1.0, 5.0)

    @Test
    fun `given existing list, when init, then state is populated with sorted products`() = runTest {
        coEvery { purchaseListRepository.getAllLists() } returns listOf(list(1))
        coEvery { productRepository.getProductsByListId(1) } returns listOf(
            product(20, "Feijão", 1),
            product(10, "Arroz", 1)
        )
        coEvery { productRepository.getProductSuggestions() } returns emptyList()

        val vm = ListDetailsViewModel(purchaseListRepository, productRepository, cartRepository)
        vm.init(1)

        val state = vm.uiState.value
        assertEquals("Lista 1", state.list?.name)
        assertEquals(listOf(20L, 10L), state.products.map { it.id })
        assertTrue(!state.isLoading)
    }

    @Test
    fun `given missing list, when init, then error is set`() = runTest {
        coEvery { purchaseListRepository.getAllLists() } returns emptyList()

        val vm = ListDetailsViewModel(purchaseListRepository, productRepository, cartRepository)
        vm.init(999)

        assertEquals("List not found", vm.uiState.value.error)
    }

    @Test
    fun `given search terms, when onSearchTermsChanged, then products are filtered`() = runTest {
        coEvery { purchaseListRepository.getAllLists() } returns listOf(list(1))
        coEvery { productRepository.getProductsByListId(1) } returns listOf(
            product(10, "Arroz", 1),
            product(20, "Feijão", 1)
        )
        coEvery { productRepository.getProductSuggestions() } returns emptyList()
        val vm = ListDetailsViewModel(purchaseListRepository, productRepository, cartRepository)
        vm.init(1)

        vm.onSearchTermsChanged(1, "feij")

        assertEquals(listOf("Feijão"), vm.uiState.value.products.map { it.name })
        assertEquals("feij", vm.uiState.value.searchTerms)
    }

    @Test
    fun `given order, when onSortOrderChanged, then products are re-sorted`() = runTest {
        coEvery { purchaseListRepository.getAllLists() } returns listOf(list(1))
        coEvery { productRepository.getProductsByListId(1) } returns listOf(
            product(20, "Zebra", 1),
            product(10, "Arroz", 1)
        )
        coEvery { productRepository.getProductSuggestions() } returns emptyList()
        val vm = ListDetailsViewModel(purchaseListRepository, productRepository, cartRepository)
        vm.init(1)

        vm.onSortOrderChanged(1, ProductSortOrder.NAME_ASC)

        coVerify(exactly = 1) { Prefs.putValue(PREF_SORT_ORDER, ProductSortOrder.NAME_ASC.name) }
        assertEquals(listOf("Arroz", "Zebra"), vm.uiState.value.products.map { it.name })
    }

    @Test
    fun `given list id, when deleteList, then deletes products and list and emits event`() = runTest {
        coEvery { productRepository.getProductsByListId(1) } returns listOf(product(10, "Arroz", 1))
        coEvery { purchaseListRepository.getAllLists() } returns listOf(list(1))
        val vm = ListDetailsViewModel(purchaseListRepository, productRepository, cartRepository)

        vm.events.test {
            vm.deleteList(1)

            assertTrue(awaitItem() is ListDetailsEvents.ListDeleted)
        }
        coVerify(exactly = 1) { productRepository.deleteProduct(product(10, "Arroz", 1)) }
        coVerify(exactly = 1) { purchaseListRepository.deleteList(list(1)) }
    }

    @Test
    fun `given product, when moveToCart, then moves product to cart and emits snackbar`() = runTest {
        coEvery { purchaseListRepository.getAllLists() } returns listOf(list(1))
        coEvery { productRepository.getProductsByListId(1) } returns listOf(product(10, "Arroz", 1))
        coEvery { productRepository.getProductSuggestions() } returns emptyList()
        val vm = ListDetailsViewModel(purchaseListRepository, productRepository, cartRepository)
        vm.init(1)
        val target = product(10, "Arroz", 1)

        vm.events.test {
            vm.moveToCart(target, cartId = 7, quantity = 3.0, price = 12.5)

            assertTrue(awaitItem() is ListDetailsEvents.ShowSnackbar)
        }
        coVerify(exactly = 1) {
            productRepository.insertProduct(
                match { it.cartId == 7L && it.listId == 0L && it.quantity == 3.0 && it.price == 12.5 }
            )
        }
    }

    @Test
    fun `given active cart, when getActiveCartId, then returns its id`() = runTest {
        val activeCart = com.renobile.carrinho.database.entities.CartEntity(3, "Carrinho", 100L, 0L, 0, 0.0, 0.0, "")
        coEvery { cartRepository.getActiveCart() } returns activeCart
        val vm = ListDetailsViewModel(purchaseListRepository, productRepository, cartRepository)

        assertEquals(3L, vm.getActiveCartId())
    }

    @Test
    fun `given no active cart, when getActiveCartId, then returns null`() = runTest {
        coEvery { cartRepository.getActiveCart() } returns null
        val vm = ListDetailsViewModel(purchaseListRepository, productRepository, cartRepository)

        assertNull(vm.getActiveCartId())
    }

    @Test
    fun `given volumes and total, then state computes them`() = runTest {
        coEvery { purchaseListRepository.getAllLists() } returns listOf(list(1))
        coEvery { productRepository.getProductsByListId(1) } returns listOf(
            product(10, "Arroz", 1).copy(quantity = 2.0),
            product(20, "Feijão", 1).copy(quantity = 3.0, price = 10.0)
        )
        coEvery { productRepository.getProductSuggestions() } returns emptyList()
        val vm = ListDetailsViewModel(purchaseListRepository, productRepository, cartRepository)
        vm.init(1)

        val state = vm.uiState.value
        assertEquals(5.0, state.volumes, 0.0)
        assertEquals(40.0, state.total, 0.0)
        assertNotNull(state.list)
    }
}
