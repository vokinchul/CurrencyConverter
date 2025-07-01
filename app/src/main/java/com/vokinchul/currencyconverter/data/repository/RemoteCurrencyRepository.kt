package com.vokinchul.currencyconverter.data.repository

import com.vokinchul.currencyconverter.core.Result
import com.vokinchul.currencyconverter.data.api.FrankfurterApi
import com.vokinchul.currencyconverter.domain.model.CurrencyRate
import com.vokinchul.currencyconverter.domain.repository.CurrencyRepository

class RemoteCurrencyRepository(
    private val api: FrankfurterApi
) : CurrencyRepository {

    override suspend fun getAvailableCurrencies(): Result<Map<String, String>> {
        return try {
            val response = api.getAvailableCurrencies()
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Ошибка загрузки валют",
                exception = e
            )
        }
    }

    override suspend fun getHistoricalRates(
        date: String,
        baseCurrency: String,
        targetCurrencies: List<String>
    ): Result<List<CurrencyRate>> {
        return try {
            val response = api.getHistoricalRates(
                date = date,
                base = baseCurrency,
                symbols = targetCurrencies.joinToString(",")
            )
            val rates = response.rates.map { (currency, rate) ->
                CurrencyRate(currency = currency, rate = rate)
            }
            Result.Success(rates)
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Ошибка загрузки курсов",
                exception = e
            )
        }
    }
}