package com.renobile.carrinho.features.cart.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renobile.carrinho.repositories.CartRepository
import com.renobile.carrinho.repositories.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CartsHistoryViewModel(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartsHistoryState())
    val uiState: StateFlow<CartsHistoryState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                updateMissingKeywords()
                
                val allCarts = cartRepository.getAllCarts().filter { it.dateClose > 0 }
                val searchTerms = _uiState.value.searchTerms
                
                val filteredCarts = if (searchTerms.isNotEmpty()) {
                    allCarts.filter {
                        it.name.contains(searchTerms, ignoreCase = true) ||
                        it.keywords.contains(searchTerms, ignoreCase = true)
                    }
                } else {
                    allCarts
                }.sortedByDescending { it.id }

                _uiState.update { it.copy(isLoading = false, carts = filteredCarts) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun updateMissingKeywords() {
        val cartsWithoutKeywords = cartRepository.getAllCarts().filter { it.dateClose > 0 && it.keywords.isEmpty() }
        cartsWithoutKeywords.forEach { cart ->
            val products = productRepository.getProductsByCartId(cart.id)
            if (products.isNotEmpty()) {
                val keywords = products.joinToString(", ") { it.name }
                cartRepository.updateCart(cart.copy(keywords = keywords))
            }
        }
    }

    fun onSearchTermsChanged(terms: String) {
        _uiState.update { it.copy(searchTerms = terms) }
        loadData()
    }
}
