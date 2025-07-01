package com.vokinchul.currencyconverter.domain.usecase

import com.vokinchul.currencyconverter.core.Result
import com.vokinchul.currencyconverter.domain.repository.CurrencyRepository

class GetAvailableCurrenciesUseCase(
    private val repository: CurrencyRepository
) {
    suspend operator fun invoke(): Result<Map<String, String>> {
        return repository.getAvailableCurrencies()
    }
}