package com.renobile.carrinho.features.list.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.database.entities.PurchaseListEntity
import com.renobile.carrinho.util.findNavControllerSafely
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListsHistoryFragment : Fragment() {

    private val viewModel: ListsHistoryViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        ListsHistoryScreen(
            viewModel = viewModel,
            onBackClick = ::onBackClick,
            onListClick = ::onListClick,
        )
    }

    private fun onBackClick() {
        findNavControllerSafely()?.popBackStack()
    }

    private fun onListClick(list: PurchaseListEntity) {
        findNavControllerSafely()?.navigate("listDetails/${list.id}")
    }
}
