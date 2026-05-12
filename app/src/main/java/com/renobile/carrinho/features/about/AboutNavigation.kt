package com.renobile.carrinho.features.about

import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.renobile.carrinho.MainViewModel
import com.renobile.carrinho.util.PREF_HAVE_PLAN
import com.renobile.carrinho.util.Prefs
import com.renobile.carrinho.util.restartApp

fun NavGraphBuilder.aboutScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    composable("about") {
        val activity = LocalActivity.current as? AppCompatActivity
        mainViewModel.setBottomBarVisible(false)
        AboutScreen(
            onBack = { navController.popBackStack() },
            onRemoveAds = { navController.navigate("removeAds") },
            onSecretAction = {
                Prefs.putValue(PREF_HAVE_PLAN, true)
                activity?.restartApp()
            }
        )
    }
}
