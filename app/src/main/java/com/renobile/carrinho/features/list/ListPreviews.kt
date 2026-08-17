package com.renobile.carrinho.features.list

import com.renobile.carrinho.database.entities.PurchaseListEntity
import com.renobile.carrinho.features.cart.productPreview

val listPreview = PurchaseListEntity(
    id = 1L,
    name = "Churrasco",
    dateOpen = System.currentTimeMillis(),
    dateClose = 0L,
    products = 3,
    units = 5.0,
    valueTotal = 150.0
)

val listProductsPreview = listOf(
    productPreview.copy(name = "Picanha", quantity = 2.0, price = 60.0),
    productPreview.copy(name = "Cerveja", quantity = 12.0, price = 4.50),
    productPreview.copy(name = "Carvão", quantity = 1.0, price = 15.0)
)
