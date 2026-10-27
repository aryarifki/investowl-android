package me.cugarete.investowl.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {

    @Test
    fun testFmtRpTrillion() {
        assertEquals("Rp 1.50 T", Format.fmtRp(1.5e12))
        assertEquals("-Rp 2.30 T", Format.fmtRp(-2.3e12))
    }

    @Test
    fun testFmtRpBillion() {
        assertEquals("Rp 45.60 B", Format.fmtRp(4.56e10))
        assertEquals("-Rp 1.20 B", Format.fmtRp(-1.2e9))
    }

    @Test
    fun testFmtRpMillion() {
        assertEquals("Rp 500.00 M", Format.fmtRp(5e8))
        assertEquals("-Rp 15.50 M", Format.fmtRp(-1.55e7))
    }

    @Test
    fun testFmtRpThousands() {
        assertEquals("Rp 12.345", Format.fmtRp(12345.0))
        assertEquals("-Rp 5.000", Format.fmtRp(-5000.0))
    }

    @Test
    fun testFmtRpNullAndNaN() {
        assertEquals("-", Format.fmtRp(null))
        assertEquals("-", Format.fmtRp(Double.NaN))
    }

    @Test
    fun testFmtPct() {
        assertEquals("+5.25%", Format.fmtPct(0.0525))
        assertEquals("-2.50%", Format.fmtPct(-0.025))
        assertEquals("0.00%", Format.fmtPct(0.0))
        assertEquals("-", Format.fmtPct(null))
        assertEquals("-", Format.fmtPct(Double.NaN))
    }

    @Test
    fun testFmtB() {
        assertEquals("+4.5B", Format.fmtB(4.5e9))
        assertEquals("-1.2B", Format.fmtB(-1.2e9))
        assertEquals("0.0B", Format.fmtB(0.0))
        assertEquals("-", Format.fmtB(null))
    }

    @Test
    fun testSignedColor() {
        assertEquals(Format.ColorPositive, Format.signedColor(100.0, isDark = false))
        assertEquals(Format.ColorNegative, Format.signedColor(-50.0, isDark = false))
        assertEquals(Format.ColorPositiveDark, Format.signedColor(100.0, isDark = true))
        assertEquals(Format.ColorNegativeDark, Format.signedColor(-50.0, isDark = true))
    }

    @Test
    fun testSignalColor() {
        assertEquals(Format.ColorPositive, Format.signalColor(1.0, isDark = false))
        assertEquals(Format.ColorNegative, Format.signalColor(-1.0, isDark = false))
        assertEquals(Format.ColorOnSurfaceVariant, Format.signalColor(0.0, isDark = false))
        assertEquals(Format.ColorOutline, Format.signalColor(null, isDark = false))
    }

    @Test
    fun testProfileTextColor() {
        assertEquals(Format.ColorPositive, Format.getProfileTextColor("Foreign Smart Money", isDark = false))
        assertEquals(Format.ColorPositiveDark, Format.getProfileTextColor("foreign smart", isDark = true))
    }
}
