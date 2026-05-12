package com.renobile.carrinho.features.cart.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.util.findNavControllerSafely
import org.koin.androidx.viewmodel.ext.android.viewModel

class CartsHistoryFragment : Fragment() {

    private val viewModel: CartsHistoryViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        CartsHistoryScreen(
            viewModel = viewModel,
            onBackClick = ::onBackClick,
            onCartClick = ::onCartClick
        )
    }

    private fun onBackClick() {
        findNavControllerSafely()?.popBackStack()
    }

    private fun onCartClick(cart: CartEntity) {
        findNavControllerSafely()?.navigate(
            "cartDetails/${cart.id}?searchTerms=${viewModel.uiState.value.searchTerms}"
        )
    }
}
