package com.renobile.carrinho.features.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.R
import com.renobile.carrinho.util.PARAM_ITEM_ID
import com.renobile.carrinho.util.findNavControllerSafely
import org.koin.androidx.viewmodel.ext.android.viewModel

class NotificationsFragment : Fragment() {

    private val viewModel: NotificationsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        NotificationsScreen(
            viewModel = viewModel,
            onBack = { findNavControllerSafely()?.popBackStack() },
            onNotificationClick = { itemId ->
                val bundle = Bundle().apply {
                    putString(PARAM_ITEM_ID, itemId)
                }
                findNavControllerSafely()?.navigate(R.id.notificationDetailsFragment, bundle)
            }
        )
    }
}
