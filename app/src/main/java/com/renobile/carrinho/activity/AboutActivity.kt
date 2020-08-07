package com.renobile.carrinho.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.renobile.carrinho.BuildConfig
import com.renobile.carrinho.R
import kotlinx.android.synthetic.main.activity_about.*
import org.jetbrains.anko.intentFor

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        bt_remove_ads.setOnClickListener {
            startActivity(intentFor<RemoveAdsActivity>())
            finish()
        }

        tv_version.text = getString(R.string.version, BuildConfig.VERSION_NAME)
    }

}
