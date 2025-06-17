package com.vokinchul.currencyconverter.domain.repository

import com.vokinchul.currencyconverter.domain.model.CurrencyRate

interface CurrencyRepository {
    suspend fun getLatestRates(
        baseCurrency: String,
        targetCurrencies: List<String>?
    ): List<CurrencyRate>

    suspend fun getAvailableCurrencies(): Map<String, String>

    suspend fun getHistoricalRates(
        date: String, // Формат: "YYYY-MM-DD"
        baseCurrency: String,
        targetCurrencies: List<String>
    ): List<CurrencyRate>
}