package com.renobile.carrinho.features.list.history

import com.renobile.carrinho.database.entities.PurchaseListEntity

data class ListsHistoryState(
    val isLoading: Boolean = false,
    val lists: List<PurchaseListEntity> = emptyList(),
    val searchTerms: String = "",
    val error: String? = null
)
