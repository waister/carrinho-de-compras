package com.renobile.carrinho.repositories

import com.renobile.carrinho.database.dao.CartDao
import com.renobile.carrinho.database.entities.CartEntity

interface CartRepository {
    suspend fun getActiveCart(): CartEntity?
    suspend fun getAllCarts(): List<CartEntity>
    suspend fun insertCart(cart: CartEntity)
    suspend fun updateCart(cart: CartEntity)
    suspend fun deleteCart(cart: CartEntity)
}

class CartRepositoryImpl(
    private val cartDao: CartDao,
) : CartRepository {
    override suspend fun getActiveCart(): CartEntity? {
        return cartDao.getAll().firstOrNull { it.dateClose == 0L }
    }

    override suspend fun getAllCarts(): List<CartEntity> {
        return cartDao.getAll()
    }

    override suspend fun insertCart(cart: CartEntity) {
        cartDao.insert(cart)
    }

    override suspend fun updateCart(cart: CartEntity) {
        cartDao.insert(cart) // Room @Insert(onConflict = REPLACE) handles update
    }

    override suspend fun deleteCart(cart: CartEntity) {
        cartDao.delete(cart)
    }
}
