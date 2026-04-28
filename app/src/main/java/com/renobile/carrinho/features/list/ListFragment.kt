package com.renobile.carrinho.features.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.R
import com.renobile.carrinho.util.findNavControllerSafely
import com.renobile.carrinho.util.longSnackbar
import com.renobile.carrinho.util.sendList
import com.renobile.carrinho.util.shareApp
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListFragment : Fragment() {

    private val viewModel: ListViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        val actions = ListActions(
            onSearchChanged = { viewModel.onSearchTermsChanged(it) },
            onCreateList = { viewModel.createList(it) },
            onAddOrEditProduct = { viewModel.addOrEditProduct(it) },
            onDeleteProduct = { viewModel.deleteProduct(it) },
            onChangeQuantity = { product, delta -> viewModel.changeQuantity(product, delta) },
            onClearList = { viewModel.clearList() },
            onOpenHistory = {
                findNavControllerSafely()?.navigate(R.id.listsHistoryFragment)
            },
            onSendList = {
                val state = viewModel.uiState.value
                activity?.sendList(state.products, state.list?.name ?: "")
            },
            onShareApp = { activity?.shareApp() },
            onMoveToCart = { product, quantity, price ->
                viewModel.moveToCart(product, quantity, price)
            }
        )

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is ListEvents.ShowSnackbar -> view?.longSnackbar(event.messageResId)
                }
            }
        }

        ListScreen(
            viewModel = viewModel,
            actions = actions,
        )
    }
}
