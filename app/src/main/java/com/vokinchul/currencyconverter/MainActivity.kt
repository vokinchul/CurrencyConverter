package com.vokinchul.currencyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vokinchul.currencyconverter.ui.feature.Screens
import com.vokinchul.currencyconverter.ui.screens.CurrencyScreen
import com.vokinchul.currencyconverter.ui.screens.ResultsScreen
import com.vokinchul.currencyconverter.ui.theme.CurrencyConverterTheme
import com.vokinchul.currencyconverter.ui.viewModel.CurrencyViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CurrencyViewModel = koinViewModel()
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
                                viewModel,
                                onNavigateToResults = {
                                    navController.navigate(Screens.ResultsScreen.name)
                                }
                            )
                        }
                        composable(Screens.ResultsScreen.name) {
                            ResultsScreen(
                                viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}