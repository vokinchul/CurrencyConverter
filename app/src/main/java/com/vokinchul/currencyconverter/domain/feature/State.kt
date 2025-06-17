package com.vokinchul.currencyconverter.domain.feature

import com.vokinchul.currencyconverter.domain.model.CurrencyRate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class State(
    val fromCurrency: String = "EUR",
    val toCurrencies: Set<String> = emptySet(),
    val amount: String = "1",
    val selectedDate: String = getCurrentDate(),
    val rates: List<CurrencyRate> = emptyList(),
    val currencies: Map<String, String> = emptyMap(),
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
