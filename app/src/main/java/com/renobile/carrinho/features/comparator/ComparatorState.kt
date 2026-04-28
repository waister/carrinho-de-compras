package com.renobile.carrinho.features.comparator

data class ComparatorState(
    val priceFirst: String = "",
    val sizeFirst: String = "",
    val priceSecond: String = "",
    val sizeSecond: String = "",
    val resultFirst: String? = null,
    val resultSecond: String? = null,
    val resultPercentage: String? = null,
    val showResult: Boolean = false
)
