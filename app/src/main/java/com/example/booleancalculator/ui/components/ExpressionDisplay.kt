package com.example.booleancalculator.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booleancalculator.domain.model.Notation
import com.example.booleancalculator.domain.model.OpType
import com.example.booleancalculator.domain.model.Token
import com.example.booleancalculator.ui.theme.BorderColor
import com.example.booleancalculator.ui.theme.ErrorRed
import com.example.booleancalculator.ui.theme.PlaceholderText
import com.example.booleancalculator.ui.theme.PrimaryBlue
import com.example.booleancalculator.ui.theme.PrimaryText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpressionDisplay(
    tokens: List<Token>,
    notation: Notation,
    cursorIndex: Int,
    errorText: String?,
    onClear: () -> Unit,
    onSelectCursor: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "caretBlink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onSelectCursor(tokens.size)
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tokens.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(32.dp)
                                .background(PrimaryBlue.copy(alpha = alpha))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Type an Expressions...",
                            fontSize = 26.sp,
                            color = PlaceholderText,
                            fontWeight = FontWeight.Normal
                        )
                    }
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        tokens.forEachIndexed { index, token ->
                            val isSelected = index == cursorIndex
                            val isOp = token is Token.Op
                            val text = when (token) {
                                is Token.Var -> token.value
                                is Token.Num -> token.value
                                is Token.LParen -> "("
                                is Token.RParen -> ")"
                                is Token.Op -> {
                                    if (notation == Notation.SYMBOL) {
                                        when (token.type) {
                                            OpType.AND -> "*"
                                            OpType.OR -> "+"
                                            OpType.NOT -> "!"
                                            else -> token.type.keyword
                                        }
                                    } else {
                                        token.type.keyword
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .drawBehind {
                                        if (isSelected) {
                                            drawLine(
                                                color = PrimaryBlue.copy(alpha = alpha),
                                                start = Offset(0f, 0f),
                                                end = Offset(0f, size.height),
                                                strokeWidth = 2.dp.toPx()
                                            )
                                        }
                                    }
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onSelectCursor(index) }
                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = text,
                                    fontSize = 26.sp,
                                    fontWeight = if (isOp) FontWeight.Bold else FontWeight.Normal,
                                    color = PrimaryText
                                )
                            }
                        }

                        if (cursorIndex >= tokens.size) {
                            Box(
                                modifier = Modifier
                                    .drawBehind {
                                        drawLine(
                                            color = PrimaryBlue.copy(alpha = alpha),
                                            start = Offset(0f, 0f),
                                            end = Offset(0f, size.height),
                                            strokeWidth = 2.dp.toPx()
                                        )
                                    }
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = " ",
                                    fontSize = 26.sp,
                                    color = Color.Transparent
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(30.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, BorderColor, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClear() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    color = PlaceholderText,
                    fontSize = 14.sp
                )
            }
        }

        if (!errorText.isNull_or_blank_safe()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorText!!,
                color = ErrorRed,
                fontSize = 14.sp
            )
        }
    }
}

private fun CharSequence?.isNull_or_blank_safe(): Boolean {
    return this == null || this.trim().isEmpty()
}
