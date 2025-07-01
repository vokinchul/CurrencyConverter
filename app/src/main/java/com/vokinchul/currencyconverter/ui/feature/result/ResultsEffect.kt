package com.vokinchul.currencyconverter.ui.feature.result

sealed class ResultsEffect
data class ShowErrorResults(val message: String) : ResultsEffect()