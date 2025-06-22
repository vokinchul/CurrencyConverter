package com.vokinchul.currencyconverter.di

import com.vokinchul.currencyconverter.data.api.FrankfurterApi
import com.vokinchul.currencyconverter.data.repository.RemoteCurrencyRepository
import com.vokinchul.currencyconverter.domain.repository.CurrencyRepository
import com.vokinchul.currencyconverter.domain.usecase.GetAvailableCurrenciesUseCase
import com.vokinchul.currencyconverter.ui.viewModel.CurrencyViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    single {
        Retrofit.Builder()
            .baseUrl("https://api.frankfurter.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FrankfurterApi::class.java)
    }

    single<CurrencyRepository> { RemoteCurrencyRepository(get()) }

    single { GetAvailableCurrenciesUseCase(get()) }

    viewModel {
        CurrencyViewModel(
            repository = get(),
            getCurrenciesUseCase = get()
        )
    }
}