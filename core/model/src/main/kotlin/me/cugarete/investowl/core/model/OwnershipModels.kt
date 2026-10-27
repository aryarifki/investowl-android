package me.cugarete.investowl.core.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class OwnershipHolder(
    val name: String,
    val pct: Double,
    val shares: Double? = null,
    val classification: String? = null,
    @SerialName("is_controlling")
    val isControlling: Boolean = false,
    @SerialName("change_pct")
    val changePct: Double? = null
)

@Immutable
@Serializable
data class OwnershipSummary(
    @SerialName("controlling_pct")
    val controllingPct: Double = 0.0,
    @SerialName("public_pct")
    val publicPct: Double = 0.0,
    @SerialName("treasury_pct")
    val treasuryPct: Double = 0.0,
    @SerialName("substantial_pct")
    val substantialPct: Double = 0.0
)

@Immutable
@Serializable
data class OwnershipComposition(
    val basis: String = "monthly_ownership_metrics",
    @SerialName("pct_asing")
    val pctAsing: Double = 0.0,
    @SerialName("pct_lokal")
    val pctLokal: Double = 0.0,
    @SerialName("pct_ritel")
    val pctRitel: Double = 0.0
)

@Immutable
@Serializable
data class OwnershipProfile(
    @SerialName("shares_outstanding")
    val sharesOutstanding: Double? = null,
    @SerialName("market_cap")
    val marketCap: Double? = null
)

@Immutable
@Serializable
data class StockOwnershipResponse(
    val ticker: String,
    val summary: OwnershipSummary = OwnershipSummary(),
    val composition: OwnershipComposition = OwnershipComposition(),
    val profile: OwnershipProfile = OwnershipProfile(),
    val holders: List<OwnershipHolder> = emptyList()
)

@Immutable
@Serializable
data class KseiOverview(
    @SerialName("periods_count")
    val periodsCount: Int = 0,
    @SerialName("balancepos_items_count")
    val balanceposItemsCount: Int = 0
)

@Immutable
@Serializable
data class IdxBaeOverview(
    @SerialName("reports_count")
    val reportsCount: Int = 0
)

@Immutable
@Serializable
data class StagingOverviewResponse(
    val status: String,
    val schema: String,
    val ksei: KseiOverview = KseiOverview(),
    @SerialName("idx_bae")
    val idxBae: IdxBaeOverview = IdxBaeOverview()
)

@Immutable
@Serializable
data class CompositionHistoryItem(
    val period: String? = null,
    @SerialName("total_shares")
    val totalShares: Double? = null,
    @SerialName("pct_asing")
    val pctAsing: Double? = null,
    @SerialName("pct_ritel_pure")
    val pctRitelPure: Double? = null,
    @SerialName("local_breakdown")
    val localBreakdown: Map<String, Double> = emptyMap(),
    @SerialName("foreign_breakdown")
    val foreignBreakdown: Map<String, Double> = emptyMap()
)

@Immutable
@Serializable
data class SidHistoryItem(
    val period: String? = null,
    @SerialName("total_shareholders")
    val totalShareholders: Long = 0,
    @SerialName("monthly_change")
    val monthlyChange: Long? = null
)

@Immutable
@Serializable
data class StagingStockProfileResponse(
    val ticker: String,
    @SerialName("composition_history")
    val compositionHistory: List<CompositionHistoryItem> = emptyList(),
    @SerialName("sid_history")
    val sidHistory: List<SidHistoryItem> = emptyList()
)

@Immutable
@Serializable
data class ParityHistoryItem(
    val period: String,
    @SerialName("retail_diff_pp")
    val retailDiffPp: Double = 0.0,
    @SerialName("foreign_diff_pp")
    val foreignDiffPp: Double = 0.0
)

@Immutable
@Serializable
data class StagingParityResponse(
    val ticker: String,
    @SerialName("months_compared")
    val monthsCompared: Int = 0,
    @SerialName("parity_status")
    val parityStatus: String = "ACCEPTABLE",
    val history: List<ParityHistoryItem> = emptyList()
)

@Immutable
@Serializable
data class InvestorSearchItem(
    val name: String,
    @SerialName("ticker_count")
    val tickerCount: Int,
    @SerialName("total_value_idr")
    val totalValueIdr: Double? = null
)

@Immutable
@Serializable
data class InvestorSearchResponse(
    val count: Int,
    val results: List<InvestorSearchItem> = emptyList()
)

@Immutable
@Serializable
data class InvestorHoldingItem(
    val ticker: String,
    @SerialName("company_name")
    val companyName: String = "",
    val pct: Double = 0.0,
    val shares: Double = 0.0,
    @SerialName("value_idr")
    val valueIdr: Double = 0.0
)

@Immutable
@Serializable
data class InvestorDetailSummary(
    @SerialName("ticker_count")
    val tickerCount: Int = 0,
    @SerialName("total_portfolio_value")
    val totalPortfolioValue: Double = 0.0
)

@Immutable
@Serializable
data class InvestorDetailResponse(
    val name: String,
    val summary: InvestorDetailSummary = InvestorDetailSummary(),
    val holdings: List<InvestorHoldingItem> = emptyList()
)
