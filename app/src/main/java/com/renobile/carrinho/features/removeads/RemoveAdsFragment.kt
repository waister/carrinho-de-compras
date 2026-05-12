package com.renobile.carrinho.features.removeads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.R
import com.renobile.carrinho.util.PARAM_SHOW_BACK
import com.renobile.carrinho.util.findNavControllerSafely
import com.renobile.carrinho.util.restartApp
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemoveAdsFragment : Fragment() {

    private val viewModel: RemoveAdsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        val state by viewModel.uiState.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.loadAd()
        }

        LaunchedEffect(Unit) {
            viewModel.events.collect { event ->
                when (event) {
                    RemoveAdsEvents.ShowRestartAlert -> alertRestartApp()
                    is RemoveAdsEvents.ShowError -> alertErrorLoad(event.messageResId)
                }
            }
        }

        RemoveAdsScreen(
            isLoading = state.isLoading,
            isAdReady = state.isAdReady,
            haveVideoPlan = state.haveVideoPlan,
            description = state.description,
            onWatchClick = { viewModel.showAd(requireActivity()) },
            onBack = {
                if (arguments?.getBoolean(PARAM_SHOW_BACK) == true) {
                    activity?.finish()
                } else {
                    findNavControllerSafely()?.popBackStack()
                }
            }
        )
    }

    private fun alertRestartApp() {
        if (!isAdded) return
        AlertDialog.Builder(requireActivity())
            .setCancelable(false)
            .setTitle(R.string.plan_success_title)
            .setMessage(R.string.plan_success_body)
            .setPositiveButton(R.string.restart_app) { _, _ ->
                activity?.restartApp()
            }
            .show()
    }

    private fun alertErrorLoad(messageResId: Int) {
        if (!isAdded) return
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.ops)
            .setMessage(messageResId)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}
