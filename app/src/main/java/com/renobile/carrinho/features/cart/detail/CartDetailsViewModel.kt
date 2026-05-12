package com.renobile.carrinho.features.cart.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renobile.carrinho.repositories.CartRepository
import com.renobile.carrinho.repositories.ProductRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CartDetailsViewModel(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartDetailsState())
    val uiState: StateFlow<CartDetailsState> = _uiState.asStateFlow()

    private val _events = Channel<CartDetailsEvents>()
    val events = _events.receiveAsFlow()

    fun init(cartId: Long, initialSearchTerms: String = "") {
        _uiState.update { it.copy(searchTerms = initialSearchTerms) }
        loadData(cartId)
    }

    private fun loadData(cartId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val carts = cartRepository.getAllCarts()
                val cart = carts.find { it.id == cartId }
                if (cart == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Cart not found") }
                    return@launch
                }

                val products = productRepository.getProductsByCartId(cartId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        cart = cart,
                        products = if (it.searchTerms.isEmpty()) products
                        else products.filter { p -> p.name.contains(it.searchTerms, ignoreCase = true) }
                            .sortedByDescending { p -> p.id }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onSearchTermsChanged(cartId: Long, terms: String) {
        _uiState.update { it.copy(searchTerms = terms) }
        loadData(cartId)
    }

    fun deleteCart(cartId: Long) {
        viewModelScope.launch {
            try {
                val products = productRepository.getProductsByCartId(cartId)
                products.forEach { productRepository.deleteProduct(it) }

                val carts = cartRepository.getAllCarts()
                val cart = carts.find { it.id == cartId }
                cart?.let { cartRepository.deleteCart(it) }

                _events.send(CartDetailsEvents.CartDeleted)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
