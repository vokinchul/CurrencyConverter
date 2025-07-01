package com.vokinchul.currencyconverter.ui.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vokinchul.currencyconverter.core.Result.Error
import com.vokinchul.currencyconverter.core.Result.Loading
import com.vokinchul.currencyconverter.core.Result.Success
import com.vokinchul.currencyconverter.domain.repository.CurrencyRepository
import com.vokinchul.currencyconverter.domain.usecase.GetHistoricalRatesUseCase
import com.vokinchul.currencyconverter.ui.feature.result.ResultsEffect
import com.vokinchul.currencyconverter.ui.feature.result.ResultsEvent
import com.vokinchul.currencyconverter.ui.feature.result.ResultsState
import com.vokinchul.currencyconverter.ui.feature.result.RetryLoadRates
import com.vokinchul.currencyconverter.ui.feature.result.ShowErrorResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResultsViewModel(
    private val repository: CurrencyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ResultsState())
    val state: StateFlow<ResultsState> = _state

    private val _effect = Channel<ResultsEffect>()
    val effect: Flow<ResultsEffect> = _effect.receiveAsFlow()

    init {
        val fromCurrency = savedStateHandle.get<String>("fromCurrency") ?: "EUR"
        val toCurrencies = savedStateHandle.get<String>("toCurrencies")
            ?.split(",")?.toSet() ?: emptySet()
        val amount = savedStateHandle.get<String>("amount") ?: "1"
        val date = savedStateHandle.get<String>("date") ?: ResultsState.getCurrentDate()

        updateState {
            copy(
                fromCurrency = fromCurrency,
                toCurrencies = toCurrencies,
                amount = amount,
                selectedDate = date
            )
        }
        loadRates(date, fromCurrency, toCurrencies)
    }

    private fun loadRates(date: String, baseCurrency: String, targetCurrencies: Set<String>) {
        if (targetCurrencies.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                updateState { copy(isLoading = true, error = null) }
            }
            val result = GetHistoricalRatesUseCase(repository).invoke(
                date = date,
                baseCurrency = baseCurrency,
                targetCurrencies = targetCurrencies.toList()
            )
            withContext(Dispatchers.Main) {
                when (result) {
                    is Success -> {
                        updateState { copy(rates = result.data, isLoading = false, error = null) }
                    }

                    is Error -> {
                        _effect.send(ShowErrorResults(result.message))
                        updateState { copy(isLoading = false, error = result.message) }
                    }

                    is Loading -> {
                    }
                }
            }
        }
    }

    private inline fun updateState(transform: ResultsState.() -> ResultsState) {
        _state.update(transform)
    }

    fun onEvent(event: ResultsEvent) {
        when (event) {
            is RetryLoadRates -> {
                loadRates(
                    state.value.selectedDate,
                    state.value.fromCurrency,
                    state.value.toCurrencies
                )
            }
        }
    }
}