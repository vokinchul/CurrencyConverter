package com.vokinchul.currencyconverter.data.api

import com.vokinchul.currencyconverter.domain.entity.LatestRatesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FrankfurterApi {
    @GET("latest")
    suspend fun getLatestRates(
        @Query("from") baseCurrency: String,
        @Query("to") targetCurrencies: String? = null
    ): LatestRatesResponse

    @GET("currencies")
    suspend fun getAvailableCurrencies(): Map<String, String>

    @GET("{date}")
    suspend fun getHistoricalRates(
        @Path("date") date: String,
        @Query("base") base: String,
        @Query("symbols") symbols: String
    ): LatestRatesResponse
}