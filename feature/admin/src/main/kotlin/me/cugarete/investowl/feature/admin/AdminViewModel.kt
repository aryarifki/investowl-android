package me.cugarete.investowl.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.cugarete.investowl.core.model.AuthGateUpdateRequest
import me.cugarete.investowl.core.model.JobConfigItem
import me.cugarete.investowl.core.model.JobRunRequest
import me.cugarete.investowl.core.model.UserApprovalRequest
import me.cugarete.investowl.core.model.UserRead
import me.cugarete.investowl.core.network.InvestowlApi

data class AdminUiState(
    val isLoading: Boolean = false,
    val selectedTab: Int = 0, // 0: User Approvals, 1: 9-Job Control, 2: Log Viewer
    val users: List<UserRead> = emptyList(),
    val jobs: Map<String, JobConfigItem> = emptyMap(),
    val authGateEnabled: Boolean = true,
    val selectedLogFile: String = "cron_daily.log",
    val logContent: String = "",
    val actionMessage: String? = null,
    val errorMessage: String? = null
)

class AdminViewModel(
    private val api: InvestowlApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState(isLoading = true))
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        if (index == 2) {
            fetchLogContent(_uiState.value.selectedLogFile)
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val usersList = api.getUsers()
                val jobsResp = try { api.getJobsStatus().jobs } catch (e: Exception) { emptyMap() }
                val gateResp = try { api.getAuthGate().requireLoginFullFeature } catch (e: Exception) { true }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        users = usersList,
                        jobs = jobsResp,
                        authGateEnabled = gateResp
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Gagal memuat admin panel") }
            }
        }
    }

    fun toggleAuthGate() {
        viewModelScope.launch {
            try {
                val newState = !_uiState.value.authGateEnabled
                api.updateAuthGate(AuthGateUpdateRequest(requireLoginFullFeature = newState))
                _uiState.update { it.copy(authGateEnabled = newState, actionMessage = "Auth-gate berhasil diubah") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal mengubah auth-gate: ${e.message}") }
            }
        }
    }

    fun setUserApproval(userId: Int, isApproved: Boolean) {
        viewModelScope.launch {
            try {
                api.approveUser(userId, UserApprovalRequest(isApproved = isApproved))
                loadData()
                _uiState.update { it.copy(actionMessage = "Status persetujuan user berhasil diperbarui") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal memperbarui status user: ${e.message}") }
            }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            try {
                api.deleteUser(userId)
                loadData()
                _uiState.update { it.copy(actionMessage = "User berhasil dihapus") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal menghapus user: ${e.message}") }
            }
        }
    }

    fun triggerJob(jobName: String) {
        viewModelScope.launch {
            try {
                api.runJob(jobName, JobRunRequest())
                _uiState.update { it.copy(actionMessage = "Job $jobName berhasil dipicu!") }
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal memicu job $jobName: ${e.message}") }
            }
        }
    }

    fun stopJob(jobName: String) {
        viewModelScope.launch {
            try {
                api.stopJob(jobName)
                _uiState.update { it.copy(actionMessage = "Job $jobName dihentikan") }
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal menghentikan job: ${e.message}") }
            }
        }
    }

    fun fetchLogContent(file: String) {
        _uiState.update { it.copy(selectedLogFile = file) }
        viewModelScope.launch {
            try {
                val resp = api.viewLog(file = file, lines = 200)
                _uiState.update { it.copy(logContent = resp.content) }
            } catch (e: Exception) {
                _uiState.update { it.copy(logContent = "Log tidak ditemukan atau belum ada eksekusi.") }
            }
        }
    }

    fun purgeCache() {
        viewModelScope.launch {
            try {
                api.purgeCache(mapOf("pattern" to "*"))
                _uiState.update { it.copy(actionMessage = "Seluruh cache Redis & backend berhasil dibersihkan!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal membersihkan cache: ${e.message}") }
            }
        }
    }
}
