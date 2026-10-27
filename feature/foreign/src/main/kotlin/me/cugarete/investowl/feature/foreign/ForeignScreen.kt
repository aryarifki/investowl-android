package me.cugarete.investowl.feature.foreign

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForeignScreen(
    viewModel: ForeignViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val radar = uiState.exodusRadar

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "FOREIGN EXODUS RADAR & RRG",
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
            FullScreenLoading("Memuat data radar kapital asing...")
            return@Scaffold
        }

        if (uiState.errorMessage != null && radar == null) {
            ErrorMessage(
                message = uiState.errorMessage!!,
                onRetry = { viewModel.loadData() },
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // State Machine Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Market State Machine v3.1",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val state = radar?.marketState ?: "NORMAL"
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (state) {
                                    "BOTTOMING", "RECOVERY_TESTING", "CONFIRMED_BOTTOM" -> MaterialTheme.colorScheme.primaryContainer
                                    "CRASH", "WARNING" -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.surfaceContainerHighest
                                }
                            ) {
                                Text(
                                    text = state,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = when (state) {
                                        "BOTTOMING", "RECOVERY_TESTING", "CONFIRMED_BOTTOM" -> MaterialTheme.colorScheme.onPrimaryContainer
                                        "CRASH", "WARNING" -> MaterialTheme.colorScheme.onErrorContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = radar?.ihsgCurrent?.let { String.format(java.util.Locale.US, "%,.2f", it) } ?: "-",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val chg = radar?.ihsgChangePct ?: 0.0
                            Text(
                                text = Format.fmtPct(chg / 100.0),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Format.signedColor(chg)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Model 6-fase berbasis Value-Weighted VWAP mendeteksi apakah bursa berada dalam fase Normal, Warning, Crash, Bottoming, Recovery Testing, atau Confirmed Bottom.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Lookback Period Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 45, 60, 90).forEach { days ->
                        FilterChip(
                            selected = uiState.selectedDays == days,
                            onClick = { viewModel.selectDays(days) },
                            label = { Text("$days Hari") }
                        )
                    }
                }
            }

            // Culprit Brokers (Top Net Sellers)
            item {
                Text(
                    text = "Culprit Brokers (Top Net Sellers 14D)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            val culprits = radar?.culpritBrokers ?: emptyList()
            if (culprits.isEmpty()) {
                item {
                    Text(
                        text = "Tidak ada broker asing dengan tekanan jual ekstrim.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(culprits) { c ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${c.brokerCode} - ${c.brokerName}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = Format.fmtRp(c.netSellIdr),
                                fontWeight = FontWeight.Bold,
                                color = Format.ColorNegative
                            )
                        }
                    }
                }
            }

            // Sector Rotation RRG Section
            item {
                Text(
                    text = "Rotasi Sektor IDX (Relative Strength vs IHSG)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            val sectors = uiState.sectorSummary?.sectors ?: emptyList()
            if (sectors.isEmpty()) {
                item {
                    Text(
                        text = "Data rotasi sektor RRG sedang diperbarui.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(sectors) { s ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = s.sector, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Kuadran: ${s.quadrant}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when (s.quadrant) {
                                        "Leading" -> Format.ColorPositive
                                        "Weakening" -> MaterialTheme.colorScheme.tertiary
                                        "Lagging" -> Format.ColorNegative
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = Format.fmtRp(s.foreignNetIdr),
                                    fontWeight = FontWeight.Bold,
                                    color = Format.signedColor(s.foreignNetIdr)
                                )
                                Text(
                                    text = "RS: ${String.format(java.util.Locale.US, "%.1f", s.rsRatio)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
