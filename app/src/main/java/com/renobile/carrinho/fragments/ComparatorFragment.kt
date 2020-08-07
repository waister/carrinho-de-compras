package com.renobile.carrinho.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.R
import com.renobile.carrinho.activity.MainActivity
import com.renobile.carrinho.util.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import java.util.*

class ComparatorFragment : Fragment(), TextWatcher {

    private var toolbar: Toolbar? = null
    private lateinit var etPriceFirst: EditText
    private lateinit var etSizeFirst: EditText
    private lateinit var etPriceSecond: EditText
    private lateinit var etSizeSecond: EditText
    private lateinit var tvResultFirst: TextView
    private lateinit var tvResultSecond: TextView
    private lateinit var tvResultPercentage: TextView
    private lateinit var llResult: LinearLayout
    private lateinit var svContent: ScrollView

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_comparator, container, false)

        toolbar = root.find(R.id.toolbar)

        etPriceFirst = root.find(R.id.et_price_first)
        etSizeFirst = root.find(R.id.et_size_first)
        etPriceSecond = root.find(R.id.et_price_second)
        etSizeSecond = root.find(R.id.et_size_second)
        llResult = root.find(R.id.ll_result)
        svContent = root.find(R.id.sv_content)

        val btSubmit = root.find<AppCompatButton>(R.id.bt_submit)

        tvResultFirst = root.find(R.id.tv_result_first)
        tvResultSecond = root.find(R.id.tv_result_second)
        tvResultPercentage = root.find(R.id.tv_result_percentage)

        etPriceFirst.addTextChangedListener(MaskMoney(etPriceFirst))
        etPriceSecond.addTextChangedListener(MaskMoney(etPriceSecond))

        etPriceFirst.addTextChangedListener(this)
        etSizeFirst.addTextChangedListener(this)
        etPriceSecond.addTextChangedListener(this)
        etSizeSecond.addTextChangedListener(this)

        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        btSubmit.setOnClickListener { submit() }

        etSizeSecond.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submit()
                return@OnEditorActionListener true
            }

            false
        })

        return root
    }

    override fun afterTextChanged(s: Editable?) {
        llResult.visibility = View.GONE
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()

        if (etPriceFirst.text.isEmpty())
            etPriceFirst.setText(Hawk.get(PREF_PRICE_FIRST, ""))

        if (etSizeFirst.text.isEmpty())
            etSizeFirst.setText(Hawk.get(PREF_SIZE_FIRST, ""))

        if (etPriceSecond.text.isEmpty())
            etPriceSecond.setText(Hawk.get(PREF_PRICE_SECOND, ""))

        if (etSizeSecond.text.isEmpty())
            etSizeSecond.setText(Hawk.get(PREF_SIZE_SECOND, ""))

        calculate(false)
    }

    override fun onDestroy() {
        super.onDestroy()

        Hawk.put(PREF_PRICE_FIRST, etPriceFirst.text.toString())
        Hawk.put(PREF_SIZE_FIRST, etSizeFirst.text.toString())
        Hawk.put(PREF_PRICE_SECOND, etPriceSecond.text.toString())
        Hawk.put(PREF_SIZE_SECOND, etSizeSecond.text.toString())
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

    private fun clearForm() {
        activity?.alert(R.string.confirmation_message, R.string.confirmation) {
            positiveButton(R.string.clear) {
                etPriceFirst.setText("")
                etSizeFirst.setText("")
                etPriceSecond.setText("")
                etSizeSecond.setText("")

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

    private fun calculate(showToast: Boolean) {
        val priceFirst = etPriceFirst.getPrice()
        val sizeFirst = etSizeFirst.getNumber()
        val priceSecond = etPriceSecond.getPrice()
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

                tvResultSecond.visibility = View.VISIBLE
                tvResultPercentage.visibility = View.VISIBLE
            } else {
                tvResultSecond.visibility = View.GONE
                tvResultPercentage.visibility = View.GONE
            }

            llResult.visibility = View.VISIBLE

            scrollBottom()
        } else {
            if (showToast) {
                var message = R.string.error_empty_price

                if (priceFirst > 0) {
                    message = R.string.error_empty_size
                }

                activity?.toast(message)
            }
        }
    }

    private fun formatPrice(price: Double): String {
        val realPrice = if (price < 0.0) 0.0 else price
        return String.format(Locale.getDefault(), "R$ %,.3f", realPrice)
    }

    private fun scrollBottom() {
        svContent.post { svContent.fullScroll(ScrollView.FOCUS_DOWN) }
    }

}
