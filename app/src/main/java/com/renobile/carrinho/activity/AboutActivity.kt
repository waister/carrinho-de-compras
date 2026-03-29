package com.renobile.carrinho.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.appbar.AppBarLayout
import com.renobile.carrinho.BuildConfig
import com.renobile.carrinho.R
import com.renobile.carrinho.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
            toolbar.setNavigationOnClickListener {
                finish()
            }

            btRemoveAds.setOnClickListener {
                val intent = Intent(this@AboutActivity, RemoveAdsActivity::class.java)
                startActivity(intent)
            }

            tvVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)
        }

        setupInsets()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.clRoot) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val appBar = binding.root.findViewById<AppBarLayout>(R.id.app_bar)
            appBar?.updatePadding(top = systemBars.top)
            view.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }
    }

}
