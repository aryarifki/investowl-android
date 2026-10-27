package me.cugarete.investowl.core.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class SignalScores(
    val technical: Double = 0.0,
    @SerialName("smart_money")
    val smartMoney: Double = 0.0,
    val sector: Double = 0.0,
    val fundamental: Double = 0.0
)

@Immutable
@Serializable
data class DailySignalItem(
    val ticker: String,
    val date: String,
    @SerialName("ml_label")
    val mlLabel: String? = null,
    @SerialName("ml_win_prob")
    val mlWinProb: Double? = null,
    @SerialName("composite_score")
    val compositeScore: Double = 0.0,
    val scores: SignalScores = SignalScores(),
    @SerialName("wyckoff_phase")
    val wyckoffPhase: String = "-",
    @SerialName("wyckoff_name")
    val wyckoffName: String = "",
    @SerialName("smart_money_notes")
    val smartMoneyNotes: String = "",
    @SerialName("sector_notes")
    val sectorNotes: String = "",
    @SerialName("persistence_5d")
    val persistence5d: Double = 0.5,
    @SerialName("rr_ratio")
    val rrRatio: Double? = null,
    @SerialName("gate_notes")
    val gateNotes: String = "",
    @SerialName("kelly_alloc")
    val kellyAlloc: Double? = null,
    @SerialName("ai_pred_mae")
    val aiPredMae: Double? = null,
    @SerialName("ai_pred_mfe")
    val aiPredMfe: Double? = null
)

@Immutable
@Serializable
data class DailySignalsResponse(
    @SerialName("latest_date")
    val latestDate: String,
    val count: Int,
    val data: List<DailySignalItem> = emptyList()
)

@Immutable
@Serializable
data class ActiveSignalItem(
    val id: Int? = null,
    val ticker: String,
    @SerialName("company_name")
    val companyName: String = "",
    @SerialName("group_name")
    val groupName: String = "-",
    @SerialName("signal_date")
    val signalDate: String? = null,
    @SerialName("entry_price")
    val entryPrice: Double = 0.0,
    @SerialName("tp_price")
    val tpPrice: Double = 0.0,
    @SerialName("sl_price")
    val slPrice: Double = 0.0,
    @SerialName("target_gain_pct")
    val targetGainPct: Double = 0.0,
    @SerialName("stop_risk_pct")
    val stopRiskPct: Double = 0.0,
    @SerialName("risk_reward_ratio")
    val riskRewardRatio: Double = 0.0,
    val status: String = "OPEN",
    @SerialName("days_held")
    val daysHeld: Int = 0,
    @SerialName("confirmation_count")
    val confirmationCount: Int = 1,
    @SerialName("composite_score")
    val compositeScore: Int = 0,
    @SerialName("wyckoff_phase")
    val wyckoffPhase: String = "-",
    @SerialName("wyckoff_name")
    val wyckoffName: String = "",
    @SerialName("ml_win_prob")
    val mlWinProb: Double? = null,
    @SerialName("vcp_grade")
    val vcpGrade: String = "NONE",
    @SerialName("gate_notes")
    val gateNotes: String = ""
)

@Immutable
@Serializable
data class ActiveSignalsResponse(
    val count: Int,
    val data: List<ActiveSignalItem> = emptyList()
)

@Immutable
@Serializable
data class BacktestHistoryItem(
    val ticker: String,
    @SerialName("entry_date")
    val entryDate: String,
    @SerialName("exit_date")
    val exitDate: String? = null,
    @SerialName("entry_price")
    val entryPrice: Double,
    @SerialName("exit_price")
    val exitPrice: Double? = null,
    @SerialName("pnl_pct")
    val pnlPct: Double = 0.0,
    val outcome: String = "OPEN",
    @SerialName("holding_days")
    val holdingDays: Int = 0
)

@Immutable
@Serializable
data class BacktestStatsResponse(
    @SerialName("win_rate")
    val winRate: Double = 0.0,
    @SerialName("profit_factor")
    val profitFactor: Double = 0.0,
    @SerialName("total_trades")
    val totalTrades: Int = 0,
    @SerialName("winning_trades")
    val winningTrades: Int = 0,
    @SerialName("losing_trades")
    val losingTrades: Int = 0
)
