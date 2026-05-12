package com.renobile.carrinho.features.cart

import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.renobile.carrinho.MainViewModel
import com.renobile.carrinho.features.cart.detail.CartDetailsActions
import com.renobile.carrinho.features.cart.detail.CartDetailsEvents
import com.renobile.carrinho.features.cart.detail.CartDetailsScreen
import com.renobile.carrinho.features.cart.detail.CartDetailsViewModel
import com.renobile.carrinho.features.cart.history.CartsHistoryScreen
import com.renobile.carrinho.features.cart.history.CartsHistoryViewModel
import com.renobile.carrinho.util.sendCart
import com.renobile.carrinho.util.shareApp
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.cartGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    onShowInterstitialAd: () -> Unit
) {
    composable("cart") {
        val activity = LocalActivity.current as? AppCompatActivity
        mainViewModel.setBottomBarVisible(true)
        val viewModel: CartViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsState()
        val actions = CartActions(
            onSearchChanged = { viewModel.onSearchTermsChanged(it) },
            onCreateCart = { viewModel.createCart(it) },
            onAddOrEditProduct = { viewModel.addOrEditProduct(it) },
            onDeleteProduct = { viewModel.deleteProduct(it) },
            onChangeQuantity = { product, delta -> viewModel.changeQuantity(product, delta) },
            onSendCart = {
                val stateValue = viewModel.uiState.value
                activity?.sendCart(stateValue.products, stateValue.cart?.name ?: "")
            },
            onClearCart = { viewModel.clearCart() },
            onOpenHistory = { navController.navigate("cartsHistory") },
            onShareApp = { activity?.shareApp() },
            onShowInterstitialAd = onShowInterstitialAd
        )

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is CartEvents.ShowInterstitialAd -> onShowInterstitialAd()
                    else -> {}
                }
            }
        }

        CartScreen(state = state, actions = actions)
    }

    composable("cartsHistory") {
        mainViewModel.setBottomBarVisible(false)
        val viewModel: CartsHistoryViewModel = koinViewModel()
        CartsHistoryScreen(
            viewModel = viewModel,
            onBackClick = { navController.popBackStack() },
            onCartClick = { cart ->
                navController.navigate("cartDetails/${cart.id}?searchTerms=${viewModel.uiState.value.searchTerms}")
            }
        )
    }

    composable(
        route = "cartDetails/{cartId}?searchTerms={searchTerms}",
        arguments = listOf(
            navArgument("cartId") { type = NavType.LongType },
            navArgument("searchTerms") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val activity = LocalActivity.current as? AppCompatActivity
        mainViewModel.setBottomBarVisible(false)
        val cartId = backStackEntry.arguments?.getLong("cartId") ?: 0L
        val searchTerms = backStackEntry.arguments?.getString("searchTerms") ?: ""
        val viewModel: CartDetailsViewModel = koinViewModel()

        LaunchedEffect(cartId, searchTerms) {
            viewModel.init(cartId, searchTerms)
        }

        val actions = CartDetailsActions(
            onSearchChanged = { viewModel.onSearchTermsChanged(cartId, it) },
            onDeleteCart = { viewModel.deleteCart(cartId) },
            onShareCart = {
                val state = viewModel.uiState.value
                activity?.sendCart(state.products, state.cart?.name ?: "")
            },
            onBack = { navController.popBackStack() }
        )

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is CartDetailsEvents.CartDeleted -> navController.popBackStack()
                    else -> {}
                }
            }
        }

        CartDetailsScreen(viewModel = viewModel, actions = actions)
    }
}
