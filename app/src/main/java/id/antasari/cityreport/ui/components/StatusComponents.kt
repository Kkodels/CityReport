package id.antasari.cityreport.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.antasari.cityreport.ui.theme.*

/**
 * Status badge pill matching new design
 */
@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "diproses", "in progress" -> StatusDiprosesBg to StatusDiprosesText
        "selesai", "completed" -> StatusSelesaiBg to StatusSelesaiText
        "menunggu", "pending" -> StatusMenungguBg to StatusMenungguText
        "darurat", "urgent" -> StatusDaruratBg to StatusDaruratText
        "tinggi", "high" -> StatusMenungguBg to StatusMenungguText
        else -> Gray200 to TextSecondary
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(CornerRadius.Small),
        color = backgroundColor
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(
                horizontal = Spacing.Small,
                vertical = Spacing.ExtraSmall
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

/**
 * Category icon with colored background
 */
@Composable
fun CategoryIcon(
    category: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 48.dp
) {
    val (bgColor, iconColor) = when (category.lowercase()) {
        "jalan rusak", "infrastruktur" -> CategoryJalanRusakBg to CategoryJalanRusak
        "sampah", "kebersihan" -> CategorySampahBg to CategorySampah
        "banjir", "bencana" -> CategoryBanjirBg to CategoryBanjir
        "lampu jalan", "penerangan" -> CategoryLampuJalanBg to CategoryLampuJalan
        "fasilitas", "taman & kota" -> CategoryFasilitasBg to CategoryFasilitas
        else -> CategoryLainnyaBg to CategoryLainnya
    }
    
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = bgColor,
                shape = RoundedCornerShape(CornerRadius.Medium)
            ),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(id = getCategoryIcon(category)),
            contentDescription = category,
            tint = iconColor,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

private fun getCategoryIcon(category: String): Int {
    return when (category.lowercase()) {
        "jalan rusak", "infrastruktur" -> android.R.drawable.ic_dialog_map
        "sampah", "kebersihan" -> android.R.drawable.ic_delete
        "banjir", "bencana" -> android.R.drawable.ic_dialog_alert
        "lampu jalan", "penerangan" -> android.R.drawable.ic_menu_view
        "fasilitas", "taman & kota" -> android.R.drawable.ic_menu_mapmode
        else -> android.R.drawable.ic_menu_more
    }
}
