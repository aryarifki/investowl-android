package me.cugarete.investowl

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import me.cugarete.investowl.core.model.AuthState

sealed class AppDestination(
    val route: Any,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Home : AppDestination(HomeRoute, "Beranda", Icons.Filled.Home, Icons.Outlined.Home)
    data object Signal : AppDestination(SignalRoute, "Sinyal AI", Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp)
    data object Foreign : AppDestination(ForeignRoute, "Arus Asing", Icons.Filled.Public, Icons.Outlined.Public)
    data object Ownership : AppDestination(OwnershipRoute, "Kepemilikan", Icons.Filled.PieChart, Icons.Outlined.PieChart)
    data object Whales : AppDestination(WhalesRoute, "Whales", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance)
    data object Backtest : AppDestination(BacktestRoute, "Backtest", Icons.Filled.Science, Icons.Outlined.Science)
    data object Admin : AppDestination(AdminRoute, "Admin", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
}

@Composable
fun InvestowlApp(
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val authState by appContainer.sessionManager.authStateFlow.collectAsState(initial = AuthState.GUEST)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationDestinations = mutableListOf(
        AppDestination.Home,
        AppDestination.Signal,
        AppDestination.Foreign,
        AppDestination.Ownership,
        AppDestination.Whales,
        AppDestination.Backtest
    )

    if (authState == AuthState.ADMIN) {
        navigationDestinations.add(AppDestination.Admin)
    }

    val adaptiveInfo = currentWindowAdaptiveInfo()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            navigationDestinations.forEach { dest ->
                val isSelected = currentRoute?.contains(dest.label, ignoreCase = true) == true ||
                        (dest == AppDestination.Home && (currentRoute == null || currentRoute.contains("HomeRoute")))

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
        modifier = modifier.fillMaxSize()
    ) {
        InvestowlNavHost(
            navController = navController,
            appContainer = appContainer
        )
    }
}
