package com.example.booleancalculator.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booleancalculator.domain.engine.BooleanEvaluator
import com.example.booleancalculator.domain.model.ASTNode
import com.example.booleancalculator.domain.model.Notation
import com.example.booleancalculator.domain.model.TruthTableRow
import com.example.booleancalculator.ui.theme.BorderColor
import com.example.booleancalculator.ui.theme.CardBackground
import com.example.booleancalculator.ui.theme.ErrorRed
import com.example.booleancalculator.ui.theme.MainBackground
import com.example.booleancalculator.ui.theme.OperationActiveBg
import com.example.booleancalculator.ui.theme.OperationResultBg
import com.example.booleancalculator.ui.theme.PrimaryBlue
import com.example.booleancalculator.ui.theme.PrimaryText
import com.example.booleancalculator.ui.theme.SecondaryText
import com.example.booleancalculator.ui.theme.SuccessGreen
import com.example.booleancalculator.viewmodel.UiState
import kotlinx.coroutines.delay

@Composable
fun TraceSolveScreen(
    state: UiState,
    onBack: () -> Unit,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    val model = state.truthTableModel ?: return
    val steps = state.traceSolveSteps
    val cursor = state.traceSolveCursor
    val totalSteps = steps.size
    val activeRowIndex = state.traceSolveRowIndex
    val activeRow = model.rows.getOrNull(activeRowIndex) ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MainBackground)
            .statusBarsPadding()
    ) {
        // Top Header
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
                    text = "←",
                    fontSize = 24.sp,
                    color = PrimaryText
                )
            }

            Column(
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = "Solving Steps",
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

        Spacer(modifier = Modifier.weight(1f))

        // Step Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Expression",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (cursor == 0) {
                    Text(
                        text = BooleanEvaluator.formatAstDisplay(model.ast, state.notation),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Press Next to evaluate the expression step by step",
                        fontSize = 14.sp,
                        color = SecondaryText
                    )
                } else {
                    val activeStepIndex = (cursor - 1).coerceAtLeast(0)
                    val currentStep = steps[activeStepIndex]
                    val activeNodeId = currentStep.nodeId

                    val completedNodes = remember(cursor, activeRowIndex) {
                        val completed = mutableSetOf<String>()
                        for (i in 0 until activeStepIndex) {
                            val s = steps[i]
                            if (s.nodeId != null) completed.add(s.nodeId)
                        }
                        completed
                    }

                    var animationPhase by remember(cursor) { mutableIntStateOf(1) }

                    LaunchedEffect(cursor) {
                        animationPhase = 1
                        delay(250)
                        animationPhase = 2
                        delay(500)
                        animationPhase = 3
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TraceExpressionNode(
                            node = model.ast,
                            row = activeRow,
                            completedNodes = completedNodes,
                            activeNodeId = activeNodeId,
                            animationPhase = animationPhase,
                            notation = state.notation
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Operation $cursor of $totalSteps · ${currentStep.type}",
                        fontSize = 13.sp,
                        color = SecondaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footer Step Controls (Hidden when finished)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val noteText = when {
                cursor == 0 -> "Ready · $totalSteps operation(s)"
                cursor >= totalSteps -> "All $totalSteps operations completed"
                else -> "$cursor of $totalSteps operations completed"
            }

            Text(
                text = noteText,
                fontSize = 14.sp,
                color = SecondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (cursor < totalSteps) 10.dp else 0.dp)
            )

            val canNext = cursor < totalSteps
            if (canNext) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (cursor > 0) {
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
                            text = "Next →",
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

@Composable
private fun TraceExpressionNode(
    node: ASTNode,
    row: TruthTableRow,
    completedNodes: Set<String>,
    activeNodeId: String?,
    animationPhase: Int,
    notation: Notation,
    parentPrec: Int = 0
) {
    if (completedNodes.contains(node.id)) {
        val evaluatedVal = row.values[node.id] ?: 0
        Text(
            text = evaluatedVal.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
        )
        return
    }

    if (node.id == activeNodeId) {
        val targetBg = when (animationPhase) {
            2 -> OperationActiveBg
            3 -> OperationResultBg
            else -> Color.Transparent
        }
        val targetTextCol = when (animationPhase) {
            2 -> ErrorRed
            3 -> SuccessGreen
            else -> PrimaryText
        }

        val animatedBg by animateColorAsState(targetValue = targetBg, animationSpec = tween(durationMillis = 350), label = "bg")
        val animatedTextCol by animateColorAsState(targetValue = targetTextCol, animationSpec = tween(durationMillis = 350), label = "textCol")

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(animatedBg)
                .animateContentSize(animationSpec = tween(durationMillis = 350))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (animationPhase == 3) {
                val evaluatedVal = row.values[node.id] ?: 0
                Text(
                    text = evaluatedVal.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = animatedTextCol
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RenderTraceNodeContents(
                        node = node,
                        row = row,
                        completedNodes = completedNodes,
                        activeNodeId = activeNodeId,
                        animationPhase = animationPhase,
                        notation = notation,
                        parentPrec = parentPrec,
                        overrideTextColor = if (animationPhase == 2) animatedTextCol else null
                    )
                }
            }
        }
        return
    }

    RenderTraceNodeContents(
        node = node,
        row = row,
        completedNodes = completedNodes,
        activeNodeId = activeNodeId,
        animationPhase = animationPhase,
        notation = notation,
        parentPrec = parentPrec,
        overrideTextColor = null
    )
}

@Composable
private fun RenderTraceNodeContents(
    node: ASTNode,
    row: TruthTableRow,
    completedNodes: Set<String>,
    activeNodeId: String?,
    animationPhase: Int,
    notation: Notation,
    parentPrec: Int,
    overrideTextColor: Color?
) {
    val defaultTextCol = overrideTextColor ?: PrimaryText

    when (node) {
        is ASTNode.VarNode -> {
            val v = row.bindings[node.name] ?: 0
            Text(
                text = v.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = defaultTextCol
            )
        }
        is ASTNode.NumNode -> {
            Text(
                text = node.value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = defaultTextCol
            )
        }
        is ASTNode.NotNode -> {
            val opSymbol = if (notation == Notation.SYMBOL) "!" else "NOT "
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = opSymbol,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = defaultTextCol
                )
                TraceExpressionNode(
                    node = node.child,
                    row = row,
                    completedNodes = completedNodes,
                    activeNodeId = activeNodeId,
                    animationPhase = animationPhase,
                    notation = notation,
                    parentPrec = 4
                )
            }
        }
        is ASTNode.BinaryOpNode -> {
            val precMap = mapOf("OR" to 1, "NOR" to 1, "XOR" to 2, "XNOR" to 2, "AND" to 3, "NAND" to 3)
            val currentPrec = precMap[node.type.keyword] ?: 3
            val needsWrap = currentPrec < parentPrec

            val opSymbol = BooleanEvaluator.opDisplay(node.type, notation)

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (needsWrap) {
                    Text(text = "(", fontSize = 20.sp, color = defaultTextCol)
                }
                TraceExpressionNode(
                    node = node.left,
                    row = row,
                    completedNodes = completedNodes,
                    activeNodeId = activeNodeId,
                    animationPhase = animationPhase,
                    notation = notation,
                    parentPrec = currentPrec
                )
                Text(
                    text = " $opSymbol ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = defaultTextCol
                )
                TraceExpressionNode(
                    node = node.right,
                    row = row,
                    completedNodes = completedNodes,
                    activeNodeId = activeNodeId,
                    animationPhase = animationPhase,
                    notation = notation,
                    parentPrec = currentPrec
                )
                if (needsWrap) {
                    Text(text = ")", fontSize = 20.sp, color = defaultTextCol)
                }
            }
        }
    }
}
