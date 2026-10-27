package me.cugarete.investowl.feature.ownership

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.cugarete.investowl.core.model.*
import me.cugarete.investowl.core.network.InvestowlApi

data class OwnershipUiState(
    val isLoading: Boolean = false,
    val selectedTab: Int = 0,
    val selectedTicker: String = "BBCA",
    val stagingOverview: StagingOverviewResponse? = null,
    val stockOwnership: StockOwnershipResponse? = null,
    val stagingProfile: StagingStockProfileResponse? = null,
    val parityCheck: StagingParityResponse? = null,
    val searchResults: List<InvestorSearchItem> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class OwnershipViewModel(
    private val api: InvestowlApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnershipUiState(isLoading = true))
    val uiState: StateFlow<OwnershipUiState> = _uiState.asStateFlow()

    init {
        loadData(uiState.value.selectedTicker)
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun selectTicker(ticker: String) {
        _uiState.update { it.copy(selectedTicker = ticker) }
        loadData(ticker)
    }

    fun onSearchQueryChanged(q: String) {
        _uiState.update { it.copy(searchQuery = q) }
        if (q.length >= 3) {
            viewModelScope.launch {
                try {
                    val resp = api.searchInvestors(q)
                    _uiState.update { it.copy(searchResults = resp.results) }
                } catch (e: Exception) {
                    // Ignore search error
                }
            }
        }
    }

    fun loadData(ticker: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val overview = try { api.getStagingOverview() } catch (e: Exception) { null }
                val ownership = try { api.getStockOwnership(ticker) } catch (e: Exception) { null }
                val stagingStock = try { api.getStagingStockProfile(ticker) } catch (e: Exception) { null }
                val parity = try { api.getStagingParity(ticker) } catch (e: Exception) { null }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        stagingOverview = overview,
                        stockOwnership = ownership,
                        stagingProfile = stagingStock,
                        parityCheck = parity
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Gagal memuat data kepemilikan KSEI / IDX BAE"
                    )
                }
            }
        }
    }
}
