package com.vokinchul.currencyconverter.ui.feature.currencyselection

sealed class CurrencySelectionEvent
data class ChangeFromCurrency(val currency: String) : CurrencySelectionEvent()
data class ToggleToCurrency(val currency: String) : CurrencySelectionEvent()
data class ChangeAmount(val amount: String) : CurrencySelectionEvent()
data class ChangeDate(val date: String) : CurrencySelectionEvent()
data class ToggleAllCurrencies(val selectAll: Boolean) : CurrencySelectionEvent()
data class ReplaceSelectedCurrencies(val currencies: Set<String>) : CurrencySelectionEvent()
object LoadCurrencies : CurrencySelectionEvent()
object NavigateToResults : CurrencySelectionEvent()