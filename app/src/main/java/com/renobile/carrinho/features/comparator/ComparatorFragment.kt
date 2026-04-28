package com.renobile.carrinho.features.comparator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.util.shareApp
import org.koin.androidx.viewmodel.ext.android.viewModel

class ComparatorFragment : Fragment() {

    private val viewModel: ComparatorViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        ComparatorScreen(
            viewModel = viewModel,
            onShare = { activity?.shareApp() }
        )
    }

}
