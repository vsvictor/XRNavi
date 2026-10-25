package com.mobilespace.xrnavi

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.jetbrains.compose.resources.Font
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.inter_variable
import xrnavi.shared.generated.resources.roboto_condensed_variable
import xrnavi.shared.generated.resources.space_grotesk_variable

@Composable
internal fun rememberInterFontFamily(): FontFamily {
    val regular = Font(Res.font.inter_variable, FontWeight.Normal)
    val medium = Font(Res.font.inter_variable, FontWeight.Medium)
    val semibold = Font(Res.font.inter_variable, FontWeight.SemiBold)
    val bold = Font(Res.font.inter_variable, FontWeight.Bold)
    return remember { FontFamily(regular, medium, semibold, bold) }
}

@Composable
internal fun rememberRobotoCondensedFontFamily(): FontFamily {
    val normal = Font(Res.font.roboto_condensed_variable, FontWeight.Normal)
    val bold = Font(Res.font.roboto_condensed_variable, FontWeight.Bold)
    return remember { FontFamily(normal, bold) }
}

@Composable
internal fun rememberSpaceGroteskFontFamily(): FontFamily {
    val normal = Font(Res.font.space_grotesk_variable, FontWeight.Normal)
    val bold = Font(Res.font.space_grotesk_variable, FontWeight.Bold)
    return remember { FontFamily(normal, bold) }
}
