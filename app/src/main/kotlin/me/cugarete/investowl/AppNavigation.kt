package me.cugarete.investowl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import me.cugarete.investowl.feature.admin.AdminScreen
import me.cugarete.investowl.feature.admin.AdminViewModel
import me.cugarete.investowl.feature.auth.AuthViewModel
import me.cugarete.investowl.feature.auth.LoginScreen
import me.cugarete.investowl.feature.auth.PendingApprovalScreen
import me.cugarete.investowl.feature.backtest.BacktestScreen
import me.cugarete.investowl.feature.backtest.BacktestViewModel
import me.cugarete.investowl.feature.foreign.ForeignScreen
import me.cugarete.investowl.feature.foreign.ForeignViewModel
import me.cugarete.investowl.feature.home.HomeScreen
import me.cugarete.investowl.feature.home.HomeViewModel
import me.cugarete.investowl.feature.konglo.KongloScreen
import me.cugarete.investowl.feature.konglo.KongloViewModel
import me.cugarete.investowl.feature.ownership.OwnershipScreen
import me.cugarete.investowl.feature.ownership.OwnershipViewModel
import me.cugarete.investowl.feature.signal.SignalScreen
import me.cugarete.investowl.feature.signal.SignalViewModel
import me.cugarete.investowl.feature.ticker.TickerDetailScreen
import me.cugarete.investowl.feature.ticker.TickerViewModel
import me.cugarete.investowl.feature.whales.WhalesScreen
import me.cugarete.investowl.feature.whales.WhalesViewModel

// --- Type-Safe Navigation Route Destinations (Kotlin 2.0+ & Navigation 2.8+) ---
@Serializable
object HomeRoute

@Serializable
data class TickerRoute(val ticker: String)

@Serializable
object OwnershipRoute

@Serializable
object KongloRoute

@Serializable
object ForeignRoute

@Serializable
object WhalesRoute

@Serializable
object SignalRoute

@Serializable
object BacktestRoute

@Serializable
object AdminRoute

@Serializable
object LoginRoute

@Serializable
object PendingRoute

@Composable
fun InvestowlNavHost(
    navController: NavHostController,
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        composable<HomeRoute> {
            val viewModel = HomeViewModel(appContainer.api, appContainer.watchlistDao)
            HomeScreen(
                viewModel = viewModel,
                onNavigateToTicker = { ticker -> navController.navigate(TickerRoute(ticker)) },
                onNavigateToSignal = { navController.navigate(SignalRoute) },
                onNavigateToForeign = { navController.navigate(ForeignRoute) }
            )
        }

        composable<TickerRoute> { backStackEntry ->
            val route: TickerRoute = backStackEntry.toRoute()
            val viewModel = TickerViewModel(route.ticker, appContainer.api, appContainer.watchlistDao)
            TickerDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable<OwnershipRoute> {
            val viewModel = OwnershipViewModel(appContainer.api)
            OwnershipScreen(
                viewModel = viewModel,
                onNavigateToTicker = { ticker -> navController.navigate(TickerRoute(ticker)) }
            )
        }

        composable<KongloRoute> {
            val viewModel = KongloViewModel(appContainer.api)
            KongloScreen(
                viewModel = viewModel,
                onNavigateToTicker = { ticker -> navController.navigate(TickerRoute(ticker)) }
            )
        }

        composable<ForeignRoute> {
            val viewModel = ForeignViewModel(appContainer.api)
            ForeignScreen(viewModel = viewModel)
        }

        composable<WhalesRoute> {
            val viewModel = WhalesViewModel(appContainer.api)
            WhalesScreen(
                viewModel = viewModel,
                onNavigateToTicker = { ticker -> navController.navigate(TickerRoute(ticker)) }
            )
        }

        composable<SignalRoute> {
            val viewModel = SignalViewModel(appContainer.api)
            SignalScreen(
                viewModel = viewModel,
                onNavigateToTicker = { ticker -> navController.navigate(TickerRoute(ticker)) }
            )
        }

        composable<BacktestRoute> {
            val viewModel = BacktestViewModel(appContainer.api)
            BacktestScreen(
                viewModel = viewModel,
                onNavigateToTicker = { ticker -> navController.navigate(TickerRoute(ticker)) }
            )
        }

        composable<AdminRoute> {
            val viewModel = AdminViewModel(appContainer.api)
            AdminScreen(viewModel = viewModel)
        }

        composable<LoginRoute> {
            val viewModel = AuthViewModel(appContainer.api, appContainer.sessionManager, appContainer.watchlistDao)
            LoginScreen(
                viewModel = viewModel,
                onContinueAsGuest = { navController.navigate(HomeRoute) }
            )
        }

        composable<PendingRoute> {
            val viewModel = AuthViewModel(appContainer.api, appContainer.sessionManager, appContainer.watchlistDao)
            PendingApprovalScreen(
                viewModel = viewModel,
                onContinueAsGuest = { navController.navigate(HomeRoute) }
            )
        }
    }
}
