package com.renobile.carrinho.repositories

import com.renobile.carrinho.database.dao.ProductDao
import com.renobile.carrinho.database.entities.ProductEntity

interface ProductRepository {
    suspend fun getProductsByCartId(cartId: Long): List<ProductEntity>
    suspend fun getProductsByListId(listId: Long): List<ProductEntity>
    suspend fun insertProduct(product: ProductEntity)
    suspend fun insertProducts(products: List<ProductEntity>)
    suspend fun deleteProduct(product: ProductEntity)
    suspend fun deleteProductsByCartId(cartId: Long)
    suspend fun getAllProductNames(): List<String>
    suspend fun getProductSuggestions(): List<com.renobile.carrinho.database.entities.ProductSuggestion>
}

class ProductRepositoryImpl(
    private val productDao: ProductDao
) : ProductRepository {
    override suspend fun getProductsByCartId(cartId: Long): List<ProductEntity> = productDao.getByCartId(cartId)

    override suspend fun getProductsByListId(listId: Long): List<ProductEntity> = productDao.getByListId(listId)

    override suspend fun insertProduct(product: ProductEntity) {
        productDao.insert(product)
    }

    override suspend fun insertProducts(products: List<ProductEntity>) {
        productDao.insertAll(products)
    }

    override suspend fun deleteProduct(product: ProductEntity) {
        productDao.delete(product)
    }

    override suspend fun deleteProductsByCartId(cartId: Long) {
        productDao.deleteByCartId(cartId)
    }

    override suspend fun getAllProductNames(): List<String> = productDao.getAllNames()

    override suspend fun getProductSuggestions(): List<com.renobile.carrinho.database.entities.ProductSuggestion> = productDao.getProductSuggestions()
}
