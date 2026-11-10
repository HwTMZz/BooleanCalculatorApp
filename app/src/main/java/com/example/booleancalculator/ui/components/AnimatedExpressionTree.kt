package com.example.booleancalculator.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booleancalculator.domain.engine.BooleanEvaluator
import com.example.booleancalculator.domain.model.ASTNode
import com.example.booleancalculator.domain.model.Notation
import com.example.booleancalculator.domain.model.TruthTableRow
import com.example.booleancalculator.ui.theme.ErrorRed
import com.example.booleancalculator.ui.theme.OperationActiveBg
import com.example.booleancalculator.ui.theme.OperationResultBg
import com.example.booleancalculator.ui.theme.PrimaryText
import com.example.booleancalculator.ui.theme.SuccessGreen

var Font_size:Int = 20

@Composable
fun AnimatedExpressionTree(
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
            fontSize = Font_size.sp,
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

        val animatedBg by animateColorAsState(targetValue = targetBg, animationSpec = tween(durationMillis = 300), label = "bg")
        val animatedTextCol by animateColorAsState(targetValue = targetTextCol, animationSpec = tween(durationMillis = 300), label = "textCol")

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
                    fontSize = Font_size.sp,
                    fontWeight = FontWeight.Bold,
                    color = animatedTextCol
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RenderNodeContents(
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

    RenderNodeContents(
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
private fun RenderNodeContents(
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
                fontSize = Font_size.sp,
                fontWeight = FontWeight.Medium,
                color = defaultTextCol
            )
        }
        is ASTNode.NumNode -> {
            Text(
                text = node.value,
                fontSize = Font_size.sp,
                fontWeight = FontWeight.Medium,
                color = defaultTextCol
            )
        }
        is ASTNode.NotNode -> {
            val opSymbol = if (notation == Notation.SYMBOL) "!" else "NOT "
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = opSymbol,
                    fontSize = Font_size.sp,
                    fontWeight = FontWeight.Bold,
                    color = defaultTextCol
                )
                AnimatedExpressionTree(
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
                    Text(text = "(", fontSize = Font_size.sp, color = defaultTextCol)
                }
                AnimatedExpressionTree(
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
                    fontSize = Font_size.sp,
                    fontWeight = FontWeight.Bold,
                    color = defaultTextCol
                )
                AnimatedExpressionTree(
                    node = node.right,
                    row = row,
                    completedNodes = completedNodes,
                    activeNodeId = activeNodeId,
                    animationPhase = animationPhase,
                    notation = notation,
                    parentPrec = currentPrec
                )
                if (needsWrap) {
                    Text(text = ")", fontSize = Font_size.sp, color = defaultTextCol)
                }
            }
        }
    }
}
