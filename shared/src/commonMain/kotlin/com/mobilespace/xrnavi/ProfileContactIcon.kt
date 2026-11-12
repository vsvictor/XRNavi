package com.mobilespace.xrnavi

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

@Composable
internal fun ProfileContactIcon(type: String) {
    val (label, path) = when (type) {
        "phone" -> "Телефон" to "M5 3H9L11 8L8 10C10 13 11 14 14 16L16 13L21 15V19C21 21 19 22 17 21C9 19 5 15 3 7C2 5 3 3 5 3Z"
        "email" -> "Електронна пошта" to "M3 5H21V19H3ZM3 5L12 12L21 5"
        "telegram" -> "Telegram" to "M2 11L22 3L18 21L11 15L7 18V13L18 6L9 14"
        "viber" -> "Viber" to "M7 3H17Q21 3 21 7V15Q21 19 17 19H9L5 22V19Q2 19 2 15V7Q2 3 7 3ZM7 7Q8 13 15 16L17 13L14 11L12 12L10 10L11 8ZM14 6Q18 6 18 10"
        "facebook" -> "Facebook" to "M14 22V13H18L19 9H14V6Q14 4 17 4H19V1H16Q10 1 10 6V9H7V13H10V22"
        "x" -> "X" to "M3 3L17 21H21L7 3ZM21 3L3 21"
        "instagram" -> "Instagram" to "M7 3H17Q21 3 21 7V17Q21 21 17 21H7Q3 21 3 17V7Q3 3 7 3ZM16 12A4 4 0 1 1 8 12A4 4 0 1 1 16 12M17 6H18"
        "tiktok" -> "TikTok" to "M14 3V16A5 5 0 1 1 9 11M14 3Q15 8 21 8V12Q17 12 14 9"
        "whatsapp" -> "WhatsApp" to "M4 17L2 22L8 20A9 9 0 1 0 4 17ZM8 7Q8 13 15 16L17 13L14 11L12 12L10 10L11 8Z"
        "signal" -> "Signal" to "M7 3L9 2M12 2H14M17 3L19 5M21 8L22 10M22 13L21 15M19 18L17 20M14 21H11M8 20L3 22L4 17M2 14V12M2 9L3 7M4 5L5 4"
        "linkedin" -> "LinkedIn" to "M3 9H7V21H3ZM3 3H7V6H3ZM11 21V9H15V11Q21 6 21 14V21M15 21V14"
        "website" -> "Вебсайт" to "M21 12A9 9 0 1 1 3 12A9 9 0 1 1 21 12ZM3 12H21M12 3Q5 12 12 21Q19 12 12 3ZM5 7H19M5 17H19"
        else -> type to "M10 14L14 10M8 16L6 18A4 4 0 0 1 1 12L7 6M16 8L18 6A4 4 0 0 1 23 12L17 18"
    }
    val vector = remember(type, path) {
        ImageVector.Builder("Contact-$type", 24.dp, 24.dp, 24f, 24f).addPath(
            pathData = PathParser().parsePathString(path).toNodes(),
            stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ).build()
    }
    Icon(
        vector, contentDescription = label, modifier = Modifier.size(20.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
