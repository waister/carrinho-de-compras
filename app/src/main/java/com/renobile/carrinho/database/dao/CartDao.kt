package com.renobile.carrinho.database.dao

import androidx.room.*
import com.renobile.carrinho.database.entities.CartEntity

@Dao
interface CartDao {
    @Query("SELECT * FROM carts ORDER BY dateOpen DESC")
    suspend fun getAll(): List<CartEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cart: CartEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(carts: List<CartEntity>)

    @Delete
    suspend fun delete(cart: CartEntity)

    @Query("SELECT COUNT(*) FROM carts")
    suspend fun count(): Int
}
