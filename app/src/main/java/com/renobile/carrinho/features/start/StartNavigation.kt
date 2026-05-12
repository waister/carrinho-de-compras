package com.renobile.carrinho.features.start

import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.renobile.carrinho.MainViewModel
import com.renobile.carrinho.util.API_ABOUT_APP
import com.renobile.carrinho.util.API_COMPARATOR
import com.renobile.carrinho.util.API_LIST
import com.renobile.carrinho.util.API_NOTIFICATIONS
import com.renobile.carrinho.util.PARAM_ITEM_ID
import com.renobile.carrinho.util.PARAM_TYPE
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.startScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    composable("start") {
        val activity = LocalActivity.current as? AppCompatActivity
        val viewModel: StartViewModel = koinViewModel()
        LaunchedEffect(Unit) {
            mainViewModel.setBottomBarVisible(false)
            launch {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        is StartEvents.NavigateToMain -> {
                            val type = activity?.intent?.getStringExtra(PARAM_TYPE)
                            val itemId = activity?.intent?.getStringExtra(PARAM_ITEM_ID)

                            val route = when (type) {
                                API_NOTIFICATIONS -> {
                                    if (itemId.isNullOrEmpty())
                                        "notifications"
                                    else
                                        "notificationDetails/$itemId"
                                }

                                API_COMPARATOR -> "comparator"
                                API_LIST -> "list"
                                API_ABOUT_APP -> "about"
                                else -> "cart"
                            }
                            navController.navigate(route) {
                                popUpTo("start") { inclusive = true }
                            }
                        }
                    }
                }
            }
            viewModel.start()
        }
        StartScreen()
    }
}
