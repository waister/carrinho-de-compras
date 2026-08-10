package com.renobile.carrinho.database.dao

import androidx.room.Room
import com.renobile.carrinho.database.AppDatabase
import com.renobile.carrinho.database.entities.PurchaseListEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PurchaseListDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: PurchaseListDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.purchaseListDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun list(id: Long, dateOpen: Long) =
        PurchaseListEntity(id, "Lista $id", dateOpen, 0L, 0, 0.0, 0.0)

    @Test
    fun `given lists, when getAll, then returns them ordered by dateOpen desc`() = runTest {
        dao.insert(list(1, 100))
        dao.insert(list(2, 200))

        assertEquals(listOf(2L, 1L), dao.getAll().map { it.id })
    }

    @Test
    fun `given lists, when insertAll then getAll, then all are persisted`() = runTest {
        dao.insertAll(listOf(list(1, 100), list(2, 200)))

        assertEquals(2, dao.getAll().size)
    }

    @Test
    fun `given list, when delete, then list is removed`() = runTest {
        dao.insert(list(1, 100))
        dao.insert(list(2, 200))

        dao.delete(list(1, 100))

        assertEquals(listOf(2L), dao.getAll().map { it.id })
    }
}
