package com.vokinchul.currencyconverter.ui.feature.currencyselection

sealed class CurrencySelectionEffect
data class ShowError(val message: String) : CurrencySelectionEffect()
data class NavigateToResultsEffect(
    val fromCurrency: String,
    val toCurrencies: Set<String>,
    val amount: String,
    val date: String
) : CurrencySelectionEffect()