package com.renobile.carrinho.features.list.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.R
import com.renobile.carrinho.util.PARAM_LIST_ID
import com.renobile.carrinho.util.findNavControllerSafely
import com.renobile.carrinho.util.longSnackbar
import com.renobile.carrinho.util.sendList
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListDetailsFragment : Fragment() {

    private val viewModel: ListDetailsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        val listId = arguments?.getLong(PARAM_LIST_ID, 0) ?: 0
        viewModel.init(listId)

        val actions = ListDetailsActions(
            onBack = { findNavControllerSafely()?.popBackStack() },
            onDeleteList = { viewModel.deleteList(listId) },
            onShareList = {
                val state = viewModel.uiState.value
                activity?.sendList(state.products, state.list?.name ?: "")
            },
            onMoveToCart = { /* Handled inside Screen/ViewModel */ }
        )

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is ListDetailsEvents.ShowSnackbar -> view?.longSnackbar(event.messageResId)
                    is ListDetailsEvents.ListDeleted -> {
                        view?.longSnackbar(R.string.list_deleted)
                        findNavControllerSafely()?.popBackStack()
                    }
                }
            }
        }

        ListDetailsScreen(
            viewModel = viewModel,
            actions = actions
        )
    }
}
