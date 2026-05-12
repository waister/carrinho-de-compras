package com.renobile.carrinho.features.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.MainActivity
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.ui.theme.MyAppTheme
import com.renobile.carrinho.util.findNavControllerSafely
import com.renobile.carrinho.util.longSnackbar
import com.renobile.carrinho.util.sendCart
import com.renobile.carrinho.util.shareApp
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

class CartFragment : Fragment() {

    private val viewModel: CartViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        MyAppTheme {
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        is CartEvents.ShowSnackbar -> showSnackbar(event.messageResId)
                        is CartEvents.ShowInterstitialAd -> onShowInterstitialAd()
                    }
                }
            }

            CartScreen(
                state = state,
                actions = CartActions(
                    onSearchChanged = ::onSearchChanged,
                    onCreateCart = ::onCreateCart,
                    onAddOrEditProduct = ::onAddOrEditProduct,
                    onDeleteProduct = ::onDeleteProduct,
                    onChangeQuantity = ::onChangeQuantity,
                    onSendCart = ::onSendCart,
                    onClearCart = ::onClearCart,
                    onOpenHistory = ::onOpenHistory,
                    onShareApp = ::onShareApp,
                    onShowInterstitialAd = ::onShowInterstitialAd,
                ),
            )
        }
    }

    private fun showSnackbar(@StringRes message: Int) {
        view?.longSnackbar(message)
    }

    private fun onSearchChanged(query: String) {
        viewModel.onSearchTermsChanged(query)
    }

    private fun onCreateCart(it: String) {
        viewModel.createCart(it)
    }

    private fun onAddOrEditProduct(it: ProductEntity) {
        viewModel.addOrEditProduct(it)
    }

    private fun onDeleteProduct(it: ProductEntity) {
        viewModel.deleteProduct(it)
    }

    private fun onChangeQuantity(product: ProductEntity, delta: Double) {
        viewModel.changeQuantity(product, delta)
    }

    private fun onSendCart() {
        val state = viewModel.uiState.value
        activity?.sendCart(state.products, state.cart?.name ?: "")
    }

    private fun onClearCart() {
        viewModel.clearCart()
    }

    private fun onOpenHistory() {
        findNavControllerSafely()?.navigate("cartsHistory")
    }

    private fun onShareApp() {
        activity?.shareApp()
    }

    private fun onShowInterstitialAd() {
        (activity as? MainActivity)?.showInterstitialAd()
    }
}
