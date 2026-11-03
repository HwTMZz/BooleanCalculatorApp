package com.example.booleancalculator.domain.engine

import com.example.booleancalculator.domain.model.ASTNode
import com.example.booleancalculator.domain.model.KMapGroup
import com.example.booleancalculator.domain.model.KMapModel
import kotlin.math.ceil

object KMapEngine {

    val GROUP_COLORS = listOf(
        "#0088FF", // Blue
        "#D93025", // Red
        "#188038", // Green
        "#E37400", // Orange
        "#A142F4", // Purple
        "#00A0B0"  // Teal
    )

    fun grayCode(bitsCount: Int): List<String> {
        val size = 1 shl bitsCount
        val out = mutableListOf<String>()
        for (i in 0 until size) {
            val g = i xor (i shr 1)
            val bin = Integer.toBinaryString(g)
            out.add(bin.padStart(bitsCount, '0'))
        }
        return out
    }

    private fun bitsToMinterm(bindings: Map<String, Int>, vars: List<String>): Int {
        var m = 0
        val n = vars.size
        for (i in 0 until n) {
            val bit = bindings[vars[i]] ?: 0
            m = m or (bit shl (n - 1 - i))
        }
        return m
    }

    fun buildKMap(ast: ASTNode, vars: List<String>): KMapModel {
        val n = vars.size
        require(n in 2..4) { "K-Map supports 2 to 4 variables" }

        val rowCountBits = ceil(n / 2.0).toInt()
        val colCountBits = n - rowCountBits

        val rowVars = vars.subList(0, rowCountBits)
        val colVars = vars.subList(rowCountBits, n)

        val rowCodes = grayCode(rowCountBits)
        val colCodes = if (colCountBits > 0) grayCode(colCountBits) else listOf("")

        val model = BooleanEvaluator.buildTruthTableModel(ast, vars)
        val rowCodeCount = rowCodes.size
        val colCodeCount = colCodes.size

        val grid = List(rowCodeCount) { rIdx ->
            List(colCodeCount) { cIdx ->
                val rCode = rowCodes[rIdx]
                val cCode = colCodes[cIdx]
                val bindings = mutableMapOf<String, Int>()
                rowVars.forEachIndexed { idx, v -> bindings[v] = rCode[idx] - '0' }
                colVars.forEachIndexed { idx, v -> bindings[v] = cCode[idx] - '0' }

                val minterm = bitsToMinterm(bindings, vars)
                val ttRow = model.rows.firstOrNull { it.index == minterm }
                ttRow?.result ?: 0
            }
        }

        val sopGroups = findKMapGroups(grid, rowVars, colVars, rowCodes, colCodes, vars, targetValue = 1)
        val posGroups = findKMapGroups(grid, rowVars, colVars, rowCodes, colCodes, vars, targetValue = 0)

        val sopExpr = if (sopGroups.isEmpty()) "0" else sopGroups.joinToString(" | ") { it.termExpression }
        val posExpr = if (posGroups.isEmpty()) "1" else posGroups.joinToString(" & ") { "(${it.termExpression})" }

        return KMapModel(
            rowVars = rowVars,
            colVars = colVars,
            rowCodes = rowCodes,
            colCodes = colCodes,
            grid = grid,
            sopGroups = sopGroups,
            posGroups = posGroups,
            sopExpression = sopExpr,
            posExpression = posExpr
        )
    }

    private fun findKMapGroups(
        grid: List<List<Int>>,
        rowVars: List<String>,
        colVars: List<String>,
        rowCodes: List<String>,
        colCodes: List<String>,
        vars: List<String>,
        targetValue: Int
    ): List<KMapGroup> {
        val numRows = grid.size
        val numCols = grid[0].size
        val targetCells = mutableSetOf<Pair<Int, Int>>()

        for (r in 0 until numRows) {
            for (c in 0 until numCols) {
                if (grid[r][c] == targetValue) {
                    targetCells.add(Pair(r, c))
                }
            }
        }

        if (targetCells.isEmpty()) return emptyList()

        // Generate all valid rectangles of sizes 16, 8, 4, 2, 1 (with wrap around)
        val candidateGroups = mutableListOf<List<Pair<Int, Int>>>()
        val validHeights = listOf(1, 2, 4).filter { it <= numRows }
        val validWidths = listOf(1, 2, 4).filter { it <= numCols }

        // Sort rectangular block sizes descending by area
        val sizes = mutableListOf<Pair<Int, Int>>()
        for (h in validHeights) {
            for (w in validWidths) {
                sizes.add(Pair(h, w))
            }
        }
        sizes.sortByDescending { it.first * it.second }

        for ((h, w) in sizes) {
            for (r in 0 until numRows) {
                for (c in 0 until numCols) {
                    val cells = mutableListOf<Pair<Int, Int>>()
                    var valid = true
                    for (dh in 0 until h) {
                        for (dw in 0 until w) {
                            val nr = (r + dh) % numRows
                            val nc = (c + dw) % numCols
                            if (grid[nr][nc] != targetValue) {
                                valid = false
                                break
                            }
                            cells.add(Pair(nr, nc))
                        }
                        if (!valid) break
                    }
                    if (valid) {
                        candidateGroups.add(cells.distinct())
                    }
                }
            }
        }

        // Filter essential/optimal prime implicant groups using greedy set cover
        val selectedGroups = mutableListOf<List<Pair<Int, Int>>>()
        val uncovered = targetCells.toMutableSet()

        while (uncovered.isNotEmpty()) {
            var bestGroup: List<Pair<Int, Int>>? = null
            var bestCoverCount = -1

            for (group in candidateGroups) {
                val coverCount = group.count { uncovered.contains(it) }
                if (coverCount > bestCoverCount) {
                    bestCoverCount = coverCount
                    bestGroup = group
                }
            }

            if (bestGroup == null || bestCoverCount <= 0) break
            selectedGroups.add(bestGroup)
            uncovered.removeAll(bestGroup.toSet())
        }

        return selectedGroups.mapIndexed { idx, cells ->
            val minterms = cells.map { (r, c) ->
                val rCode = rowCodes[r]
                val cCode = colCodes[c]
                val bindings = mutableMapOf<String, Int>()
                rowVars.forEachIndexed { i, v -> bindings[v] = rCode[i] - '0' }
                colVars.forEachIndexed { i, v -> bindings[v] = cCode[i] - '0' }
                bitsToMinterm(bindings, vars)
            }.toSet()

            val termExpr = deriveGroupTerm(cells, rowVars, colVars, rowCodes, colCodes, targetValue)

            KMapGroup(
                id = idx + 1,
                minterms = minterms,
                cells = cells,
                colorHex = GROUP_COLORS[idx % GROUP_COLORS.size],
                termExpression = termExpr
            )
        }
    }

    private fun deriveGroupTerm(
        cells: List<Pair<Int, Int>>,
        rowVars: List<String>,
        colVars: List<String>,
        rowCodes: List<String>,
        colCodes: List<String>,
        targetValue: Int
    ): String {
        val allVars = rowVars + colVars
        val terms = mutableListOf<String>()

        allVars.forEachIndexed { vIdx, vName ->
            val bitValues = cells.map { (r, c) ->
                val fullCode = rowCodes[r] + colCodes[c]
                fullCode[vIdx] - '0'
            }.distinct()

            if (bitValues.size == 1) {
                val bit = bitValues[0]
                if (targetValue == 1) {
                    // SOP: 1 -> V, 0 -> !V
                    terms.add(if (bit == 1) vName else "!$vName")
                } else {
                    // POS: 0 -> V, 1 -> !V
                    terms.add(if (bit == 0) vName else "!$vName")
                }
            }
        }

        return if (targetValue == 1) {
            if (terms.isEmpty()) "1" else terms.joinToString(" & ")
        } else {
            if (terms.isEmpty()) "0" else terms.joinToString(" | ")
        }
    }
}
