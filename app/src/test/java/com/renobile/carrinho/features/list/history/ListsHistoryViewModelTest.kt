package com.renobile.carrinho.features.list.history

import app.cash.turbine.test
import com.renobile.carrinho.database.entities.PurchaseListEntity
import com.renobile.carrinho.repositories.PurchaseListRepository
import io.mockk.coEvery
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
class ListsHistoryViewModelTest {

    private val purchaseListRepository = mockk<PurchaseListRepository>(relaxed = true)
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
    fun `given lists exists, when init, then load only closed lists`() = runTest {
        // Given
        val openList = PurchaseListEntity(
            id = 1,
            name = "Open List",
            dateOpen = 100,
            dateClose = 0,
            products = 0,
            units = 0.0,
            valueTotal = 0.0,
        )
        val closedList = PurchaseListEntity(
            id = 2,
            name = "Closed List",
            dateOpen = 100,
            dateClose = 200,
            products = 1,
            units = 1.0,
            valueTotal = 10.0,
        )

        coEvery { purchaseListRepository.getAllLists() } returns listOf(openList, closedList)

        // When
        val viewModel = ListsHistoryViewModel(purchaseListRepository)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.lists.size)
            assertEquals("Closed List", state.lists[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given search terms, when search, then filter lists by name`() = runTest {
        // Given
        val list1 = PurchaseListEntity(
            id = 1,
            name = "Groceries",
            dateOpen = 100,
            dateClose = 200,
            products = 1,
            units = 1.0,
            valueTotal = 10.0,
        )
        val list2 = PurchaseListEntity(
            id = 2,
            name = "Party",
            dateOpen = 100,
            dateClose = 200,
            products = 1,
            units = 1.0,
            valueTotal = 10.0,
        )

        coEvery { purchaseListRepository.getAllLists() } returns listOf(list1, list2)

        val viewModel = ListsHistoryViewModel(purchaseListRepository)

        // When
        viewModel.onSearchTermsChanged("Party")

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.lists.size)
            assertEquals("Party", state.lists[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
