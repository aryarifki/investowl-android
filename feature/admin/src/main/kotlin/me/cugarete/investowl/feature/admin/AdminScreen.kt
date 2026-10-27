package me.cugarete.investowl.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.cugarete.investowl.core.common.Format
import me.cugarete.investowl.core.designsystem.*
import me.cugarete.investowl.core.model.JobConfigItem
import me.cugarete.investowl.core.model.UserRead

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val tabs = listOf("Approval Pengguna", "9-Job Control", "Terminal Logs")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ADMIN & JOB CONTROL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "ROOT ACCESS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.purgeCache() }) {
                        Text("Purge Cache", color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            FullScreenLoading("Memuat panel administrator...")
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.actionMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.actionMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                0 -> UserApprovalTab(uiState, viewModel::setUserApproval, viewModel::deleteUser, viewModel::toggleAuthGate)
                1 -> JobControlTab(uiState.jobs, viewModel::triggerJob, viewModel::stopJob)
                2 -> TerminalLogTab(uiState.selectedLogFile, uiState.logContent, viewModel::fetchLogContent)
            }
        }
    }
}

@Composable
private fun UserApprovalTab(
    uiState: AdminUiState,
    onApprove: (Int, Boolean) -> Unit,
    onDelete: (Int) -> Unit,
    onToggleGate: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auth Gatekeeper (Require Login)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Jika aktif, fitur sinyal & kepemilikan memerlukan login dan approval admin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.authGateEnabled,
                        onCheckedChange = { onToggleGate() }
                    )
                }
            }
        }

        item {
            Text(
                text = "Daftar Pengguna Terdaftar (${uiState.users.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(uiState.users) { u ->
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = u.name ?: u.email, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${u.email} • Role: ${u.role}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (u.role != "admin") {
                            Switch(
                                checked = u.isApproved,
                                onCheckedChange = { onApprove(u.id, it) }
                            )
                            IconButton(onClick = { onDelete(u.id) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Hapus",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "ADMIN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
private fun JobControlTab(
    jobs: Map<String, JobConfigItem>,
    onRunJob: (String) -> Unit,
    onStopJob: (String) -> Unit
) {
    val predefinedJobs = listOf(
        "scanner" to ("Scanner AI & Wyckoff" to "AI & Smart Money Stock Scanner"),
        "auditor" to ("Auditor Sinyal" to "Evaluasi sinyal OPEN & Update TP/SL"),
        "backfill" to ("Daily Backfill" to "Backfill data transaksi & broker harian"),
        "backfill_monthly" to ("Monthly Backfill" to "Backfill broker bulanan historis"),
        "etl_flow" to ("ETL Foreign Flow" to "Komputasi HMM, VAR, Network, Heatmap"),
        "backfill_fundamentals" to ("Fundamentals & Governance" to "Tarik data fundamental dari IDX"),
        "db_audit" to ("DB Health Audit" to "Audit integritas DB & self-healing"),
        "pipeline" to ("Full Pipeline Harian" to "Pipeline Harian Lengkap (run_daily.sh)"),
        "backup" to ("Database Backup" to "Pencadangan DB ke Telegram & Drive")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(predefinedJobs) { (jobKey, meta) ->
            val running = jobs[jobKey]?.isRunning == true

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = meta.first, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            if (running) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "RUNNING",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = meta.second,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            if (running) onStopJob(jobKey) else onRunJob(jobKey)
                        }
                    ) {
                        Icon(
                            imageVector = if (running) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (running) "Stop" else "Jalankan",
                            tint = if (running) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TerminalLogTab(
    selectedFile: String,
    content: String,
    onSelectFile: (String) -> Unit
) {
    val logFiles = listOf("cron_daily.log", "scanner.log", "auditor.log", "backfill.log", "etl_flow.log")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            logFiles.forEach { f ->
                FilterChip(
                    selected = selectedFile == f,
                    onClick = { onSelectFile(f) },
                    label = { Text(f.replace(".log", ""), style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Linux Log Emulator Console
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
        ) {
            Text(
                text = content.ifEmpty { "Log kosong atau belum ada output." },
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color(0xFF38BDF8),
                lineHeight = 16.sp
            )
        }
    }
}
