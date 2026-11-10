package com.example.booleancalculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booleancalculator.ui.theme.BorderColor
import com.example.booleancalculator.ui.theme.CardBackground
import com.example.booleancalculator.ui.theme.MainBackground
import com.example.booleancalculator.ui.theme.PrimaryBlue
import com.example.booleancalculator.ui.theme.PrimaryText
import com.example.booleancalculator.ui.theme.SecondaryText
import com.example.booleancalculator.viewmodel.UiState

/*
ส่วนแสดง Solving Step เมื่อผู้ใช้กรอกค่าของแต่ละตัวแปรลงไป
*/

@Composable
fun SimplifySolveScreen(
    state: UiState,
    onBack: () -> Unit,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = state.identitySteps
    val currentStep = state.simplifyStep.coerceIn(1, steps.size.coerceAtLeast(1))
    val visibleSteps = steps.take(currentStep)

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
                .padding(20.dp, 16.dp, 16.dp, 6.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {

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
                text = "✕",
                fontSize = 16.sp,
                color = PrimaryText,
                fontWeight = FontWeight.Bold
            )
        }
            Column (
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = "Simplify Expressions",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
                Text(
                    text = "Using Quine-McCluskey Algorithm",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
            }
        }

//        // Subtitle matching reference web
//        Text(
//            text = "Boolean Identities",
//            fontSize = 16.sp,
//            color = PrimaryText,
//            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
//        )

        // Steps List Container (.identity-steps style)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackground)
                .padding(start = 16.dp, end = 16.dp, top = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                visibleSteps.forEachIndexed { idx, step ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                if (idx < visibleSteps.size - 1) {
                                    val strokeWidth = 1.dp.toPx()
                                    drawLine(
                                        color = Color(0xFFE5E5EA),
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = strokeWidth
                                    )
                                }
                            }
                            .padding(vertical = 12.dp)
                    ) {
                        if (idx == 0) {
                            // Line 1 spans full width
                            Text(
                                text = step.expression,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        } else {
                            // 3-column layout matching reference web (.identity-step grid)
                            Column(
                                modifier = Modifier
//                                    .background(Color(0xFFF2F2F7))
                                .fillMaxWidth(),
                                horizontalAlignment = Alignment.Start

                            ) {// Column 1: Expression
                                Text(
                                    text = step.expression,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = PrimaryText,
//                                    modifier = Modifier.weight(1.15f)
                                )
//                                Text(text = "Hee")
                                Text(
                                        text = step.law,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = SecondaryText,
//                                        modifier = Modifier.weight(0.8f)
                                    )

                                    // Column 3: Rule Formula (Primary Blue)
                                    Text(
                                        text = step.identity,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = PrimaryBlue,
//                                        modifier = Modifier.weight(1.25f)
                                    )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {


                                    // Column 2: Law Name

                                }
                            }

                        }
                    }
                }
            }
        }

        // Footer Step Controls (Leave only Previous when finished, remove Done)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 0.dp, 20.dp, 24.dp)
        ) {
            Text(
                text = "Line $currentStep of ${steps.size}",
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
                if (currentStep > 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(CardBackground)
                            .border(1.5.dp, BorderColor, RoundedCornerShape(22.dp))
                            .clickable { onPrevStep() }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "← Previous",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText
                        )
                    }
                }

                val canNext = currentStep < steps.size
                if (canNext) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(PrimaryBlue)
                            .border(1.5.dp, PrimaryBlue, RoundedCornerShape(22.dp))
                            .clickable { onNextStep() }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Next →",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CardBackground
                        )
                    }
                }
            }
        }
    }
}
