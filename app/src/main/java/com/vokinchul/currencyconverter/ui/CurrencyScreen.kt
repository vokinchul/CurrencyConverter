package com.vokinchul.currencyconverter.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vokinchul.currencyconverter.domain.feature.Effect
import com.vokinchul.currencyconverter.domain.feature.Event
import com.vokinchul.currencyconverter.domain.feature.State
import com.vokinchul.currencyconverter.ui.viewModel.CurrencyViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyScreen(
    viewModel: CurrencyViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .systemBarsPadding()
    ) {
        TopAppBar(
            title = { Text("Currency Converter") },
        )

        FromCurrency(state, viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        ToCurrency(state, viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        Amount(state, viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        Date(viewModel)

        Spacer(modifier = Modifier.height(24.dp))

        TemporaryResultsToSecondScreen(state)

        ButtonConvert(onClick = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FromCurrency(
    state: State,
    viewModel: CurrencyViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            readOnly = true,
            value = "${
                state.currencies[state.fromCurrency] ?: state.fromCurrency
            } (${state.fromCurrency})",
            onValueChange = {},
            label = { Text("From currency") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = MaterialTheme.shapes.small
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            state.currencies.keys.sorted().forEach { currency ->
                DropdownMenuItem(
                    text = {
                        Text(
                            "${state.currencies[currency] ?: currency} ($currency)",
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    onClick = {
                        viewModel.onEvent(Event.ChangeFromCurrency(currency))
                        expanded = false
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToCurrency(
    state: State,
    viewModel: CurrencyViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val allSelected = state.toCurrencies.size == state.currencies.size

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = when {
                state.currencies.isEmpty() -> "Loading..."
                allSelected -> "All currencies"
                state.toCurrencies.size == 1 -> state.currencies[state.toCurrencies.first()]
                    ?: state.toCurrencies.first()

                else -> "${state.toCurrencies.size} selected"
            },
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable),
            label = { Text("To currencies") },
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            shape = MaterialTheme.shapes.small
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All currencies") },
                onClick = {
                    viewModel.onEvent(Event.ToggleAllCurrencies(selectAll = !allSelected))
                },
                trailingIcon = {
                    if (allSelected) Icon(Icons.Default.Check, null)
                }
            )
            Divider()
            state.currencies.keys.sorted().forEach { currency ->
                DropdownMenuItem(
                    text = { Text("${state.currencies[currency]} ($currency)") },
                    onClick = {
                        if (state.toCurrencies.size > 1 || !state.toCurrencies.contains(
                                currency)) {
                            viewModel.onEvent(Event.ToggleToCurrency(currency))
                        }
                    },
                    trailingIcon = {
                        if (state.toCurrencies.contains(currency))
                            Icon(Icons.Default.Check, null)
                    }
                )
            }
        }
    }
}

@Composable
private fun Amount(
    state: State,
    viewModel: CurrencyViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = state.amount,
        onValueChange = {
            viewModel.onEvent(
                Event.ChangeAmount(it)
            )
        },
        label = { Text("Amount") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Date(viewModel: CurrencyViewModel) {
    val dateFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    val initialDateMillis = System.currentTimeMillis()
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
    var selectedDate by remember {
        mutableStateOf(dateFormatter.format(Date(initialDateMillis)))
    }
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = SimpleDateFormat(
                            "yyyy-MM-dd", Locale.getDefault()
                        )
                            .format(Date(it))
                        viewModel.onEvent(Event.ChangeDate(date))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true }
    ) {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            label = { Text("Date") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            interactionSource = remember { MutableInteractionSource() },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = MaterialTheme.shapes.small
        )

    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newDate = dateFormatter.format(Date(millis))
                        selectedDate = newDate
                        viewModel.onEvent(Event.ChangeDate(newDate))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ColumnScope.TemporaryResultsToSecondScreen(state: State) {
    when {
        state.isLoading -> CircularProgressIndicator(
            Modifier.align(Alignment.CenterHorizontally)
        )

        state.error != null -> Text(
            "Error: ${state.error}",
            color = MaterialTheme.colorScheme.error
        )

        state.rates.isNotEmpty() -> {
            val amountValue = state.amount.toDoubleOrNull() ?: 1.0
            Column {
                Text("Results:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                state.rates.forEach { rate ->
                    Text(
                        "${state.amount} ${state.fromCurrency} = ${
                            "%.4f".format(
                                amountValue * rate.rate
                            )
                        } ${rate.currency}"
                    )
                }
            }
        }
    }
}

@Composable
fun ButtonConvert(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            Text(
                text = "Convert",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
