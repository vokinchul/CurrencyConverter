package com.vokinchul.currencyconverter.data.api

import com.vokinchul.currencyconverter.data.model.LatestRatesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FrankfurterApi {
    @GET("currencies")
    suspend fun getAvailableCurrencies(): Map<String, String>

    @GET("{date}")
    suspend fun getHistoricalRates(
        @Path("date") date: String,
        @Query("base") base: String,
        @Query("symbols") symbols: String
    ): LatestRatesResponse
}