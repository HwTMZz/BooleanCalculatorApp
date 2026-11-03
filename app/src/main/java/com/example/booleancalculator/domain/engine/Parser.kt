package com.example.booleancalculator.domain.engine

import com.example.booleancalculator.domain.model.ASTNode
import com.example.booleancalculator.domain.model.OpType
import com.example.booleancalculator.domain.model.Token

object Parser {
    fun parse(tokens: List<Token>): ASTNode {
        if (tokens.isEmpty()) throw IllegalArgumentException("Empty expression")
        var pos = 0

        fun peek(): Token? = if (pos < tokens.size) tokens[pos] else null
        fun advance(): Token = tokens[pos++]

        fun expectRParen() {
            val t = peek()
            if (t !is Token.RParen) {
                val found = when (t) {
                    is Token.Var -> t.value
                    is Token.Num -> t.value
                    is Token.Op -> t.type.keyword
                    is Token.LParen -> "("
                    null -> null
                    else -> null
                }
                val msg = if (t != null) "Unexpected \"$found\" — expected a closing parenthesis \")\"" else "Incomplete expression — expected a closing parenthesis \")\""
                throw IllegalArgumentException(msg)
            }
            advance()
        }

        lateinit var parseOrImpl: () -> ASTNode

        fun parsePrimary(): ASTNode {
            val t = peek() ?: throw IllegalArgumentException("Incomplete expression")
            return when (t) {
                is Token.LParen -> {
                    advance()
                    val node = parseOrImpl()
                    expectRParen()
                    node
                }
                is Token.Var -> {
                    advance()
                    ASTNode.VarNode(name = t.value)
                }
                is Token.Num -> {
                    advance()
                    ASTNode.NumNode(value = t.value)
                }
                else -> {
                    val label = when (t) {
                        is Token.Op -> t.type.keyword
                        is Token.RParen -> ")"
                        else -> t.toString()
                    }
                    throw IllegalArgumentException("Unexpected token \"$label\"")
                }
            }
        }

        fun parseNot(): ASTNode {
            val t = peek()
            if (t is Token.Op && t.type == OpType.NOT) {
                advance()
                val child = parseNot()
                return ASTNode.NotNode(child = child)
            }
            return parsePrimary()
        }

        fun parseAnd(): ASTNode {
            var node = parseNot()
            while (true) {
                val t = peek()
                if (t is Token.Op && (t.type == OpType.AND || t.type == OpType.NAND)) {
                    val opToken = advance() as Token.Op
                    val right = parseNot()
                    node = ASTNode.BinaryOpNode(type = opToken.type, left = node, right = right)
                } else {
                    break
                }
            }
            return node
        }

        fun parseXor(): ASTNode {
            var node = parseAnd()
            while (true) {
                val t = peek()
                if (t is Token.Op && (t.type == OpType.XOR || t.type == OpType.XNOR)) {
                    val opToken = advance() as Token.Op
                    val right = parseAnd()
                    node = ASTNode.BinaryOpNode(type = opToken.type, left = node, right = right)
                } else {
                    break
                }
            }
            return node
        }

        fun parseOr(): ASTNode {
            var node = parseXor()
            while (true) {
                val t = peek()
                if (t is Token.Op && (t.type == OpType.OR || t.type == OpType.NOR)) {
                    val opToken = advance() as Token.Op
                    val right = parseXor()
                    node = ASTNode.BinaryOpNode(type = opToken.type, left = node, right = right)
                } else {
                    break
                }
            }
            return node
        }

        parseOrImpl = { parseOr() }

        val ast = parseOr()
        if (pos < tokens.size) {
            val t = tokens[pos]
            val value = when (t) {
                is Token.Var -> t.value
                is Token.Num -> t.value
                is Token.Op -> t.type.keyword
                is Token.LParen -> "("
                is Token.RParen -> ")"
            }
            throw IllegalArgumentException("Unexpected token \"$value\"")
        }
        return ast
    }
}
