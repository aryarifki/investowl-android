package me.cugarete.investowl.core.network

import me.cugarete.investowl.core.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkSerializationTest {

    @Test
    fun testParsePriceHistoryResponse() {
        val jsonStr = """
            {
                "ticker": "BBCA",
                "count": 2,
                "sort_order": "asc",
                "data": [
                    {
                        "date": "2026-10-23",
                        "ticker": "BBCA",
                        "open": 10500.0,
                        "high": 10650.0,
                        "low": 10450.0,
                        "close": 10600.0,
                        "volume": 75000000.0,
                        "value": 795000000000.0,
                        "foreign_buy": 45000000.0,
                        "foreign_sell": 30000000.0
                    }
                ]
            }
        """.trimIndent()

        val resp = NetworkClient.json.decodeFromString<PriceHistoryResponse>(jsonStr)
        assertEquals("BBCA", resp.ticker)
        assertEquals(2, resp.count)
        assertEquals(1, resp.data.size)
        assertEquals(10600.0, resp.data[0].close)
    }

    @Test
    fun testParseDailySignalsResponseWithHonestNull() {
        val jsonStr = """
            {
                "latest_date": "2026-10-24",
                "count": 1,
                "data": [
                    {
                        "ticker": "BBRI",
                        "date": "2026-10-24",
                        "ml_label": "ACCUMULATION",
                        "ml_win_prob": null,
                        "composite_score": 85.0,
                        "scores": {
                            "technical": 25.0,
                            "smart_money": 30.0,
                            "sector": 15.0,
                            "fundamental": 15.0
                        },
                        "wyckoff_phase": "Phase C - Spring",
                        "wyckoff_name": "Accumulation",
                        "smart_money_notes": "Heavy foreign net buy across 5 days",
                        "sector_notes": "Financials leading relative strength",
                        "persistence_5d": 0.8,
                        "rr_ratio": 3.2,
                        "gate_notes": "Passed all liquidity and governance checks",
                        "kelly_alloc": 0.125
                    }
                ]
            }
        """.trimIndent()

        val resp = NetworkClient.json.decodeFromString<DailySignalsResponse>(jsonStr)
        assertEquals("2026-10-24", resp.latestDate)
        assertEquals(1, resp.count)
        val signal = resp.data[0]
        assertEquals("BBRI", signal.ticker)
        assertNull(signal.mlWinProb) // Honest Null check!
        assertEquals(85.0, signal.compositeScore, 0.001)
        assertEquals(0.125, signal.kellyAlloc)
    }

    @Test
    fun testParseForeignExodusRadarResponse() {
        val jsonStr = """
            {
                "market_state": "BOTTOMING",
                "ihsg_current": 7720.5,
                "ihsg_change_pct": 0.45,
                "timeline": [
                    {
                        "date": "2026-10-24",
                        "ihsg_close": 7720.5,
                        "daily_flow": 250000000000.0,
                        "cumulative_flow": -1500000000000.0
                    }
                ],
                "culprit_brokers": [
                    {
                        "broker_code": "ZP",
                        "broker_name": "Credit Suisse",
                        "net_sell_idr": -350000000000.0
                    }
                ]
            }
        """.trimIndent()

        val resp = NetworkClient.json.decodeFromString<ForeignExodusRadarResponse>(jsonStr)
        assertEquals("BOTTOMING", resp.marketState)
        assertEquals(7720.5, resp.ihsgCurrent)
        assertEquals(1, resp.timeline.size)
        assertEquals(1, resp.culpritBrokers.size)
        assertEquals("ZP", resp.culpritBrokers[0].brokerCode)
    }

    @Test
    fun testParseStagingOverviewResponse() {
        val jsonStr = """
            {
                "status": "ready",
                "schema": "ownership_staging",
                "ksei": {
                    "periods_count": 8,
                    "balancepos_items_count": 12500
                },
                "idx_bae": {
                    "reports_count": 1005
                }
            }
        """.trimIndent()

        val resp = NetworkClient.json.decodeFromString<StagingOverviewResponse>(jsonStr)
        assertEquals("ready", resp.status)
        assertEquals(8, resp.ksei.periodsCount)
        assertEquals(1005, resp.idxBae.reportsCount)
    }

    @Test
    fun testParseStockOwnershipResponse() {
        val jsonStr = """
            {
                "ticker": "BBCA",
                "summary": {
                    "controlling_pct": 54.94,
                    "public_pct": 45.06,
                    "treasury_pct": 0.0,
                    "substantial_pct": 54.94
                },
                "composition": {
                    "basis": "monthly_ownership_metrics",
                    "pct_asing": 41.5,
                    "pct_lokal": 58.5,
                    "pct_ritel": 15.2
                },
                "profile": {
                    "shares_outstanding": 123275050000.0,
                    "market_cap": 1250000000000000.0
                },
                "holders": [
                    {
                        "name": "PT DWIMURIA INVESTAMA ANDALAN",
                        "pct": 54.94,
                        "shares": 67729700000.0,
                        "classification": "Corporate",
                        "is_controlling": true,
                        "change_pct": 0.0
                    }
                ]
            }
        """.trimIndent()

        val resp = NetworkClient.json.decodeFromString<StockOwnershipResponse>(jsonStr)
        assertEquals("BBCA", resp.ticker)
        assertEquals(54.94, resp.summary.controllingPct, 0.001)
        assertTrue(resp.holders[0].isControlling)
    }
}
