package me.cugarete.investowl.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> DataGrid2D(
    items: List<T>,
    pinnedHeader: String,
    pinnedColumnWidth: Dp = 100.dp,
    columns: List<DataGridColumn<T>>,
    pinnedCellContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    emptyMessage: String = "Tidak ada data"
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val horizontalScrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxWidth()) {
        // Sticky Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(vertical = 8.dp)
        ) {
            // Pinned Left Header
            Box(
                modifier = Modifier
                    .width(pinnedColumnWidth)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = pinnedHeader,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Scrollable Headers
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
            ) {
                columns.forEach { col ->
                    Box(
                        modifier = Modifier
                            .width(col.width)
                            .padding(horizontal = 8.dp),
                        contentAlignment = col.alignment
                    ) {
                        Text(
                            text = col.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Data Rows
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(items) { index, item ->
                val rowBg = if (index % 2 == 0) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowBg)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pinned Left Cell
                    Box(
                        modifier = Modifier
                            .width(pinnedColumnWidth)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        pinnedCellContent(item)
                    }

                    // Scrollable Cells
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        columns.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .width(col.width)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = col.alignment
                            ) {
                                col.cellContent(item)
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

data class DataGridColumn<T>(
    val title: String,
    val width: Dp = 100.dp,
    val alignment: Alignment = Alignment.CenterEnd,
    val cellContent: @Composable (T) -> Unit
)
