package com.renobile.carrinho.features.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.database.entities.ProductEntity
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

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is ListEvents.ShowSnackbar -> showSnackbar(event.messageResId)
                }
            }
        }

        ListScreen(
            viewModel = viewModel,
            actions = ListActions(
                onSearchChanged = ::onSearchChanged,
                onCreateList = ::onCreateList,
                onAddOrEditProduct = ::onAddOrEditProduct,
                onDeleteProduct = ::onDeleteProduct,
                onChangeQuantity = ::onChangeQuantity,
                onClearList = ::onClearList,
                onOpenHistory = ::onOpenHistory,
                onSendList = ::onSendList,
                onShareApp = ::onShareApp,
                onMoveToCart = ::onMoveToCart,
            ),
        )
    }

    private fun showSnackbar(@StringRes message: Int) {
        view?.longSnackbar(message)
    }

    private fun onSearchChanged(query: String) {
        viewModel.onSearchTermsChanged(query)
    }

    private fun onCreateList(it: String) {
        viewModel.createList(it)
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

    private fun onSendList() {
        val state = viewModel.uiState.value
        activity?.sendList(state.products, state.list?.name ?: "")
    }

    private fun onClearList() {
        viewModel.clearList()
    }

    private fun onOpenHistory() {
        findNavControllerSafely()?.navigate("listsHistory")
    }

    private fun onShareApp() {
        activity?.shareApp()
    }

    private fun onMoveToCart(product: ProductEntity, quantity: Double, price: Double) {
        viewModel.moveToCart(product, quantity, price)
    }
}
