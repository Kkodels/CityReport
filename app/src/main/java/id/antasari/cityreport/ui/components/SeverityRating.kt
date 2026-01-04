package id.antasari.cityreport.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.antasari.cityreport.ui.theme.Gray300
import id.antasari.cityreport.ui.theme.Gray600
import id.antasari.cityreport.ui.theme.Primary

@Composable
fun SeverityRating(
    selectedLevel: Int,
    onLevelSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Tingkat Keparahan",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(Modifier.height(8.dp))
        
        // 5 numbered buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (level in 1..5) {
                SeverityButton(
                    level = level,
                    isSelected = selectedLevel == level,
                    onClick = { onLevelSelected(level) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Legend
        Text(
            text = "1 = Ringan, 5 = Sangat Parah",
            style = MaterialTheme.typography.bodySmall,
            color = Gray600
        )
    }
}

@Composable
fun SeverityButton(
    level: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Primary else Color.White
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Primary else Gray300,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = level.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Gray600
        )
    }
}
