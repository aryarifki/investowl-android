package me.cugarete.investowl.feature.konglo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.cugarete.investowl.core.model.CongloGroupDetailResponse
import me.cugarete.investowl.core.model.CongloGroupItem
import me.cugarete.investowl.core.network.InvestowlApi

data class KongloUiState(
    val isLoading: Boolean = false,
    val groups: List<CongloGroupItem> = emptyList(),
    val selectedGroup: CongloGroupDetailResponse? = null,
    val errorMessage: String? = null
)

class KongloViewModel(
    private val api: InvestowlApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(KongloUiState(isLoading = true))
    val uiState: StateFlow<KongloUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val resp = api.getCongloGroups()
                val groups = resp.groups
                _uiState.update { it.copy(isLoading = false, groups = groups) }
                if (groups.isNotEmpty()) {
                    selectGroup(groups.first().id)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Gagal memuat grup konglomerasi") }
            }
        }
    }

    fun selectGroup(groupId: String) {
        viewModelScope.launch {
            try {
                val detail = api.getCongloGroupDetail(groupId)
                _uiState.update { it.copy(selectedGroup = detail) }
            } catch (e: Exception) {
                // Keep existing selection if detail fails
            }
        }
    }
}
