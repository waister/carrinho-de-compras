package com.renobile.carrinho.features.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.MainActivity
import com.renobile.carrinho.R
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
            val actions = CartActions(
                onSearchChanged = { viewModel.onSearchTermsChanged(it) },
                onCreateCart = { viewModel.createCart(it) },
                onAddOrEditProduct = { viewModel.addOrEditProduct(it) },
                onDeleteProduct = { viewModel.deleteProduct(it) },
                onChangeQuantity = { product, delta -> viewModel.changeQuantity(product, delta) },
                onSendCart = {
                    val state = viewModel.uiState.value
                    activity?.sendCart(state.products, state.cart?.name ?: "")
                },
                onClearCart = { viewModel.clearCart() },
                onOpenHistory = {
                    findNavControllerSafely()?.navigate(R.id.cartsHistoryFragment)
                },
                onShareApp = { activity?.shareApp() },
                onShowInterstitialAd = { (activity as? MainActivity)?.showInterstitialAd() },
            )

            LaunchedEffect(Unit) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        is CartEvents.ShowSnackbar -> view?.longSnackbar(event.messageResId)
                        is CartEvents.ShowInterstitialAd -> (activity as? MainActivity)?.showInterstitialAd()
                    }
                }
            }

            CartScreen(
                viewModel = viewModel,
                actions = actions,
            )
        }
    }
}
