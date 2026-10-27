package me.cugarete.investowl.core.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class CompanyMeta(
    val name: String = "",
    val sector: String = "",
    val group: String = ""
)

@Immutable
@Serializable
data class BrokerSummaryRow(
    val side: String,
    val broker: String,
    val type: String = "",
    val net: Double,
    val spark: List<Double> = emptyList(),
    @SerialName("is_affiliated")
    val isAffiliated: Boolean = false,
    @SerialName("affiliation_note")
    val affiliationNote: String? = null
)

@Immutable
@Serializable
data class AffiliatedBroker(
    val broker: String,
    val note: String,
    val role: String = "controller"
)

@Immutable
@Serializable
data class PricePerformanceRow(
    val period: String,
    val value: Double
)

@Immutable
@Serializable
data class ProfileFlowRow(
    val label: String,
    val net: Double
)

@Immutable
@Serializable
data class SmartDailyRow(
    val date: String,
    @SerialName("smart_net")
    val smartNet: Double,
    @SerialName("cumulative_net")
    val cumulativeNet: Double
)

@Immutable
@Serializable
data class PriceChartRow(
    val date: String,
    val price: Double
)

@Immutable
@Serializable
data class RetailSummary(
    val sentiment: String = "",
    val description: String = "",
    val tone: String = "neutral",
    @SerialName("retail_cum_net")
    val retailCumNet: Double = 0.0,
    @SerialName("smart_cum_net")
    val smartCumNet: Double = 0.0
)

@Immutable
@Serializable
data class TickerDetailResponse(
    val ticker: String,
    val company: CompanyMeta = CompanyMeta(),
    @SerialName("analysis_date")
    val analysisDate: String? = null,
    @SerialName("window_start")
    val windowStart: String? = null,
    @SerialName("window_days")
    val windowDays: Int = 20,
    val close: Double? = null,
    @SerialName("ret_5d")
    val ret5d: Double? = null,
    @SerialName("ret_10d")
    val ret10d: Double? = null,
    val signal: String? = null,
    @SerialName("signal_score")
    val signalScore: Double = 0.0,
    @SerialName("foreign_5d")
    val foreign5d: Double = 0.0,
    @SerialName("conviction_score")
    val convictionScore: Double = 0.0,
    val verdict: String = "",
    val alerts: List<String> = emptyList(),
    @SerialName("broker_summary")
    val brokerSummary: List<BrokerSummaryRow> = emptyList(),
    @SerialName("affiliated_brokers_detected")
    val affiliatedBrokersDetected: List<AffiliatedBroker> = emptyList(),
    @SerialName("price_performance")
    val pricePerformance: List<PricePerformanceRow> = emptyList(),
    @SerialName("profile_flow")
    val profileFlow: List<ProfileFlowRow> = emptyList(),
    @SerialName("smart_daily")
    val smartDaily: List<SmartDailyRow> = emptyList(),
    @SerialName("price_chart")
    val priceChart: List<PriceChartRow> = emptyList(),
    @SerialName("bandar_vwap")
    val bandarVwap: Double? = null,
    @SerialName("bandar_pnl_pct")
    val bandarPnlPct: Double? = null,
    @SerialName("retail_summary")
    val retailSummary: RetailSummary? = null
)
