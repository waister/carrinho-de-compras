package com.renobile.carrinho.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.renobile.carrinho.R
import com.renobile.carrinho.databinding.ActivityRemoveAdsBinding
import com.renobile.carrinho.fragments.RemoveAdsFragment
import com.renobile.carrinho.util.PARAM_FRAGMENT
import com.renobile.carrinho.util.PARAM_SHOW_BACK

class RemoveAdsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemoveAdsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRemoveAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        var fragment = supportFragmentManager.findFragmentByTag(PARAM_FRAGMENT)

        if (fragment == null) {
            fragment = RemoveAdsFragment()

            val bundle = Bundle()
            bundle.putBoolean(PARAM_SHOW_BACK, true)
            fragment.setArguments(bundle)

            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(R.id.fragment_container, fragment, PARAM_FRAGMENT)
            transaction.commitNow()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

}
