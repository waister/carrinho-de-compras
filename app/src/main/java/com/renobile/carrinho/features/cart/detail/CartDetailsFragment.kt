package com.renobile.carrinho.features.cart.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.R
import com.renobile.carrinho.util.PARAM_CART_ID
import com.renobile.carrinho.util.PARAM_SEARCH_TERMS
import com.renobile.carrinho.util.findNavControllerSafely
import com.renobile.carrinho.util.longSnackbar
import com.renobile.carrinho.util.sendCart
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

class CartDetailsFragment : Fragment() {

    private val viewModel: CartDetailsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        val cartId = arguments?.getLong(PARAM_CART_ID, 0) ?: 0
        val searchTerms = arguments?.getString(PARAM_SEARCH_TERMS) ?: ""

        viewModel.init(cartId, searchTerms)

        val actions = CartDetailsActions(
            onSearchChanged = { viewModel.onSearchTermsChanged(cartId, it) },
            onDeleteCart = { viewModel.deleteCart(cartId) },
            onShareCart = {
                val state = viewModel.uiState.value
                activity?.sendCart(state.products, state.cart?.name ?: "")
            },
            onBack = { findNavControllerSafely()?.popBackStack() }
        )

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is CartDetailsEvents.ShowSnackbar -> view?.longSnackbar(event.messageResId)
                    is CartDetailsEvents.CartDeleted -> {
                        view?.longSnackbar(R.string.cart_deleted)
                        findNavControllerSafely()?.popBackStack()
                    }
                }
            }
        }

        CartDetailsScreen(
            viewModel = viewModel,
            actions = actions
        )
    }
}
