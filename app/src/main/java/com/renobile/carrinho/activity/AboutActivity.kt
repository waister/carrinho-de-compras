package com.renobile.carrinho.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.renobile.carrinho.BuildConfig
import com.renobile.carrinho.R
import com.renobile.carrinho.databinding.ActivityAboutBinding
import org.jetbrains.anko.intentFor

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
                startActivity(intentFor<RemoveAdsActivity>())
            }

            tvVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)
        }
    }

}
