package com.renobile.carrinho.features.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.util.findNavControllerSafely
import org.koin.androidx.viewmodel.ext.android.viewModel

class NotificationsFragment : Fragment() {

    private val viewModel: NotificationsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        NotificationsScreen(
            viewModel = viewModel,
            onBackClick = ::onBackClick,
            onNotificationClick = ::onNotificationClick,
        )
    }

    private fun onBackClick() {
        findNavControllerSafely()?.popBackStack()
    }

    private fun onNotificationClick(itemId: String) {
        findNavControllerSafely()?.navigate("notificationDetails/$itemId")
    }
}
