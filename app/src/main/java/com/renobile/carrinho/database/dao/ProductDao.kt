package com.renobile.carrinho.database.dao

import androidx.room.*
import com.renobile.carrinho.database.entities.ProductEntity

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE cartId = :cartId")
    suspend fun getByCartId(cartId: Long): List<ProductEntity>

    @Query("SELECT DISTINCT name FROM products ORDER BY name ASC")
    suspend fun getAllNames(): List<String>

    @Query("SELECT * FROM products WHERE listId = :listId")
    suspend fun getByListId(listId: Long): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("DELETE FROM products WHERE cartId = :cartId")
    suspend fun deleteByCartId(cartId: Long)
}
