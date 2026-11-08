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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booleancalculator.domain.engine.BooleanEvaluator
import com.example.booleancalculator.ui.components.AnimatedExpressionTree
import com.example.booleancalculator.ui.theme.BorderColor
import com.example.booleancalculator.ui.theme.CardBackground
import com.example.booleancalculator.ui.theme.MainBackground
import com.example.booleancalculator.ui.theme.PrimaryBlue
import com.example.booleancalculator.ui.theme.PrimaryText
import com.example.booleancalculator.ui.theme.SecondaryText
import com.example.booleancalculator.ui.theme.TableHighlight
import com.example.booleancalculator.viewmodel.UiState
import kotlinx.coroutines.delay

@Composable
fun HowComeScreen(
    state: UiState,
    onBack: () -> Unit,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    val model = state.truthTableModel ?: return
    val progress = state.howComeProgress ?: return
    val totalSteps = progress.steps.size
    val currentCursor = progress.cursor

    // 1. Persistent Fixed Header
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MainBackground)
            .statusBarsPadding()
    ) {
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
                    text = "Truth Table Solving",
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

        // 2. Main Body: Expression Section (A) + Truth Table Section (B)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section A: Expression Display Section
            if (currentCursor == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
//                        .background(CardBackground)
//                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
//                        .padding(16.dp)
                ) {
                    Column {
//                        Text(
//                            text = "Before we begin",
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = PrimaryText
//                        )
                        Text(
                            text = BooleanEvaluator.formatAstDisplay(model.ast, state.notation),
                            fontSize = 25.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                        Text(
                            text = "This expression contains ${model.vars.size} variable(s) (${model.vars.joinToString(", ")}). Therefore, it has 2^${model.vars.size} = ${model.rows.size} possible Boolean combinations.",
                            fontSize = 14.sp,
                            color = SecondaryText,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Operation Order:",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
//                        Text(
//                            text = BooleanEvaluator.getEvaluationOrderExplanation(model.ast, state.notation),
//                            fontSize = 13.sp,
//                            color = SecondaryText,
//                            modifier = Modifier.padding(vertical = 4.dp)
//                        )
                        progress.operationOrder.forEachIndexed { idx, node ->
                            Text(
                                text = "${idx + 1}. ${BooleanEvaluator.nodeLabel(node, state.notation)}",
                                fontSize = 14.sp,
                                color = SecondaryText,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                        }
                    }
                }
            } else {
                val activeStepIndex = (currentCursor - 1).coerceAtLeast(0)
                val currentStep = progress.steps[activeStepIndex]
                val activeRowIndex = currentStep.rowIndex
                val activeRow = model.rows[activeRowIndex]
                val activeNodeId = currentStep.nodeId

                val completedNodes = remember(currentCursor, activeRowIndex) {
                    val completed = mutableSetOf<String>()
                    for (i in 0 until activeStepIndex) {
                        val step = progress.steps[i]
                        if (step.rowIndex == activeRowIndex && step.nodeId != null) {
                            completed.add(step.nodeId)
                        }
                    }
                    completed
                }

                var animationPhase by remember(currentCursor) { mutableIntStateOf(1) }

                LaunchedEffect(currentCursor) {
                    animationPhase = 1
                    delay(250)
                    animationPhase = 2
                    delay(500)
                    animationPhase = 3
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground)
                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Expression",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedExpressionTree(
                                node = model.ast,
                                row = activeRow,
                                completedNodes = completedNodes,
                                activeNodeId = activeNodeId,
                                animationPhase = animationPhase,
                                notation = state.notation
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val opLabel = currentStep.type
                        Text(
                            text = "Row ${activeRowIndex + 1} of ${model.rows.size} · $opLabel",
                            fontSize = 13.sp,
                            color = SecondaryText
                        )
                    }
                }
            }

            // Section B: Truth Table Display Section
            Text(
                text = "Truth Table",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryText
            )

            val activeRowIdx = if (currentCursor > 0) progress.steps[(currentCursor - 1).coerceAtLeast(0)].rowIndex else null
            val tableScrollState = rememberScrollState()

            val density = LocalDensity.current
            LaunchedEffect(activeRowIdx) {
                if (activeRowIdx != null && activeRowIdx >= 0) {
                    val rowHeightPx = with(density) { 36.dp.toPx() }
                    val viewportHeightPx = with(density) { 220.dp.toPx() }
                    val targetScroll = ((activeRowIdx + 2) * rowHeightPx - viewportHeightPx).coerceAtLeast(0f)
                    tableScrollState.animateScrollTo(targetScroll.toInt())
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(max = 260.dp)
                    .fillMaxWidth()
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground)
                    .verticalScroll(tableScrollState)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TableHighlight)
                ) {
                    model.vars.forEach { v ->
                        Text(
                            text = v,
                            modifier = Modifier.weight(1f).padding(8.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText
                        )
                    }
                    Text(
                        text = "Result",
                        modifier = Modifier.weight(1f).padding(8.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }

                model.rows.forEachIndexed { rIdx, row ->
                    val isRevealed = activeRowIdx != null && rIdx <= activeRowIdx
                    val isActive = activeRowIdx != null && rIdx == activeRowIdx

                    val isRowComplete = if (isActive && currentCursor > 0) {
                        val step = progress.steps[currentCursor - 1]
                        step.rowIndex == rIdx && step.nodeId == model.ast.id
                    } else rIdx < (activeRowIdx ?: -1)

                    if (isRevealed) {
                        val bg = if (isActive) TableHighlight else CardBackground

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bg)
                                .border(0.5.dp, BorderColor),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            row.bits.forEach { b ->
                                Text(
                                    text = b.toString(),
                                    modifier = Modifier.weight(1f).padding(8.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    color = PrimaryText
                                )
                            }
                            Text(
                                text = if (isRowComplete) row.result.toString() else "",
                                modifier = Modifier.weight(1f).padding(8.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        }
                    }
                }
            }
        }

        // 3. Persistent Fixed Footer Navigation Controls (Leave only Previous when finished, remove Done/Next)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val noteText = when {
                currentCursor == 0 -> "$totalSteps operations across ${model.rows.size} rows"
                currentCursor >= totalSteps -> "All $totalSteps operations completed"
                else -> "Step $currentCursor of $totalSteps completed"
            }

            Text(
                text = noteText,
                fontSize = 14.sp,
                color = SecondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (currentCursor > 0) 10.dp else 0.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (currentCursor > 0) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(CardBackground)
                            .border(1.5.dp, BorderColor, RoundedCornerShape(22.dp))
                            .clickable { onPrevStep() }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "← Previous",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText
                        )
                    }
                }

                val canNext = currentCursor < totalSteps
                if (canNext) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(PrimaryBlue)
                            .border(1.5.dp, PrimaryBlue, RoundedCornerShape(22.dp))
                            .clickable { onNextStep() }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Next Step →",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CardBackground
                        )
                    }
                }
            }
        }
    }
}


