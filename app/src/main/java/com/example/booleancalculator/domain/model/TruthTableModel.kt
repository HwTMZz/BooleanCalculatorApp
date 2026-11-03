package com.example.booleancalculator.domain.model

data class ColumnInfo(
    val nodeId: String,
    val label: String,
    val type: String,
    val dependencies: List<String>,
    val isVariable: Boolean,
    val isResult: Boolean
)

data class TruthTableRow(
    val index: Int,
    val bits: List<Int>,
    val bindings: Map<String, Int>,
    val values: Map<String, Int>,
    val result: Int
)

data class TruthTableFilter(
    val id: String,
    val label: String,
    val nodeId: String?,
    val dependencies: List<String>,
    val nodeIds: List<String>,
    val kind: String
)

data class ASTAnalysisResult(
    val nodes: List<ASTNode>,
    val byId: Map<String, ASTNode>,
    val opNodes: List<ASTNode>,
    val filters: List<TruthTableFilter>
)

data class EvaluationTraceStep(
    val nodeId: String,
    val type: OpType,
    val before: String,
    val result: Int,
    val dependencies: List<String>
)

data class DetailedEvalResult(
    val value: Int,
    val values: Map<String, Int>,
    val trace: List<EvaluationTraceStep>
)

data class TruthTableModel(
    val ast: ASTNode,
    val vars: List<String>,
    val analysis: ASTAnalysisResult,
    val columns: List<ColumnInfo>,
    val rows: List<TruthTableRow>,
    val resultNodeId: String
)
