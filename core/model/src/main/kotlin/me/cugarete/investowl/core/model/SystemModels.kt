package me.cugarete.investowl.core.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class JobConfigItem(
    val name: String,
    val label: String,
    val description: String,
    val category: String = "daily",
    @SerialName("is_running")
    val isRunning: Boolean = false,
    @SerialName("last_run")
    val lastRun: String? = null,
    @SerialName("last_status")
    val lastStatus: String? = null
)

@Immutable
@Serializable
data class JobsStatusResponse(
    val timestamp: String? = null,
    val jobs: Map<String, JobConfigItem> = emptyMap()
)

@Immutable
@Serializable
data class DailyLogsResponse(
    val timestamp: String,
    val logs: Map<String, String> = emptyMap()
)

@Immutable
@Serializable
data class JobRunRequest(
    val universe: String? = null,
    val days: Int? = 1,
    val force: Boolean = false
)

@Immutable
@Serializable
data class LogViewResponse(
    val filename: String = "",
    val content: String = "",
    val lines: Int = 0
)

@Immutable
@Serializable
data class CacheStatsResponse(
    @SerialName("redis_connected")
    val redisConnected: Boolean = true,
    @SerialName("keys_count")
    val keysCount: Int = 0
)
