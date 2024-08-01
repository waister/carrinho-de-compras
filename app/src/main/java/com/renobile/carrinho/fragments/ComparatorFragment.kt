package com.renobile.carrinho.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.R
import com.renobile.carrinho.activity.MainActivity
import com.renobile.carrinho.databinding.FragmentComparatorBinding
import com.renobile.carrinho.util.MaskMoney
import com.renobile.carrinho.util.PREF_PRICE_FIRST
import com.renobile.carrinho.util.PREF_PRICE_SECOND
import com.renobile.carrinho.util.PREF_SIZE_FIRST
import com.renobile.carrinho.util.PREF_SIZE_SECOND
import com.renobile.carrinho.util.formatPercent
import com.renobile.carrinho.util.fromHtml
import com.renobile.carrinho.util.getNumber
import com.renobile.carrinho.util.getDouble
import com.renobile.carrinho.util.hide
import com.renobile.carrinho.util.hideKeyboard
import com.renobile.carrinho.util.setEmpty
import com.renobile.carrinho.util.shareApp
import com.renobile.carrinho.util.show
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import java.util.Locale

class ComparatorFragment : Fragment(), TextWatcher {

    private var _binding: FragmentComparatorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComparatorBinding.inflate(inflater, container, false)

        initViews()

        return binding.root
    }

    private fun initViews() = with(binding) {
        etPriceFirst.addTextChangedListener(MaskMoney(etPriceFirst))
        etPriceSecond.addTextChangedListener(MaskMoney(etPriceSecond))

        etPriceFirst.addTextChangedListener(this@ComparatorFragment)
        etSizeFirst.addTextChangedListener(this@ComparatorFragment)
        etPriceSecond.addTextChangedListener(this@ComparatorFragment)
        etSizeSecond.addTextChangedListener(this@ComparatorFragment)

        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        btSubmit.setOnClickListener { submit() }

        etSizeSecond.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submit()
                return@OnEditorActionListener true
            }

            false
        })
    }

    override fun afterTextChanged(s: Editable?) = with(binding) {
        llResult.hide()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            if (etPriceFirst.text.isNullOrEmpty())
                etPriceFirst.setText(Hawk.get(PREF_PRICE_FIRST, ""))

            if (etSizeFirst.text.isNullOrEmpty())
                etSizeFirst.setText(Hawk.get(PREF_SIZE_FIRST, ""))

            if (etPriceSecond.text.isNullOrEmpty())
                etPriceSecond.setText(Hawk.get(PREF_PRICE_SECOND, ""))

            if (etSizeSecond.text.isNullOrEmpty())
                etSizeSecond.setText(Hawk.get(PREF_SIZE_SECOND, ""))
        }

        calculate(false)
    }

    override fun onDestroy() {
        super.onDestroy()

        binding.apply {
            Hawk.put(PREF_PRICE_FIRST, etPriceFirst.text.toString())
            Hawk.put(PREF_SIZE_FIRST, etSizeFirst.text.toString())
            Hawk.put(PREF_PRICE_SECOND, etPriceSecond.text.toString())
            Hawk.put(PREF_SIZE_SECOND, etSizeSecond.text.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_comparator, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clear -> clearForm()
            R.id.action_share -> activity.shareApp()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun clearForm() = with(binding) {
        activity?.alert(R.string.confirmation_message, R.string.confirmation) {
            positiveButton(R.string.clear) {
                etPriceFirst.setEmpty()
                etSizeFirst.setEmpty()
                etPriceSecond.setEmpty()
                etSizeSecond.setEmpty()

                etPriceFirst.clearFocus()
                etPriceFirst.hideKeyboard()
            }
            negativeButton(R.string.cancel) {}
        }?.show()
    }

    private fun submit() {
        (activity as MainActivity).showInterstitialAd()

        calculate(true)

        activity.hideKeyboard()
    }

    private fun calculate(showToast: Boolean) = with(binding) {
        val priceFirst = etPriceFirst.getDouble()
        val sizeFirst = etSizeFirst.getNumber()
        val priceSecond = etPriceSecond.getDouble()
        val sizeSecond = etSizeSecond.getNumber()

        if (priceFirst > 0 && sizeFirst > 0) {
            val realFirst = priceFirst / sizeFirst
            val resultFirst = getString(R.string.result_first, formatPrice(realFirst))

            tvResultFirst.text = resultFirst.fromHtml()

            if (priceSecond > 0 && sizeSecond > 0) {
                val realSecond = priceSecond / sizeSecond
                val resultSecond = getString(R.string.result_second, formatPrice(realSecond))
                tvResultSecond.text = resultSecond.fromHtml()

                val firstBiggest = realFirst > realSecond
                val larger = if (firstBiggest) realFirst else realSecond
                val less = if (firstBiggest) realSecond else realFirst

                if (larger == less) {
                    tvResultPercentage.setText(R.string.result_equals)
                } else {
                    val percentage = (larger - less) / larger
                    val formatted = percentage.formatPercent()

                    val word = if (firstBiggest) 2 else 1
                    val result = getString(R.string.result_percentage, word, formatted)
                    tvResultPercentage.text = result.fromHtml()
                }

                tvResultSecond.show()
                tvResultPercentage.show()
            } else {
                tvResultSecond.hide()
                tvResultPercentage.hide()
            }

            llResult.show()

            scrollBottom()
        } else if (showToast) {
            var message = R.string.error_empty_price

            if (priceFirst > 0) {
                message = R.string.error_empty_size
            }

            activity?.toast(message)
        }

        return@with
    }

    private fun formatPrice(price: Double): String {
        val realPrice = if (price < 0.0) 0.0 else price
        return String.format(Locale.getDefault(), "R$ %,.3f", realPrice)
    }

    private fun scrollBottom() = with(binding) {
        svContent.post { svContent.fullScroll(ScrollView.FOCUS_DOWN) }
    }

}
