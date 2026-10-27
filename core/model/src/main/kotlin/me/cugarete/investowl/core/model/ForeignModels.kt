package me.cugarete.investowl.core.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class MarketState {
    NORMAL,
    WARNING,
    CRASH,
    BOTTOMING,
    RECOVERY_TESTING,
    CONFIRMED_BOTTOM
}

@Immutable
@Serializable
data class ExodusTimelineItem(
    val date: String,
    @SerialName("ihsg_close")
    val ihsgClose: Double = 0.0,
    @SerialName("daily_flow")
    val dailyFlow: Double = 0.0,
    @SerialName("cumulative_flow")
    val cumulativeFlow: Double = 0.0
)

@Immutable
@Serializable
data class CulpritBroker(
    @SerialName("broker_code")
    val brokerCode: String,
    @SerialName("broker_name")
    val brokerName: String,
    @SerialName("net_sell_idr")
    val netSellIdr: Double = 0.0
)

@Immutable
@Serializable
data class ForeignExodusRadarResponse(
    @SerialName("market_state")
    val marketState: String,
    @SerialName("ihsg_current")
    val ihsgCurrent: Double? = null,
    @SerialName("ihsg_change_pct")
    val ihsgChangePct: Double? = null,
    val timeline: List<ExodusTimelineItem> = emptyList(),
    @SerialName("culprit_brokers")
    val culpritBrokers: List<CulpritBroker> = emptyList()
)

@Immutable
@Serializable
data class SectorSummaryItem(
    val sector: String,
    @SerialName("rs_ratio")
    val rsRatio: Double = 100.0,
    @SerialName("rs_momentum")
    val rsMomentum: Double = 100.0,
    @SerialName("foreign_net_idr")
    val foreignNetIdr: Double = 0.0,
    val quadrant: String = "Leading"
)

@Immutable
@Serializable
data class SectorRotationSummary(
    val asOf: String? = null,
    val sectors: List<SectorSummaryItem> = emptyList()
)

@Immutable
@Serializable
data class ForeignFlowFeatures(
    @SerialName("foreign_hhi")
    val foreignHhi: Double? = null,
    @SerialName("foreign_zscore")
    val foreignZscore: Double? = null
)

@Immutable
@Serializable
data class ForeignFlowTimeseries(
    val dates: List<String> = emptyList(),
    @SerialName("close_prices")
    val closePrices: List<Double> = emptyList(),
    @SerialName("foreign_net")
    val foreignNet: List<Double> = emptyList()
)

@Immutable
@Serializable
data class HmmRegime(
    val regime: Int,
    val label: String,
    val probability: Double
)

@Immutable
@Serializable
data class ForeignFlowModels(
    val hmm: List<HmmRegime> = emptyList()
)

@Immutable
@Serializable
data class ForeignFlowDetailResponse(
    val ticker: String,
    val company: String? = null,
    @SerialName("lookback_days")
    val lookbackDays: Int = 60,
    @SerialName("latest_date")
    val latestDate: String? = null,
    val features: ForeignFlowFeatures = ForeignFlowFeatures(),
    val timeseries: ForeignFlowTimeseries = ForeignFlowTimeseries(),
    val models: ForeignFlowModels = ForeignFlowModels()
)
