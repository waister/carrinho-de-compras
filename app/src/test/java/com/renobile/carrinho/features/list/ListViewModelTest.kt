package com.renobile.carrinho.features.list

import com.renobile.carrinho.database.entities.PurchaseListEntity
import com.renobile.carrinho.repositories.CartRepository
import com.renobile.carrinho.repositories.ProductRepository
import com.renobile.carrinho.repositories.PurchaseListRepository
import com.renobile.carrinho.util.Prefs
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {

    private val purchaseListRepository = mockk<PurchaseListRepository>(relaxed = true)
    private val productRepository = mockk<ProductRepository>(relaxed = true)
    private val cartRepository = mockk<CartRepository>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(Prefs)
        every { Prefs.getValue<String>(any(), any()) } answers { secondArg() }
    }

    @After
    fun tearDown() {
        unmockkObject(Prefs)
        Dispatchers.resetMain()
    }

    @Test
    fun `given active list, when import items, then products are inserted`() = runTest {
        // Given
        val activeList = PurchaseListEntity(
            id = 1,
            name = "My List",
            dateOpen = 100,
            dateClose = 0,
            products = 0,
            units = 0.0,
            valueTotal = 0.0,
        )
        coEvery { purchaseListRepository.getAllLists() } returns listOf(activeList)

        val viewModel = ListViewModel(purchaseListRepository, productRepository, cartRepository, testDispatcher)
        val itemsToImport = listOf("Arroz", "Feijão", "Café")

        // When
        viewModel.importList(itemsToImport)

        // Then
        coVerify(exactly = 1) { productRepository.insertProducts(any()) }
        coVerify {
            productRepository.insertProducts(
                match {
                    it.size == 3 &&
                        it[0].name == "Arroz" &&
                        it[0].listId == 1L &&
                        it[1].name == "Feijão" &&
                        it[1].listId == 1L &&
                        it[2].name == "Café" &&
                        it[2].listId == 1L
                },
            )
        }
    }

    @Test
    fun `given active list, when import items with quantity, then products are inserted with correct quantity`() = runTest {
        // Given
        val activeList = PurchaseListEntity(
            id = 1,
            name = "My List",
            dateOpen = 100,
            dateClose = 0,
            products = 0,
            units = 0.0,
            valueTotal = 0.0,
        )
        coEvery { purchaseListRepository.getAllLists() } returns listOf(activeList)

        val viewModel = ListViewModel(purchaseListRepository, productRepository, cartRepository, testDispatcher)
        val itemsToImport = listOf("3 Laranjas", "2.5 Abacates", "Feijão")

        // When
        viewModel.importList(itemsToImport)

        // Then
        coVerify {
            productRepository.insertProducts(
                match {
                    it.size == 3 &&
                        it[0].name == "Laranjas" &&
                        it[0].quantity == 3.0 &&
                        it[1].name == "Abacates" &&
                        it[1].quantity == 2.5 &&
                        it[2].name == "Feijão" &&
                        it[2].quantity == 1.0
                },
            )
        }
    }

    @Test
    fun `given existing products in list, when import items with duplicate names, then duplicates are not inserted`() = runTest {
        // Given
        val activeList = PurchaseListEntity(
            id = 1,
            name = "My List",
            dateOpen = 100,
            dateClose = 0,
            products = 1,
            units = 1.0,
            valueTotal = 0.0,
        )
        val existingProduct = com.renobile.carrinho.database.entities.ProductEntity(
            id = 10,
            cartId = 0,
            listId = 1,
            name = "Palito de Dente",
            quantity = 1.0,
            price = 0.0,
        )
        coEvery { purchaseListRepository.getAllLists() } returns listOf(activeList)
        coEvery { productRepository.getProductsByListId(1) } returns listOf(existingProduct)

        val viewModel = ListViewModel(purchaseListRepository, productRepository, cartRepository, testDispatcher)
        val itemsToImport = listOf("palito de dente", "Arroz")

        // When
        viewModel.importList(itemsToImport)

        // Then
        coVerify(exactly = 1) {
            productRepository.insertProducts(
                match {
                    it.size == 1 && it[0].name == "Arroz"
                },
            )
        }
    }
}
