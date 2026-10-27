package me.cugarete.investowl.feature.signal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.cugarete.investowl.core.model.ActiveSignalItem
import me.cugarete.investowl.core.model.DailySignalItem
import me.cugarete.investowl.core.network.InvestowlApi

data class SignalUiState(
    val isLoading: Boolean = false,
    val selectedTab: Int = 0, // 0: Daily AI Signals, 1: Active Episodes
    val latestDate: String = "",
    val dailySignals: List<DailySignalItem> = emptyList(),
    val activeSignals: List<ActiveSignalItem> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class SignalViewModel(
    private val api: InvestowlApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignalUiState(isLoading = true))
    val uiState: StateFlow<SignalUiState> = _uiState.asStateFlow()

    init {
        loadSignals()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun refreshSignals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                api.refreshSignals()
                loadSignals()
            } catch (e: Exception) {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun loadSignals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val daily = api.getDailySignals(limit = 100)
                val active = try { api.getActiveSignals(limit = 50) } catch (e: Exception) { null }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        latestDate = daily.latestDate,
                        dailySignals = daily.data,
                        activeSignals = active?.data ?: emptyList()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.message ?: "Gagal memuat sinyal AI"
                    )
                }
            }
        }
    }
}
