package me.cugarete.investowl.feature.whales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.cugarete.investowl.core.model.BrokerWhaleListItem
import me.cugarete.investowl.core.model.BrokerWhalesDetailResponse
import me.cugarete.investowl.core.network.InvestowlApi

data class WhalesUiState(
    val isLoading: Boolean = false,
    val whales: List<BrokerWhaleListItem> = emptyList(),
    val filteredWhales: List<BrokerWhaleListItem> = emptyList(),
    val searchQuery: String = "",
    val selectedBrokerCode: String? = null,
    val selectedBrokerDetail: BrokerWhalesDetailResponse? = null,
    val isDetailLoading: Boolean = false,
    val errorMessage: String? = null
)

class WhalesViewModel(
    private val api: InvestowlApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(WhalesUiState(isLoading = true))
    val uiState: StateFlow<WhalesUiState> = _uiState.asStateFlow()

    init {
        loadWhales()
    }

    fun loadWhales() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val list = api.getWhales()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        whales = list,
                        filteredWhales = list
                    )
                }
                if (list.isNotEmpty()) {
                    selectBroker(list.first().brokerCode)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Gagal memuat daftar broker whales") }
            }
        }
    }

    fun onSearchQueryChanged(q: String) {
        _uiState.update { state ->
            val filtered = if (q.isBlank()) {
                state.whales
            } else {
                state.whales.filter {
                    it.brokerCode.contains(q.trim(), ignoreCase = true) ||
                            it.profileLabel.contains(q.trim(), ignoreCase = true)
                }
            }
            state.copy(searchQuery = q, filteredWhales = filtered)
        }
    }

    fun selectBroker(code: String) {
        _uiState.update { it.copy(selectedBrokerCode = code, isDetailLoading = true) }
        viewModelScope.launch {
            try {
                val detail = api.getWhaleDetail(code, days = 20, limit = 20)
                _uiState.update { it.copy(selectedBrokerDetail = detail, isDetailLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isDetailLoading = false) }
            }
        }
    }
}
