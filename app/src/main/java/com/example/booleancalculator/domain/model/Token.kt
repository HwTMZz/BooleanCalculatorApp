package com.example.booleancalculator.domain.model

sealed interface Token {
    val pos: Int

    data class Var(val value: String, override val pos: Int = 0) : Token
    data class Num(val value: String, override val pos: Int = 0) : Token
    data class Op(val type: OpType, override val pos: Int = 0) : Token
    data class LParen(override val pos: Int = 0) : Token
    data class RParen(override val pos: Int = 0) : Token
}
