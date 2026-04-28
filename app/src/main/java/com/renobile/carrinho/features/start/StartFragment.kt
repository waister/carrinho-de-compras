package com.renobile.carrinho.features.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import androidx.navigation.navOptions
import com.renobile.carrinho.R
import com.renobile.carrinho.ui.theme.MyAppTheme
import com.renobile.carrinho.util.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StartFragment : Fragment() {

    private val viewModel: StartViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        MyAppTheme {
            LaunchedEffect(Unit) {
                launch {
                    viewModel.events.collectLatest { event ->
                        when (event) {
                            is StartEvents.NavigateToMain -> initApp()
                        }
                    }
                }
                viewModel.start()
            }
            StartScreen()
        }
    }

    private fun initApp() {
        val type = activity?.intent?.getStringExtra(PARAM_TYPE)
        val itemId = activity?.intent?.getStringExtra(PARAM_ITEM_ID)
        appLog(TAG, "Received type from notification: $type")
        appLog(TAG, "Received itemId from notification: $itemId")

        val bundle = Bundle()

        val route = when (type) {
            API_NOTIFICATIONS -> {
                if (itemId.isNullOrEmpty()) {
                    R.id.notificationsFragment
                } else {
                    bundle.apply {
                        putString(PARAM_ITEM_ID, itemId)
                    }
                    R.id.notificationDetailsFragment
                }
            }
            API_ABOUT_APP -> R.id.aboutFragment
            else -> R.id.cartFragment
        }

        val options = navOptions {
            popUpTo(R.id.startFragment) { inclusive = true }
        }
        findNavControllerSafely()?.navigate(route, bundle, options)
    }

    companion object {
        const val TAG = "StartFragment"
        const val IDENTIFIER_VERSION: String = "-v3"
    }
}
