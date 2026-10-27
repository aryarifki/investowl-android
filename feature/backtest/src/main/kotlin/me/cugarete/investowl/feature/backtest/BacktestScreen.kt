package me.cugarete.investowl.feature.backtest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.cugarete.investowl.core.common.Format
import me.cugarete.investowl.core.designsystem.*
import me.cugarete.investowl.core.model.BacktestHistoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BacktestScreen(
    viewModel: BacktestViewModel,
    onNavigateToTicker: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val stats = uiState.stats

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "STRATEGY BACKTEST & ALPHA LAB",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            FullScreenLoading("Memuat data validasi backtest...")
            return@Scaffold
        }

        if (uiState.errorMessage != null && stats == null) {
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
            // Horizon Selector Filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5, 10, 15, 20).forEach { h ->
                    FilterChip(
                        selected = uiState.selectedHorizon == h,
                        onClick = { viewModel.selectHorizon(h) },
                        label = { Text("Holding T+$h", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Metrics Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Win Rate",
                    value = Format.fmtPct(stats?.winRate ?: 0.0),
                    subtitle = "${stats?.winningTrades ?: 0}W / ${stats?.losingTrades ?: 0}L",
                    valueColor = Format.ColorPositive,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Profit Factor",
                    value = String.format(java.util.Locale.US, "%.2f", stats?.profitFactor ?: 1.0),
                    subtitle = "Gross Win / Gross Loss",
                    valueColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Historical Trades 2D Grid
            Text(
                text = "Histori Eksekusi Sinyal (Sampel 100 Trades Terakhir)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            DataGrid2D(
                items = uiState.history,
                pinnedHeader = "Ticker",
                pinnedColumnWidth = 90.dp,
                columns = listOf(
                    DataGridColumn("Tgl Entry", width = 100.dp, alignment = Alignment.CenterStart) { h ->
                        Text(text = h.entryDate, style = MaterialTheme.typography.bodySmall)
                    },
                    DataGridColumn("Harga Beli", width = 100.dp, alignment = Alignment.CenterEnd) { h ->
                        Text(text = "Rp ${h.entryPrice.toInt()}", style = MaterialTheme.typography.bodySmall)
                    },
                    DataGridColumn("PnL (%)", width = 100.dp, alignment = Alignment.CenterEnd) { h ->
                        Text(
                            text = Format.fmtPct(h.pnlPct / 100.0),
                            fontWeight = FontWeight.Bold,
                            color = Format.signedColor(h.pnlPct)
                        )
                    },
                    DataGridColumn("Hasil", width = 90.dp, alignment = Alignment.CenterStart) { h ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (h.outcome == "WIN") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = h.outcome,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (h.outcome == "WIN") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                ),
                pinnedCellContent = { h ->
                    Text(
                        text = h.ticker,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    }
}
