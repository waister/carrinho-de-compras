package com.renobile.carrinho

import android.content.Intent
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.tooling.preview.Preview
import com.renobile.carrinho.ui.theme.MyAppTheme
import androidx.compose.ui.platform.LocalInspectionMode
import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.features.cart.CartActions
import com.renobile.carrinho.features.cart.CartScreen
import com.renobile.carrinho.features.cart.CartState
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.renobile.carrinho.features.about.aboutScreen
import com.renobile.carrinho.features.cart.cartGraph
import com.renobile.carrinho.features.comparator.comparatorScreen
import com.renobile.carrinho.features.list.listGraph
import com.renobile.carrinho.features.more.moreScreen
import com.renobile.carrinho.features.notification.notificationGraph
import com.renobile.carrinho.features.removeads.removeAdsScreen
import com.renobile.carrinho.features.start.startScreen
import com.renobile.carrinho.util.PREF_ADMOB_AD_MAIN_ID
import com.renobile.carrinho.util.Prefs
import com.renobile.carrinho.util.loadBannerAd
import com.renobile.carrinho.util.storeAppLink

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onShowInterstitialAd: () -> Unit,
) {
    val navController = rememberNavController()
    val uiState by mainViewModel.uiState.collectAsState()

    MainScreen(
        uiState = uiState,
        navController = navController,
        onUpdatePlanStatus = { mainViewModel.updatePlanStatus() },
        onVersionUpdateHandled = { mainViewModel.onVersionUpdateHandled() }
    ) {
        MainNavHost(navController, mainViewModel, onShowInterstitialAd)
    }
}

@Composable
internal fun MainScreen(
    uiState: MainState,
    navController: NavHostController,
    onUpdatePlanStatus: () -> Unit,
    onVersionUpdateHandled: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onUpdatePlanStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
            builder.setOnDismissListener { onVersionUpdateHandled() }
            builder.show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            AnimatedVisibility(
                visible = uiState.isBottomBarVisible && uiState.areBarsVisible,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Column(
                        modifier = Modifier.windowInsetsPadding(
                            WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                        )
                    ) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = Color.White.copy(alpha = 0.2f)
                        )
                        val isPreview = LocalInspectionMode.current
                        AdBanner(
                            adUnitId = if (isPreview) "" else Prefs.getValue(PREF_ADMOB_AD_MAIN_ID, ""),
                            havePlan = uiState.havePlan
                        )
                        MainBottomNavigation(navController)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            content(paddingValues)
        }
    }
}

@Composable
fun AdBanner(
    adUnitId: String,
    havePlan: Boolean
) {
    if (havePlan || adUnitId.isEmpty()) {
        return
    }

    var isAdVisible by remember { mutableStateOf(true) }

    if (!isAdVisible) return

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
                    shimmer = null,
                    onAdLoaded = { success ->
                        isAdVisible = success
                    }
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

    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = Color.White,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(painterResource(item.iconRes), contentDescription = null) },
                label = { Text(stringResource(item.labelRes)) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.7f),
                    unselectedTextColor = Color.White.copy(alpha = 0.7f),
                    indicatorColor = Color.Transparent
                ),
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
    NavHost(navController, startDestination = "start") {
        startScreen(navController, mainViewModel)
        cartGraph(navController, mainViewModel, onShowInterstitialAd)
        listGraph(navController, mainViewModel)
        comparatorScreen(mainViewModel)
        removeAdsScreen(navController, mainViewModel)
        moreScreen(navController, mainViewModel)
        notificationGraph(navController, mainViewModel)
        aboutScreen(navController, mainViewModel)
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val dummyState = CartState(
        cart = CartEntity(
            id = 1,
            name = "Compras Semanal",
            dateOpen = System.currentTimeMillis(),
            dateClose = 0L,
            products = 2,
            units = 5.0,
            valueTotal = 50.0,
            keywords = "",
        ),
        products = listOf(
            ProductEntity(1, 1, 0, "Arroz", 2.0, 15.0),
            ProductEntity(2, 1, 0, "Feijão", 3.0, 10.0),
        ),
    )

    MyAppTheme {
        MainScreen(
            uiState = MainState(
                isBottomBarVisible = true,
                areBarsVisible = true,
                havePlan = false
            ),
            navController = rememberNavController(),
            onUpdatePlanStatus = {},
            onVersionUpdateHandled = {},
            content = {
                CartScreen(
                    state = dummyState,
                    actions = CartActions()
                )
            }
        )
    }
}
