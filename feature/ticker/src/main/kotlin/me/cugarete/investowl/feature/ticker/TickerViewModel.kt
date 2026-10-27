package me.cugarete.investowl.feature.ticker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.cugarete.investowl.core.database.WatchlistDao
import me.cugarete.investowl.core.database.WatchlistEntity
import me.cugarete.investowl.core.model.ForeignFlowDetailResponse
import me.cugarete.investowl.core.model.PriceHistoryResponse
import me.cugarete.investowl.core.model.StockOwnershipResponse
import me.cugarete.investowl.core.model.TickerDetailResponse
import me.cugarete.investowl.core.network.InvestowlApi

data class TickerUiState(
    val ticker: String,
    val isLoading: Boolean = false,
    val selectedTab: Int = 0,
    val isWatchlisted: Boolean = false,
    val bandarDetail: TickerDetailResponse? = null,
    val priceHistory: PriceHistoryResponse? = null,
    val foreignFlow: ForeignFlowDetailResponse? = null,
    val ownership: StockOwnershipResponse? = null,
    val errorMessage: String? = null
)

class TickerViewModel(
    val ticker: String,
    private val api: InvestowlApi,
    private val watchlistDao: WatchlistDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(TickerUiState(ticker = ticker, isLoading = true))
    val uiState: StateFlow<TickerUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeWatchlistStatus()
    }

    private fun observeWatchlistStatus() {
        viewModelScope.launch {
            watchlistDao.isWatchlisted(ticker).collect { watchlisted ->
                _uiState.update { it.copy(isWatchlisted = watchlisted) }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun toggleWatchlist() {
        viewModelScope.launch {
            if (_uiState.value.isWatchlisted) {
                watchlistDao.delete(ticker)
            } else {
                watchlistDao.insert(WatchlistEntity(ticker = ticker))
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val detail = api.getTickerDetail(ticker = ticker)
                val price = try { api.getStockHistory(ticker = ticker, limit = 100) } catch (e: Exception) { null }
                val foreign = try { api.getForeignFlowDetail(ticker = ticker) } catch (e: Exception) { null }
                val own = try { api.getStockOwnership(ticker = ticker) } catch (e: Exception) { null }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        bandarDetail = detail,
                        priceHistory = price,
                        foreignFlow = foreign,
                        ownership = own
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Gagal memuat detail emiten $ticker"
                    )
                }
            }
        }
    }
}
