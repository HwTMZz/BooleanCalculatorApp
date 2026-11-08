package com.example.booleancalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booleancalculator.domain.model.KMapGroup
import com.example.booleancalculator.domain.model.KMapModel
import com.example.booleancalculator.ui.theme.CardBackground
import com.example.booleancalculator.ui.theme.PrimaryText
import com.example.booleancalculator.ui.theme.SuccessGreen
import com.example.booleancalculator.ui.theme.TableHighlight

private fun calculateMinterm(rIdx: Int, cIdx: Int, rowCodes: List<String>, colCodes: List<String>): Int {
    val fullBinary = rowCodes[rIdx] + colCodes[cIdx]
    return if (fullBinary.isNotEmpty()) {
        try {
            Integer.parseInt(fullBinary, 2)
        } catch (e: Exception) {
            0
        }
    } else 0
}

private val KMAP_GROUP_PALETTE = listOf(
    Color(0xFF0088FF),
    Color(0xFFD93025),
    Color(0xFF188038),
    Color(0xFFE37400),
    Color(0xFFA142F4),
    Color(0xFF00A0B0)
)

private fun getGroupColor(group: KMapGroup): Color {
    val hexColor = parseGroupColor(group.colorHex)
    if (hexColor != Color(0xFF0088FF) || group.colorHex.equals("#0088FF", ignoreCase = true)) {
        return hexColor
    }
    val fallbackIdx = (group.id - 1).coerceAtLeast(0) % KMAP_GROUP_PALETTE.size
    return KMAP_GROUP_PALETTE[fallbackIdx]
}

private fun parseGroupColor(hex: String): Color {
    return when (hex.uppercase()) {
        "#0088FF" -> Color(0xFF0088FF)
        "#D93025" -> Color(0xFFD93025)
        "#188038" -> Color(0xFF188038)
        "#E37400" -> Color(0xFFE37400)
        "#A142F4" -> Color(0xFFA142F4)
        "#00A0B0" -> Color(0xFF00A0B0)
        else -> Color(0xFF0088FF)
    }
}

@Composable
fun KMapView(
    kmap: KMapModel,
    step: Int = 7,
    targetVal: Int = 1,
    groups: List<KMapGroup> = emptyList(),
    modifier: Modifier = Modifier
) {
    val rowCodes = kmap.rowCodes
    val colCodes = kmap.colCodes
    val rowLabel = kmap.rowVars.joinToString("")
    val colLabel = kmap.colVars.joinToString("")

    Column(
        modifier = modifier
            .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp))
            .background(CardBackground)
    ) {
        // Header Row (Column Headers)
        Row {
            // Corner cell with diagonal slash dividing row variables and column variables
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 48.dp)
                    .border(0.5.dp, Color.Black)
                    .drawBehind {
                        drawLine(
                            color = Color.Black,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                if (step >= 1) {
                    // Column label at top right
                    Text(
                        text = colLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    )
                    // Row label at bottom left
                    Text(
                        text = rowLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp)
                    )
                }
            }

            colCodes.forEach { cCode ->
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 48.dp)
                        .border(0.5.dp, Color.Black)
                        .background(TableHighlight),
                    contentAlignment = Alignment.Center
                ) {
                    if (step >= 2) {
                        Text(
                            text = cCode,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }
                }
            }
        }

        // Grid Rows
        rowCodes.forEachIndexed { rIdx, rCode ->
            Row {
                // Row header cell
                Box(
                    modifier = Modifier
                        .size(width = 56.dp, height = 48.dp)
                        .border(0.5.dp, Color.Black)
                        .background(TableHighlight),
                    contentAlignment = Alignment.Center
                ) {
                    if (step >= 2) {
                        Text(
                            text = rCode,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }
                }

                // Cell values
                colCodes.forEachIndexed { cIdx, _ ->
                    val cellVal = kmap.grid[rIdx][cIdx]
                    val mintermIdx = calculateMinterm(rIdx, cIdx, rowCodes, colCodes)

                    val isCellFilled = step >= 3
                    val isTargetCell = cellVal == targetVal
                    val isHighlightedTarget = isCellFilled && isTargetCell && step >= 4

                    val groupForCell = if (step >= 6) groups.firstOrNull { g -> g.cells.contains(Pair(rIdx, cIdx)) } else null
                    val groupColor = groupForCell?.let { getGroupColor(it) }

                    val bg = when {
                        groupColor != null -> groupColor.copy(alpha = 0.20f)
                        isHighlightedTarget -> TableHighlight
                        else -> CardBackground
                    }

                    val borderColor = groupColor ?: Color.Black
                    val borderWidth = if (groupColor != null) 2.5.dp else 0.5.dp

                    Box(
                        modifier = Modifier
                            .size(width = 48.dp, height = 48.dp)
                            .background(bg)
                            .border(borderWidth, borderColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCellFilled) {
                            // Minterm index in small green text at top right
                            Text(
                                text = mintermIdx.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                            )
                            // Cell value (0 or 1)
                            Text(
                                text = cellVal.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = groupColor ?: PrimaryText
                            )
                        }
                    }
                }
            }
        }
    }
}
