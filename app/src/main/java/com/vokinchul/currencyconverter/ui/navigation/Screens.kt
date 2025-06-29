package com.vokinchul.currencyconverter.ui.navigation

sealed class Screens(val name: String) {
    object MainScreen : Screens("main_screen")
    object ResultsScreen : Screens("result_screen")
}