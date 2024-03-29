package com.renobile.carrinho.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

import java.text.NumberFormat

class MaskMoney(private val editText: EditText) : TextWatcher {
    private var isUpdating = false

    private val numberFormat = NumberFormat.getCurrencyInstance()

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, after: Int) {
        if (isUpdating) {
            isUpdating = false
            return
        }

        isUpdating = true

        var value = charSequence.toString()

        value = value.replace("\\D+".toRegex(), "")

        try {
            value = numberFormat.format(java.lang.Double.parseDouble(value) / 100)

            editText.setText(value)
            editText.setSelection(value.length)
        } catch (ignored: NumberFormatException) {
        }

    }

    override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(editable: Editable) {}
}