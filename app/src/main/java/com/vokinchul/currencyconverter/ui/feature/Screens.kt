package com.vokinchul.currencyconverter.ui.feature

sealed class Screens(val name: String) {
    object MainScreen : Screens("main_screen")
    object ResultsScreen : Screens("result_screen")
}