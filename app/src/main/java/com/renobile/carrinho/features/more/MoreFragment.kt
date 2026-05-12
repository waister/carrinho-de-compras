package com.renobile.carrinho.features.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.util.findNavControllerSafely
import com.renobile.carrinho.util.shareApp

class MoreFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        MoreScreen(
            onNotifications = ::onNotifications,
            onAbout = ::onAbout,
            onShare = ::onShare,
            onRemoveAds = ::onRemoveAds,
            onBack = ::onBack,
        )
    }


    private fun onNotifications() {
        findNavControllerSafely()?.navigate("notifications")
    }

    private fun onAbout() {
        findNavControllerSafely()?.navigate("about")
    }

    private fun onShare() {
        activity?.shareApp()
    }

    private fun onRemoveAds() {
        findNavControllerSafely()?.navigate("removeAds")
    }

    private fun onBack() {
        findNavControllerSafely()?.popBackStack()
    }
}
