package com.renobile.carrinho.database.dao

import androidx.room.Room
import com.renobile.carrinho.database.AppDatabase
import com.renobile.carrinho.database.entities.CartEntity
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
class CartDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: CartDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.cartDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun cart(id: Long, dateOpen: Long) =
        CartEntity(id, "Carrinho $id", dateOpen, 0L, 0, 0.0, 0.0, "")

    @Test
    fun `given carts, when getAll, then returns them ordered by dateOpen desc`() = runTest {
        dao.insert(cart(1, 100))
        dao.insert(cart(2, 200))
        dao.insert(cart(3, 150))

        val all = dao.getAll()

        assertEquals(listOf(2L, 3L, 1L), all.map { it.id })
    }

    @Test
    fun `given cart, when insert then count, then cart is persisted`() = runTest {
        dao.insert(cart(1, 100))

        assertEquals(1, dao.count())
    }

    @Test
    fun `given carts, when insertAll then getAll, then all are persisted`() = runTest {
        dao.insertAll(listOf(cart(1, 100), cart(2, 200)))

        assertEquals(2, dao.getAll().size)
    }

    @Test
    fun `given cart, when delete, then cart is removed`() = runTest {
        val target = cart(1, 100)
        dao.insert(target)
        dao.insert(cart(2, 200))

        dao.delete(target)

        assertEquals(listOf(2L), dao.getAll().map { it.id })
    }
}
