package com.example.booleancalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booleancalculator.ui.theme.PrimaryBlue
import com.example.booleancalculator.ui.theme.PrimaryText

@Composable
fun AnswerDisplay(
    tupleText: String,
    answerValue: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(PrimaryBlue)
        )
        Spacer(modifier = Modifier.width(14.dp))

        val text = buildAnnotatedString {
            append("Answer ($tupleText) = ")
            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)) {
                append(answerValue.toString())
            }
        }

        Text(
            text = text,
            fontSize = 22.sp,
            color = PrimaryText
        )
    }
}
