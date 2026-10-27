package me.cugarete.investowl.feature.foreign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.cugarete.investowl.core.model.ForeignExodusRadarResponse
import me.cugarete.investowl.core.model.SectorRotationSummary
import me.cugarete.investowl.core.network.InvestowlApi

data class ForeignUiState(
    val isLoading: Boolean = false,
    val selectedDays: Int = 45,
    val exodusRadar: ForeignExodusRadarResponse? = null,
    val sectorSummary: SectorRotationSummary? = null,
    val errorMessage: String? = null
)

class ForeignViewModel(
    private val api: InvestowlApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForeignUiState(isLoading = true))
    val uiState: StateFlow<ForeignUiState> = _uiState.asStateFlow()

    init {
        loadData(uiState.value.selectedDays)
    }

    fun selectDays(days: Int) {
        _uiState.update { it.copy(selectedDays = days) }
        loadData(days)
    }

    fun loadData(days: Int = _uiState.value.selectedDays) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val radar = api.getForeignExodusRadar(days = days)
                val sectors = try { api.getSectorsSummary() } catch (e: Exception) { null }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        exodusRadar = radar,
                        sectorSummary = sectors
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Gagal memuat radar arus modal asing"
                    )
                }
            }
        }
    }
}
