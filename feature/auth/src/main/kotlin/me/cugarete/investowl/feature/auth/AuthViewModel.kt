package me.cugarete.investowl.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.cugarete.investowl.core.database.SessionManager
import me.cugarete.investowl.core.database.WatchlistDao
import me.cugarete.investowl.core.database.WatchlistEntity
import me.cugarete.investowl.core.model.AuthState
import me.cugarete.investowl.core.model.UserRead
import me.cugarete.investowl.core.model.UserSyncRequest
import me.cugarete.investowl.core.model.UserWatchlistUpdate
import me.cugarete.investowl.core.network.InvestowlApi

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val api: InvestowlApi,
    private val sessionManager: SessionManager,
    private val watchlistDao: WatchlistDao
) : ViewModel() {

    val authState: StateFlow<AuthState> = sessionManager.authStateFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuthState.GUEST
    )

    val currentUser: StateFlow<UserRead?> = sessionManager.userFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signInWithGoogleToken(email: String, name: String?, image: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val user = api.syncUser(
                    UserSyncRequest(
                        email = email,
                        name = name,
                        image = image,
                        provider = "google"
                    )
                )
                sessionManager.saveSession(user)
                syncWatchlistWithBackend(user.email)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Gagal autentikasi Google") }
            }
        }
    }

    private suspend fun syncWatchlistWithBackend(email: String) {
        try {
            val remote = api.getWatchlist(email)
            val local = watchlistDao.getWatchlistTickersList()

            // Merge local and remote watchlists
            val merged = (remote.watchlist + local).distinct()
            if (merged.isNotEmpty()) {
                watchlistDao.clear()
                watchlistDao.insertAll(merged.map { WatchlistEntity(ticker = it) })
                api.updateWatchlist(email, UserWatchlistUpdate(watchlist = merged))
            }
        } catch (e: Exception) {
            // Ignore background sync errors gracefully
        }
    }

    fun signOut() {
        viewModelScope.launch {
            sessionManager.clearSession()
        }
    }
}
