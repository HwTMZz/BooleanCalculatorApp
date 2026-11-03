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

@Composable
fun KMapView(
    kmap: KMapModel,
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

            colCodes.forEach { cCode ->
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 48.dp)
                        .border(0.5.dp, Color.Black)
                        .background(TableHighlight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cCode,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
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
                    Text(
                        text = rCode,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }

                // Cell values
                colCodes.forEachIndexed { cIdx, _ ->
                    val cellVal = kmap.grid[rIdx][cIdx]
                    val mintermIdx = calculateMinterm(rIdx, cIdx, rowCodes, colCodes)

                    Box(
                        modifier = Modifier
                            .size(width = 48.dp, height = 48.dp)
                            .border(0.5.dp, Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
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
                            color = PrimaryText
                        )
                    }
                }
            }
        }
    }
}
