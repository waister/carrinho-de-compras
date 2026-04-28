package com.renobile.carrinho.features.cart.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.R
import com.renobile.carrinho.util.PARAM_CART_ID
import com.renobile.carrinho.util.PARAM_SEARCH_TERMS
import com.renobile.carrinho.util.findNavControllerSafely
import org.koin.androidx.viewmodel.ext.android.viewModel

class CartsHistoryFragment : Fragment() {

    private val viewModel: CartsHistoryViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        CartsHistoryScreen(
            viewModel = viewModel,
            onBack = { findNavControllerSafely()?.popBackStack() },
            onCartClick = { cart ->
                val bundle = Bundle().apply {
                    putLong(PARAM_CART_ID, cart.id)
                    putString(PARAM_SEARCH_TERMS, viewModel.uiState.value.searchTerms)
                }
                findNavControllerSafely()?.navigate(R.id.cartDetailsFragment, bundle)
            }
        )
    }
}
