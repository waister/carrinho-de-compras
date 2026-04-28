package com.renobile.carrinho

data class MainState(
    val versionUpdate: VersionUpdate? = null,
    val isBottomBarVisible: Boolean = true,
)

sealed class VersionUpdate {
    data object Needed : VersionUpdate()
    data object Available : VersionUpdate()
}

data class BottomNavItem(
    val route: String,
    val iconRes: Int,
    val labelRes: Int,
)
