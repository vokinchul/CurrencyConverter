package com.vokinchul.currencyconverter.data.repository

import com.vokinchul.currencyconverter.data.api.FrankfurterApi
import com.vokinchul.currencyconverter.domain.model.CurrencyRate
import com.vokinchul.currencyconverter.domain.repository.CurrencyRepository

class CurrencyRepositoryImpl(
    private val api: FrankfurterApi
) : CurrencyRepository {

    override suspend fun getLatestRates(
        baseCurrency: String,
        targetCurrencies: List<String>?
    ): List<CurrencyRate> {
        val targets = targetCurrencies?.joinToString(",")
        val response = api.getLatestRates(baseCurrency, targets)
        return response.rates.map { CurrencyRate(it.key, it.value) }
    }

    override suspend fun getAvailableCurrencies(): Map<String, String> {
        return api.getAvailableCurrencies()
    }

    override suspend fun getHistoricalRates(
        date: String,
        baseCurrency: String,
        targetCurrencies: List<String>
    ) = api.getHistoricalRates(
        date = date,
        base = baseCurrency,
        symbols = targetCurrencies.joinToString(",")
    ).rates.map { CurrencyRate(it.key, it.value) }

}