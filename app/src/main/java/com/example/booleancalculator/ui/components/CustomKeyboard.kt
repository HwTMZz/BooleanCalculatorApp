package com.example.booleancalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booleancalculator.domain.model.Notation
import com.example.booleancalculator.domain.model.VarSet
import com.example.booleancalculator.ui.theme.BorderColor
import com.example.booleancalculator.ui.theme.CardBackground
import com.example.booleancalculator.ui.theme.MainBackground
import com.example.booleancalculator.ui.theme.PrimaryBlue
import com.example.booleancalculator.ui.theme.SecondaryText

@Composable
fun CustomKeyboard(
    varSet: VarSet,
    notation: Notation,
    isAdvanced: Boolean,
    onKeyClick: (kind: String, value: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // -------------------------------------------------------------------------
    // WIDTH CONTROL:
    // .fillMaxWidth() on Column and Row makes the keyboard span 100% of screen width.
    // .weight(1f) on each key distributes the width equally across the 4 columns.
    // -------------------------------------------------------------------------
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        if (!isAdvanced) {
            // Variable Row (4 columns)
            Row(modifier = Modifier.fillMaxWidth()) {
                varSet.vars.forEach { v ->
                    KeyButton(
                        text = v,
                        isOp = false,
                        onClick = { onKeyClick("VAR", v) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // Advanced Operator Row
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("NAND", "NOR", "XOR", "XNOR").forEach { op ->
                    KeyButton(
                        text = op,
                        isOp = true,
                        onClick = { onKeyClick(op, "") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Standard Operator Row
        Row(modifier = Modifier.fillMaxWidth()) {
            val andLabel = if (notation == Notation.SYMBOL) "*" else "AND"
            val orLabel = if (notation == Notation.SYMBOL) "+" else "OR"
            val notLabel = if (notation == Notation.SYMBOL) "!" else "NOT"

            KeyButton(
                text = andLabel,
                isOp = true,
                onClick = { onKeyClick("AND", "") },
                modifier = Modifier.weight(1f)
            )
            KeyButton(
                text = orLabel,
                isOp = true,
                onClick = { onKeyClick("OR", "") },
                modifier = Modifier.weight(1f)
            )
            KeyButton(
                text = notLabel,
                isOp = true,
                onClick = { onKeyClick("NOT", "") },
                modifier = Modifier.weight(1f)
            )

            // Parentheses split button (Col 4)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .border(0.5.dp, BorderColor)
                    .background(MainBackground)
            ) {
                ParenButton(
                    text = "(",
                    onClick = { onKeyClick("LPAREN", "") },
                    modifier = Modifier.weight(1f)
                )
                ParenButton(
                    text = ")",
                    onClick = { onKeyClick("RPAREN", "") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun KeyButton(
    text: String,
    isOp: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bg = if (isPressed) PrimaryBlue else MainBackground
    val textColor = if (isPressed) CardBackground else SecondaryText
    val fontSize = if (isOp) 20.sp else 24.sp

    // -------------------------------------------------------------------------
    // HEIGHT CONTROL:
    // .padding(vertical = 28.dp, horizontal = 4.dp) controls the vertical height
    // of each key. Increasing this value increases the height of the keyboard.
    // -------------------------------------------------------------------------
    Box(
        modifier = modifier
            .border(0.5.dp, BorderColor)
            .background(bg)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(vertical = 34.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ParenButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bg = if (isPressed) PrimaryBlue else MainBackground
    val textColor = if (isPressed) CardBackground else SecondaryText

    // -------------------------------------------------------------------------
    // HEIGHT CONTROL (Parentheses buttons):
    // .padding(vertical = 28.dp, horizontal = 2.dp) controls the height of parenthesis keys.
    // -------------------------------------------------------------------------
    Box(
        modifier = modifier
            .background(bg)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(vertical = 34.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}
