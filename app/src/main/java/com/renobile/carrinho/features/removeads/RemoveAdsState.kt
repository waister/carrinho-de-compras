package com.renobile.carrinho.features.removeads

data class RemoveAdsState(
    val isLoading: Boolean = false,
    val isAdReady: Boolean = false,
    val haveVideoPlan: Boolean = false,
    val description: String = "",
)

sealed interface RemoveAdsEvents {
    data object ShowRestartAlert : RemoveAdsEvents
    data class ShowError(val messageResId: Int) : RemoveAdsEvents
}
