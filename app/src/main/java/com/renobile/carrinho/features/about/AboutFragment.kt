package com.renobile.carrinho.features.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.renobile.carrinho.util.PREF_HAVE_PLAN
import com.renobile.carrinho.util.Prefs
import com.renobile.carrinho.util.findNavControllerSafely
import com.renobile.carrinho.util.restartApp

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        AboutScreen(
            onBack = ::onBack,
            onRemoveAds = ::onRemoveAds,
            onSecretAction = ::onSecretAction,
        )
    }

    private fun onBack() {
        findNavControllerSafely()?.popBackStack()
    }

    private fun onRemoveAds() {
        findNavControllerSafely()?.navigate("removeAds")
    }

    private fun onSecretAction() {
        Prefs.putValue(PREF_HAVE_PLAN, true)
        requireActivity().restartApp()
    }
}
