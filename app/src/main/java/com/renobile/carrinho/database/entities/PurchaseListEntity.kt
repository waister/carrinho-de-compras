package com.renobile.carrinho.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_lists")
data class PurchaseListEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val dateOpen: Long,
    val dateClose: Long,
    val products: Int,
    val units: Double,
    val valueTotal: Double
)
