package com.vokinchul.currencyconverter.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vokinchul.currencyconverter.ui.feature.currencyselection.ChangeAmount
import com.vokinchul.currencyconverter.ui.feature.currencyselection.ChangeDate
import com.vokinchul.currencyconverter.ui.feature.currencyselection.ChangeFromCurrency
import com.vokinchul.currencyconverter.ui.feature.currencyselection.CurrencySelectionState
import com.vokinchul.currencyconverter.ui.feature.currencyselection.NavigateToResults
import com.vokinchul.currencyconverter.ui.feature.currencyselection.NavigateToResultsEffect
import com.vokinchul.currencyconverter.ui.feature.currencyselection.ReplaceSelectedCurrencies
import com.vokinchul.currencyconverter.ui.feature.currencyselection.ShowError
import com.vokinchul.currencyconverter.ui.feature.currencyselection.ToggleAllCurrencies
import com.vokinchul.currencyconverter.ui.feature.currencyselection.ToggleToCurrency
import com.vokinchul.currencyconverter.ui.viewModel.CurrencyViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyScreen(
    viewModel: CurrencyViewModel = koinViewModel(),
    onNavigateToResults: (String, Set<String>, String, String) -> Unit
) {
    val state by viewModel.currencySelectionState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                is NavigateToResultsEffect -> {
                    onNavigateToResults(
                        effect.fromCurrency,
                        effect.toCurrencies,
                        effect.amount,
                        effect.date
                    )
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

        CurrencyView(state, viewModel, fromCurrency = true)

        Spacer(modifier = Modifier.height(16.dp))

        CurrencyView(state, viewModel, fromCurrency = false)

        Spacer(modifier = Modifier.height(16.dp))

        Amount(state, viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        DatePicker(viewModel)

        ButtonConvert(
            onClick = { viewModel.onEvent(NavigateToResults) },
            enabled = state.toCurrencies.isNotEmpty()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyView(
    currencySelectionState: CurrencySelectionState,
    viewModel: CurrencyViewModel,
    fromCurrency: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val allSelected =
        currencySelectionState.toCurrencies.size == currencySelectionState.currencies.size

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    if (fromCurrency) MenuAnchorType.PrimaryNotEditable
                    else MenuAnchorType.PrimaryEditable
                ),
            readOnly = true,
            value = when {
                fromCurrency -> currencySelectionState.currencies[currencySelectionState.fromCurrency]
                    ?: currencySelectionState.fromCurrency

                allSelected -> "All currencies"
                currencySelectionState.toCurrencies.isEmpty() -> "No currencies selected"
                else -> currencySelectionState.toCurrencies.joinToString {
                    currencySelectionState.currencies[it] ?: it
                }
            },
            onValueChange = {},
            label = { Text(if (fromCurrency) "From currency" else "To currencies") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = MaterialTheme.shapes.small
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (!fromCurrency) {
                DropdownMenuItem(
                    text = { Text("All currencies") },
                    onClick = {
                        viewModel.onEvent(ToggleAllCurrencies(selectAll = !allSelected))
                        expanded = false
                    },
                    trailingIcon = {
                        if (allSelected) Icon(Icons.Default.Check, null)
                    }
                )
                Divider()
            }

            currencySelectionState.currencies.keys.sorted().forEach { currency ->
                val isSelected = if (fromCurrency) currencySelectionState.fromCurrency == currency
                else currencySelectionState.toCurrencies.contains(currency)

                DropdownMenuItem(
                    text = {
                        Text(
                            "${currencySelectionState.currencies[currency] ?: currency} ($currency)",
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    onClick = {
                        if (fromCurrency) {
                            viewModel.onEvent(ChangeFromCurrency(currency))
                        } else {
                            when {
                                allSelected -> {
                                    viewModel.onEvent(
                                        ReplaceSelectedCurrencies(
                                            setOf(currency)
                                        )
                                    )
                                }

                                currencySelectionState.toCurrencies.size == 1 && isSelected -> {
                                    viewModel.onEvent(ToggleAllCurrencies(selectAll = true))
                                }

                                else -> {
                                    viewModel.onEvent(ToggleToCurrency(currency))
                                }
                            }
                        }
                        expanded = false
                        focusManager.clearFocus()
                    },
                    trailingIcon = {
                        if (isSelected) Icon(Icons.Default.Check, null)
                    }
                )
            }
        }
    }
}

@Composable
private fun Amount(
    currencySelectionState: CurrencySelectionState,
    viewModel: CurrencyViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = currencySelectionState.amount,
        onValueChange = {
            viewModel.onEvent(
                ChangeAmount(it)
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
private fun DatePicker(viewModel: CurrencyViewModel) {
    val state by viewModel.currencySelectionState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    OutlinedTextField(
        value = state.selectedDate,
        onValueChange = {},
        label = { Text("Date") },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Select date")
        },
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        enabled = false
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                dateFormatter.parse(state.selectedDate)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newDate = dateFormatter.format(Date(millis))
                        viewModel.onEvent(ChangeDate(newDate))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun ButtonConvert(
    onClick: () -> Unit,
    enabled: Boolean
) {
    var isClicked by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Button(
            onClick = {
                if (!isClicked) {
                    isClicked = true
                    onClick()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = enabled && !isClicked,
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
    LaunchedEffect(isClicked) {
        if (isClicked) {
            delay(1000)
            isClicked = false
        }
    }
}