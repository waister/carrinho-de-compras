package com.renobile.carrinho.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.renobile.carrinho.R
import com.renobile.carrinho.activity.AboutActivity
import com.renobile.carrinho.activity.NotificationsActivity
import com.renobile.carrinho.activity.RemoveAdsActivity
import com.renobile.carrinho.util.shareApp
import com.renobile.carrinho.util.storeAppLink
import org.jetbrains.anko.browse
import org.jetbrains.anko.find
import org.jetbrains.anko.intentFor

class MoreFragment : Fragment() {

    private lateinit var llMain: LinearLayout

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_more, container, false)

        llMain = root.find(R.id.ll_main)

        addButton(R.string.notifications, R.drawable.ic_bell)
        addButton(R.string.about_app, R.drawable.ic_information_outline)
        addButton(R.string.share_app, R.drawable.ic_share_variant)
        addButton(R.string.rate_app, R.drawable.ic_star)
        addButton(R.string.remove_ads, R.drawable.ic_crown)

        return root
    }

    private fun addButton(title: Int, icon: Int) {
        @SuppressLint("InflateParams")
        val buttonView = layoutInflater.inflate(R.layout.item_button_menu, null) as TextView

        buttonView.setText(title)

        val drawable = ContextCompat.getDrawable(requireActivity(), icon)
        buttonView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

        buttonView.setOnClickListener {
            when (title) {
                R.string.notifications -> startActivity(activity?.intentFor<NotificationsActivity>())
                R.string.about_app -> startActivity(activity?.intentFor<AboutActivity>())
                R.string.share_app -> activity.shareApp()
                R.string.rate_app -> activity?.browse(storeAppLink())
                R.string.remove_ads -> startActivity(activity?.intentFor<RemoveAdsActivity>())
            }
        }

        llMain.addView(buttonView)
    }

}
