package me.cugarete.investowl.feature.ticker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.cugarete.investowl.core.common.Format
import me.cugarete.investowl.core.designsystem.*
import me.cugarete.investowl.core.model.BrokerSummaryRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TickerDetailScreen(
    viewModel: TickerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val detail = uiState.bandarDetail

    val tabs = listOf(
        "Ringkasan",
        "Smart Flow",
        "Broker & Governance",
        "Kausalitas",
        "Foreign Flow",
        "Tabel Mentah"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.ticker,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (detail?.company?.name?.isNotBlank() == true) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = detail.company.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleWatchlist() }) {
                        Icon(
                            imageVector = if (uiState.isWatchlisted) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Watchlist",
                            tint = if (uiState.isWatchlisted) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            FullScreenLoading("Memuat data mendalam ${uiState.ticker}...")
            return@Scaffold
        }

        if (uiState.errorMessage != null && detail == null) {
            ErrorMessage(
                message = uiState.errorMessage!!,
                onRetry = { viewModel.loadData() },
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Price & Signal Header Banner
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = detail?.close?.let { "Rp ${it.toInt()}" } ?: "-",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "5D: ${Format.fmtPct(detail?.ret5d)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Format.signedColor(detail?.ret5d)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "10D: ${Format.fmtPct(detail?.ret10d)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Format.signedColor(detail?.ret10d)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Format.signalColor(detail?.signalScore).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = detail?.signal ?: "NETRAL",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Format.signalColor(detail?.signalScore),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Conviction: ${detail?.convictionScore?.toInt() ?: 0}/100",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Scrollable 6 Dynamic Tabs
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (uiState.selectedTab) {
                0 -> SummaryTab(detail)
                1 -> SmartFlowTab(detail)
                2 -> BrokerSummaryTab(detail)
                3 -> CausalityTab(detail)
                4 -> ForeignFlowTab(uiState.foreignFlow)
                5 -> RawTablesTab(detail)
            }
        }
    }
}

@Composable
private fun SummaryTab(detail: me.cugarete.investowl.core.model.TickerDetailResponse?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Verdict Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Analisis & Verdict Institusi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = detail?.verdict ?: "Belum ada analisis untuk emiten ini.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Bandar VWAP & Retail Sentiment
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Bandar VWAP (Top Buyers)",
                    value = detail?.bandarVwap?.let { "Rp ${it.toInt()}" } ?: "-",
                    subtitle = detail?.bandarPnlPct?.let { "PnL Bandar: ${Format.fmtPct(it / 100.0)}" },
                    valueColor = Format.signedColor(detail?.bandarPnlPct),
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Foreign Flow 5D",
                    value = Format.fmtRp(detail?.foreign5d),
                    subtitle = "Arus modal asing 5 hari",
                    valueColor = Format.signedColor(detail?.foreign5d),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Retail Flow Summary
        item {
            val ret = detail?.retailSummary
            if (ret != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (ret.tone) {
                            "positive" -> MaterialTheme.colorScheme.primaryContainer
                            "negative" -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = ret.sentiment.ifEmpty { "Retail Sentiment Tracker" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ret.description.ifEmpty { "Analisis pergerakan investor ritel vs institusi." },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Contradiction Alerts
        if (detail?.alerts?.isNotEmpty() == true) {
            item {
                Text(
                    text = "Peringatan Anomali & Kontradiksi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            items(detail.alerts) { alert ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = "⚠ $alert",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SmartFlowTab(detail: me.cugarete.investowl.core.model.TickerDetailResponse?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Histori Harian Smart Money Net Flow",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        val flows = detail?.smartDaily ?: emptyList()
        if (flows.isEmpty()) {
            item {
                Text(
                    text = "Tidak ada rekaman smart flow harian.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(flows) { row ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = row.date, fontWeight = FontWeight.SemiBold)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Net: ${Format.fmtRp(row.smartNet)}",
                                color = Format.signedColor(row.smartNet),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Kumulatif: ${Format.fmtRp(row.cumulativeNet)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrokerSummaryTab(detail: me.cugarete.investowl.core.model.TickerDetailResponse?) {
    val rows = detail?.brokerSummary ?: emptyList()

    Column(modifier = Modifier.fillMaxSize()) {
        if (detail?.affiliatedBrokersDetected?.isNotEmpty() == true) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🚨 Deteksi Afiliasi Broker: ${detail.affiliatedBrokersDetected.joinToString { "${it.broker} (${it.note})" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        DataGrid2D(
            items = rows,
            pinnedHeader = "Broker",
            columns = listOf(
                DataGridColumn("Sisi", width = 80.dp, alignment = Alignment.CenterStart) { r ->
                    Text(
                        text = r.side,
                        fontWeight = FontWeight.Bold,
                        color = if (r.side == "Buy") Format.ColorPositive else Format.ColorNegative
                    )
                },
                DataGridColumn("Tipe", width = 120.dp, alignment = Alignment.CenterStart) { r ->
                    Text(text = r.type, style = MaterialTheme.typography.bodySmall)
                },
                DataGridColumn("Nilai Net (IDR)", width = 130.dp, alignment = Alignment.CenterEnd) { r ->
                    Text(
                        text = Format.fmtRp(r.net),
                        fontWeight = FontWeight.Bold,
                        color = Format.signedColor(r.net)
                    )
                }
            ),
            pinnedCellContent = { r ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = r.broker,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (r.isAffiliated) {
                        Spacer(modifier = Modifier.width(4.dp))
                        GovernanceBadge("AFILIASI", isAffiliated = true)
                    }
                }
            }
        )
    }
}

@Composable
private fun CausalityTab(detail: me.cugarete.investowl.core.model.TickerDetailResponse?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Granger Causality & Forward Alpha Scan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Menguji apakah akumulasi broker asing atau broker tertentu memimpin (lead) pergerakan harga 10 hari ke depan dengan signifikansi statistik p-value < 0.05.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ForeignFlowTab(foreignFlow: me.cugarete.investowl.core.model.ForeignFlowDetailResponse?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Herfindahl Index (HHI)",
                    value = foreignFlow?.features?.foreignHhi?.let { String.format(java.util.Locale.US, "%.3f", it) } ?: "-",
                    subtitle = "Konsentrasi broker asing",
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Foreign Z-Score",
                    value = foreignFlow?.features?.foreignZscore?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: "-",
                    subtitle = "Deviasi arus terhadap histori",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HMM Regime Probabilities (Hidden Markov Model)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val regimes = foreignFlow?.models?.hmm ?: emptyList()
                    if (regimes.isEmpty()) {
                        Text(
                            text = "Model HMM sedang dikomputasi oleh pipeline background.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        regimes.forEach { reg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = reg.label, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "${(reg.probability * 100).toInt()}%",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RawTablesTab(detail: me.cugarete.investowl.core.model.TickerDetailResponse?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Tabel Rekapitulasi Profil Broker",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        val profiles = detail?.profileFlow ?: emptyList()
        items(profiles) { p ->
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = p.label, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = Format.fmtRp(p.net),
                        fontWeight = FontWeight.Bold,
                        color = Format.signedColor(p.net)
                    )
                }
            }
        }
    }
}
