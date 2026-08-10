package com.renobile.carrinho.database.dao

import androidx.room.Room
import com.renobile.carrinho.database.AppDatabase
import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.database.entities.ProductEntity
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
class ProductDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ProductDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.productDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun product(id: Long, cartId: Long, listId: Long, name: String, quantity: Double = 1.0) =
        ProductEntity(id, cartId, listId, name, quantity, 5.0)

    @Test
    fun `given products, when insert then getByCartId, then returns only cart products`() = runTest {
        dao.insert(product(1, 1, 0, "Arroz"))
        dao.insert(product(2, 1, 0, "Feijão"))
        dao.insert(product(3, 2, 0, "Café"))

        val result = dao.getByCartId(1)

        assertEquals(listOf("Feijão", "Arroz"), result.map { it.name })
    }

    @Test
    fun `given products, when insert then getByListId, then returns only list products`() = runTest {
        dao.insert(product(1, 0, 1, "Arroz"))
        dao.insert(product(2, 0, 1, "Feijão"))
        dao.insert(product(3, 0, 2, "Café"))

        val result = dao.getByListId(2)

        assertEquals(listOf("Café"), result.map { it.name })
    }

    @Test
    fun `given products, when getAllNames, then returns distinct names ordered`() = runTest {
        dao.insert(product(1, 1, 0, "Feijão"))
        dao.insert(product(2, 1, 0, "Arroz"))
        dao.insert(product(3, 1, 0, "Feijão"))

        assertEquals(listOf("Arroz", "Feijão"), dao.getAllNames())
    }

    @Test
    fun `given product, when deleteByCartId, then removes cart products`() = runTest {
        dao.insert(product(1, 1, 0, "Arroz"))
        dao.insert(product(2, 2, 0, "Café"))

        dao.deleteByCartId(1)

        assertEquals(listOf("Café"), dao.getByCartId(2).map { it.name })
        assertEquals(0, dao.getByCartId(1).size)
    }

    @Test
    fun `given product, when delete, then removes product`() = runTest {
        val target = product(1, 1, 0, "Arroz")
        dao.insert(target)
        dao.insert(product(2, 1, 0, "Café"))

        dao.delete(target)

        assertEquals(listOf("Café"), dao.getByCartId(1).map { it.name })
    }

    @Test
    fun `given products, when insertAll, then all are persisted`() = runTest {
        dao.insertAll(listOf(product(1, 1, 0, "Arroz"), product(2, 1, 0, "Café")))

        assertEquals(2, dao.getByCartId(1).size)
    }

    @Test
    fun `given products in carts and lists, when getProductSuggestions, then returns suggestions by recency`() = runTest {
        db.cartDao().insert(CartEntity(1, "Carrinho", 100L, 0L, 0, 0.0, 0.0, ""))
        db.purchaseListDao().insert(PurchaseListEntity(2, "Lista", 200L, 0L, 0, 0.0, 0.0))
        dao.insert(product(1, 1, 0, "Arroz"))
        dao.insert(product(2, 0, 2, "Feijão"))

        val suggestions = dao.getProductSuggestions()

        assertEquals(listOf("Feijão", "Arroz"), suggestions.map { it.name })
    }
}
