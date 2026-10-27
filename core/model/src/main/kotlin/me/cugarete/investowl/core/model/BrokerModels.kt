package me.cugarete.investowl.core.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class BrokerFlowRow(
    val ticker: String,
    val date: String,
    @SerialName("foreign_net_broker")
    val foreignNetBroker: Double? = null,
    @SerialName("local_net_broker")
    val localNetBroker: Double? = null,
    @SerialName("gov_net_broker")
    val govNetBroker: Double? = null,
    @SerialName("total_value")
    val totalValue: Double? = null,
    @SerialName("bandar_signal")
    val bandarSignal: String? = null,
    @SerialName("bandar_signal_score")
    val bandarSignalScore: Double? = null,
    @SerialName("foreign_signal")
    val foreignSignal: String? = null
)

@Immutable
@Serializable
data class BrokerFlowHistoryResponse(
    val ticker: String,
    val count: Int,
    val data: List<BrokerFlowRow> = emptyList(),
    @SerialName("sort_order")
    val sortOrder: String = "asc"
)

@Immutable
@Serializable
data class BrokerFlowSummary(
    val ticker: String,
    @SerialName("period_days")
    val periodDays: Int,
    @SerialName("trading_days")
    val tradingDays: Int,
    @SerialName("foreign_net_broker_sum")
    val foreignNetBrokerSum: Double,
    @SerialName("local_net_broker_sum")
    val localNetBrokerSum: Double,
    @SerialName("gov_net_broker_sum")
    val govNetBrokerSum: Double = 0.0,
    @SerialName("total_value_sum")
    val totalValueSum: Double = 0.0,
    @SerialName("foreign_dominance_pct")
    val foreignDominancePct: Double = 0.0,
    @SerialName("latest_bandar_signal")
    val latestBandarSignal: String? = null,
    @SerialName("latest_bandar_signal_score")
    val latestBandarSignalScore: Double? = null,
    @SerialName("latest_foreign_signal")
    val latestForeignSignal: String? = null,
    @SerialName("accumulation_days")
    val accumulationDays: Int = 0,
    @SerialName("distribution_days")
    val distributionDays: Int = 0,
    @SerialName("latest_date")
    val latestDate: String? = null
)

@Immutable
@Serializable
data class BrokerWhaleListItem(
    @SerialName("broker_code")
    val brokerCode: String,
    val profile: String,
    @SerialName("profile_label")
    val profileLabel: String,
    @SerialName("profile_desc")
    val profileDesc: String,
    @SerialName("is_smart")
    val isSmart: Boolean
)

@Immutable
@Serializable
data class WhalePositionItem(
    val ticker: String,
    @SerialName("company_name")
    val companyName: String? = null,
    @SerialName("net_buy_idr")
    val netBuyIdr: Double? = null,
    @SerialName("net_sell_idr")
    val netSellIdr: Double? = null,
    @SerialName("net_flow_idr")
    val netFlowIdr: Double = 0.0
)

@Immutable
@Serializable
data class BrokerWhalesDetailResponse(
    @SerialName("broker_code")
    val brokerCode: String,
    @SerialName("broker_name")
    val brokerName: String? = null,
    val profile: String? = null,
    @SerialName("is_smart")
    val isSmart: Boolean = false,
    @SerialName("top_accumulated")
    val topAccumulated: List<WhalePositionItem> = emptyList(),
    @SerialName("top_distributed")
    val topDistributed: List<WhalePositionItem> = emptyList()
)
