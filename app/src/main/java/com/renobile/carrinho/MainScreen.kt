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
import com.renobile.carrinho.util.havePlan
import com.renobile.carrinho.util.loadBannerAd
import com.renobile.carrinho.util.storeAppLink

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
        Box(
            modifier = Modifier.padding(
                bottom = paddingValues.calculateBottomPadding()
            )
        ) {
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
