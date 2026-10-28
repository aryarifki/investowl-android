package me.cugarete.investowl.feature.ownership

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import me.cugarete.investowl.core.common.Format
import me.cugarete.investowl.core.designsystem.ErrorMessage
import me.cugarete.investowl.core.designsystem.FullScreenLoading
import me.cugarete.investowl.core.designsystem.Spacing
import me.cugarete.investowl.core.model.ExplorerResponse
import me.cugarete.investowl.core.network.InvestowlApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerScreen(
    api: InvestowlApi,
    onNavigateToTicker: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var activeSubTab by remember { mutableStateOf("snapshot") } // snapshot vs parity
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var explorerData by remember { mutableStateOf<ExplorerResponse?>(null) }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCondition by remember { mutableStateOf("all") }

    val snapshotPresets = listOf(
        "all" to "Semua Emiten",
        "controlling_high" to "Pengendali >50%",
        "public_low" to "Publik <10%",
        "many_holders" to "Pemegang ≥20",
        "treasury" to "Ada Saham Treasury"
    )

    val parityPresets = listOf(
        "bertambah" to "Net Buy / Bertambah",
        "berkurang" to "Net Sell / Berkurang",
        "tetap" to "Porsi Tetap"
    )

    fun loadData() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val resp = if (activeSubTab == "snapshot") {
                    api.getOwnershipExplorer(
                        mode = "stocks",
                        condition = if (selectedCondition == "all") null else selectedCondition,
                        query = searchQuery.ifBlank { null },
                        limit = 50
                    )
                } else {
                    api.getOwnershipExplorerCt(
                        mode = "stocks",
                        condition = selectedCondition,
                        limit = 50
                    )
                }
                explorerData = resp
            } catch (e: Exception) {
                errorMessage = e.message ?: "Gagal memuat data explorer"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(activeSubTab, selectedCondition) {
        loadData()
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        // Sub-Tab Switcher (Snapshot vs Parity)
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
        ) {
            SegmentedButton(
                selected = activeSubTab == "snapshot",
                onClick = {
                    activeSubTab = "snapshot"
                    selectedCondition = "all"
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("Snapshot Screener", style = MaterialTheme.typography.labelMedium)
            }
            SegmentedButton(
                selected = activeSubTab == "parity",
                onClick = {
                    activeSubTab = "parity"
                    selectedCondition = "bertambah"
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("Parity Explorer", style = MaterialTheme.typography.labelMedium)
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari ticker saham...", style = MaterialTheme.typography.bodySmall) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                IconButton(onClick = { loadData() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp))
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp)
        )

        // Presets Chips
        val currentPresets = if (activeSubTab == "snapshot") snapshotPresets else parityPresets
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            items(currentPresets) { (id, label) ->
                FilterChip(
                    selected = selectedCondition == id,
                    onClick = { selectedCondition = id },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        when {
            isLoading -> FullScreenLoading("Memuat data explorer...")
            errorMessage != null -> ErrorMessage(message = errorMessage!!, onRetry = { loadData() })
            explorerData != null -> {
                val results = explorerData!!.results
                if (results.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Tidak ada data yang cocok dengan kriteria filter",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Menampilkan ${results.size} dari ${explorerData!!.total} hasil",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        items(results) { rowJson ->
                            val ticker = rowJson["ticker"]?.jsonPrimitive?.contentOrNull ?: ""
                            val name = rowJson["name"]?.jsonPrimitive?.contentOrNull ?: ""
                            val sector = rowJson["sector"]?.jsonPrimitive?.contentOrNull ?: "-"
                            val holderCount = rowJson["holder_count"]?.jsonPrimitive?.intOrNull ?: 0
                            val totalPct = rowJson["total_pct"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                            val controllingPct = rowJson["controlling_pct"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                            val publicPct = rowJson["public_pct"]?.jsonPrimitive?.doubleOrNull ?: 0.0

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { if (ticker.isNotBlank()) onNavigateToTicker(ticker) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = ticker,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                                            ) {
                                                Text(
                                                    text = sector,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "$holderCount Pemegang",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (name.isNotBlank()) {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Pengendali: ${Format.fmtPct(controllingPct)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Publik: ${Format.fmtPct(publicPct)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Total: ${Format.fmtPct(totalPct)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
