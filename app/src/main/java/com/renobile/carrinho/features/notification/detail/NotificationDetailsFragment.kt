package com.renobile.carrinho.features.notification.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.util.PARAM_ITEM_ID
import com.renobile.carrinho.util.findNavControllerSafely
import org.koin.androidx.viewmodel.ext.android.viewModel

class NotificationDetailsFragment : Fragment() {

    private val viewModel: NotificationDetailsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        val itemId = arguments?.getString(PARAM_ITEM_ID) ?: ""
        viewModel.init(itemId)

        NotificationDetailsScreen(
            viewModel = viewModel,
            onBack = { findNavControllerSafely()?.popBackStack() }
        )
    }
}
