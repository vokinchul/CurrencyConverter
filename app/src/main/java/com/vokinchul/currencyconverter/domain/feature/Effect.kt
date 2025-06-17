package com.vokinchul.currencyconverter.domain.feature

sealed class Effect {
    data class ShowError(val message: String) : Effect()
}