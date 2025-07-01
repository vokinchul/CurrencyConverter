package com.vokinchul.currencyconverter.domain.usecase

import com.vokinchul.currencyconverter.core.Result
import com.vokinchul.currencyconverter.domain.model.CurrencyRate
import com.vokinchul.currencyconverter.domain.repository.CurrencyRepository

class GetHistoricalRatesUseCase(
    private val repository: CurrencyRepository
) {
    suspend operator fun invoke(
        date: String,
        baseCurrency: String,
        targetCurrencies: List<String>
    ): Result<List<CurrencyRate>> {
        return repository.getHistoricalRates(
            date, baseCurrency, targetCurrencies
        )
    }
}