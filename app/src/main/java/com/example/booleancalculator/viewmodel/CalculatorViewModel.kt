package com.example.booleancalculator.viewmodel

import androidx.lifecycle.ViewModel
import com.example.booleancalculator.domain.engine.BooleanEvaluator
import com.example.booleancalculator.domain.engine.KMapEngine
import com.example.booleancalculator.domain.engine.Parser
import com.example.booleancalculator.domain.engine.SimplificationEngine
import com.example.booleancalculator.domain.engine.Tokenizer
import com.example.booleancalculator.domain.model.ASTNode
import com.example.booleancalculator.domain.model.HowComeProgress
import com.example.booleancalculator.domain.model.HowComeRowRange
import com.example.booleancalculator.domain.model.HowComeStep
import com.example.booleancalculator.domain.model.IdentityStep
import com.example.booleancalculator.domain.model.KMapMode
import com.example.booleancalculator.domain.model.KMapModel
import com.example.booleancalculator.domain.model.Notation
import com.example.booleancalculator.domain.model.OpType
import com.example.booleancalculator.domain.model.SimplificationResult
import com.example.booleancalculator.domain.model.Token
import com.example.booleancalculator.domain.model.TruthTableModel
import com.example.booleancalculator.domain.model.VarSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class Screen {
    MAIN,
    RESULT_OVERLAY,
    FULL_TRUTH_TABLE,
    HOW_COME,
    TRACE_SOLVE,
    SIMPLIFY_SOLVE,
    KMAP_SOLVE
}

data class UiState(
    val varSet: VarSet = VarSet.WXYZ,
    val notation: Notation = Notation.WORD,
    val isAdvanced: Boolean = false,
    val tokens: List<Token> = emptyList(),
    val cursorIndex: Int = 0,
    val assignments: Map<String, String> = mapOf("W" to "*", "X" to "*", "Y" to "*", "Z" to "*"),
    val isAssignedInitialized: Boolean = false,
    val error: String? = null,
    val ast: ASTNode? = null,
    val truthTableModel: TruthTableModel? = null,
    val truthTableFilter: String = "all",
    val currentScreen: Screen = Screen.MAIN,
    val howComeProgress: HowComeProgress? = null,
    val traceSolveCursor: Int = 0,
    val traceSolveSteps: List<HowComeStep> = emptyList(),
    val traceSolveRowIndex: Int = 0,
    val simplifyResult: SimplificationResult? = null,
    val identitySteps: List<IdentityStep> = emptyList(),
    val simplifyStep: Int = 1,
    val kMapModel: KMapModel? = null,
    val kMapSolveStep: Int = 0,
    val kMapMode: KMapMode = KMapMode.SOP
)

class CalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun addToken(kind: String, value: String = "") {
        _uiState.update { currentState ->
            val mutableTokens = currentState.tokens.toMutableList()
            val insertAt = currentState.cursorIndex.coerceIn(0, mutableTokens.size)

            when (kind) {
                "PAREN" -> {
                    val openCount = mutableTokens.count { it is Token.LParen }
                    val closeCount = mutableTokens.count { it is Token.RParen }
                    val newToken = if (openCount > closeCount) Token.RParen(insertAt) else Token.LParen(insertAt)
                    mutableTokens.add(insertAt, newToken)
                }
                "LPAREN" -> mutableTokens.add(insertAt, Token.LParen(insertAt))
                "RPAREN" -> mutableTokens.add(insertAt, Token.RParen(insertAt))
                "VAR" -> mutableTokens.add(insertAt, Token.Var(value, insertAt))
                "NUM" -> mutableTokens.add(insertAt, Token.Num(value, insertAt))
                else -> {
                    val opType = try { OpType.valueOf(kind) } catch (e: Exception) { null }
                    if (opType != null) {
                        mutableTokens.add(insertAt, Token.Op(opType, insertAt))
                    }
                }
            }

            val newCursor = insertAt + 1
            val updatedState = currentState.copy(
                tokens = mutableTokens,
                cursorIndex = newCursor,
                error = null
            )
            reparse(updatedState)
        }
    }

    fun deleteToken() {
        _uiState.update { currentState ->
            val mutableTokens = currentState.tokens.toMutableList()
            if (currentState.cursorIndex > 0 && mutableTokens.isNotEmpty()) {
                val deleteAt = (currentState.cursorIndex - 1).coerceIn(0, mutableTokens.size - 1)
                mutableTokens.removeAt(deleteAt)
                val updatedState = currentState.copy(
                    tokens = mutableTokens,
                    cursorIndex = deleteAt,
                    error = null
                )
                reparse(updatedState)
            } else {
                currentState
            }
        }
    }

    fun setCursorIndex(index: Int) {
        _uiState.update { currentState ->
            currentState.copy(cursorIndex = index.coerceIn(0, currentState.tokens.size))
        }
    }

    fun clearExpression() {
        _uiState.update { currentState ->
            currentState.copy(
                tokens = emptyList(),
                cursorIndex = 0,
                error = null,
                ast = null,
                truthTableModel = null,
                simplifyResult = null,
                kMapModel = null,
                howComeProgress = null,
                traceSolveSteps = emptyList(),
                currentScreen = Screen.MAIN
            )
        }
    }

    fun cycleVarSet() {
        _uiState.update { currentState ->
            val varSetValues = VarSet.entries
            val nextIdx = (varSetValues.indexOf(currentState.varSet) + 1) % varSetValues.size
            val nextVarSet = varSetValues[nextIdx]

            val currentVars = currentState.varSet.vars
            val nextVars = nextVarSet.vars

            val updatedTokens = currentState.tokens.map { token ->
                if (token is Token.Var) {
                    val idx = currentVars.indexOf(token.value)
                    if (idx >= 0 && idx < nextVars.size) Token.Var(nextVars[idx], token.pos) else token
                } else token
            }

            val updatedAssignments = mutableMapOf<String, String>()
            nextVars.forEachIndexed { idx, v ->
                val prevKey = if (idx < currentVars.size) currentVars[idx] else null
                updatedAssignments[v] = if (prevKey != null) currentState.assignments[prevKey] ?: "*" else "*"
            }

            val updatedState = currentState.copy(
                varSet = nextVarSet,
                tokens = updatedTokens,
                assignments = updatedAssignments,
                truthTableModel = null,
                simplifyResult = null,
                kMapModel = null,
                howComeProgress = null,
                traceSolveSteps = emptyList()
            )
            reparse(updatedState)
        }
    }

    fun toggleNotation() {
        _uiState.update { currentState ->
            currentState.copy(notation = if (currentState.notation == Notation.WORD) Notation.SYMBOL else Notation.WORD)
        }
    }

    fun toggleAdvanced() {
        _uiState.update { currentState ->
            currentState.copy(isAdvanced = !currentState.isAdvanced)
        }
    }

    fun cycleAssignment(variable: String) {
        _uiState.update { currentState ->
            val newAssignments = currentState.assignments.toMutableMap()
            var newInitialized = currentState.isAssignedInitialized

            if (!newInitialized) {
                // Initialize all variables in current varset to 0 first time
                currentState.varSet.vars.forEach { v -> newAssignments[v] = "0" }
                newAssignments[variable] = "1"
                newInitialized = true
            } else {
                val currentVal = newAssignments[variable] ?: "0"
                newAssignments[variable] = if (currentVal == "0") "1" else "0"
            }

            currentState.copy(
                assignments = newAssignments,
                isAssignedInitialized = newInitialized,
                truthTableModel = null,
                howComeProgress = null,
                traceSolveSteps = emptyList()
            )
        }
    }

    fun resetAssignments() {
        _uiState.update { currentState ->
            val resetMap = currentState.varSet.vars.associateWith { "*" }
            currentState.copy(
                assignments = resetMap,
                isAssignedInitialized = false,
                truthTableModel = null,
                howComeProgress = null,
                traceSolveSteps = emptyList()
            )
        }
    }

    fun openResultOverlay() {
        _uiState.update { currentState ->
            if (currentState.ast == null) return@update currentState
            val model = BooleanEvaluator.buildTruthTableModel(currentState.ast, BooleanEvaluator.extractVars(currentState.ast))
            val simp = SimplificationEngine.simplifyExpression(currentState.ast, model.vars)
            val kmap = if (model.vars.size in 2..4) KMapEngine.buildKMap(currentState.ast, model.vars) else null

            currentState.copy(
                truthTableModel = model,
                simplifyResult = simp,
                kMapModel = kmap,
                truthTableFilter = "all",
                currentScreen = Screen.RESULT_OVERLAY
            )
        }
    }

    fun closeOverlay() {
        _uiState.update { currentState ->
            currentState.copy(currentScreen = Screen.MAIN)
        }
    }

    fun openFullTruthTable() {
        _uiState.update { currentState ->
            val ast = currentState.ast ?: return@update currentState
            val model = BooleanEvaluator.buildTruthTableModel(ast, BooleanEvaluator.extractVars(ast))
            currentState.copy(
                truthTableModel = model,
                truthTableFilter = "all",
                currentScreen = Screen.FULL_TRUTH_TABLE
            )
        }
    }

    fun setTruthTableFilter(filterId: String) {
        _uiState.update { currentState ->
            currentState.copy(truthTableFilter = filterId)
        }
    }

    fun openHowCome() {
        _uiState.update { currentState ->
            val ast = currentState.ast ?: return@update currentState
            val model = BooleanEvaluator.buildTruthTableModel(ast, BooleanEvaluator.extractVars(ast))
            val progress = createHowComeProgress(model, currentState.notation)
            currentState.copy(
                truthTableModel = model,
                howComeProgress = progress,
                currentScreen = Screen.HOW_COME
            )
        }
    }

    fun nextHowComeStep() {
        _uiState.update { currentState ->
            val progress = currentState.howComeProgress ?: return@update currentState
            if (progress.cursor >= progress.steps.size) return@update currentState
            val newCursor = progress.cursor + 1
            val currentStep = progress.steps[newCursor - 1]
            val updatedProgress = progress.copy(
                cursor = newCursor,
                activePhase = 3,
                activeRowIndex = currentStep.rowIndex
            )
            currentState.copy(howComeProgress = updatedProgress)
        }
    }

    fun prevHowComeStep() {
        _uiState.update { currentState ->
            val progress = currentState.howComeProgress ?: return@update currentState
            if (progress.cursor <= 0) return@update currentState
            val newCursor = progress.cursor - 1
            val prevStep = if (newCursor > 0) progress.steps[newCursor - 1] else null
            val updatedProgress = progress.copy(
                cursor = newCursor,
                activePhase = if (newCursor == 0) 0 else 3,
                activeRowIndex = prevStep?.rowIndex
            )
            currentState.copy(howComeProgress = updatedProgress)
        }
    }

    fun openTraceSolve() {
        _uiState.update { currentState ->
            val ast = currentState.ast ?: return@update currentState
            val vars = BooleanEvaluator.extractVars(ast)
            val bindings = vars.associateWith { v ->
                val a = currentState.assignments[v]
                if (a == "0" || a == "1") a.toInt() else 0
            }
            val model = BooleanEvaluator.buildTruthTableModel(ast, vars)
            val rowIndex = model.rows.indexOfFirst { row -> vars.all { v -> row.bindings[v] == bindings[v] } }.coerceAtLeast(0)
            val selectedRow = model.rows[rowIndex]

            val operations = model.analysis.opNodes
            val steps = if (operations.isNotEmpty()) {
                operations.map { node ->
                    HowComeStep(
                        rowIndex = rowIndex,
                        nodeId = node.id,
                        result = selectedRow.values[node.id] ?: 0,
                        type = BooleanEvaluator.nodeLabel(node, currentState.notation)
                    )
                }
            } else {
                listOf(
                    HowComeStep(
                        rowIndex = rowIndex,
                        nodeId = null,
                        result = selectedRow.result,
                        type = "RESULT"
                    )
                )
            }

            currentState.copy(
                truthTableModel = model,
                traceSolveSteps = steps,
                traceSolveRowIndex = rowIndex,
                traceSolveCursor = 0,
                currentScreen = Screen.TRACE_SOLVE
            )
        }
    }

    fun nextTraceSolveStep() {
        _uiState.update { currentState ->
            if (currentState.traceSolveCursor >= currentState.traceSolveSteps.size) return@update currentState
            currentState.copy(traceSolveCursor = currentState.traceSolveCursor + 1)
        }
    }

    fun prevTraceSolveStep() {
        _uiState.update { currentState ->
            if (currentState.traceSolveCursor <= 0) return@update currentState
            currentState.copy(traceSolveCursor = currentState.traceSolveCursor - 1)
        }
    }

    fun openSimplifySolve() {
        _uiState.update { currentState ->
            val ast = currentState.ast ?: return@update currentState
            val steps = SimplificationEngine.buildIdentitySimplificationSteps(ast, currentState.notation)
            currentState.copy(
                identitySteps = steps,
                simplifyStep = 1,
                currentScreen = Screen.SIMPLIFY_SOLVE
            )
        }
    }

    fun nextSimplifyStep() {
        _uiState.update { currentState ->
            if (currentState.simplifyStep >= currentState.identitySteps.size) return@update currentState
            currentState.copy(simplifyStep = currentState.simplifyStep + 1)
        }
    }

    fun prevSimplifyStep() {
        _uiState.update { currentState ->
            if (currentState.simplifyStep <= 1) return@update currentState
            currentState.copy(simplifyStep = currentState.simplifyStep - 1)
        }
    }

    fun openKMapSolve() {
        _uiState.update { currentState ->
            val ast = currentState.ast ?: return@update currentState
            val vars = BooleanEvaluator.extractVars(ast)
            val kmap = KMapEngine.buildKMap(ast, vars)
            currentState.copy(
                kMapModel = kmap,
                kMapSolveStep = 0,
                currentScreen = Screen.KMAP_SOLVE
            )
        }
    }

    fun nextKMapStep() {
        _uiState.update { currentState ->
            if (currentState.kMapSolveStep >= 7) return@update currentState
            currentState.copy(kMapSolveStep = currentState.kMapSolveStep + 1)
        }
    }

    fun prevKMapStep() {
        _uiState.update { currentState ->
            if (currentState.kMapSolveStep <= 0) return@update currentState
            currentState.copy(kMapSolveStep = currentState.kMapSolveStep - 1)
        }
    }

    fun toggleKMapMode() {
        _uiState.update { currentState ->
            currentState.copy(kMapMode = if (currentState.kMapMode == KMapMode.SOP) KMapMode.POS else KMapMode.SOP)
        }
    }

    private fun reparse(state: UiState): UiState {
        if (state.tokens.isEmpty()) {
            return state.copy(
                error = null,
                ast = null,
                truthTableModel = null,
                simplifyResult = null,
                kMapModel = null,
                howComeProgress = null,
                traceSolveSteps = emptyList()
            )
        }
        return try {
            val parseStr = state.tokens.joinToString(" ") { token ->
                when (token) {
                    is Token.Var -> token.value
                    is Token.Num -> token.value
                    is Token.LParen -> "("
                    is Token.RParen -> ")"
                    is Token.Op -> token.type.keyword
                }
            }
            val parsedTokens = Tokenizer.tokenize(parseStr, "VAR")
            val ast = Parser.parse(parsedTokens)
            state.copy(
                error = null,
                ast = ast,
                truthTableModel = null,
                simplifyResult = null,
                kMapModel = null,
                howComeProgress = null,
                traceSolveSteps = emptyList()
            )
        } catch (e: Exception) {
            state.copy(
                error = e.message,
                ast = null,
                truthTableModel = null,
                simplifyResult = null,
                kMapModel = null,
                howComeProgress = null,
                traceSolveSteps = emptyList()
            )
        }
    }

    private fun createHowComeProgress(model: TruthTableModel, notation: Notation): HowComeProgress {
        val operations = model.analysis.opNodes
        val steps = mutableListOf<HowComeStep>()
        val rowRanges = mutableListOf<HowComeRowRange>()

        model.rows.forEachIndexed { rowIndex, row ->
            val start = steps.size
            if (operations.isEmpty()) {
                steps.add(HowComeStep(rowIndex, null, row.result, "RESULT"))
            } else {
                operations.forEach { node ->
                    steps.add(HowComeStep(rowIndex, node.id, row.values[node.id] ?: 0, BooleanEvaluator.nodeLabel(node, notation)))
                }
            }
            rowRanges.add(HowComeRowRange(start, steps.size))
        }

        return HowComeProgress(
            steps = steps,
            operationOrder = operations,
            rowRanges = rowRanges,
            cursor = 0,
            activePhase = 0
        )
    }
}
