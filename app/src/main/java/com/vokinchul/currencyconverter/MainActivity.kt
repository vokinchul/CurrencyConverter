package com.vokinchul.currencyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.vokinchul.currencyconverter.ui.theme.CurrencyConverterTheme
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlin.coroutines.coroutineContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CurrencyConverterTheme {

            }
        }
    }
}


@Preview
@Composable
fun PreviewLight() {
    CurrencyConverterTheme(
        darkTheme = false
    ) {

    }
}

@Preview
@Composable
fun PreviewDark() {
    CurrencyConverterTheme(
        darkTheme = true
    ) {

    }
}