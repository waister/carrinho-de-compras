package com.renobile.carrinho.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carts")
data class CartEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val dateOpen: Long,
    val dateClose: Long,
    val products: Int,
    val units: Double,
    val valueTotal: Double,
    val keywords: String
)
