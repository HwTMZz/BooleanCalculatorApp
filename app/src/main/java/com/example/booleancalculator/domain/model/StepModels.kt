package com.example.booleancalculator.domain.model

data class IdentityStep(
    val expression: String,
    val law: String,
    val identity: String
)

data class HowComeStep(
    val rowIndex: Int,
    val nodeId: String?,
    val result: Int,
    val type: String
)

data class HowComeRowRange(
    val start: Int,
    val end: Int
)

data class HowComeProgress(
    val steps: List<HowComeStep>,
    val operationOrder: List<ASTNode>,
    val rowRanges: List<HowComeRowRange>,
    val cursor: Int = 0,
    val activePhase: Int = 0,
    val activeRowIndex: Int? = null
)
