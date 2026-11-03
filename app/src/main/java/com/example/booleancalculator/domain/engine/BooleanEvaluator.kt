package com.example.booleancalculator.domain.engine

import com.example.booleancalculator.domain.model.ASTAnalysisResult
import com.example.booleancalculator.domain.model.ASTNode
import com.example.booleancalculator.domain.model.ColumnInfo
import com.example.booleancalculator.domain.model.DetailedEvalResult
import com.example.booleancalculator.domain.model.EvaluationTraceStep
import com.example.booleancalculator.domain.model.Notation
import com.example.booleancalculator.domain.model.OpType
import com.example.booleancalculator.domain.model.TruthTableFilter
import com.example.booleancalculator.domain.model.TruthTableModel
import com.example.booleancalculator.domain.model.TruthTableRow

object BooleanEvaluator {

    fun annotateAST(ast: ASTNode): Pair<ASTNode, List<ASTNode>> {
        var nextId = 0
        val nodesList = mutableListOf<ASTNode>()

        fun walk(node: ASTNode): ASTNode {
            val currentId = "n${nextId++}"
            val annotated = when (node) {
                is ASTNode.VarNode -> node.copyWithId(currentId)
                is ASTNode.NumNode -> node.copyWithId(currentId)
                is ASTNode.NotNode -> node.copy(child = walk(node.child), id = currentId)
                is ASTNode.BinaryOpNode -> node.copy(left = walk(node.left), right = walk(node.right), id = currentId)
            }
            nodesList.add(annotated)
            return annotated
        }

        val rootAnnotated = walk(ast)
        return Pair(rootAnnotated, nodesList)
    }

    fun extractVars(ast: ASTNode, preferredOrder: List<String> = listOf("W", "X", "Y", "Z", "A", "B", "C", "D", "P", "Q", "R", "S", "X1", "X2", "X3", "X4")): List<String> {
        val found = mutableSetOf<String>()
        fun walk(node: ASTNode) {
            when (node) {
                is ASTNode.VarNode -> found.add(node.name)
                is ASTNode.NotNode -> walk(node.child)
                is ASTNode.BinaryOpNode -> {
                    walk(node.left)
                    walk(node.right)
                }
                is ASTNode.NumNode -> {}
            }
        }
        walk(ast)

        val preferred = preferredOrder.filter { found.contains(it) }
        val rest = found.filter { !preferred.contains(it) }.sorted()
        return preferred + rest
    }

    fun astDependencies(node: ASTNode): List<String> {
        val found = mutableSetOf<String>()
        fun walk(n: ASTNode) {
            when (n) {
                is ASTNode.VarNode -> found.add(n.name)
                is ASTNode.NotNode -> walk(n.child)
                is ASTNode.BinaryOpNode -> {
                    walk(n.left)
                    walk(n.right)
                }
                is ASTNode.NumNode -> {}
            }
        }
        walk(node)
        return found.toList()
    }

    fun opDisplay(type: OpType, notation: Notation): String {
        return if (notation == Notation.SYMBOL) {
            when (type) {
                OpType.AND -> "*"
                OpType.OR -> "+"
                OpType.NOT -> "!"
                else -> type.keyword
            }
        } else {
            type.keyword
        }
    }

    fun nodeLabel(node: ASTNode, notation: Notation = Notation.WORD): String {
        return when (node) {
            is ASTNode.VarNode -> node.name
            is ASTNode.NumNode -> node.value
            is ASTNode.NotNode -> {
                val childStr = nodeLabel(node.child, notation)
                if (notation == Notation.SYMBOL) "!$childStr" else "NOT $childStr"
            }
            is ASTNode.BinaryOpNode -> {
                val opStr = opDisplay(node.type, notation)
                val leftStr = nodeLabel(node.left, notation)
                val rightStr = nodeLabel(node.right, notation)
                "$leftStr $opStr $rightStr"
            }
        }
    }

    fun formatAstDisplay(node: ASTNode, notation: Notation = Notation.WORD, parentPrecedence: Int = 0): String {
        val precedence = mapOf(
            "OR" to 1, "NOR" to 1,
            "XOR" to 2, "XNOR" to 2,
            "AND" to 3, "NAND" to 3,
            "NOT" to 4,
            "VAR" to 5, "NUM" to 5
        )

        val typeKey = when (node) {
            is ASTNode.VarNode -> "VAR"
            is ASTNode.NumNode -> "NUM"
            is ASTNode.NotNode -> "NOT"
            is ASTNode.BinaryOpNode -> node.type.keyword
        }
        val currentPrec = precedence[typeKey] ?: 5

        val text = when (node) {
            is ASTNode.VarNode -> node.name
            is ASTNode.NumNode -> node.value
            is ASTNode.NotNode -> {
                val childText = formatAstDisplay(node.child, notation, currentPrec)
                if (notation == Notation.SYMBOL) "!$childText" else "NOT $childText"
            }
            is ASTNode.BinaryOpNode -> {
                val opStr = opDisplay(node.type, notation)
                val leftText = formatAstDisplay(node.left, notation, currentPrec)
                val rightText = formatAstDisplay(node.right, notation, currentPrec)
                "$leftText $opStr $rightText"
            }
        }

        return if (currentPrec < parentPrecedence) "($text)" else text
    }

    fun serializeCanonical(node: ASTNode, parentPrec: Int? = null): String {
        val prec = mapOf(
            "OR" to 1, "NOR" to 1,
            "XOR" to 2, "XNOR" to 2,
            "AND" to 3, "NAND" to 3,
            "NOT" to 4, "VAR" to 5, "NUM" to 5
        )
        fun wrap(s: String, p: Int): String {
            return if (parentPrec != null && p < parentPrec) "($s)" else s
        }

        return when (node) {
            is ASTNode.VarNode -> node.name
            is ASTNode.NumNode -> node.value
            is ASTNode.NotNode -> "!${serializeCanonical(node.child, prec["NOT"])}"
            is ASTNode.BinaryOpNode -> {
                when (node.type) {
                    OpType.AND -> wrap("${serializeCanonical(node.left, prec["AND"])} & ${serializeCanonical(node.right, prec["AND"])}", prec["AND"]!!)
                    OpType.OR -> wrap("${serializeCanonical(node.left, prec["OR"])} | ${serializeCanonical(node.right, prec["OR"])}", prec["OR"]!!)
                    OpType.XOR -> wrap("${serializeCanonical(node.left, prec["XOR"])} ^ ${serializeCanonical(node.right, prec["XOR"])}", prec["XOR"]!!)
                    OpType.NAND -> "!(${serializeCanonical(node.left)} & ${serializeCanonical(node.right)})"
                    OpType.NOR -> "!(${serializeCanonical(node.left)} | ${serializeCanonical(node.right)})"
                    OpType.XNOR -> "!(${serializeCanonical(node.left)} ^ ${serializeCanonical(node.right)})"
                    OpType.NOT -> "!${serializeCanonical(node.left)}"
                }
            }
        }
    }

    fun getOrderedOperationNodes(ast: ASTNode): List<ASTNode> {
        val (annotatedAst, _) = annotateAST(ast)
        val allOpNodes = mutableListOf<ASTNode>()

        fun collectAllOps(node: ASTNode) {
            node.children.forEach { collectAllOps(it) }
            if (node !is ASTNode.VarNode && node !is ASTNode.NumNode) {
                allOpNodes.add(node)
            }
        }
        collectAllOps(annotatedAst)

        val precedence = mapOf(
            OpType.NOT to 0,
            OpType.AND to 1, OpType.NAND to 1,
            OpType.XOR to 2, OpType.XNOR to 2,
            OpType.OR to 3, OpType.NOR to 3
        )

        val resultList = mutableListOf<ASTNode>()
        val completedIds = mutableSetOf<String>()

        while (resultList.size < allOpNodes.size) {
            val available = allOpNodes.filter { node ->
                !completedIds.contains(node.id) &&
                        node.children.all { child ->
                            child is ASTNode.VarNode || child is ASTNode.NumNode || completedIds.contains(child.id)
                        }
            }
            if (available.isEmpty()) break

            val next = available.minByOrNull { node ->
                when (node) {
                    is ASTNode.NotNode -> precedence[OpType.NOT] ?: 0
                    is ASTNode.BinaryOpNode -> precedence[node.type] ?: 1
                    else -> 5
                }
            } ?: available.first()

            resultList.add(next)
            completedIds.add(next.id)
        }

        return if (resultList.size == allOpNodes.size) resultList else allOpNodes
    }

    fun getEvaluationOrderExplanation(ast: ASTNode, notation: Notation = Notation.WORD): String {
        val opNodes = getOrderedOperationNodes(ast)
        if (opNodes.isEmpty()) {
            return "This expression contains no Boolean operations, so each row maps directly to its variable value."
        }

        val hasNot = opNodes.any { it is ASTNode.NotNode }
        val hasAnd = opNodes.any { it is ASTNode.BinaryOpNode && (it.type == OpType.AND || it.type == OpType.NAND) }
        val hasXor = opNodes.any { it is ASTNode.BinaryOpNode && (it.type == OpType.XOR || it.type == OpType.XNOR) }
        val hasOr = opNodes.any { it is ASTNode.BinaryOpNode && (it.type == OpType.OR || it.type == OpType.NOR) }

        val parts = mutableListOf<String>()
        if (hasNot) parts.add("NOT (negation)")
        if (hasAnd) parts.add(if (notation == Notation.SYMBOL) "* (product)" else "AND")
        if (hasXor) parts.add("XOR")
        if (hasOr) parts.add(if (notation == Notation.SYMBOL) "+ (sum)" else "OR")

        val orderText = if (parts.size > 1) parts.joinToString(" → ") else parts.firstOrNull() ?: ""

        return "Evaluate parenthesized groups from the innermost outward, following standard operator precedence: $orderText."
    }

    fun astAnalysis(ast: ASTNode, vars: List<String>): ASTAnalysisResult {
        val (annotatedAst, nodes) = annotateAST(ast)
        val byId = nodes.associateBy { it.id }
        val opNodes = getOrderedOperationNodes(ast)

        val filters = mutableListOf(
            TruthTableFilter(
                id = "all",
                label = "Show All",
                nodeId = null,
                dependencies = vars,
                nodeIds = emptyList(),
                kind = "all"
            )
        )

        opNodes.forEach { node ->
            val subtreeIds = mutableListOf<String>()
            fun collectSubtree(n: ASTNode) {
                n.children.forEach { collectSubtree(it) }
                subtreeIds.add(n.id)
            }
            collectSubtree(node)

            filters.add(
                TruthTableFilter(
                    id = "node-${node.id}",
                    label = nodeLabel(node),
                    nodeId = node.id,
                    dependencies = astDependencies(node),
                    nodeIds = subtreeIds,
                    kind = "node"
                )
            )
        }

        return ASTAnalysisResult(listOf(annotatedAst) + nodes, byId, opNodes, filters)
    }

    fun evalBooleanDetailed(ast: ASTNode, bindings: Map<String, Int>): DetailedEvalResult {
        val values = mutableMapOf<String, Int>()
        val trace = mutableListOf<EvaluationTraceStep>()

        fun ev(node: ASTNode): Pair<Int, String> {
            when (node) {
                is ASTNode.VarNode -> {
                    val value = bindings[node.name] ?: 0
                    values[node.id] = value
                    return Pair(value, node.name)
                }
                is ASTNode.NumNode -> {
                    val value = node.value.toIntOrNull() ?: 0
                    values[node.id] = value
                    return Pair(value, node.value)
                }
                is ASTNode.NotNode -> {
                    val c = ev(node.child)
                    val value = if (c.first != 0) 0 else 1
                    values[node.id] = value
                    trace.add(
                        EvaluationTraceStep(
                            nodeId = node.id,
                            type = OpType.NOT,
                            before = "NOT ${c.second}",
                            result = value,
                            dependencies = astDependencies(node)
                        )
                    )
                    return Pair(value, "NOT ${c.second}")
                }
                is ASTNode.BinaryOpNode -> {
                    val a = ev(node.left)
                    val b = ev(node.right)
                    val value = when (node.type) {
                        OpType.AND -> if (a.first != 0 && b.first != 0) 1 else 0
                        OpType.OR -> if (a.first != 0 || b.first != 0) 1 else 0
                        OpType.XOR -> if (a.first != b.first) 1 else 0
                        OpType.NAND -> if (a.first != 0 && b.first != 0) 0 else 1
                        OpType.NOR -> if (a.first != 0 || b.first != 0) 0 else 1
                        OpType.XNOR -> if (a.first == b.first) 1 else 0
                        OpType.NOT -> if (a.first == 0) 1 else 0
                    }
                    values[node.id] = value
                    trace.add(
                        EvaluationTraceStep(
                            nodeId = node.id,
                            type = node.type,
                            before = "${a.second} ${node.type.keyword} ${b.second}",
                            result = value,
                            dependencies = astDependencies(node)
                        )
                    )
                    return Pair(value, nodeLabel(node))
                }
            }
        }

        val final = ev(ast)
        return DetailedEvalResult(value = final.first, values = values, trace = trace)
    }

    fun buildTruthTableModel(ast: ASTNode, vars: List<String>): TruthTableModel {
        val analysis = astAnalysis(ast, vars)
        val rootNode = analysis.nodes.first()

        val rows = mutableListOf<TruthTableRow>()
        val n = vars.size
        val total = 1 shl n

        for (i in 0 until total) {
            val bindings = mutableMapOf<String, Int>()
            val bits = mutableListOf<Int>()
            for (b in 0 until n) {
                val bit = (i shr (n - 1 - b)) and 1
                bindings[vars[b]] = bit
                bits.add(bit)
            }
            val detailed = evalBooleanDetailed(rootNode, bindings)
            rows.add(
                TruthTableRow(
                    index = i,
                    bits = bits,
                    bindings = bindings,
                    values = detailed.values,
                    result = detailed.value
                )
            )
        }

        val variableColumns = analysis.nodes.filter { it is ASTNode.VarNode }.distinctBy { (it as ASTNode.VarNode).name }
        val intermediateColumns = analysis.nodes.filter { it !is ASTNode.VarNode && it !is ASTNode.NumNode && it.id != rootNode.id }.distinctBy { it.id }
        val orderedColumns = variableColumns + intermediateColumns + listOf(rootNode)

        val columns = orderedColumns.map { node ->
            ColumnInfo(
                nodeId = node.id,
                label = if (node is ASTNode.VarNode) node.name else nodeLabel(node),
                type = when (node) {
                    is ASTNode.VarNode -> "VAR"
                    is ASTNode.NumNode -> "NUM"
                    is ASTNode.NotNode -> "NOT"
                    is ASTNode.BinaryOpNode -> node.type.keyword
                },
                dependencies = astDependencies(node),
                isVariable = node is ASTNode.VarNode,
                isResult = node.id == rootNode.id
            )
        }

        return TruthTableModel(
            ast = rootNode,
            vars = vars,
            analysis = analysis,
            columns = columns,
            rows = rows,
            resultNodeId = rootNode.id
        )
    }
}
