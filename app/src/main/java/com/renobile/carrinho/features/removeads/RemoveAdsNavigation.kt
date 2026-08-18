package com.renobile.carrinho.features.removeads

import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.renobile.carrinho.MainViewModel
import com.renobile.carrinho.util.restartApp
import com.renobile.carrinho.util.toast
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.removeAdsScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
) {
    composable("removeAds") {
        val activity = LocalActivity.current as? AppCompatActivity
        LaunchedEffect(Unit) {
            mainViewModel.setBottomBarVisible(true)
        }
        val viewModel: RemoveAdsViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.loadAd()
        }

        LaunchedEffect(viewModel.events) {
            viewModel.events.collect { event ->
                when (event) {
                    is RemoveAdsEvents.ShowError -> {
                        activity?.toast(event.messageResId)
                    }
                    else -> {}
                }
            }
        }

        RemoveAdsScreen(
            state = state,
            actions = RemoveAdsActions(
                onWatchClick = { activity?.let { viewModel.showAd(it) } },
                onBack = { navController.popBackStack() },
                onRestart = { activity?.restartApp() },
                onDismissRestart = { viewModel.dismissRestartDialog() },
            ),
        )
    }
}
