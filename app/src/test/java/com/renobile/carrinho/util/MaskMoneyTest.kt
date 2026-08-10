package com.renobile.carrinho.util

import android.widget.EditText
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.text.NumberFormat
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MaskMoneyTest {

    @Test
    fun `given digits input, when onTextChanged, then text is formatted as currency`() {
        Locale.setDefault(Locale.forLanguageTag("pt-BR"))
        val editText = EditText(RuntimeEnvironment.getApplication())

        MaskMoney(editText).onTextChanged("100", 0, 0, 3)

        assertEquals(NumberFormat.getCurrencyInstance().format(1.0), editText.text.toString())
    }

    @Test
    fun `given recursive update, when onTextChanged called while updating, then no loop`() {
        Locale.setDefault(Locale.forLanguageTag("pt-BR"))
        val editText = EditText(RuntimeEnvironment.getApplication())
        val mask = MaskMoney(editText)

        mask.onTextChanged("1234", 0, 0, 4)
        val formatted = editText.text.toString()
        mask.onTextChanged(formatted, 0, 4, formatted.length)

        assertEquals(NumberFormat.getCurrencyInstance().format(12.34), editText.text.toString())
    }

    @Test
    fun `given empty input, when onTextChanged, then text stays empty`() {
        Locale.setDefault(Locale.forLanguageTag("pt-BR"))
        val editText = EditText(RuntimeEnvironment.getApplication())

        MaskMoney(editText).onTextChanged("", 0, 0, 0)

        assertEquals("", editText.text.toString())
    }
}
