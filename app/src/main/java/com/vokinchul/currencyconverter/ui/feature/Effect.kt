package com.vokinchul.currencyconverter.ui.feature

sealed class Effect
data class ShowError(val message: String) : Effect()
object NavigateToResultsEffect : Effect()