package com.vokinchul.currencyconverter.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vokinchul.currencyconverter.domain.usecase.GetAvailableCurrenciesUseCase
import com.vokinchul.currencyconverter.ui.feature.currencyselection.ChangeAmount
import com.vokinchul.currencyconverter.ui.feature.currencyselection.ChangeDate
import com.vokinchul.currencyconverter.ui.feature.currencyselection.ChangeFromCurrency
import com.vokinchul.currencyconverter.ui.feature.currencyselection.CurrencySelectionEffect
import com.vokinchul.currencyconverter.ui.feature.currencyselection.CurrencySelectionEvent
import com.vokinchul.currencyconverter.ui.feature.currencyselection.CurrencySelectionState
import com.vokinchul.currencyconverter.ui.feature.currencyselection.LoadCurrencies
import com.vokinchul.currencyconverter.ui.feature.currencyselection.NavigateToResults
import com.vokinchul.currencyconverter.ui.feature.currencyselection.NavigateToResultsEffect
import com.vokinchul.currencyconverter.ui.feature.currencyselection.ReplaceSelectedCurrencies
import com.vokinchul.currencyconverter.ui.feature.currencyselection.ShowError
import com.vokinchul.currencyconverter.ui.feature.currencyselection.ToggleAllCurrencies
import com.vokinchul.currencyconverter.ui.feature.currencyselection.ToggleToCurrency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CurrencySelectionViewModel(
    private val getCurrenciesUseCase: GetAvailableCurrenciesUseCase
) : ViewModel() {

    private val _currencySelectionState = MutableStateFlow(CurrencySelectionState())
    val currencySelectionState: StateFlow<CurrencySelectionState> = _currencySelectionState

    private val _effect = Channel<CurrencySelectionEffect>()
    val effect: Flow<CurrencySelectionEffect> = _effect.receiveAsFlow()

    init {
        loadAvailableCurrencies()
    }

    fun onEvent(currencySelectionEvent: CurrencySelectionEvent) {
        when (currencySelectionEvent) {
            is ChangeFromCurrency -> {
                updateState { copy(fromCurrency = currencySelectionEvent.currency) }
            }

            is ToggleToCurrency -> {
                updateState {
                    val newSelection = if (toCurrencies.contains(currencySelectionEvent.currency)) {
                        toCurrencies - currencySelectionEvent.currency
                    } else {
                        toCurrencies + currencySelectionEvent.currency
                    }
                    copy(toCurrencies = newSelection)
                }
            }

            is ToggleAllCurrencies -> {
                updateState {
                    copy(
                        toCurrencies = if (currencySelectionEvent.selectAll) currencies.keys.toSet()
                        else emptySet()
                    )
                }
            }

            is ReplaceSelectedCurrencies -> {
                updateState { copy(toCurrencies = currencySelectionEvent.currencies) }
            }

            is ChangeAmount -> {
                updateState { copy(amount = currencySelectionEvent.amount) }
            }

            is ChangeDate -> {
                updateState { copy(selectedDate = currencySelectionEvent.date) }
            }

            is NavigateToResults -> {
                viewModelScope.launch {
                    if (currencySelectionState.value.toCurrencies.isNotEmpty()) {
                        _effect.send(
                            NavigateToResultsEffect(
                                fromCurrency = currencySelectionState.value.fromCurrency,
                                toCurrencies = currencySelectionState.value.toCurrencies,
                                amount = currencySelectionState.value.amount,
                                date = currencySelectionState.value.selectedDate
                            )
                        )
                    } else {
                        _effect.send(
                            ShowError(
                                "Пожалуйста, " +
                                        "выберите хотя бы одну целевую валюту"
                            )
                        )
                    }
                }
            }

            LoadCurrencies -> {
                loadAvailableCurrencies()
            }
        }
    }

    private fun loadAvailableCurrencies() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                updateState { copy(isLoading = true) }
            }
            try {
                val currencies = getCurrenciesUseCase.invoke()
                withContext(Dispatchers.Main) {
                    updateState {
                        copy(
                            currencies = currencies,
                            toCurrencies = currencies.keys.toSet()
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _effect.send(ShowError(e.message ?: "Не удалось загрузить валюты"))
                }
            } finally {
                withContext(Dispatchers.Main) {
                    updateState { copy(isLoading = false) }
                }
            }
        }
    }

    private inline fun updateState(transform: CurrencySelectionState.() -> CurrencySelectionState) {
        _currencySelectionState.update(transform)
    }
}