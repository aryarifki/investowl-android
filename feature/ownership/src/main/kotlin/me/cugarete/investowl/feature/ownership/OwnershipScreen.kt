package me.cugarete.investowl.feature.ownership

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import me.cugarete.investowl.core.model.OwnershipHolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnershipScreen(
    viewModel: OwnershipViewModel,
    onNavigateToTicker: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val tabs = listOf("Komposisi KSEI & BAE", "Major Holders", "Audit Paritas", "Cari Investor")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "KEPEMILIKAN KSEI & IDX BAE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "OFFICIAL PUBLIC",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
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
            FullScreenLoading("Memuat data kepemilikan KSEI & BAE...")
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Ticker selector bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("BBCA", "BBRI", "BMRI", "BBNI", "TLKM", "ASII").forEach { ticker ->
                    FilterChip(
                        selected = uiState.selectedTicker == ticker,
                        onClick = { viewModel.selectTicker(ticker) },
                        label = { Text(ticker, fontWeight = FontWeight.Bold) }
                    )
                }
            }

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
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (uiState.selectedTab) {
                0 -> KseiCompositionTab(uiState)
                1 -> MajorHoldersTab(uiState.stockOwnership?.holders ?: emptyList())
                2 -> ParityAuditTab(uiState)
                3 -> InvestorSearchTab(uiState, viewModel::onSearchQueryChanged)
            }
        }
    }
}

@Composable
private fun KseiCompositionTab(uiState: OwnershipUiState) {
    val comp = uiState.stockOwnership?.composition
    val profile = uiState.stockOwnership?.profile
    val staging = uiState.stagingProfile?.compositionHistory?.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Kepemilikan Asing",
                    value = Format.fmtPct((staging?.pctAsing ?: comp?.pctAsing ?: 0.0) / 100.0),
                    subtitle = "Investor institusi & retail asing",
                    valueColor = Color(0xFF3A7BD5),
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Kepemilikan Ritel Murni",
                    value = Format.fmtPct((staging?.pctRitelPure ?: comp?.pctRitel ?: 0.0) / 100.0),
                    subtitle = "Individu domestik KSEI",
                    valueColor = Color(0xFF1DB87A),
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
                        text = "Profil Saham & Kapitalisasi Pasar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Shares Outstanding:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = profile?.sharesOutstanding?.let { String.format(java.util.Locale.US, "%,.0f lembar", it) } ?: "-",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Kapitalisasi Pasar (Market Cap):", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = Format.fmtRp(profile?.marketCap),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Histori Jumlah Pemegang Saham (SID) dari IDX BAE
        val sidList = uiState.stagingProfile?.sidHistory ?: emptyList()
        item {
            Text(
                text = "Histori Jumlah Pemegang Saham (SID) - IDX BAE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (sidList.isEmpty()) {
            item {
                Text(
                    text = "Data SID BAE sedang disinkronkan dari laporan IDX bulanan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(sidList) { sid ->
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
                        Text(text = sid.period ?: "-", fontWeight = FontWeight.SemiBold)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format(java.util.Locale.US, "%,d Pemegang", sid.totalShareholders),
                                fontWeight = FontWeight.Bold
                            )
                            if (sid.monthlyChange != null) {
                                val chg = sid.monthlyChange ?: 0L
                                val sign = if (chg > 0) "+" else ""
                                Text(
                                    text = "$sign${String.format(java.util.Locale.US, "%,d", chg)} SID",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (chg >= 0) Format.ColorPositive else Format.ColorNegative
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
private fun MajorHoldersTab(holders: List<OwnershipHolder>) {
    Column(modifier = Modifier.fillMaxSize()) {
        DataGrid2D(
            items = holders,
            pinnedHeader = "Pemegang Saham",
            pinnedColumnWidth = 160.dp,
            columns = listOf(
                DataGridColumn("Porsi (%)", width = 90.dp, alignment = Alignment.CenterEnd) { h ->
                    Text(
                        text = Format.fmtPct(h.pct / 100.0),
                        fontWeight = FontWeight.Bold,
                        color = if (h.isControlling) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                },
                DataGridColumn("Status", width = 110.dp, alignment = Alignment.CenterStart) { h ->
                    if (h.isControlling) {
                        GovernanceBadge("PENGENDALI", isAffiliated = false)
                    } else {
                        Text(text = h.classification ?: "Substansial", style = MaterialTheme.typography.bodySmall)
                    }
                },
                DataGridColumn("Jumlah Lembar", width = 140.dp, alignment = Alignment.CenterEnd) { h ->
                    Text(
                        text = h.shares?.let { String.format(java.util.Locale.US, "%,.0f", it) } ?: "-",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            ),
            pinnedCellContent = { h ->
                Text(
                    text = h.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
            }
        )
    }
}

@Composable
private fun ParityAuditTab(uiState: OwnershipUiState) {
    val parity = uiState.parityCheck

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status Paritas KSEI vs Baseline",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (parity?.parityStatus == "EXCELLENT") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = parity?.parityStatus ?: "AUDITED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Membandingkan data staging KSEI publik terhadap baseline CT pada ${parity?.monthsCompared ?: 8} periode pelaporan untuk memastikan akurasi data kepemilikan bursa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        val history = parity?.history ?: emptyList()
        items(history) { h ->
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
                    Text(text = h.period, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Diff Ritel: ${h.retailDiffPp} pp",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Diff Asing: ${h.foreignDiffPp} pp",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InvestorSearchTab(
    uiState: OwnershipUiState,
    onQueryChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onQueryChanged,
            placeholder = { Text("Ketik minimal 3 huruf nama investor (misal: Salim, Prajogo)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (uiState.searchResults.isEmpty() && uiState.searchQuery.length >= 3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ditemukan investor dengan nama tersebut.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.searchResults) { item ->
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
                            Text(text = item.name, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${item.tickerCount} Emiten Terdaftar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (item.totalValueIdr != null) {
                            Text(
                                text = Format.fmtRp(item.totalValueIdr),
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
