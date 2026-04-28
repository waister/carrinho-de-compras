package com.renobile.carrinho.features.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.R
import com.renobile.carrinho.util.findNavControllerSafely
import com.renobile.carrinho.util.shareApp

class MoreFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        MoreScreen(
            onNotifications = { findNavControllerSafely()?.navigate(R.id.notificationsFragment) },
            onAbout = { findNavControllerSafely()?.navigate(R.id.aboutFragment) },
            onShare = { activity?.shareApp() },
            onRemoveAds = { findNavControllerSafely()?.navigate(R.id.removeAdsFragment) },
            onBack = { findNavControllerSafely()?.popBackStack() }
        )
    }

}
