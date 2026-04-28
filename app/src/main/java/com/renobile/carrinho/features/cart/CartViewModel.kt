package com.renobile.carrinho.features.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.repositories.CartRepository
import com.renobile.carrinho.repositories.ProductRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CartViewModel(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartState())
    val uiState: StateFlow<CartState> = _uiState.asStateFlow()

    private val _events = Channel<CartEvents>()
    val events = _events.receiveAsFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val activeCart = cartRepository.getActiveCart()
                val products = activeCart?.let { productRepository.getProductsByCartId(it.id) } ?: emptyList()
                val names = productRepository.getAllProductNames()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        cart = activeCart,
                        products = if (it.searchTerms.isEmpty()) products
                        else products.filter { p -> p.name.contains(it.searchTerms, ignoreCase = true) },
                        productNames = names
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onSearchTermsChanged(terms: String) {
        _uiState.update { it.copy(searchTerms = terms) }
        loadData()
    }

    fun createCart(name: String) {
        viewModelScope.launch {
            try {
                val currentCart = _uiState.value.cart
                val currentProducts = productRepository.getProductsByCartId(currentCart?.id ?: -1)

                if (currentCart != null) {
                    if (currentProducts.isEmpty()) {
                        // Se o carrinho atual está vazio, apenas atualizamos o nome se foi passado um,
                        // ou mantemos ele como o "novo" carrinho.
                        if (name.isNotEmpty()) {
                            cartRepository.updateCart(currentCart.copy(name = name))
                        }
                        loadData()
                        _events.send(CartEvents.ShowSnackbar(R.string.create_cart_success))
                        return@launch
                    } else {
                        // Arquiva o carrinho atual com os produtos
                        val updatedCart = currentCart.copy(
                            dateClose = System.currentTimeMillis(),
                            products = currentProducts.size,
                            units = currentProducts.sumOf { it.quantity },
                            valueTotal = currentProducts.sumOf { it.price * it.quantity },
                            keywords = currentProducts.joinToString(", ") { it.name }
                        )
                        cartRepository.updateCart(updatedCart)
                    }
                }

                // Cria o novo carrinho
                val finalName = if (name.isEmpty()) createCartListName() else name
                val newId = System.currentTimeMillis()
                val newCart = CartEntity(
                    id = newId,
                    name = finalName,
                    dateOpen = System.currentTimeMillis(),
                    dateClose = 0L,
                    products = 0,
                    units = 0.0,
                    valueTotal = 0.0,
                    keywords = ""
                )
                cartRepository.insertCart(newCart)
                loadData()
                _events.send(CartEvents.ShowSnackbar(R.string.create_cart_success))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun createCartListName(): String {
        val currentMillis = System.currentTimeMillis()
        val day = java.text.SimpleDateFormat("dd", java.util.Locale.getDefault()).format(currentMillis)
        val month = java.text.SimpleDateFormat("MM", java.util.Locale.getDefault()).format(currentMillis)
        return "Compras $day/$month"
    }

    fun addOrEditProduct(product: ProductEntity) {
        viewModelScope.launch {
            try {
                productRepository.insertProduct(product)
                loadData()
                _events.send(CartEvents.ShowSnackbar(R.string.product_added))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            try {
                productRepository.deleteProduct(product)
                loadData()
                _events.send(CartEvents.ShowSnackbar(R.string.success_delete))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearCart() {
        val cartId = _uiState.value.cart?.id ?: return
        viewModelScope.launch {
            try {
                productRepository.deleteProductsByCartId(cartId)
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun changeQuantity(product: ProductEntity, delta: Double) {
        if (delta < 0 && (product.quantity + delta) <= 0) {
            viewModelScope.launch { _events.send(CartEvents.ShowSnackbar(R.string.error_quantity_min)) }
            return
        }
        viewModelScope.launch {
            try {
                val updatedProduct = product.copy(quantity = product.quantity + delta)
                productRepository.insertProduct(updatedProduct)
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
