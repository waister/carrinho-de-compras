package com.renobile.carrinho.repositories

import com.renobile.carrinho.database.dao.ProductDao
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.database.entities.ProductSuggestion
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductRepositoryImplTest {

    private val productDao = mockk<ProductDao>(relaxed = true)
    private val repository = ProductRepositoryImpl(productDao)

    private val product = ProductEntity(1, 1, 0, "Arroz", 2.0, 10.0)

    @Test
    fun `given cart id, when getProductsByCartId, then delegates to dao`() = runTest {
        coEvery { productDao.getByCartId(1) } returns listOf(product)

        assertEquals(listOf(product), repository.getProductsByCartId(1))
    }

    @Test
    fun `given list id, when getProductsByListId, then delegates to dao`() = runTest {
        coEvery { productDao.getByListId(1) } returns listOf(product)

        assertEquals(listOf(product), repository.getProductsByListId(1))
    }

    @Test
    fun `given product, when insertProduct, then delegates to dao`() = runTest {
        repository.insertProduct(product)

        coVerify(exactly = 1) { productDao.insert(product) }
    }

    @Test
    fun `given products, when insertProducts, then delegates to dao`() = runTest {
        val products = listOf(product)

        repository.insertProducts(products)

        coVerify(exactly = 1) { productDao.insertAll(products) }
    }

    @Test
    fun `given product, when deleteProduct, then delegates to dao`() = runTest {
        repository.deleteProduct(product)

        coVerify(exactly = 1) { productDao.delete(product) }
    }

    @Test
    fun `given cart id, when deleteProductsByCartId, then delegates to dao`() = runTest {
        repository.deleteProductsByCartId(1)

        coVerify(exactly = 1) { productDao.deleteByCartId(1) }
    }

    @Test
    fun `when getAllProductNames, then delegates to dao`() = runTest {
        coEvery { productDao.getAllNames() } returns listOf("Arroz", "Feijão")

        assertEquals(listOf("Arroz", "Feijão"), repository.getAllProductNames())
    }

    @Test
    fun `when getProductSuggestions, then delegates to dao`() = runTest {
        val suggestions = listOf(ProductSuggestion("Arroz", 100L))
        coEvery { productDao.getProductSuggestions() } returns suggestions

        assertEquals(suggestions, repository.getProductSuggestions())
    }
}
