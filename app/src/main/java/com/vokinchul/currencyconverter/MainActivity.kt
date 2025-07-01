package com.vokinchul.currencyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vokinchul.currencyconverter.ui.navigation.Screens
import com.vokinchul.currencyconverter.ui.screens.CurrencyScreen
import com.vokinchul.currencyconverter.ui.screens.ResultsScreen
import com.vokinchul.currencyconverter.ui.theme.CurrencyConverterTheme
import com.vokinchul.currencyconverter.ui.viewModel.CurrencySelectionViewModel
import com.vokinchul.currencyconverter.ui.viewModel.ResultsViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CurrencyConverterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screens.MainScreen.name
                    ) {
                        composable(Screens.MainScreen.name) {
                            CurrencyScreen(
                                viewModel = koinViewModel<CurrencySelectionViewModel>(),
                                onNavigateToResults = { fromCurrency, toCurrencies, amount, date ->
                                    val toCurrenciesStr = toCurrencies.joinToString(",")
                                    navController.navigate(
                                        "${
                                            Screens.ResultsScreen.name
                                        }/$fromCurrency/$toCurrenciesStr/$amount/$date"
                                    )
                                }
                            )
                        }
                        composable(
                            route = "${
                                Screens.ResultsScreen.name
                            }/{fromCurrency}/{toCurrencies}/{amount}/{date}",
                            arguments = listOf(
                                navArgument("fromCurrency") { type = NavType.StringType },
                                navArgument("toCurrencies") { type = NavType.StringType },
                                navArgument("amount") { type = NavType.StringType },
                                navArgument("date") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            ResultsScreen(
                                viewModel = koinViewModel<ResultsViewModel>(),
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}