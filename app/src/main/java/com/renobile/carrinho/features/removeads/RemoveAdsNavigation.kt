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
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.removeAdsScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    composable("removeAds") {
        val activity = LocalActivity.current as? AppCompatActivity
        mainViewModel.setBottomBarVisible(true)
        val viewModel: RemoveAdsViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.loadAd()
        }

        RemoveAdsScreen(
            isLoading = state.isLoading,
            isAdReady = state.isAdReady,
            haveVideoPlan = state.haveVideoPlan,
            description = state.description,
            onWatchClick = { activity?.let { viewModel.showAd(it) } },
            onBack = { navController.popBackStack() }
        )
    }
}
