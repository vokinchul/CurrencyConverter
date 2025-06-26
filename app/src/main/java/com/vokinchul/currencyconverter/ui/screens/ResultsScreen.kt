package com.vokinchul.currencyconverter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vokinchul.currencyconverter.domain.model.CurrencyRate
import com.vokinchul.currencyconverter.ui.viewModel.CurrencyViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    viewModel: CurrencyViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .systemBarsPadding()
    ) {
        TopAppBar(
            title = { Text("Converted Results") },
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                InfoRow("From currency", state.fromCurrency)
                InfoRow("To currencies", state.toCurrencies.joinToString())
                InfoRow("Date", state.selectedDate, isDate = true)

                Spacer(modifier = Modifier.height(16.dp))

                if (state.rates.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                    ) {
                        items(
                            items = state.rates,
                            key = { rate -> rate.currency }
                        ) { rate ->
                            RateItem(
                                amount = state.amount,
                                fromCurrency = state.fromCurrency,
                                rate = rate
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No rates available")
                    }
                }
            }

            ButtonBack(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RateItem(
    amount: String,
    fromCurrency: String,
    rate: CurrencyRate
) {
    Text(
        text = "$amount $fromCurrency = ${
            "%.4f".format(
                (amount.toDoubleOrNull() ?: 1.0) * rate.rate
            )
        } ${rate.currency}",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
    )
}

@Composable
fun ButtonBack(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Button(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            Text(
                text = "Back",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, isDate: Boolean = false) {
    val formattedValue = remember(value, isDate) {

        if (isDate) {
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .parse(value)
                    ?.let { date ->
                        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(date)
                    } ?: value
            } catch (e: Exception) {
                value
            }
        } else {
            value
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Spacer(modifier = Modifier.weight(0.05f))

        Box(
            modifier = Modifier
                .weight(0.35f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.weight(0.01f))

        Box(
            modifier = Modifier
                .weight(0.6f)
                .padding(top = 3.dp)
        ) {
            Text(
                text = formattedValue,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Left
            )
        }
        Spacer(modifier = Modifier.weight(0.05f))
    }
}