package com.renobile.carrinho.features.list.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.R
import com.renobile.carrinho.util.PARAM_LIST_ID
import com.renobile.carrinho.util.findNavControllerSafely
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListsHistoryFragment : Fragment() {

    private val viewModel: ListsHistoryViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        ListsHistoryScreen(
            viewModel = viewModel,
            onBack = { findNavControllerSafely()?.popBackStack() },
            onListClick = { list ->
                val bundle = Bundle().apply {
                    putLong(PARAM_LIST_ID, list.id)
                }
                findNavControllerSafely()?.navigate(R.id.listDetailsFragment, bundle)
            }
        )
    }
}
