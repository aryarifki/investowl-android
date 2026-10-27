package me.cugarete.investowl.feature.backtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.cugarete.investowl.core.model.BacktestHistoryItem
import me.cugarete.investowl.core.model.BacktestStatsResponse
import me.cugarete.investowl.core.network.InvestowlApi

data class BacktestUiState(
    val isLoading: Boolean = false,
    val selectedHorizon: Int = 10,
    val stats: BacktestStatsResponse? = null,
    val history: List<BacktestHistoryItem> = emptyList(),
    val errorMessage: String? = null
)

class BacktestViewModel(
    private val api: InvestowlApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(BacktestUiState(isLoading = true))
    val uiState: StateFlow<BacktestUiState> = _uiState.asStateFlow()

    init {
        loadData(uiState.value.selectedHorizon)
    }

    fun selectHorizon(horizon: Int) {
        _uiState.update { it.copy(selectedHorizon = horizon) }
        loadData(horizon)
    }

    fun loadData(horizon: Int = _uiState.value.selectedHorizon) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val statsResp = api.getBacktestStats(horizon = horizon)
                val historyResp = api.getBacktestHistory(limit = 100)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        stats = statsResp,
                        history = historyResp
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Gagal memuat hasil backtest"
                    )
                }
            }
        }
    }
}
