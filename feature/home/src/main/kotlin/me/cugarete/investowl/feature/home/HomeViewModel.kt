package me.cugarete.investowl.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.cugarete.investowl.core.database.WatchlistDao
import me.cugarete.investowl.core.model.DailySignalItem
import me.cugarete.investowl.core.network.InvestowlApi

data class HomeUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val allTickers: List<String> = emptyList(),
    val filteredTickers: List<String> = emptyList(),
    val watchlistTickers: List<String> = emptyList(),
    val topSignals: List<DailySignalItem> = emptyList(),
    val ihsgClose: Double? = null,
    val ihsgChangePct: Double? = null,
    val marketState: String = "NORMAL",
    val errorMessage: String? = null
)

class HomeViewModel(
    private val api: InvestowlApi,
    private val watchlistDao: WatchlistDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
        observeWatchlist()
    }

    private fun observeWatchlist() {
        viewModelScope.launch {
            watchlistDao.getWatchlistTickers().collect { watchlist ->
                _uiState.update { it.copy(watchlistTickers = watchlist) }
            }
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val universeDeferred = api.getUniverse()
                val exodusDeferred = try { api.getForeignExodusRadar(days = 15) } catch (e: Exception) { null }
                val signalsDeferred = try { api.getDailySignals(limit = 10) } catch (e: Exception) { null }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        allTickers = universeDeferred.tickers,
                        topSignals = signalsDeferred?.data?.take(5) ?: emptyList(),
                        ihsgClose = exodusDeferred?.ihsgCurrent,
                        ihsgChangePct = exodusDeferred?.ihsgChangePct,
                        marketState = exodusDeferred?.marketState ?: "NORMAL"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Gagal memuat data pasar") }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                emptyList()
            } else {
                state.allTickers.filter { it.contains(query.trim(), ignoreCase = true) }.take(10)
            }
            state.copy(searchQuery = query, filteredTickers = filtered)
        }
    }
}
