package com.example.booleancalculator.domain.model

enum class VarSet(val vars: List<String>) {
    WXYZ(listOf("W", "X", "Y", "Z")),
    ABCD(listOf("A", "B", "C", "D")),
    PQRS(listOf("P", "Q", "R", "S")),
    X1234(listOf("X1", "X2", "X3", "X4"))
}
