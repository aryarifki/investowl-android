package me.cugarete.investowl.core.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class PriceRow(
    val date: String,
    val ticker: String,
    val open: Double? = null,
    val high: Double? = null,
    val low: Double? = null,
    val close: Double? = null,
    val volume: Double? = null,
    val value: Double? = null,
    @SerialName("foreign_buy")
    val foreignBuy: Double? = null,
    @SerialName("foreign_sell")
    val foreignSell: Double? = null
)

@Immutable
@Serializable
data class PriceHistoryResponse(
    val ticker: String,
    val count: Int,
    val data: List<PriceRow> = emptyList(),
    @SerialName("sort_order")
    val sortOrder: String = "asc"
)

@Immutable
@Serializable
data class TickerUniverseResponse(
    val tickers: List<String> = emptyList(),
    val mode: String? = null,
    val count: Int? = null
)
