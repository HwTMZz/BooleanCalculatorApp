package com.example.booleancalculator.domain.model

data class SimplificationResult(
    val expr: String,
    val minterms: List<Int>,
    val essential: List<String>,
    val steps: List<String>
)
