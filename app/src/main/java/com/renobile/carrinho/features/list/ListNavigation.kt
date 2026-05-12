package com.renobile.carrinho.features.list

import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.renobile.carrinho.MainViewModel
import com.renobile.carrinho.features.list.detail.ListDetailsActions
import com.renobile.carrinho.features.list.detail.ListDetailsEvents
import com.renobile.carrinho.features.list.detail.ListDetailsScreen
import com.renobile.carrinho.features.list.detail.ListDetailsViewModel
import com.renobile.carrinho.features.list.history.ListsHistoryScreen
import com.renobile.carrinho.features.list.history.ListsHistoryViewModel
import com.renobile.carrinho.util.sendList
import com.renobile.carrinho.util.shareApp
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.listGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    composable("list") {
        val activity = LocalActivity.current as? AppCompatActivity
        mainViewModel.setBottomBarVisible(true)
        val viewModel: ListViewModel = koinViewModel()
        val actions = ListActions(
            onSearchChanged = { viewModel.onSearchTermsChanged(it) },
            onCreateList = { viewModel.createList(it) },
            onAddOrEditProduct = { viewModel.addOrEditProduct(it) },
            onDeleteProduct = { viewModel.deleteProduct(it) },
            onChangeQuantity = { product, delta -> viewModel.changeQuantity(product, delta) },
            onClearList = { viewModel.clearList() },
            onOpenHistory = { navController.navigate("listsHistory") },
            onSendList = {
                val state = viewModel.uiState.value
                activity?.sendList(state.products, state.list?.name ?: "")
            },
            onShareApp = { activity?.shareApp() },
            onMoveToCart = { product, quantity, price -> viewModel.moveToCart(product, quantity, price) }
        )
        ListScreen(viewModel = viewModel, actions = actions)
    }

    composable("listsHistory") {
        mainViewModel.setBottomBarVisible(false)
        val viewModel: ListsHistoryViewModel = koinViewModel()
        ListsHistoryScreen(
            viewModel = viewModel,
            onBackClick = { navController.popBackStack() },
            onListClick = { list -> navController.navigate("listDetails/${list.id}") }
        )
    }

    composable(
        route = "listDetails/{listId}",
        arguments = listOf(navArgument("listId") { type = NavType.LongType })
    ) { backStackEntry ->
        val activity = LocalActivity.current as? AppCompatActivity
        mainViewModel.setBottomBarVisible(false)
        val listId = backStackEntry.arguments?.getLong("listId") ?: 0L
        val viewModel: ListDetailsViewModel = koinViewModel()

        LaunchedEffect(listId) {
            viewModel.init(listId)
        }

        val actions = ListDetailsActions(
            onBack = { navController.popBackStack() },
            onDeleteList = { viewModel.deleteList(listId) },
            onShareList = {
                val state = viewModel.uiState.value
                activity?.sendList(state.products, state.list?.name ?: "")
            },
            onMoveToCart = { /* Handled in Screen */ }
        )

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is ListDetailsEvents.ListDeleted -> navController.popBackStack()
                    else -> {}
                }
            }
        }

        ListDetailsScreen(viewModel = viewModel, actions = actions)
    }
}
