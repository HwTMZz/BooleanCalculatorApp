package com.example.booleancalculator.domain.engine

import com.example.booleancalculator.domain.model.ASTNode
import com.example.booleancalculator.domain.model.IdentityStep
import com.example.booleancalculator.domain.model.Notation
import com.example.booleancalculator.domain.model.OpType
import com.example.booleancalculator.domain.model.SimplificationResult

object SimplificationEngine {

    private fun combineTerms(a: String, b: String): String? {
        if (a.length != b.length) return null
        var diff = 0
        val res = StringBuilder()
        for (i in a.indices) {
            if (a[i] == b[i]) {
                res.append(a[i])
            } else if (a[i] == '-' || b[i] == '-') {
                return null
            } else {
                diff++
                res.append('-')
            }
        }
        return if (diff == 1) res.toString() else null
    }

    private fun toBinaryString(m: Int, numVars: Int): String {
        val bin = Integer.toBinaryString(m)
        return bin.padStart(numVars, '0')
    }

    fun quineMcCluskey(minterms: List<Int>, numVars: Int): Pair<List<String>, List<String>> {
        if (minterms.isEmpty()) return Pair(emptyList(), emptyList())
        var currentLevel = minterms.map { toBinaryString(it, numVars) }.distinct().toMutableList()
        val primeImplicants = mutableSetOf<String>()
        val steps = mutableListOf<String>()
        var level = 1

        while (currentLevel.isNotEmpty()) {
            val used = mutableSetOf<String>()
            val next = mutableSetOf<String>()

            for (i in currentLevel.indices) {
                for (j in i + 1 until currentLevel.size) {
                    val combined = combineTerms(currentLevel[i], currentLevel[j])
                    if (combined != null) {
                        next.add(combined)
                        used.add(currentLevel[i])
                        used.add(currentLevel[j])
                    }
                }
            }

            currentLevel.forEach { t ->
                if (!used.contains(t)) primeImplicants.add(t)
            }
            steps.add("Round $level: combined ${currentLevel.size} term(s) -> ${next.size} new term(s), ${currentLevel.count { !used.contains(it) }} could not combine further.")
            currentLevel = next.toList().toMutableList()
            level++
        }

        fun covers(pi: String, minterm: Int): Boolean {
            val bits = toBinaryString(minterm, numVars)
            for (i in pi.indices) {
                if (pi[i] != '-' && pi[i] != bits[i]) return false
            }
            return true
        }

        val piList = primeImplicants.toList()
        val chart = piList.map { pi -> minterms.filter { covers(pi, it) } }
        val essential = mutableSetOf<String>()
        val covered = mutableSetOf<Int>()

        minterms.forEach { m ->
            val coveringPis = piList.filter { covers(it, m) }
            if (coveringPis.size == 1) essential.add(coveringPis[0])
        }

        essential.forEach { pi ->
            val idx = piList.indexOf(pi)
            if (idx >= 0) {
                chart[idx].forEach { covered.add(it) }
            }
        }

        var remaining = minterms.filter { !covered.contains(it) }.toMutableList()
        val chosen = essential.toMutableSet()

        while (remaining.isNotEmpty()) {
            var best: String? = null
            var bestCount = -1

            piList.forEachIndexed { idx, pi ->
                if (!chosen.contains(pi)) {
                    val count = chart[idx].count { remaining.contains(it) }
                    if (count > bestCount) {
                        bestCount = count
                        best = pi
                    }
                }
            }

            val foundBest = best ?: break
            if (bestCount <= 0) break
            chosen.add(foundBest)
            val bestIdx = piList.indexOf(foundBest)
            remaining = remaining.filter { !chart[bestIdx].contains(it) }.toMutableList()
        }

        return Pair(chosen.toList(), steps)
    }

    fun piToExpression(pi: String, vars: List<String>): String {
        val parts = mutableListOf<String>()
        for (i in pi.indices) {
            if (i < vars.size) {
                when (pi[i]) {
                    '1' -> parts.add(vars[i])
                    '0' -> parts.add("!${vars[i]}")
                }
            }
        }
        return if (parts.isNotEmpty()) parts.joinToString(" & ") else "1"
    }

    fun simplifyExpression(ast: ASTNode, vars: List<String>): SimplificationResult {
        val model = BooleanEvaluator.buildTruthTableModel(ast, vars)
        val minterms = model.rows.filter { it.result == 1 }.map { it.index }
        val total = 1 shl vars.size

        if (minterms.isEmpty()) {
            return SimplificationResult("0", minterms, emptyList(), emptyList())
        }
        if (minterms.size == total) {
            return SimplificationResult("1", minterms, emptyList(), emptyList())
        }

        val (essential, steps) = quineMcCluskey(minterms, vars.size)
        val expr = essential.joinToString(" | ") { piToExpression(it, vars) }
        return SimplificationResult(expr, minterms, essential, steps)
    }

    // --- Comprehensive Boolean Identity Laws Transformation Engine ---

    private fun booleanNodeKey(node: ASTNode): String {
        return when (node) {
            is ASTNode.VarNode -> "VAR:${node.name}"
            is ASTNode.NumNode -> "NUM:${node.value}"
            is ASTNode.NotNode -> "NOT(${booleanNodeKey(node.child)})"
            is ASTNode.BinaryOpNode -> "${node.type.keyword}(${booleanNodeKey(node.left)},${booleanNodeKey(node.right)})"
        }
    }

    private fun sameBooleanNode(a: ASTNode, b: ASTNode): Boolean {
        return booleanNodeKey(a) == booleanNodeKey(b)
    }

    private fun isBooleanComplement(a: ASTNode, b: ASTNode): Boolean {
        return (a is ASTNode.NotNode && sameBooleanNode(a.child, b)) ||
                (b is ASTNode.NotNode && sameBooleanNode(b.child, a))
    }

    private fun booleanFactors(node: ASTNode, type: OpType): List<ASTNode> {
        if (node !is ASTNode.BinaryOpNode || node.type != type) return listOf(node)
        return booleanFactors(node.left, type) + booleanFactors(node.right, type)
    }

    private fun combineBooleanFactors(factors: List<ASTNode>, type: OpType): ASTNode {
        if (factors.isEmpty()) return ASTNode.NumNode(if (type == OpType.AND) "1" else "0")
        var current = factors[0]
        for (i in 1 until factors.size) {
            current = ASTNode.BinaryOpNode(type = type, left = current, right = factors[i])
        }
        return current
    }

    private data class RewriteResult(
        val node: ASTNode,
        val law: String,
        val identity: String
    )

    private fun booleanIdentityRewrite(node: ASTNode, notation: Notation): RewriteResult? {
        val sym = notation == Notation.SYMBOL

        if (node is ASTNode.BinaryOpNode) {
            when (node.type) {
                OpType.NAND -> {
                    val converted = ASTNode.NotNode(
                        child = ASTNode.BinaryOpNode(type = OpType.AND, left = node.left, right = node.right)
                    )
                    return RewriteResult(converted, "De Morgan's Law", "!(XY) = !X + !Y")
                }
                OpType.NOR -> {
                    val converted = ASTNode.NotNode(
                        child = ASTNode.BinaryOpNode(type = OpType.OR, left = node.left, right = node.right)
                    )
                    return RewriteResult(converted, "De Morgan's Law", "!(X + Y) = !X · !Y")
                }
                OpType.XOR -> {
                    val expanded = ASTNode.BinaryOpNode(
                        type = OpType.OR,
                        left = ASTNode.BinaryOpNode(type = OpType.AND, left = node.left, right = ASTNode.NotNode(node.right)),
                        right = ASTNode.BinaryOpNode(type = OpType.AND, left = ASTNode.NotNode(node.left), right = node.right)
                    )
                    return RewriteResult(expanded, "XOR Definition", if (sym) "X ⊕ Y = X Ȳ + X̄ Y" else "X XOR Y = X AND NOT Y OR NOT X AND Y")
                }
                OpType.XNOR -> {
                    val expanded = ASTNode.BinaryOpNode(
                        type = OpType.OR,
                        left = ASTNode.BinaryOpNode(type = OpType.AND, left = node.left, right = node.right),
                        right = ASTNode.BinaryOpNode(type = OpType.AND, left = ASTNode.NotNode(node.left), right = ASTNode.NotNode(node.right))
                    )
                    return RewriteResult(expanded, "XNOR Definition", if (sym) "X XNOR Y = XY + X̄ Ȳ" else "X XNOR Y = X AND Y OR NOT X AND NOT Y")
                }
                else -> {}
            }
        }

        if (node is ASTNode.NotNode && node.child is ASTNode.NotNode) {
            return RewriteResult(
                node = node.child.child,
                law = "Involution Law",
                identity = if (sym) "X̄̄ = X" else "NOT (NOT X) = X"
            )
        }

        if (node is ASTNode.NotNode && node.child is ASTNode.BinaryOpNode) {
            val childBin = node.child
            if (childBin.type == OpType.AND) {
                val dm = ASTNode.BinaryOpNode(
                    type = OpType.OR,
                    left = ASTNode.NotNode(childBin.left),
                    right = ASTNode.NotNode(childBin.right)
                )
                return RewriteResult(dm, "De Morgan's Law", if (sym) "!(XY) = X̄ + Ȳ" else "NOT (X AND Y) = NOT X OR NOT Y")
            }
            if (childBin.type == OpType.OR) {
                val dm = ASTNode.BinaryOpNode(
                    type = OpType.AND,
                    left = ASTNode.NotNode(childBin.left),
                    right = ASTNode.NotNode(childBin.right)
                )
                return RewriteResult(dm, "De Morgan's Law", if (sym) "!(X + Y) = X̄ · Ȳ" else "NOT (X OR Y) = NOT X AND NOT Y")
            }
        }

        if (node !is ASTNode.BinaryOpNode) return null
        val type = node.type
        if (type != OpType.AND && type != OpType.OR) return null

        val left = node.left
        val right = node.right
        val identityVal = if (type == OpType.AND) "1" else "0"
        val annihilatorVal = if (type == OpType.AND) "0" else "1"

        val identityFormula = when {
            type == OpType.AND && sym -> "X · 1 = X"
            type == OpType.AND -> "X AND 1 = X"
            sym -> "X + 0 = X"
            else -> "X OR 0 = X"
        }

        val annihilatorFormula = when {
            type == OpType.AND && sym -> "X · 0 = 0"
            type == OpType.AND -> "X AND 0 = 0"
            sym -> "X + 1 = 1"
            else -> "X OR 1 = 1"
        }

        val complementFormula = when {
            type == OpType.AND && sym -> "X · X̄ = 0"
            type == OpType.AND -> "NOT X AND X = 0"
            sym -> "X̄ + X = 1"
            else -> "NOT X OR X = 1"
        }

        if (left is ASTNode.NumNode && left.value == identityVal) return RewriteResult(right, "Identity Law", identityFormula)
        if (right is ASTNode.NumNode && right.value == identityVal) return RewriteResult(left, "Identity Law", identityFormula)

        if (left is ASTNode.NumNode && left.value == annihilatorVal) return RewriteResult(left, "Null Law", annihilatorFormula)
        if (right is ASTNode.NumNode && right.value == annihilatorVal) return RewriteResult(right, "Null Law", annihilatorFormula)

        if (isBooleanComplement(left, right)) {
            return RewriteResult(
                node = ASTNode.NumNode(if (type == OpType.AND) "0" else "1"),
                law = "Complement Law",
                identity = complementFormula
            )
        }

        if (sameBooleanNode(left, right)) {
            val idempotentFormula = when {
                type == OpType.AND && sym -> "X · X = X"
                type == OpType.AND -> "X AND X = X"
                sym -> "X + X = X"
                else -> "X OR X = X"
            }
            return RewriteResult(
                node = left,
                law = "Idempotent Law",
                identity = idempotentFormula
            )
        }

        if (type == OpType.OR) {
            if (left is ASTNode.BinaryOpNode && left.type == OpType.AND && booleanFactors(left, OpType.AND).any { sameBooleanNode(it, right) }) {
                return RewriteResult(right, "Absorption Law", if (sym) "X + XY = X" else "X OR (X AND Y) = X")
            }
            if (right is ASTNode.BinaryOpNode && right.type == OpType.AND && booleanFactors(right, OpType.AND).any { sameBooleanNode(it, left) }) {
                return RewriteResult(left, "Absorption Law", if (sym) "X + XY = X" else "X OR (X AND Y) = X")
            }
        } else {
            if (left is ASTNode.BinaryOpNode && left.type == OpType.OR && booleanFactors(left, OpType.OR).any { sameBooleanNode(it, right) }) {
                return RewriteResult(right, "Absorption Law", if (sym) "X(X + Y) = X" else "X AND (X OR Y) = X")
            }
            if (right is ASTNode.BinaryOpNode && right.type == OpType.OR && booleanFactors(right, OpType.OR).any { sameBooleanNode(it, left) }) {
                return RewriteResult(left, "Absorption Law", if (sym) "X(X + Y) = X" else "X AND (X OR Y) = X")
            }
        }

        if (type == OpType.OR) {
            val leftTerms = booleanFactors(left, OpType.AND).toMutableList()
            val rightTerms = booleanFactors(right, OpType.AND).toMutableList()
            val commonIndex = leftTerms.indexOfFirst { a -> rightTerms.any { b -> sameBooleanNode(a, b) } }
            if (commonIndex >= 0) {
                val common = leftTerms.removeAt(commonIndex)
                val otherIndex = rightTerms.indexOfFirst { b -> sameBooleanNode(common, b) }
                if (otherIndex >= 0) rightTerms.removeAt(otherIndex)

                val factored = ASTNode.BinaryOpNode(
                    type = OpType.AND,
                    left = common,
                    right = ASTNode.BinaryOpNode(
                        type = OpType.OR,
                        left = combineBooleanFactors(leftTerms, OpType.AND),
                        right = combineBooleanFactors(rightTerms, OpType.AND)
                    )
                )
                val distFormula = if (sym) "(XY + XZ) = X(Y + Z)" else "(X AND Y) OR (X AND Z) = X AND (Y OR Z)"
                return RewriteResult(factored, "Distributive Law", distFormula)
            }
        } else {
            val leftTerms = booleanFactors(left, OpType.OR).toMutableList()
            val rightTerms = booleanFactors(right, OpType.OR).toMutableList()
            val commonIndex = leftTerms.indexOfFirst { a -> rightTerms.any { b -> sameBooleanNode(a, b) } }
            if (commonIndex >= 0) {
                val common = leftTerms.removeAt(commonIndex)
                val otherIndex = rightTerms.indexOfFirst { b -> sameBooleanNode(common, b) }
                if (otherIndex >= 0) rightTerms.removeAt(otherIndex)

                val factored = ASTNode.BinaryOpNode(
                    type = OpType.OR,
                    left = common,
                    right = ASTNode.BinaryOpNode(
                        type = OpType.AND,
                        left = combineBooleanFactors(leftTerms, OpType.OR),
                        right = combineBooleanFactors(rightTerms, OpType.OR)
                    )
                )
                val distFormula = if (sym) "X + YZ = (X + Y)(X + Z)" else "X OR (Y AND Z) = (X OR Y) AND (X OR Z)"
                return RewriteResult(factored, "Distributive Law", distFormula)
            }
        }

        if (type == OpType.OR) {
            var single: ASTNode? = null
            var prod: ASTNode? = null
            if (left !is ASTNode.BinaryOpNode && right is ASTNode.BinaryOpNode && right.type == OpType.AND) {
                single = left
                prod = right
            } else if (right !is ASTNode.BinaryOpNode && left is ASTNode.BinaryOpNode && left.type == OpType.AND) {
                single = right
                prod = left
            }
            if (single != null && prod is ASTNode.BinaryOpNode) {
                val factors = booleanFactors(prod, OpType.AND)
                val compIndex = factors.indexOfFirst { isBooleanComplement(it, single) }
                if (compIndex >= 0) {
                    val distributed = ASTNode.BinaryOpNode(
                        type = OpType.AND,
                        left = ASTNode.BinaryOpNode(type = OpType.OR, left = single, right = factors[compIndex]),
                        right = ASTNode.BinaryOpNode(type = OpType.OR, left = single, right = combineBooleanFactors(factors.filterIndexed { i, _ -> i != compIndex }, OpType.AND))
                    )
                    val distFormula = if (sym) "X + YZ = (X + Y)(X + Z)" else "X OR (Y AND Z) = (X OR Y) AND (X OR Z)"
                    return RewriteResult(distributed, "Distributive Law", distFormula)
                }
            }
        }

        return null
    }

    private fun findBooleanIdentityRewrite(node: ASTNode, notation: Notation): RewriteResult? {
        when (node) {
            is ASTNode.NotNode -> {
                val childRewrite = findBooleanIdentityRewrite(node.child, notation)
                if (childRewrite != null) {
                    return childRewrite.copy(node = node.copy(child = childRewrite.node))
                }
            }
            is ASTNode.BinaryOpNode -> {
                val leftRewrite = findBooleanIdentityRewrite(node.left, notation)
                if (leftRewrite != null) {
                    return leftRewrite.copy(node = node.copy(left = leftRewrite.node))
                }
                val rightRewrite = findBooleanIdentityRewrite(node.right, notation)
                if (rightRewrite != null) {
                    return rightRewrite.copy(node = node.copy(right = rightRewrite.node))
                }
            }
            else -> {}
        }
        return booleanIdentityRewrite(node, notation)
    }

    fun formatIdentityExpression(node: ASTNode, parentPrecedence: Int = 0, notation: Notation = Notation.WORD): String {
        if (notation == Notation.SYMBOL) {
            return formatCompactMath(node, parentPrecedence)
        }
        return BooleanEvaluator.formatAstDisplay(node, notation, parentPrecedence)
    }

    private fun formatCompactMath(node: ASTNode, parentPrecedence: Int = 0): String {
        fun formatNode(n: ASTNode, pPrec: Int): String {
            return when (n) {
                is ASTNode.VarNode -> n.name
                is ASTNode.NumNode -> n.value
                is ASTNode.NotNode -> {
                    val childStr = formatNode(n.child, 4)
                    if (n.child is ASTNode.VarNode) "${n.child.name}̄" else "!($childStr)"
                }
                is ASTNode.BinaryOpNode -> {
                    val prec = when (n.type) {
                        OpType.OR, OpType.NOR -> 1
                        OpType.XOR, OpType.XNOR -> 2
                        OpType.AND, OpType.NAND -> 3
                        OpType.NOT -> 4
                    }
                    val leftStr = formatNode(n.left, prec)
                    val rightStr = formatNode(n.right, prec)

                    val text = when (n.type) {
                        OpType.AND -> {
                            // Universal algebraic grouping: wrap OR subexpressions or constant numbers in parentheses
                            val l = if (n.left is ASTNode.BinaryOpNode && n.left.type == OpType.OR) "($leftStr)" else leftStr
                            val r = if (n.right is ASTNode.BinaryOpNode && n.right.type == OpType.OR) "($rightStr)" else rightStr
                            val lFinal = if (n.left is ASTNode.NumNode) "($leftStr)" else l
                            val rFinal = if (n.right is ASTNode.NumNode) "($r)" else r
                            "$lFinal$rFinal"
                        }
                        OpType.OR -> "$leftStr + $rightStr"
                        OpType.XOR -> "$leftStr ⊕ $rightStr"
                        OpType.NAND -> "!($leftStr $rightStr)"
                        OpType.NOR -> "!($leftStr + $rightStr)"
                        OpType.XNOR -> "!($leftStr ⊕ $rightStr)"
                        OpType.NOT -> "!$leftStr"
                    }

                    if (prec < pPrec) "($text)" else text
                }
            }
        }
        return formatNode(node, parentPrecedence)
    }

    fun buildIdentitySimplificationSteps(ast: ASTNode, notation: Notation = Notation.WORD): List<IdentityStep> {
        var current = ast
        val steps = mutableListOf(IdentityStep(formatIdentityExpression(current, notation = notation), "", ""))
        val seen = mutableSetOf(booleanNodeKey(current))

        for (count in 0 until 64) {
            val rewrite = findBooleanIdentityRewrite(current, notation) ?: break
            val key = booleanNodeKey(rewrite.node)
            if (seen.contains(key)) break
            current = rewrite.node
            seen.add(key)
            steps.add(
                IdentityStep(
                    expression = formatIdentityExpression(current, notation = notation),
                    law = rewrite.law,
                    identity = rewrite.identity
                )
            )
        }
        return steps
    }
}
