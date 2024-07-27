package com.renobile.carrinho.fragments

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
import org.jetbrains.anko.browse
import org.jetbrains.anko.intentFor

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
        val bindingItem = ItemButtonMenuBinding.inflate(layoutInflater).root.apply {
            setText(title)
            setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(requireActivity(), icon), null, null, null
            )
            setOnClickListener {
                when (title) {
                    R.string.notifications -> startActivity(activity?.intentFor<NotificationsActivity>())
                    R.string.about_app -> startActivity(activity?.intentFor<AboutActivity>())
                    R.string.share_app -> activity.shareApp()
                    R.string.rate_app -> activity?.browse(storeAppLink())
                    R.string.remove_ads -> startActivity(activity?.intentFor<RemoveAdsActivity>())
                }
            }
        }

        binding.llMain.addView(bindingItem)
    }

}
