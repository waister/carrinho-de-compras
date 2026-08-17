package com.renobile.carrinho.features.removeads

data class RemoveAdsState(
    val isLoading: Boolean = false,
    val isAdReady: Boolean = false,
    val haveVideoPlan: Boolean = false,
    val description: String = "",
    val showRestartDialog: Boolean = false
)

data class RemoveAdsActions(
    val onWatchClick: () -> Unit = {},
    val onBack: () -> Unit = {},
    val onRestart: () -> Unit = {},
    val onDismissRestart: () -> Unit = {}
)

sealed interface RemoveAdsEvents {
    data class ShowError(val messageResId: Int) : RemoveAdsEvents
}
