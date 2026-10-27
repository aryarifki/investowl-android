package me.cugarete.investowl.core.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class UserRead(
    val id: Int,
    val email: String,
    val name: String? = null,
    val image: String? = null,
    val provider: String? = null,
    val role: String = "user",
    @SerialName("is_approved")
    val isApproved: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Immutable
@Serializable
data class UserSyncRequest(
    val email: String,
    val name: String? = null,
    val image: String? = null,
    val provider: String = "google"
)

@Immutable
@Serializable
data class UserApprovalRequest(
    @SerialName("is_approved")
    val isApproved: Boolean
)

@Immutable
@Serializable
data class UserWatchlistResponse(
    val watchlist: List<String> = emptyList()
)

@Immutable
@Serializable
data class UserWatchlistUpdate(
    val watchlist: List<String>
)

@Immutable
@Serializable
data class AuthGateStatus(
    @SerialName("require_login_full_feature")
    val requireLoginFullFeature: Boolean
)

@Immutable
@Serializable
data class AuthGateUpdateRequest(
    @SerialName("require_login_full_feature")
    val requireLoginFullFeature: Boolean,
    @SerialName("admin_email")
    val adminEmail: String? = null
)

enum class AuthState {
    GUEST,
    PENDING_APPROVAL,
    ACTIVE_USER,
    ADMIN
}
