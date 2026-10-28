package me.cugarete.investowl

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import me.cugarete.investowl.core.network.NetworkClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParametersDrawerSheet(
    appContainer: AppContainer,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var activeUniverse by remember { mutableStateOf("watchlist") }
    var windowDays by remember { mutableIntStateOf(20) }
    var horizon by remember { mutableIntStateOf(10) }

    var actionStatus by remember { mutableStateOf<String?>(null) }
    var isExecuting by remember { mutableStateOf(false) }

    val currentHost by appContainer.sessionManager.baseUrlFlow.collectAsState(initial = NetworkClient.DEFAULT_BASE_URL)
    var customHostInput by remember { mutableStateOf("") }
    var showHostDialog by remember { mutableStateOf(false) }

    val universes = listOf(
        "watchlist" to "Watchlist Lokal",
        "idx80" to "IDX80 Terlikuid",
        "lq45" to "LQ45 Blue Chip",
        "idx_high_dividend" to "High Dividend 20",
        "idx_bumn" to "BUMN 20",
        "idx_smc" to "Small-Mid Cap (SMC)",
        "esg_kehati" to "ESG Kehati",
        "idxfinance" to "Sektor Finansial",
        "idxenergy" to "Sektor Energi",
        "idxtechno" to "Sektor Teknologi",
        "idxinfra" to "Sektor Infrastruktur",
        "bisnis-27" to "Bisnis-27"
    )

    val windowOptions = listOf(20, 30, 60, 90, 180)
    val horizonOptions = listOf(1, 3, 5, 10)

    fun runAction(name: String, block: suspend () -> Unit) {
        coroutineScope.launch {
            isExecuting = true
            actionStatus = "Menjalankan $name..."
            try {
                block()
                actionStatus = "$name berhasil!"
            } catch (e: Exception) {
                actionStatus = "Gagal: ${e.message}"
            } finally {
                isExecuting = false
            }
        }
    }

    ModalDrawerSheet(modifier = modifier.width(320.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "Parameters",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Parameters & Model",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Konfigurasi Kuantitatif",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Server Host Info & Selector
            Text(
                text = "HOST SERVER BACKEND",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentHost,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        Text(
                            text = if (currentHost.contains("avenia.my.id")) "Produksi Resmi (Live)" else "Kustom / Dev",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = {
                        customHostInput = currentHost
                        showHostDialog = true
                    }) {
                        Text("Ganti", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Universe Selector
            Text(
                text = "UNIVERSE PASAR IDX",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            var universeDropdownOpen by remember { mutableStateOf(false) }
            OutlinedCard(
                onClick = { universeDropdownOpen = true },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentLabel = universes.firstOrNull { it.first == activeUniverse }?.second ?: activeUniverse
                    Text(text = currentLabel, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = universeDropdownOpen,
                    onDismissRequest = { universeDropdownOpen = false }
                ) {
                    universes.forEach { (id, label) ->
                        DropdownMenuItem(
                            text = { Text(label, style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                activeUniverse = id
                                universeDropdownOpen = false
                            }
                        )
                    }
                }
            }

            // Window Days
            Text(
                text = "JENDELA PERIODE (WINDOW HARI)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                windowOptions.forEach { w ->
                    FilterChip(
                        selected = windowDays == w,
                        onClick = { windowDays = w },
                        label = { Text("${w}D", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Horizon
            Text(
                text = "HORIZON PREDIKSI (HARI KERJA)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                horizonOptions.forEach { h ->
                    FilterChip(
                        selected = horizon == h,
                        onClick = { horizon = h },
                        label = { Text("T+$h", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Pipeline Triggers
            Text(
                text = "KONTROL PIPELINE & CACHE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            FilledTonalButton(
                onClick = {
                    runAction("Refresh Universe") {
                        appContainer.api.refreshUniverse()
                        appContainer.api.purgeCachePattern("*")
                    }
                },
                enabled = !isExecuting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Refresh Master Tickers", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(6.dp))

            FilledTonalButton(
                onClick = {
                    runAction("Purge Seluruh Cache") {
                        appContainer.api.purgeCachePattern("*")
                    }
                },
                enabled = !isExecuting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Purge Seluruh Cache Redis", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    runAction("Pipeline Hari Ini") {
                        val payload = buildJsonObject {
                            put("universe_mode", JsonPrimitive(activeUniverse))
                        }
                        appContainer.api.runBandarPipeline(payload)
                    }
                },
                enabled = !isExecuting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Jalankan Pipeline Hari Ini", style = MaterialTheme.typography.labelSmall)
            }

            if (actionStatus != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                ) {
                    Text(
                        text = actionStatus!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }

    if (showHostDialog) {
        AlertDialog(
            onDismissRequest = { showHostDialog = false },
            title = { Text("Ubah Base URL Backend", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column {
                    Text(
                        text = "Tentukan host backend FastAPI:",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = customHostInput,
                        onValueChange = { customHostInput = it },
                        label = { Text("URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        SuggestionChip(
                            onClick = { customHostInput = "https://investowl.avenia.my.id" },
                            label = { Text("Live Produksi", style = MaterialTheme.typography.labelSmall) }
                        )
                        SuggestionChip(
                            onClick = { customHostInput = "http://10.0.2.2:8001" },
                            label = { Text("Emulator Local", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            appContainer.sessionManager.setBaseUrl(customHostInput)
                            showHostDialog = false
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHostDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
