package com.example.booleancalculator.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.booleancalculator.domain.engine.BooleanEvaluator
import com.example.booleancalculator.ui.components.AnswerDisplay
import com.example.booleancalculator.ui.components.ControlRow
import com.example.booleancalculator.ui.components.CustomKeyboard
import com.example.booleancalculator.ui.components.ExpressionDisplay
import com.example.booleancalculator.ui.components.MainActionButton
import com.example.booleancalculator.ui.components.TopStatusSpacer
import com.example.booleancalculator.ui.components.VariableAssignmentRow
import com.example.booleancalculator.ui.theme.MainBackground
import com.example.booleancalculator.viewmodel.Screen
import com.example.booleancalculator.viewmodel.UiState

@Composable
fun MainScreen(
    state: UiState,
    onAddToken: (kind: String, value: String) -> Unit,
    onDeleteToken: () -> Unit,
    onClearExpression: () -> Unit,
    onCycleVarSet: () -> Unit,
    onToggleNotation: () -> Unit,
    onToggleAdvance: () -> Unit,
    onCycleAssignment: (String) -> Unit,
    onResetAssignments: () -> Unit,
    onOpenResultOverlay: () -> Unit,
    onSelectCursor: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverlayOpen = state.currentScreen == Screen.RESULT_OVERLAY
    val alpha by animateFloatAsState(
        targetValue = if (isOverlayOpen) 0.6f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "backdropAlpha"
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MainBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // WindowInsets()
            TopStatusSpacer()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                ExpressionDisplay(
                    tokens = state.tokens,
                    notation = state.notation,
                    cursorIndex = state.cursorIndex,
                    errorText = state.error,
                    onClear = onClearExpression,
                    onSelectCursor = onSelectCursor
                )

                val ast = state.ast
                if (ast != null) {
                    val vars = remember(ast) { BooleanEvaluator.extractVars(ast) }
                    val hasAssigned = remember(vars, state.assignments) {
                        vars.any { v -> state.assignments[v] == "0" || state.assignments[v] == "1" }
                    }
                    if (hasAssigned) {
                        val (tupleStr, answerVal) = remember(ast, vars, state.assignments) {
                            val bindings = vars.associateWith { v ->
                                val a = state.assignments[v]
                                if (a == "0" || a == "1") a.toInt() else 0
                            }
                            val detailed = BooleanEvaluator.evalBooleanDetailed(ast, bindings)
                            val tStr = vars.joinToString(", ") { v -> "$v:${bindings[v]}" }
                            Pair(tStr, detailed.value)
                        }

                        AnswerDisplay(
                            tupleText = tupleStr,
                            answerValue = answerVal
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                MainActionButton(
                    enabled = state.ast != null,
                    onClick = onOpenResultOverlay
                )
            }

            // Bottom Panel (with bottom spacing / navigation bars padding)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                VariableAssignmentRow(
                    varSet = state.varSet,
                    assignments = state.assignments,
                    onToggleVar = onCycleAssignment,
                    onReset = onResetAssignments
                )

                ControlRow(
                    varSet = state.varSet,
                    notation = state.notation,
                    isAdvanced = state.isAdvanced,
                    onCycleVarSet = onCycleVarSet,
                    onToggleNotation = onToggleNotation,
                    onToggleAdvance = onToggleAdvance,
                    onDelete = onDeleteToken
                )

                CustomKeyboard(
                    varSet = state.varSet,
                    notation = state.notation,
                    isAdvanced = state.isAdvanced,
                    onKeyClick = onAddToken
                )
            }
        }

        // Floating darkening rectangle on top of all other elements in MainScreen (except ResultOverlay)
        if (alpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = alpha))
            )
        }
    }
}
