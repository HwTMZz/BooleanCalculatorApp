package com.example.booleancalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.booleancalculator.ui.screens.FullTruthTableScreen
import com.example.booleancalculator.ui.screens.HowComeScreen
import com.example.booleancalculator.ui.screens.KMapSolveScreen
import com.example.booleancalculator.ui.screens.MainScreen
import com.example.booleancalculator.ui.screens.ResultOverlay
import com.example.booleancalculator.ui.screens.SimplifySolveScreen
import com.example.booleancalculator.ui.screens.TraceSolveScreen
import com.example.booleancalculator.ui.theme.BooleanCalculatorTheme
import com.example.booleancalculator.viewmodel.CalculatorViewModel
import com.example.booleancalculator.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BooleanCalculatorTheme {
                BooleanCalculatorApp()
            }
        }
    }
}

@Composable
fun BooleanCalculatorApp(
    viewModel: CalculatorViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (state.currentScreen) {
            Screen.MAIN, Screen.RESULT_OVERLAY -> {
                MainScreen(
                    state = state,
                    onAddToken = { kind, value -> viewModel.addToken(kind, value) },
                    onDeleteToken = { viewModel.deleteToken() },
                    onClearExpression = { viewModel.clearExpression() },
                    onCycleVarSet = { viewModel.cycleVarSet() },
                    onToggleNotation = { viewModel.toggleNotation() },
                    onToggleAdvance = { viewModel.toggleAdvanced() },
                    onCycleAssignment = { v -> viewModel.cycleAssignment(v) },
                    onResetAssignments = { viewModel.resetAssignments() },
                    onOpenResultOverlay = { viewModel.openResultOverlay() },
                    onSelectCursor = { idx -> viewModel.setCursorIndex(idx) }
                )

                if (state.currentScreen == Screen.RESULT_OVERLAY) {
                    ResultOverlay(
                        state = state,
                        onClose = { viewModel.closeOverlay() },
                        onOpenTraceSolve = { viewModel.openTraceSolve() },
                        onOpenFullTruthTable = { viewModel.openFullTruthTable() },
                        onOpenHowCome = { viewModel.openHowCome() },
                        onOpenSimplifySolve = { viewModel.openSimplifySolve() },
                        onOpenKMapSolve = { viewModel.openKMapSolve() }
                    )
                }
            }

            Screen.FULL_TRUTH_TABLE -> {
                FullTruthTableScreen(
                    state = state,
                    onBack = { viewModel.openResultOverlay() },
                    onFilterSelect = { filterId -> viewModel.setTruthTableFilter(filterId) }
                )
            }

            Screen.HOW_COME -> {
                HowComeScreen(
                    state = state,
                    onBack = { viewModel.openResultOverlay() },
                    onNextStep = { viewModel.nextHowComeStep() },
                    onPrevStep = { viewModel.prevHowComeStep() }
                )
            }

            Screen.TRACE_SOLVE -> {
                TraceSolveScreen(
                    state = state,
                    onBack = { viewModel.openResultOverlay() },
                    onNextStep = { viewModel.nextTraceSolveStep() },
                    onPrevStep = { viewModel.prevTraceSolveStep() }
                )
            }

            Screen.SIMPLIFY_SOLVE -> {
                SimplifySolveScreen(
                    state = state,
                    onBack = { viewModel.openResultOverlay() },
                    onNextStep = { viewModel.nextSimplifyStep() },
                    onPrevStep = { viewModel.prevSimplifyStep() }
                )
            }

            Screen.KMAP_SOLVE -> {
                KMapSolveScreen(
                    state = state,
                    onBack = { viewModel.openResultOverlay() },
                    onNextStep = { viewModel.nextKMapStep() },
                    onPrevStep = { viewModel.prevKMapStep() },
                    onToggleKMapMode = { viewModel.toggleKMapMode() }
                )
            }
        }
    }
}
