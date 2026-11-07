package com.example.booleancalculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.booleancalculator.domain.engine.BooleanEvaluator
import com.example.booleancalculator.ui.components.KMapView
import com.example.booleancalculator.ui.theme.BorderColor
import com.example.booleancalculator.ui.theme.CardBackground
import com.example.booleancalculator.ui.theme.OverlayBackdrop
import com.example.booleancalculator.ui.theme.PrimaryBlue
import com.example.booleancalculator.ui.theme.PrimaryText
import com.example.booleancalculator.ui.theme.SecondaryText
import com.example.booleancalculator.ui.theme.TableHighlight
import com.example.booleancalculator.viewmodel.UiState
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp as spUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultOverlay(
    state: UiState,
    onClose: () -> Unit,
    onOpenTraceSolve: () -> Unit,
    onOpenFullTruthTable: () -> Unit,
    onOpenHowCome: () -> Unit,
    onOpenSimplifySolve: () -> Unit,
    onOpenKMapSolve: () -> Unit,
    modifier: Modifier = Modifier
) {
    val model = state.truthTableModel ?: return
    val ast = state.ast ?: return
    val vars = model.vars

    // High Performance: Cache evaluation calculations with remember
    val hasAssigned = remember(vars, state.assignments) {
        vars.any { v -> state.assignments[v] == "0" || state.assignments[v] == "1" }
    }

    val (assignStr, calcValue) = remember(ast, vars, state.assignments) {
        val bindings = vars.associateWith { v ->
            val a = state.assignments[v]
            if (a == "0" || a == "1") a.toInt() else 0
        }
        val detailed = BooleanEvaluator.evalBooleanDetailed(ast, bindings)
        val aStr = vars.map { "$it:${bindings[it]}" }.joinToString(", ")
        Pair(aStr, detailed.value)
    }

    val canonicalExpr = remember(ast, state.notation) {
        BooleanEvaluator.formatAstDisplay(ast, state.notation)
    }

    val simplifiedExpr = remember(state.simplifyResult) {
        state.simplifyResult?.expr ?: ""
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun dismissSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onClose()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { onClose() },
        sheetState = sheetState,
        scrimColor = OverlayBackdrop,
        containerColor = Color.Transparent,
        contentColor = PrimaryText,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = CardBackground.copy(alpha = 0.8f),
                width = 38.dp,
                height = 5.dp
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Result",
                    color = CardBackground,
                    fontSize = 30.spUnit,
                    fontWeight = FontWeight.ExtraBold
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CardBackground)
                        .clickable { dismissSheet() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        color = PrimaryText,
                        fontSize = 16.spUnit,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Cards Scroll View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card 1: Calculate Expressions (if variable assigned)
                if (hasAssigned) {
                    ResultCard {
                        Text(
                            text = "Calculate Expressions",
                            fontSize = 20.spUnit,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryText
                        )
                        Text(
                            text = assignStr,
                            fontSize = 14.spUnit,
                            color = SecondaryText,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )
                        Text(
                            text = "$canonicalExpr = $calcValue",
                            fontSize = 18.spUnit,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        CardPrimaryButton(
                            text = "Show Solving Steps",
                            onClick = onOpenTraceSolve
                        )
                    }
                }

                // Card 2: Full Truth Table Preview
                ResultCard {
                    Text(
                        text = "Full Truth Table",
                        fontSize = 20.spUnit,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Preview Table (first 8 rows)
                    val previewRows = remember(model) { model.rows.take(8) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TableHighlight)
                        ) {
                            vars.forEach { v ->
                                Text(
                                    text = v,
                                    modifier = Modifier.weight(1f).padding(8.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.spUnit,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryText
                                )
                            }
                            Text(
                                text = "F",
                                modifier = Modifier.weight(1f).padding(8.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 14.spUnit,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        }

                        // Table Rows
                        previewRows.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.5.dp, BorderColor)
                            ) {
                                row.bits.forEach { b ->
                                    Text(
                                        text = b.toString(),
                                        modifier = Modifier.weight(1f).padding(6.dp),
                                        textAlign = TextAlign.Center,
                                        fontSize = 14.spUnit,
                                        color = PrimaryText
                                    )
                                }
                                Text(
                                    text = row.result.toString(),
                                    modifier = Modifier.weight(1f).padding(6.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.spUnit,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    CardPrimaryButton(
                        text = "Show Full Table",
                        onClick = onOpenFullTruthTable
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CardSecondaryButton(
                        text = "How'd it come?  ➔",
                        onClick = onOpenHowCome
                    )
                }

                // Card 3: Simplify Expressions
                ResultCard {
                    Text(
                        text = "Simplify Expressions",
                        fontSize = 20.spUnit,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText
                    )
                    Text(
                        text = "$canonicalExpr\n↓\n$simplifiedExpr",
                        fontSize = 16.spUnit,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    CardPrimaryButton(
                        text = "Show Solving Steps",
                        onClick = onOpenSimplifySolve
                    )
                }

                // Card 4: K-Map Preview (2-4 variables)
                val kmap = state.kMapModel
                if (kmap != null && vars.size in 2..4) {
                    ResultCard {
                        Text(
                            text = "K-Map",
                            fontSize = 20.spUnit,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryText,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            KMapView(kmap = kmap)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        CardPrimaryButton(
                            text = "Show Solving Steps",
                            onClick = onOpenKMapSolve
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun CardPrimaryButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PrimaryBlue)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = CardBackground,
            fontSize = 16.spUnit,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CardSecondaryButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.5.dp, BorderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = SecondaryText,
            fontSize = 15.spUnit,
            fontWeight = FontWeight.SemiBold
        )
    }
}
