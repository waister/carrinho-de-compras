package com.renobile.carrinho.features.more

import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.renobile.carrinho.MainViewModel
import com.renobile.carrinho.util.shareApp

fun NavGraphBuilder.moreScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    composable("more") {
        val activity = LocalActivity.current as? AppCompatActivity
        mainViewModel.setBottomBarVisible(true)
        MoreScreen(
            onNotifications = { navController.navigate("notifications") },
            onAbout = { navController.navigate("about") },
            onShare = { activity?.shareApp() },
            onRemoveAds = { navController.navigate("removeAds") },
            onBack = { navController.popBackStack() }
        )
    }
}
