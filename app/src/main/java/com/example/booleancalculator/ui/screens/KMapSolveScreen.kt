package com.example.booleancalculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booleancalculator.domain.model.KMapMode
import com.example.booleancalculator.ui.components.KMapView
import com.example.booleancalculator.ui.theme.BorderColor
import com.example.booleancalculator.ui.theme.CardBackground
import com.example.booleancalculator.ui.theme.MainBackground
import com.example.booleancalculator.ui.theme.PrimaryBlue
import com.example.booleancalculator.ui.theme.PrimaryText
import com.example.booleancalculator.ui.theme.SecondaryText
import com.example.booleancalculator.ui.theme.TableHighlight
import com.example.booleancalculator.viewmodel.UiState

@Composable
fun KMapSolveScreen(
    state: UiState,
    onBack: () -> Unit,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onToggleKMapMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val kmap = state.kMapModel ?: return
    val step = state.kMapSolveStep
    val mode = state.kMapMode
    val groups = if (mode == KMapMode.SOP) kmap.sopGroups else kmap.posGroups
    val resultExpr = if (mode == KMapMode.SOP) kmap.sopExpression else kmap.posExpression

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MainBackground)
            .statusBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CardBackground)
                        .border(1.dp, BorderColor, CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "←",
                        fontSize = 24.sp,
                        color = PrimaryText
                    )
                }

                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = "K-Map Solving Steps",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText
                    )
                    Text(
                        text = "Step ${step + 1} of 8",
                        fontSize = 13.sp,
                        color = SecondaryText
                    )
                }
            }

            // SOP / POS Mode Toggle Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBackground)
                    .border(1.5.dp, PrimaryBlue, RoundedCornerShape(20.dp))
                    .clickable { onToggleKMapMode() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.name,
                    color = PrimaryBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Scrollable Body
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Explanation Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBackground)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    val title = when (step) {
                        0 -> "Step 1: K-Map Grid Layout"
                        1 -> "Step 2: Variable Allocation"
                        2 -> "Step 3: Gray-Code Ordering"
                        3 -> "Step 4: Cell Population"
                        4 -> "Step 5: Identify Target Cells ($mode)"
                        5 -> "Step 6: Group Formation Rules"
                        6 -> "Step 7: Grouping Overlays"
                        else -> "Step 8: Final Simplified Expression"
                    }

                    val desc = when (step) {
                        0 -> "Displaying blank grid structure based on total variables."
                        1 -> "Rows represent '${kmap.rowVars.joinToString("")}', columns represent '${kmap.colVars.joinToString("")}'."
                        2 -> "Headers use Gray code (00, 01, 11, 10) so adjacent cells differ by exactly 1 bit."
                        3 -> "Filling grid values cell-by-cell from the truth table outputs."
                        4 -> "Highlighting cells for $mode simplification."
                        5 -> "Finding optimal rectangular groups of sizes 1, 2, 4, 8, 16 with edge wrap-around."
                        6 -> "Formed ${groups.size} group(s) covering all required cells."
                        else -> "Resulting $mode expression: $resultExpr"
                    }

                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        fontSize = 14.sp,
                        color = SecondaryText,
                        lineHeight = 20.sp
                    )
                }
            }

            // K-Map Grid Display (Using standard KMapView)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                KMapView(kmap = kmap)
            }

            // Group Terms List (step >= 6)
            if (step >= 6 && groups.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Group Terms ($mode):",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    groups.forEach { g ->
                        Text(
                            text = "Group ${g.id}: ${g.termExpression} (covers minterms ${g.minterms.joinToString(", ")})",
                            fontSize = 14.sp,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Result Expression (step >= 7)
            if (step >= 7) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TableHighlight)
                        .border(1.5.dp, PrimaryBlue, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Simplified Expression ($mode) = $resultExpr",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Footer Step Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Step ${step + 1} of 8",
                fontSize = 14.sp,
                color = SecondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(CardBackground)
                        .border(1.5.dp, BorderColor, RoundedCornerShape(22.dp))
                        .clickable(enabled = step > 0) { onPrevStep() }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "← Previous",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (step > 0) SecondaryText else SecondaryText.copy(alpha = 0.4f)
                    )
                }

                val canNext = step < 7
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (canNext) PrimaryBlue else CardBackground)
                        .border(1.5.dp, if (canNext) PrimaryBlue else BorderColor, RoundedCornerShape(22.dp))
                        .clickable { if (canNext) onNextStep() else onBack() }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (canNext) "Next Step →" else "Done",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canNext) CardBackground else SecondaryText
                    )
                }
            }
        }
    }
}
