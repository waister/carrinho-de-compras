package com.renobile.carrinho.repositories

import com.renobile.carrinho.database.dao.PurchaseListDao
import com.renobile.carrinho.database.entities.PurchaseListEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PurchaseListRepositoryImplTest {

    private val purchaseListDao = mockk<PurchaseListDao>(relaxed = true)
    private val repository = PurchaseListRepositoryImpl(purchaseListDao)

    private val list = PurchaseListEntity(1, "Lista", 100, 0, 0, 0.0, 0.0)

    @Test
    fun `when getAllLists, then returns all lists`() = runTest {
        coEvery { purchaseListDao.getAll() } returns listOf(list)

        assertEquals(listOf(list), repository.getAllLists())
    }

    @Test
    fun `given list, when insertList, then delegates to dao`() = runTest {
        repository.insertList(list)

        coVerify(exactly = 1) { purchaseListDao.insert(list) }
    }

    @Test
    fun `given list, when deleteList, then delegates to dao`() = runTest {
        repository.deleteList(list)

        coVerify(exactly = 1) { purchaseListDao.delete(list) }
    }
}
