package com.renobile.carrinho.features.list.detail

import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.database.entities.PurchaseListEntity
import com.renobile.carrinho.util.ProductSortOrder

data class ListDetailsState(
    val isLoading: Boolean = false,
    val list: PurchaseListEntity? = null,
    val products: List<ProductEntity> = emptyList(),
    val productNames: List<String> = emptyList(),
    val suggestions: List<com.renobile.carrinho.database.entities.ProductSuggestion> = emptyList(),
    val sortOrder: ProductSortOrder = ProductSortOrder.NEWEST,
    val error: String? = null
) {
    val volumes: Double get() = products.sumOf { it.quantity }
    val total: Double get() = products.sumOf { it.price * it.quantity }
}

sealed interface ListDetailsEvents {
    data class ShowSnackbar(val messageResId: Int) : ListDetailsEvents
    data object ListDeleted : ListDetailsEvents
}
