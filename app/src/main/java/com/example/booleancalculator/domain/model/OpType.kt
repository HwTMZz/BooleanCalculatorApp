package com.example.booleancalculator.domain.model

enum class OpType(val keyword: String, val symbol: String) {
    AND("AND", "*"),
    OR("OR", "+"),
    NOT("NOT", "!"),
    NAND("NAND", "NAND"),
    NOR("NOR", "NOR"),
    XOR("XOR", "^"),
    XNOR("XNOR", "XNOR")
}
