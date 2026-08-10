package com.renobile.carrinho.database.dao

import androidx.room.*
import com.renobile.carrinho.database.entities.ProductEntity

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE cartId = :cartId ORDER BY id DESC")
    suspend fun getByCartId(cartId: Long): List<ProductEntity>

    @Query("SELECT DISTINCT name FROM products ORDER BY name ASC")
    suspend fun getAllNames(): List<String>

    @Query("""
        SELECT name, MAX(lastDate) as lastDate FROM (
            SELECT p.name, MAX(c.dateOpen) as lastDate 
            FROM products p 
            JOIN carts c ON p.cartId = c.id 
            GROUP BY p.name
            UNION
            SELECT p.name, MAX(l.dateOpen) as lastDate
            FROM products p
            JOIN purchase_lists l ON p.listId = l.id
            GROUP BY p.name
        ) GROUP BY name ORDER BY lastDate DESC
    """)
    suspend fun getProductSuggestions(): List<com.renobile.carrinho.database.entities.ProductSuggestion>

    @Query("SELECT * FROM products WHERE listId = :listId ORDER BY id DESC")
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
