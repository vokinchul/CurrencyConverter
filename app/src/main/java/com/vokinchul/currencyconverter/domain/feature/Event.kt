package com.vokinchul.currencyconverter.domain.feature

sealed class Event {
    data class ChangeFromCurrency(val currency: String) : Event()
    data class ToggleToCurrency(val currency: String) : Event()
    data class ChangeAmount(val amount: String) : Event()
    data class ChangeDate(val date: String) : Event()
    data class ToggleAllCurrencies(val selectAll: Boolean) : Event()
    object LoadCurrencies : Event()

}