package com.example.booleancalculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booleancalculator.domain.engine.BooleanEvaluator
import com.example.booleancalculator.ui.theme.BorderColor
import com.example.booleancalculator.ui.theme.CardBackground
import com.example.booleancalculator.ui.theme.MainBackground
import com.example.booleancalculator.ui.theme.PrimaryBlue
import com.example.booleancalculator.ui.theme.PrimaryText
import com.example.booleancalculator.ui.theme.SecondaryText
import com.example.booleancalculator.ui.theme.TableHighlight
import com.example.booleancalculator.viewmodel.UiState

@Composable
fun FullTruthTableScreen(
    state: UiState,
    onBack: () -> Unit,
    onFilterSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val model = state.truthTableModel ?: return
    val filters = model.analysis.filters
    val activeFilter = filters.firstOrNull { f -> f.id == state.truthTableFilter } ?: filters.first()

    val selectedCols = if (activeFilter.kind == "all") {
        model.columns.filter { it.isVariable || it.type != "VAR" || it.isResult }
    } else {
        val nodeIds = activeFilter.nodeIds.ifEmpty { listOfNotNull(activeFilter.nodeId) }
        val byId = model.columns.associateBy { it.nodeId }
        nodeIds.mapNotNull { byId[it] }
    }

    // Calculate exact column widths to fit single-line header text without wrapping
    val colWidths = selectedCols.map { col ->
        val label = if (col.isVariable) col.label else {
            val n = model.analysis.byId[col.nodeId]
            if (n != null) BooleanEvaluator.formatAstDisplay(n, state.notation) else col.label
        }
        val calculatedDp = label.length * 9 + 24
        maxOf(calculatedDp, 56).dp
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MainBackground)
            .statusBarsPadding()
    ) {
        // Page Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(CardBackground)
                    .border(1.dp, BorderColor, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    fontSize = 16.sp,
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = "Full Truth Table",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText
                )
                Text(
                    text = BooleanEvaluator.formatAstDisplay(model.ast, state.notation),
                    fontSize = 13.sp,
                    color = SecondaryText
                )
            }
        }

        // Horizontal Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { f ->
                val isActive = f.id == state.truthTableFilter
                val bg = if (isActive) PrimaryBlue else CardBackground
                val textCol = if (isActive) CardBackground else SecondaryText
                val borderCol = if (isActive) PrimaryBlue else BorderColor
                val label = if (f.kind == "all") f.label else {
                    val node = model.analysis.byId[f.nodeId]
                    if (node != null) BooleanEvaluator.formatAstDisplay(node, state.notation) else f.label
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(bg)
                        .border(1.5.dp, borderCol, RoundedCornerShape(20.dp))
                        .clickable { onFilterSelect(f.id) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = textCol,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Table with adaptive fill width / horizontal scroll
        val horizScroll = rememberScrollState()
        val vertScroll = rememberScrollState()

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(CardBackground)
        ) {
            val screenWidthDp = maxWidth
            val totalColsWidthDp = colWidths.fold(0.dp) { acc, dp -> acc + dp }
            val shouldFill = totalColsWidthDp < screenWidthDp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(vertScroll)
                    .then(if (shouldFill) Modifier.fillMaxWidth() else Modifier.horizontalScroll(horizScroll))
            ) {
                // Table Header
                Row(
                    modifier = Modifier
                        .background(TableHighlight)
                        .then(if (shouldFill) Modifier.fillMaxWidth() else Modifier.width(totalColsWidthDp))
                ) {
                    selectedCols.forEachIndexed { colIdx, col ->
                        val label = if (col.isVariable) col.label else {
                            val n = model.analysis.byId[col.nodeId]
                            if (n != null) BooleanEvaluator.formatAstDisplay(n, state.notation) else col.label
                        }
                        Box(
                            modifier = Modifier
                                .then(if (shouldFill) Modifier.weight(1f) else Modifier.width(colWidths[colIdx]))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Visible,
                                color = if (col.isResult) PrimaryText else SecondaryText
                            )
                        }
                    }
                }

                // Table Rows
                model.rows.forEach { row ->
                    Row(
                        modifier = Modifier
                            .border(0.5.dp, BorderColor)
                            .then(if (shouldFill) Modifier.fillMaxWidth() else Modifier.width(totalColsWidthDp))
                    ) {
                        selectedCols.forEachIndexed { colIdx, col ->
                            val valCell = row.values[col.nodeId] ?: 0
                            Box(
                                modifier = Modifier
                                    .then(if (shouldFill) Modifier.weight(1f) else Modifier.width(colWidths[colIdx]))
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = valCell.toString(),
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    fontWeight = if (col.isResult) FontWeight.ExtraBold else FontWeight.Normal,
                                    maxLines = 1,
                                    softWrap = false,
                                    color = PrimaryText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Footer
        Text(
            text = "${model.rows.size} rows · ${selectedCols.size} columns",
            fontSize = 13.sp,
            color = SecondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
    }
}
