package com.example.booleancalculator.domain.engine

import com.example.booleancalculator.domain.model.OpType
import com.example.booleancalculator.domain.model.Token

object Tokenizer {
    private val KEYWORDS = mapOf(
        "AND" to OpType.AND,
        "OR" to OpType.OR,
        "NOT" to OpType.NOT,
        "NAND" to OpType.NAND,
        "NOR" to OpType.NOR,
        "XOR" to OpType.XOR,
        "XNOR" to OpType.XNOR
    )

    private val SYMBOL_MAP = mapOf(
        '&' to OpType.AND,
        '|' to OpType.OR,
        '!' to OpType.NOT,
        '~' to OpType.NOT,
        '^' to OpType.XOR
    )

    private val COMPACT_VARS = listOf("X1", "X2", "X3", "X4", "W", "X", "Y", "Z", "A", "B", "C", "D", "P", "Q", "R", "S")

    private fun compactVariableTokens(word: String, pos: Int): List<Token>? {
        val upper = word.uppercase()
        if (upper.isEmpty() || !upper.all { it in 'A'..'Z' || it in '0'..'9' }) return null
        if (!upper.any { it in 'A'..'Z' }) return null

        val memo = HashMap<String, List<String>?>()

        fun segment(s: String): List<String>? {
            if (s.isEmpty()) return emptyList()
            if (memo.containsKey(s)) return memo[s]

            for (v in COMPACT_VARS.sortedByDescending { it.length }) {
                if (s.startsWith(v)) {
                    val rest = segment(s.substring(v.length))
                    if (rest != null) {
                        val out = listOf(v) + rest
                        memo[s] = out
                        return out
                    }
                }
            }
            memo[s] = null
            return null
        }

        val parts = segment(upper)
        if (parts != null && parts.size > 1) {
            val result = mutableListOf<Token>()
            parts.forEachIndexed { idx, v ->
                if (idx > 0) {
                    result.add(Token.Op(OpType.AND, pos + idx))
                }
                result.add(Token.Var(v, pos + idx))
            }
            return result
        }
        return null
    }

    fun tokenize(input: String, leafType: String = "VAR"): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val n = input.length

        while (i < n) {
            val c = input[i]
            if (c.isWhitespace()) {
                i++
                continue
            }
            if (c == '(') {
                tokens.add(Token.LParen(i))
                i++
                continue
            }
            if (c == ')') {
                tokens.add(Token.RParen(i))
                i++
                continue
            }
            if (SYMBOL_MAP.containsKey(c)) {
                tokens.add(Token.Op(SYMBOL_MAP.getValue(c), i))
                i++
                continue
            }
            if (c.isLetter()) {
                var j = i
                while (j < n && (input[j].isLetterOrDigit())) {
                    j++
                }
                val word = input.substring(i, j)
                val upper = word.uppercase()

                if (KEYWORDS.containsKey(upper)) {
                    tokens.add(Token.Op(KEYWORDS.getValue(upper), i))
                } else if (leafType == "VAR") {
                    val compact = compactVariableTokens(word, i)
                    if (compact != null) {
                        tokens.addAll(compact)
                    } else {
                        tokens.add(Token.Var(upper, i))
                    }
                } else {
                    throw IllegalArgumentException("Unexpected letters \"$word\"")
                }
                i = j
                continue
            }
            if (c.isDigit()) {
                var j = i
                while (j < n && input[j].isDigit()) {
                    j++
                }
                val digits = input.substring(i, j)
                if (leafType == "NUM" || digits == "0" || digits == "1") {
                    tokens.add(Token.Num(digits, i))
                } else {
                    throw IllegalArgumentException("Unexpected number \"$digits\"")
                }
                i = j
                continue
            }
            throw IllegalArgumentException("Unexpected character \"$c\"")
        }

        if (leafType != "VAR") return tokens

        val withImplicitAnd = mutableListOf<Token>()
        tokens.forEach { token ->
            val previous = withImplicitAnd.lastOrNull()
            val isPrevExprEnd = previous is Token.Var || previous is Token.Num || previous is Token.RParen
            val isCurrExprStart = token is Token.Var || token is Token.Num || token is Token.LParen
            val beginsNegatedExpr = token is Token.Op && token.type == OpType.NOT

            if (previous != null && isPrevExprEnd && (isCurrExprStart || beginsNegatedExpr)) {
                withImplicitAnd.add(Token.Op(OpType.AND, token.pos))
            }
            withImplicitAnd.add(token)
        }

        return withImplicitAnd
    }
}
