package com.vokinchul.currencyconverter.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vokinchul.currencyconverter.domain.repository.CurrencyRepository
import com.vokinchul.currencyconverter.domain.usecase.GetAvailableCurrenciesUseCase
import com.vokinchul.currencyconverter.domain.usecase.GetHistoricalRatesUseCase
import com.vokinchul.currencyconverter.ui.feature.ChangeAmount
import com.vokinchul.currencyconverter.ui.feature.ChangeDate
import com.vokinchul.currencyconverter.ui.feature.ChangeFromCurrency
import com.vokinchul.currencyconverter.ui.feature.Effect
import com.vokinchul.currencyconverter.ui.feature.Event
import com.vokinchul.currencyconverter.ui.feature.LoadCurrencies
import com.vokinchul.currencyconverter.ui.feature.ReplaceSelectedCurrencies
import com.vokinchul.currencyconverter.ui.feature.ShowError
import com.vokinchul.currencyconverter.ui.feature.State
import com.vokinchul.currencyconverter.ui.feature.ToggleAllCurrencies
import com.vokinchul.currencyconverter.ui.feature.ToggleToCurrency
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CurrencyViewModel(
    private val repository: CurrencyRepository,
    private val getCurrenciesUseCase: GetAvailableCurrenciesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    private val _effect = Channel<Effect>()
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    init {
        onEvent(LoadCurrencies)
        viewModelScope.launch {
            state
                .map { it.currencies }
                .distinctUntilChanged()
                .collect { currencies ->
                    if (currencies.isNotEmpty() && state.value.toCurrencies.isEmpty()) {
                        updateState { copy(toCurrencies = currencies.keys.toSet()) }
                    }
                }
        }
    }

    fun onEvent(event: Event) {
        when (event) {
            is ChangeFromCurrency -> {
                updateState { copy(fromCurrency = event.currency) }
                loadRates(state.value.selectedDate)
            }

            is ReplaceSelectedCurrencies -> {
                updateState { copy(toCurrencies = event.currencies) }
            }

            is ToggleAllCurrencies -> {
                updateState {
                    copy(
                        toCurrencies = if (event.selectAll) currencies.keys.toSet()
                        else toCurrencies
                    )
                }
            }

            is ToggleToCurrency -> {
                updateState {
                    val newSelection = if (toCurrencies.contains(event.currency)) {
                        toCurrencies - event.currency
                    } else {
                        toCurrencies + event.currency
                    }
                    copy(toCurrencies = newSelection)
                }
            }

            is ChangeAmount -> {
                updateState { copy(amount = event.amount) }
            }

            is ChangeDate -> {
                updateState { copy(selectedDate = event.date) }
                loadRates(event.date)
            }

            LoadCurrencies -> {
                loadAvailableCurrencies()
            }
        }
    }


    private fun loadRates(date: String) {
        if (_state.value.toCurrencies.isEmpty()) return
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            try {
                val rates = GetHistoricalRatesUseCase(repository).invoke(
                    date = date,
                    baseCurrency = _state.value.fromCurrency,
                    targetCurrencies = _state.value.toCurrencies.toList()
                )
                updateState { copy(rates = rates) }
            } catch (e: Exception) {
                _effect.send(ShowError(e.message ?: "Unknown error"))
                updateState { copy(error = e.message) }
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun loadAvailableCurrencies() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            try {
                val currencies = getCurrenciesUseCase.invoke()
                updateState { copy(currencies = currencies) }
            } catch (e: Exception) {
                _effect.send(
                    ShowError(
                        e.message ?: "Failed to load currencies"
                    )
                )
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }

    private inline fun updateState(transform: State.() -> State) {
        _state.update(transform)
    }
}