package com.vokinchul.currencyconverter.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vokinchul.currencyconverter.domain.feature.Effect
import com.vokinchul.currencyconverter.domain.feature.Event
import com.vokinchul.currencyconverter.domain.feature.State
import com.vokinchul.currencyconverter.domain.repository.CurrencyRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class CurrencyViewModel(
    private val repository: CurrencyRepository
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    private val _effect = Channel<Effect>()
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    init {
        onEvent(Event.LoadCurrencies)
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
            is Event.ChangeFromCurrency -> {
                updateState { copy(fromCurrency = event.currency) }
                loadRates(state.value.selectedDate)
            }

            is Event.ToggleToCurrency -> {
                updateState {
                    val updatedSelection = if (toCurrencies.contains(event.currency)) {
                        toCurrencies - event.currency
                    } else {
                        toCurrencies + event.currency
                    }

                    if (updatedSelection.isEmpty()) {
                        this
                    } else {
                        copy(toCurrencies = updatedSelection)
                    }
                }
                loadRates(state.value.selectedDate)
            }

            is Event.ToggleAllCurrencies -> {
                updateState {
                    if (event.selectAll) {
                        copy(toCurrencies = currencies.keys.toSet())
                    } else {
                        if (toCurrencies.size == currencies.size) {
                            copy(toCurrencies = setOf(toCurrencies.first()))
                        } else {
                            copy(toCurrencies = currencies.keys.toSet())
                        }
                    }
                }
                loadRates(state.value.selectedDate)
            }

            is Event.ChangeAmount -> {
                updateState { copy(amount = event.amount) }
            }

            is Event.ChangeDate -> {
                updateState { copy(selectedDate = event.date) }
                loadRates(event.date)
            }

            Event.LoadCurrencies -> {
                loadAvailableCurrencies()
            }
        }
    }


    private fun loadRates(date: String) {
        if (_state.value.toCurrencies.isEmpty()) return
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            try {
                val rates = repository.getHistoricalRates(
                    date = date,
                    baseCurrency = _state.value.fromCurrency,
                    targetCurrencies = _state.value.toCurrencies.toList()
                )
                updateState { copy(rates = rates) }
            } catch (e: Exception) {
                _effect.send(Effect.ShowError(e.message ?: "Unknown error"))
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
                val currencies = repository.getAvailableCurrencies()
                updateState { copy(currencies = currencies) }
            } catch (e: Exception) {
                _effect.send(
                    Effect.ShowError(
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