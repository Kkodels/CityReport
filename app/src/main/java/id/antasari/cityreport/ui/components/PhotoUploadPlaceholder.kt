package id.antasari.cityreport.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.antasari.cityreport.ui.theme.Gray400
import id.antasari.cityreport.ui.theme.Gray600
import id.antasari.cityreport.ui.theme.Primary

@Composable
fun PhotoUploadPlaceholder(
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Foto",
            style = MaterialTheme.typography.titleSmall
        )
        
        Spacer(Modifier.height(8.dp))
        
        // Dashed border box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .border(
                    width = 2.dp,
                    color = Gray400,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Gray400
                )
                
                Spacer(Modifier.height(8.dp))
                
                TextButton(onClick = onUploadClick) {
                    Text(
                        text = "Tap untuk upload foto",
                        color = Primary,
                        textAlign = TextAlign.Center
                    )
                }
                
                Text(
                    text = "(Opsional)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
            }
        }
    }
}
