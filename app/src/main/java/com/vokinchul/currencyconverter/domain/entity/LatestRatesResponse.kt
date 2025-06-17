package com.vokinchul.currencyconverter.domain.entity

data class LatestRatesResponse(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)