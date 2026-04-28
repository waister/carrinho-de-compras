package com.renobile.carrinho

import android.content.Intent
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.renobile.carrinho.features.about.AboutScreen
import com.renobile.carrinho.features.cart.CartActions
import com.renobile.carrinho.features.cart.CartEvents
import com.renobile.carrinho.features.cart.CartScreen
import com.renobile.carrinho.features.cart.CartViewModel
import com.renobile.carrinho.features.cart.detail.CartDetailsActions
import com.renobile.carrinho.features.cart.detail.CartDetailsEvents
import com.renobile.carrinho.features.cart.detail.CartDetailsScreen
import com.renobile.carrinho.features.cart.detail.CartDetailsViewModel
import com.renobile.carrinho.features.cart.history.CartsHistoryScreen
import com.renobile.carrinho.features.cart.history.CartsHistoryViewModel
import com.renobile.carrinho.features.comparator.ComparatorScreen
import com.renobile.carrinho.features.comparator.ComparatorViewModel
import com.renobile.carrinho.features.list.ListActions
import com.renobile.carrinho.features.list.ListScreen
import com.renobile.carrinho.features.list.ListViewModel
import com.renobile.carrinho.features.list.detail.ListDetailsActions
import com.renobile.carrinho.features.list.detail.ListDetailsEvents
import com.renobile.carrinho.features.list.detail.ListDetailsScreen
import com.renobile.carrinho.features.list.detail.ListDetailsViewModel
import com.renobile.carrinho.features.list.history.ListsHistoryScreen
import com.renobile.carrinho.features.list.history.ListsHistoryViewModel
import com.renobile.carrinho.features.more.MoreScreen
import com.renobile.carrinho.features.notification.NotificationsScreen
import com.renobile.carrinho.features.notification.NotificationsViewModel
import com.renobile.carrinho.features.notification.detail.NotificationDetailsScreen
import com.renobile.carrinho.features.notification.detail.NotificationDetailsViewModel
import com.renobile.carrinho.features.removeads.RemoveAdsScreen
import com.renobile.carrinho.features.removeads.RemoveAdsViewModel
import com.renobile.carrinho.features.start.StartEvents
import com.renobile.carrinho.features.start.StartScreen
import com.renobile.carrinho.features.start.StartViewModel
import com.renobile.carrinho.util.API_ABOUT_APP
import com.renobile.carrinho.util.API_NOTIFICATIONS
import com.renobile.carrinho.util.PARAM_ITEM_ID
import com.renobile.carrinho.util.PARAM_TYPE
import com.renobile.carrinho.util.PREF_ADMOB_AD_MAIN_ID
import com.renobile.carrinho.util.PREF_HAVE_PLAN
import com.renobile.carrinho.util.Prefs
import com.renobile.carrinho.util.havePlan
import com.renobile.carrinho.util.loadBannerAd
import com.renobile.carrinho.util.restartApp
import com.renobile.carrinho.util.sendCart
import com.renobile.carrinho.util.sendList
import com.renobile.carrinho.util.shareApp
import com.renobile.carrinho.util.storeAppLink
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onShowInterstitialAd: () -> Unit,
) {
    val navController = rememberNavController()
    val uiState by mainViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.versionUpdate) {
        uiState.versionUpdate?.let { update ->
            val builder = AlertDialog.Builder(context)
                .setTitle(R.string.updated_title)
                .setPositiveButton(R.string.updated_positive) { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, storeAppLink().toUri())
                    context.startActivity(intent)
                }

            when (update) {
                VersionUpdate.Needed -> {
                    builder.setMessage(R.string.update_needed)
                        .setNegativeButton(R.string.updated_logout) { _, _ -> (context as? AppCompatActivity)?.finish() }
                        .setOnCancelListener { (context as? AppCompatActivity)?.finish() }
                }

                VersionUpdate.Available -> {
                    builder.setMessage(R.string.update_available)
                        .setNegativeButton(R.string.updated_negative, null)
                }
            }
            builder.setOnDismissListener { mainViewModel.onVersionUpdateHandled() }
            builder.show()
        }
    }

    Scaffold(
        bottomBar = {
            if (uiState.isBottomBarVisible) {
                Column {
                    AdBanner(Prefs.getValue(PREF_ADMOB_AD_MAIN_ID, ""))
                    MainBottomNavigation(navController)
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            MainNavHost(navController, mainViewModel, onShowInterstitialAd)
        }
    }
}

@Composable
fun AdBanner(adUnitId: String) {
    if (havePlan() || adUnitId.isEmpty()) return

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                context.loadBannerAd(
                    adViewContainer = this,
                    adUnitId = adUnitId,
                    adSize = null,
                    collapsible = false,
                    shimmer = null
                )
            }
        }
    )
}

@Composable
fun MainBottomNavigation(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("cart", R.drawable.ic_cart_outline, R.string.cart),
        BottomNavItem("list", R.drawable.ic_format_list_checks, R.string.list),
        BottomNavItem("comparator", R.drawable.ic_select_compare, R.string.compare),
        BottomNavItem("removeAds", R.drawable.ic_crown, R.string.premium),
        BottomNavItem("more", R.drawable.ic_dots_horizontal, R.string.more)
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(painterResource(item.iconRes), contentDescription = null) },
                label = { Text(stringResource(item.labelRes)) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun MainNavHost(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    onShowInterstitialAd: () -> Unit,
) {
    val activity = LocalContext.current as? AppCompatActivity

    NavHost(navController, startDestination = "start") {
        composable("start") {
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
                                        if (itemId.isNullOrEmpty()) "notifications"
                                        else "notificationDetails/$itemId"
                                    }

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

        composable("cart") {
            mainViewModel.setBottomBarVisible(true)
            val viewModel: CartViewModel = koinViewModel()
            val actions = CartActions(
                onSearchChanged = { viewModel.onSearchTermsChanged(it) },
                onCreateCart = { viewModel.createCart(it) },
                onAddOrEditProduct = { viewModel.addOrEditProduct(it) },
                onDeleteProduct = { viewModel.deleteProduct(it) },
                onChangeQuantity = { product, delta -> viewModel.changeQuantity(product, delta) },
                onSendCart = {
                    val state = viewModel.uiState.value
                    activity?.sendCart(state.products, state.cart?.name ?: "")
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

            CartScreen(viewModel = viewModel, actions = actions)
        }

        composable("list") {
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
                onShareApp = { activity?.shareApp() }
            )
            ListScreen(viewModel = viewModel, actions = actions)
        }

        composable("comparator") {
            mainViewModel.setBottomBarVisible(true)
            val viewModel: ComparatorViewModel = koinViewModel()
            ComparatorScreen(
                viewModel = viewModel,
                onShare = { activity?.shareApp() }
            )
        }

        composable("removeAds") {
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

        composable("more") {
            mainViewModel.setBottomBarVisible(true)
            MoreScreen(
                onNotifications = { navController.navigate("notifications") },
                onAbout = { navController.navigate("about") },
                onShare = { activity?.shareApp() },
                onRemoveAds = { navController.navigate("removeAds") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("cartsHistory") {
            mainViewModel.setBottomBarVisible(false)
            val viewModel: CartsHistoryViewModel = koinViewModel()
            CartsHistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
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

        composable("listsHistory") {
            mainViewModel.setBottomBarVisible(false)
            val viewModel: ListsHistoryViewModel = koinViewModel()
            ListsHistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onListClick = { list -> navController.navigate("listDetails/${list.id}") }
            )
        }

        composable(
            route = "listDetails/{listId}",
            arguments = listOf(navArgument("listId") { type = NavType.LongType })
        ) { backStackEntry ->
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

        composable("notifications") {
            mainViewModel.setBottomBarVisible(false)
            val viewModel: NotificationsViewModel = koinViewModel()
            NotificationsScreen(
                viewModel = viewModel,
                onNotificationClick = { notificationId ->
                    navController.navigate("notificationDetails/${notificationId}")
                },
                onBack = { navController.popBackStack() }
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

        composable("about") {
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
}
