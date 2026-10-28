package me.cugarete.investowl

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.cugarete.investowl.core.designsystem.Spacing
import me.cugarete.investowl.feature.konglo.KongloScreen
import me.cugarete.investowl.feature.konglo.KongloViewModel
import me.cugarete.investowl.feature.ownership.ExplorerScreen
import me.cugarete.investowl.feature.ownership.OwnershipScreen
import me.cugarete.investowl.feature.ownership.OwnershipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchScreen(
    appContainer: AppContainer,
    onNavigateToTicker: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedResearchTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        Triple("Ownership", Icons.Outlined.PieChart, "KSEI & SID BAE"),
        Triple("Group", Icons.Outlined.AccountTree, "8 Konglomerat IDX"),
        Triple("Explorer", Icons.Outlined.FilterList, "Screener Kepemilikan")
    )

    Column(modifier = modifier.fillMaxSize()) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            tabs.forEachIndexed { index, (label, icon, _) ->
                SegmentedButton(
                    selected = selectedResearchTab == index,
                    onClick = { selectedResearchTab = index },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
                    icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp)) }
                ) {
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedResearchTab) {
                0 -> {
                    val ownershipVm = remember { OwnershipViewModel(appContainer.api) }
                    OwnershipScreen(
                        viewModel = ownershipVm,
                        onNavigateToTicker = onNavigateToTicker
                    )
                }
                1 -> {
                    val kongloVm = remember { KongloViewModel(appContainer.api) }
                    KongloScreen(
                        viewModel = kongloVm,
                        onNavigateToTicker = onNavigateToTicker
                    )
                }
                2 -> {
                    ExplorerScreen(
                        api = appContainer.api,
                        onNavigateToTicker = onNavigateToTicker
                    )
                }
            }
        }
    }
}
