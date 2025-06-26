package com.vokinchul.currencyconverter

import android.app.Application
import com.vokinchul.currencyconverter.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class CurrencyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CurrencyApp)
            modules(appModule)
        }
    }
}