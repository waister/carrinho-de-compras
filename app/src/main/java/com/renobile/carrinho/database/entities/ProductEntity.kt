package com.renobile.carrinho.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: Long,
    val cartId: Long,
    val listId: Long,
    val name: String,
    val quantity: Double,
    val price: Double
)
