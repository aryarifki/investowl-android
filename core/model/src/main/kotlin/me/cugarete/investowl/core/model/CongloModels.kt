package me.cugarete.investowl.core.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class CongloConstituent(
    val ticker: String,
    @SerialName("company_name")
    val companyName: String = "",
    val weight: Double = 0.0,
    @SerialName("market_cap")
    val marketCap: Double = 0.0,
    @SerialName("mtd_return")
    val mtdReturn: Double = 0.0
)

@Immutable
@Serializable
data class CongloGroupItem(
    val id: String,
    val name: String,
    @SerialName("controller_name")
    val controllerName: String = "",
    @SerialName("ticker_count")
    val tickerCount: Int = 0,
    @SerialName("total_market_cap")
    val totalMarketCap: Double = 0.0,
    @SerialName("mtd_return")
    val mtdReturn: Double = 0.0
)

@Immutable
@Serializable
data class CongloGroupsResponse(
    val groups: List<CongloGroupItem> = emptyList(),
    @SerialName("top_groups")
    val topGroups: List<CongloGroupItem> = emptyList()
)

@Immutable
@Serializable
data class CongloGroupDetailResponse(
    val group: CongloGroupItem,
    val constituents: List<CongloConstituent> = emptyList()
)
