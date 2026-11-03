package com.example.booleancalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booleancalculator.ui.theme.CardBackground
import com.example.booleancalculator.ui.theme.PrimaryBlue

@Composable
fun MainActionButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!enabled) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(PrimaryBlue)
                .clickable { onClick() }
                .padding(vertical = 16.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Show Full Table  ↩",
                color = CardBackground,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
