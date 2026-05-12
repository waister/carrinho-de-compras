package com.renobile.carrinho.features.notification

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.renobile.carrinho.MainViewModel
import com.renobile.carrinho.features.notification.detail.NotificationDetailsScreen
import com.renobile.carrinho.features.notification.detail.NotificationDetailsViewModel
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.notificationGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    composable("notifications") {
        mainViewModel.setBottomBarVisible(false)
        val viewModel: NotificationsViewModel = koinViewModel()
        NotificationsScreen(
            viewModel = viewModel,
            onNotificationClick = { notificationId ->
                navController.navigate("notificationDetails/${notificationId}")
            },
            onBackClick = { navController.popBackStack() }
        )
    }

    composable(
        route = "notificationDetails/{itemId}",
        arguments = listOf(navArgument("itemId") { type = NavType.StringType })
    ) { backStackEntry ->
        mainViewModel.setBottomBarVisible(false)
        val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
        val viewModel: NotificationDetailsViewModel = koinViewModel()

        LaunchedEffect(itemId) {
            viewModel.init(itemId)
        }

        NotificationDetailsScreen(
            viewModel = viewModel,
            onBack = { navController.popBackStack() }
        )
    }
}
