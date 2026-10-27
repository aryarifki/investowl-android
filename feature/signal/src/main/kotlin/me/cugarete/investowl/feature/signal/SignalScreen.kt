package me.cugarete.investowl.feature.signal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import me.cugarete.investowl.core.model.ActiveSignalItem
import me.cugarete.investowl.core.model.DailySignalItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalScreen(
    viewModel: SignalViewModel,
    onNavigateToTicker: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val tabs = listOf("Top Sinyal Harian", "Episode Sinyal Aktif (OPEN)")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI SIGNAL SCANNER",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "WYCKOFF + ML",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (uiState.latestDate.isNotBlank()) {
                            Text(
                                text = "Scan Terakhir: ${uiState.latestDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshSignals() },
                        enabled = !uiState.isRefreshing
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Cache")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            FullScreenLoading("Memindai sinyal kuantitatif bursa...")
            return@Scaffold
        }

        if (uiState.errorMessage != null && uiState.dailySignals.isEmpty()) {
            ErrorMessage(
                message = uiState.errorMessage!!,
                onRetry = { viewModel.loadSignals() },
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = uiState.selectedTab,
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

            when (uiState.selectedTab) {
                0 -> DailySignalsList(uiState.dailySignals, onNavigateToTicker)
                1 -> ActiveSignalsList(uiState.activeSignals, onNavigateToTicker)
            }
        }
    }
}

@Composable
private fun DailySignalsList(
    signals: List<DailySignalItem>,
    onNavigateToTicker: (String) -> Unit
) {
    if (signals.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tidak ada sinyal harian yang memenuhi kualifikasi kuantitatif.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(signals) { s ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTicker(s.ticker) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = s.ticker,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = s.wyckoffPhase,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Skor: ${s.compositeScore.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val prob = s.mlWinProb
                            Text(
                                text = if (prob != null) "P(WIN): ${(prob * 100).toInt()}%" else "P(WIN): N/A",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (s.smartMoneyNotes.isNotBlank()) {
                        Text(
                            text = s.smartMoneyNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "R:R: ${s.rrRatio?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: "-"}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        val kelly = s.kellyAlloc
                        if (kelly != null) {
                            Text(
                                text = "Half-Kelly Sizing: ${(kelly * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
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

@Composable
private fun ActiveSignalsList(
    signals: List<ActiveSignalItem>,
    onNavigateToTicker: (String) -> Unit
) {
    if (signals.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tidak ada episode sinyal yang berstatus OPEN.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(signals) { s ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTicker(s.ticker) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = s.ticker,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "HARI KE-${s.daysHeld}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = s.companyName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Skor: ${s.compositeScore}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val prob = s.mlWinProb
                            Text(
                                text = if (prob != null) "P(WIN): ${(prob * 100).toInt()}%" else "P(WIN): N/A",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Entry Price", style = MaterialTheme.typography.labelSmall)
                            Text("Rp ${s.entryPrice.toInt()}", fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Target (TP)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "Rp ${s.tpPrice.toInt()} (+${s.targetGainPct}%)",
                                fontWeight = FontWeight.Bold,
                                color = Format.ColorPositive
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Stop Loss (SL)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "Rp ${s.slPrice.toInt()} (-${s.stopRiskPct}%)",
                                fontWeight = FontWeight.Bold,
                                color = Format.ColorNegative
                            )
                        }
                    }
                }
            }
        }
    }
}
