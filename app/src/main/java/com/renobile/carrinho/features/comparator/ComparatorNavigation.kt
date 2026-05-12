package com.renobile.carrinho.features.comparator

import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.renobile.carrinho.MainViewModel
import com.renobile.carrinho.util.shareApp
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.comparatorScreen(mainViewModel: MainViewModel) {
    composable("comparator") {
        val activity = LocalActivity.current as? AppCompatActivity
        mainViewModel.setBottomBarVisible(true)
        val viewModel: ComparatorViewModel = koinViewModel()
        ComparatorScreen(
            viewModel = viewModel,
            onShare = { activity?.shareApp() }
        )
    }
}
