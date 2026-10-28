package me.cugarete.investowl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import me.cugarete.investowl.core.model.AuthState
import me.cugarete.investowl.core.network.NetworkClient

sealed class AppDestination(
    val route: Any,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : AppDestination(HomeRoute, "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object Emiten : AppDestination(TickerRoute("BBCA"), "Emiten", Icons.Filled.ShowChart, Icons.Outlined.ShowChart)
    data object Signal : AppDestination(SignalRoute, "Signal", Icons.Filled.Sensors, Icons.Outlined.Sensors)
    data object Foreign : AppDestination(ForeignRoute, "Foreign", Icons.Filled.Public, Icons.Outlined.Public)
    data object Research : AppDestination(ResearchRoute, "Riset", Icons.Filled.PieChart, Icons.Outlined.PieChart)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestowlApp(
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val authState by appContainer.sessionManager.authStateFlow.collectAsState(initial = AuthState.GUEST)
    val currentHost by appContainer.sessionManager.baseUrlFlow.collectAsState(initial = NetworkClient.DEFAULT_BASE_URL)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var showSearchDialog by remember { mutableStateOf(false) }
    var searchTickerInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val destinations = listOf(
        AppDestination.Home,
        AppDestination.Emiten,
        AppDestination.Signal,
        AppDestination.Foreign,
        AppDestination.Research
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ParametersDrawerSheet(
                appContainer = appContainer,
                onClose = { coroutineScope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Investowl",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "QUANT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = "Buka Parameters Drawer",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        // Quick Search Button
                        IconButton(onClick = { showSearchDialog = true }) {
                            Icon(Icons.Outlined.Search, contentDescription = "Cari Saham")
                        }

                        // Purge Cache Button
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        appContainer.api.purgeCachePattern("*")
                                        snackbarHostState.showSnackbar("Cache Redis berhasil dibersihkan")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Gagal bersihkan cache: ${e.message}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.CleaningServices, contentDescription = "Bersihkan Cache")
                        }

                        // Admin Portal Button if Admin
                        if (authState == AuthState.ADMIN) {
                            IconButton(onClick = { navController.navigate(AdminRoute) }) {
                                Icon(
                                    Icons.Outlined.AdminPanelSettings,
                                    contentDescription = "Admin Portal",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            NavigationSuiteScaffold(
                navigationSuiteItems = {
                    destinations.forEach { dest ->
                        val isSelected = when (dest) {
                            AppDestination.Home -> currentRoute == null || currentRoute.contains("HomeRoute")
                            AppDestination.Emiten -> currentRoute?.contains("TickerRoute") == true
                            AppDestination.Signal -> currentRoute?.contains("SignalRoute") == true || currentRoute?.contains("BacktestRoute") == true
                            AppDestination.Foreign -> currentRoute?.contains("ForeignRoute") == true
                            AppDestination.Research -> currentRoute?.contains("ResearchRoute") == true || currentRoute?.contains("OwnershipRoute") == true || currentRoute?.contains("KongloRoute") == true
                        }

                        item(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = dest.label
                                )
                            },
                            label = { Text(dest.label) }
                        )
                    }
                },
                modifier = Modifier.padding(innerPadding)
            ) {
                InvestowlNavHost(
                    navController = navController,
                    appContainer = appContainer
                )
            }
        }
    }

    // Quick Ticker Search Dialog
    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = { Text("Pencarian Emiten", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column {
                    Text(
                        text = "Masukkan kode saham 4 huruf (contoh: BBCA, ASII, BMRI):",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = searchTickerInput,
                        onValueChange = { searchTickerInput = it.uppercase().take(6) },
                        placeholder = { Text("BBCA") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Pilihan Cepat:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("BBCA", "BBRI", "BMRI", "ASII", "TLKM").forEach { t ->
                            SuggestionChip(
                                onClick = {
                                    showSearchDialog = false
                                    navController.navigate(TickerRoute(t))
                                },
                                label = { Text(t, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val t = searchTickerInput.trim().uppercase()
                        if (t.isNotBlank()) {
                            showSearchDialog = false
                            navController.navigate(TickerRoute(t))
                        }
                    }
                ) {
                    Text("Buka Saham")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSearchDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
