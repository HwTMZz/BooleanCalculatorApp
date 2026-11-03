package com.example.booleancalculator.domain.model

data class KMapCell(
    val rowIndex: Int,
    val colIndex: Int,
    val rowCode: String,
    val colCode: String,
    val bindings: Map<String, Int>,
    val value: Int
)

data class KMapGroup(
    val id: Int,
    val minterms: Set<Int>,
    val cells: List<Pair<Int, Int>>, // (row, col)
    val colorHex: String,
    val termExpression: String
)

data class KMapModel(
    val rowVars: List<String>,
    val colVars: List<String>,
    val rowCodes: List<String>,
    val colCodes: List<String>,
    val grid: List<List<Int>>,
    val sopGroups: List<KMapGroup>,
    val posGroups: List<KMapGroup>,
    val sopExpression: String,
    val posExpression: String
)

enum class KMapMode { SOP, POS }
