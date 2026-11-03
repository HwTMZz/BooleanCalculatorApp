package com.example.booleancalculator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    background = MainBackground,
    surface = CardBackground,
    onPrimary = CardBackground,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    outline = BorderColor,
    error = ErrorRed
)

@Composable
fun BooleanCalculatorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
