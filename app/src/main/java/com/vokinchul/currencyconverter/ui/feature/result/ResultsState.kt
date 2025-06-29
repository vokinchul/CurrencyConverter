package com.vokinchul.currencyconverter.ui.feature.result

import com.vokinchul.currencyconverter.domain.model.CurrencyRate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ResultsState(
    val fromCurrency: String = "EUR",
    val toCurrencies: Set<String> = emptySet(),
    val amount: String = "1",
    val selectedDate: String = getCurrentDate(),
    val rates: List<CurrencyRate> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        fun getCurrentDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}