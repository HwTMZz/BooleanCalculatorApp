package com.example.booleancalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booleancalculator.domain.model.Notation
import com.example.booleancalculator.domain.model.VarSet
import com.example.booleancalculator.ui.theme.BorderColor
import com.example.booleancalculator.ui.theme.CardBackground
import com.example.booleancalculator.ui.theme.DarkButton
import com.example.booleancalculator.ui.theme.PrimaryText
import com.example.booleancalculator.ui.theme.SecondaryText

@Composable
fun ControlRow(
    varSet: VarSet,
    notation: Notation,
    isAdvanced: Boolean,
    onCycleVarSet: () -> Unit,
    onToggleNotation: () -> Unit,
    onToggleAdvance: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // VarSet Button
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackground)
                .border(1.5.dp, BorderColor, RoundedCornerShape(20.dp))
                .clickable { onCycleVarSet() }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = varSet.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryText
            )
        }

        // Notation Button
        Box(
            modifier = Modifier
                .weight(1.2f)
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackground)
                .border(1.5.dp, BorderColor, RoundedCornerShape(20.dp))
                .clickable { onToggleNotation() }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (notation == Notation.WORD) "AND OR NOT" else "* + !",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryText
            )
        }

        // Advance Button
        val advBg = if (isAdvanced) DarkButton else CardBackground
        val advText = if (isAdvanced) CardBackground else SecondaryText
        val advBorder = if (isAdvanced) DarkButton else BorderColor

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(advBg)
                .border(1.5.dp, advBorder, RoundedCornerShape(20.dp))
                .clickable { onToggleAdvance() }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Advance",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = advText
            )
        }

        // Delete Button
        Box(
            modifier = Modifier
                .width(52.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackground)
                .border(1.5.dp, BorderColor, RoundedCornerShape(20.dp))
                .clickable { onDelete() }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⌫",
                fontSize = 18.sp,
                color = PrimaryText,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
