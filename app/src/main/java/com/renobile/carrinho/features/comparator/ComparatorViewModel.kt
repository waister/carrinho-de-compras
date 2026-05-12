package com.renobile.carrinho.features.comparator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.renobile.carrinho.R
import com.renobile.carrinho.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

class ComparatorViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ComparatorState())
    val uiState: StateFlow<ComparatorState> = _uiState.asStateFlow()

    init {
        _uiState.update { 
            it.copy(
                priceFirst = Prefs.getValue(PREF_PRICE_FIRST, ""),
                sizeFirst = Prefs.getValue(PREF_SIZE_FIRST, ""),
                priceSecond = Prefs.getValue(PREF_PRICE_SECOND, ""),
                sizeSecond = Prefs.getValue(PREF_SIZE_SECOND, "")
            )
        }
        calculate(false)
    }

    fun onPriceFirstChanged(value: String) {
        _uiState.update { it.copy(priceFirst = value, showResult = false) }
    }

    fun onSizeFirstChanged(value: String) {
        _uiState.update { it.copy(sizeFirst = value, showResult = false) }
    }

    fun onPriceSecondChanged(value: String) {
        _uiState.update { it.copy(priceSecond = value, showResult = false) }
    }

    fun onSizeSecondChanged(value: String) {
        _uiState.update { it.copy(sizeSecond = value, showResult = false) }
    }

    fun clear() {
        _uiState.update { 
            it.copy(
                priceFirst = "",
                sizeFirst = "",
                priceSecond = "",
                sizeSecond = "",
                showResult = false
            )
        }
        savePrefs()
    }

    fun calculate(showToast: Boolean = true) {
        val state = _uiState.value
        val priceFirst = state.priceFirst.parseToDouble()
        val sizeFirst = state.sizeFirst.parseToDouble()
        val priceSecond = state.priceSecond.parseToDouble()
        val sizeSecond = state.sizeSecond.parseToDouble()

        if (priceFirst > 0 && sizeFirst > 0) {
            val realFirst = priceFirst / sizeFirst
            val resFirst = getApplication<Application>().getString(R.string.result_first, formatPrice(realFirst))

            var resSecond: String? = null
            var resPercent: String? = null

            if (priceSecond > 0 && sizeSecond > 0) {
                val realSecond = priceSecond / sizeSecond
                resSecond = getApplication<Application>().getString(R.string.result_second, formatPrice(realSecond))

                val firstBiggest = realFirst > realSecond
                val larger = if (firstBiggest) realFirst else realSecond
                val less = if (firstBiggest) realSecond else realFirst

                resPercent = if (larger == less) {
                    getApplication<Application>().getString(R.string.result_equals)
                } else {
                    val percentage = (larger - less) / larger
                    val formatted = percentage.formatPercent()
                    val word = if (firstBiggest) 2 else 1
                    getApplication<Application>().getString(R.string.result_percentage, word, formatted)
                }
            }

            _uiState.update {
                it.copy(
                    resultFirst = resFirst,
                    resultSecond = resSecond,
                    resultPercentage = resPercent,
                    showResult = true
                )
            }
            savePrefs()
        } else if (showToast) {
            // Toast logic handled in Fragment/Screen via Events if needed, 
            // but for simplicity here I'll just not update result
        }
    }

    private fun savePrefs() {
        val state = _uiState.value
        Prefs.putValue(PREF_PRICE_FIRST, state.priceFirst)
        Prefs.putValue(PREF_SIZE_FIRST, state.sizeFirst)
        Prefs.putValue(PREF_PRICE_SECOND, state.priceSecond)
        Prefs.putValue(PREF_SIZE_SECOND, state.sizeSecond)
    }

    private fun formatPrice(price: Double): String {
        val realPrice = if (price < 0.0) 0.0 else price
        return String.Companion.format(Locale.getDefault(), "R$ %,.3f", realPrice)
    }

    private fun String.parseToDouble(): Double {
        val clean = this.replace(Regex("[^0-9,.]"), "").replace(",", ".")
        return clean.toDoubleOrNull() ?: 0.0
    }
}
