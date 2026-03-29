package com.renobile.carrinho.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.renobile.carrinho.R
import com.renobile.carrinho.activity.AboutActivity
import com.renobile.carrinho.activity.NotificationsActivity
import com.renobile.carrinho.activity.RemoveAdsActivity
import com.renobile.carrinho.databinding.FragmentMoreBinding
import com.renobile.carrinho.databinding.ItemButtonMenuBinding
import com.renobile.carrinho.util.shareApp
import com.renobile.carrinho.util.storeAppLink

class MoreFragment : Fragment() {

    private var _binding: FragmentMoreBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreBinding.inflate(inflater, container, false)

        addButton(R.string.notifications, R.drawable.ic_bell)
        addButton(R.string.about_app, R.drawable.ic_information_outline)
        addButton(R.string.share_app, R.drawable.ic_share_variant)
        addButton(R.string.rate_app, R.drawable.ic_star)
        addButton(R.string.remove_ads, R.drawable.ic_crown)

        return binding.root
    }

    private fun addButton(title: Int, icon: Int) {
        val button = ItemButtonMenuBinding.inflate(layoutInflater).root.apply {
            setText(title)
            setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(requireContext(), icon), null, null, null
            )
            setOnClickListener {
                when (title) {
                    R.string.notifications -> {
                        val intent = Intent(requireContext(), NotificationsActivity::class.java)
                        startActivity(intent)
                    }
                    R.string.about_app -> {
                        val intent = Intent(requireContext(), AboutActivity::class.java)
                        startActivity(intent)
                    }
                    R.string.share_app -> activity?.shareApp()
                    R.string.rate_app -> {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(storeAppLink()))
                        startActivity(intent)
                    }
                    R.string.remove_ads -> {
                        val intent = Intent(requireContext(), RemoveAdsActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }

        binding.llMain.addView(button)
    }

}
