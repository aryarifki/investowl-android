package me.cugarete.investowl.core.common

import androidx.compose.ui.graphics.Color
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

/**
 * Porting 1:1 dari investowl/frontend/lib/format.ts.
 * Menghasilkan representasi string dan warna sesuai WCAG AA.
 */
object Format {
    val ColorPositive = Color(0xFF166534)
    val ColorNegative = Color(0xFF991B1B)
    val ColorPositiveDark = Color(0xFF10B981)
    val ColorNegativeDark = Color(0xFFF43F5E)
    val ColorOutline = Color(0xFF84746A)
    val ColorOnSurfaceVariant = Color(0xFF4E453F)

    private val idSymbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    private val idFormat = DecimalFormat("#,##0", idSymbols)

    fun fmtRp(n: Double?): String {
        if (n == null || n.isNaN()) return "-"
        val sign = if (n < 0) "-" else ""
        val v = abs(n)
        return when {
            v >= 1e12 -> String.format(Locale.US, "%sRp %.2f T", sign, v / 1e12)
            v >= 1e9 -> String.format(Locale.US, "%sRp %.2f B", sign, v / 1e9)
            v >= 1e6 -> String.format(Locale.US, "%sRp %.2f M", sign, v / 1e6)
            else -> String.format(Locale.US, "%sRp %s", sign, idFormat.format(v.toLong()))
        }
    }

    fun fmtRadarFlow(n: Double?): String {
        if (n == null || n.isNaN()) return "-"
        val sign = if (n < 0) "-" else ""
        val v = abs(n)
        return if (v >= 1e12) {
            String.format(Locale.US, "%sRp %.2f T", sign, v / 1e12)
        } else {
            String.format(Locale.US, "%sRp %.2f B", sign, v / 1e9)
        }
    }

    fun fmtPct(n: Double?, decimals: Int = 2): String {
        if (n == null || n.isNaN()) return "-"
        val sign = if (n > 0) "+" else ""
        val fmt = "%s%." + decimals + "f%%"
        return String.format(Locale.US, fmt, sign, n * 100.0)
    }

    fun fmtB(v: Double?): String {
        if (v == null || v.isNaN()) return "-"
        val s = String.format(Locale.US, "%.1f", abs(v) / 1e9)
        val sign = when {
            v > 0 -> "+"
            v < 0 -> "-"
            else -> ""
        }
        return "${sign}${s}B"
    }

    fun signedColor(n: Double?, isDark: Boolean = false): Color {
        if (n == null || n.isNaN()) return ColorOnSurfaceVariant
        val pos = if (isDark) ColorPositiveDark else ColorPositive
        val neg = if (isDark) ColorNegativeDark else ColorNegative
        return if (n >= 0) pos else neg
    }

    fun signalColor(score: Double?, isDark: Boolean = false): Color {
        if (score == null || score.isNaN()) return ColorOutline
        val pos = if (isDark) ColorPositiveDark else ColorPositive
        val neg = if (isDark) ColorNegativeDark else ColorNegative
        return when {
            score > 0 -> pos
            score < 0 -> neg
            else -> ColorOnSurfaceVariant
        }
    }

    fun getProfileTextColor(label: String, isDark: Boolean = false): Color {
        val l = label.lowercase()
        return when {
            l.contains("foreign smart") -> if (isDark) ColorPositiveDark else ColorPositive
            l.contains("local inst") -> Color(0xFF38BDF8) // Light Blue / Sky
            l.contains("market maker") -> Color(0xFFA855F7) // Purple
            l.contains("speculative") -> Color(0xFFF59E0B) // Amber
            else -> ColorOnSurfaceVariant
        }
    }
}
