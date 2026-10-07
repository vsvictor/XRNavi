package com.mobilespace.xrnavi.presentation.ui

import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.mustang_photograph
import xrnavi.shared.generated.resources.mustang_cockpit


@Composable
internal fun CockpitBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(106.dp)
            .clip(RoundedCornerShape(16.dp)),
    ) {
        Image(
            painter = painterResource(Res.drawable.mustang_cockpit),
            contentDescription = stringResource(Res.string.ui_inter_yer_avtomobilya_mustang),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to ApexBackground.copy(alpha = 0.87f),
                        1f to ApexBackground.copy(alpha = 0.13f),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            LabelText(
                stringResource(Res.string.ui_bilshe_nizh_doroha),
                ApexText,
                22.sp,
                weight = FontWeight.Bold,
                condensed = true,
            )
            LabelText(stringResource(Res.string.ui_odyn_profil_usi_vashi_avtomobili), ApexMuted, 10.sp)
        }
    }
}

@Composable
internal fun AccountTypeSelector(
    personalAccount: Boolean,
    onSelect: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ApexSurface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AccountTypeOption(
            label = stringResource(Res.string.ui_osobystyy),
            icon = "♙",
            selected = personalAccount,
            onClick = { onSelect(true) },
        )
        AccountTypeOption(
            label = stringResource(Res.string.ui_korporatyvnyy),
            icon = "▤",
            selected = !personalAccount,
            onClick = { onSelect(false) },
        )
    }
}

@Composable
private fun RowScope.AccountTypeOption(
    label: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .height(38.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Color(0xFF20262E) else Color.Transparent)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        LabelText(icon, if (selected) ApexLinkRed else ApexMuted, 14.sp)
        Spacer(Modifier.width(6.dp))
        LabelText(
            label,
            if (selected) ApexText else ApexMuted,
            12.sp,
            weight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
internal fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.mustang_photograph),
            contentDescription = stringResource(Res.string.ui_ford_mustang_u_vechirnomu_misti),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to ApexBackground.copy(alpha = 0.4f),
                        0.4f to Color.Transparent,
                        1f to ApexBackground,
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ApexRed),
                    contentAlignment = Alignment.Center,
                ) {
                    LabelText("➤", ApexText, 17.sp)
                }
                Spacer(Modifier.width(8.dp))
                LabelText(stringResource(Res.string.ui_apex), ApexText, 24.sp, weight = FontWeight.Bold, condensed = true)
                Spacer(Modifier.width(8.dp))
                LabelText(stringResource(Res.string.ui_drive), ApexMuted, 10.sp)
            }
            Column {
                LabelText(
                    text = stringResource(Res.string.ui_tviy_rytm_tviy_marshrut),
                    color = ApexText,
                    fontSize = 38.sp,
                    weight = FontWeight.Bold,
                    condensed = true,
                    lineHeight = 40.sp,
                )
                Spacer(Modifier.height(7.dp))
                LabelText(
                    stringResource(Res.string.ui_navihatsiya_dlya_kozhnoho_khto_za_kermom),
                    ApexMuted,
                    13.sp,
                )
            }
        }
    }
}

@Composable
internal fun ApexTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    error: String?,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    trailingLabel: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTrailingClick: (() -> Unit)? = null,
) {
    Column {
        LabelText(label, ApexMuted, 12.sp)
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ApexSurface)
                .border(
                    1.dp,
                    if (error != null) ApexLinkRed else ApexBorder,
                    RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 14.dp),
            singleLine = true,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            textStyle = TextStyle(
                color = ApexText,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif,
            ),
            cursorBrush = SolidColor(ApexRed),
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (value.isEmpty()) LabelText(hint, ApexSubtle, 14.sp)
                        innerTextField()
                    }
                    LabelText(
                        trailingLabel,
                        ApexMuted,
                        18.sp,
                        modifier = Modifier.then(
                            if (onTrailingClick != null) Modifier.clickable(onClick = onTrailingClick)
                            else Modifier,
                        ),
                    )
                }
            },
        )
        error?.let {
            Spacer(Modifier.height(4.dp))
            LabelText(it, ApexLinkRed, 11.sp)
        }
    }
}

@Composable
internal fun PrimaryButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) ApexRed else ApexRed.copy(alpha = 0.6f))
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (enabled) {
            LabelText("→", ApexText, 19.sp)
        } else {
            CircularProgressIndicator(
                modifier = Modifier.width(18.dp).height(18.dp),
                color = ApexText,
                strokeWidth = 2.dp,
            )
        }
        Spacer(Modifier.width(10.dp))
        LabelText(label, ApexText, 15.sp, weight = FontWeight.SemiBold)
    }
}

@Composable
internal fun AlternativeDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.weight(1f).height(1.dp).background(ApexBorder))
        LabelText(stringResource(Res.string.ui_abo_uviyty_z), ApexSubtle, 10.sp)
        Spacer(Modifier.weight(1f).height(1.dp).background(ApexBorder))
    }
}

@Composable
internal fun RowScope.ProviderButton(icon: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .weight(1f)
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ApexSurface)
            .border(BorderStroke(1.dp, ApexBorder), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabelText(icon, ApexText, 18.sp)
        Spacer(Modifier.width(9.dp))
        LabelText(label, ApexText, 13.sp, weight = FontWeight.Medium)
    }
}

@Composable
internal fun LabelText(
    text: String,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
    weight: FontWeight = FontWeight.Normal,
    condensed: Boolean = false,
    lineHeight: androidx.compose.ui.unit.TextUnit = fontSize * 1.35f,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = weight,
        fontFamily = FontFamily.SansSerif,
        letterSpacing = if (condensed) (-1).sp else 0.sp,
        lineHeight = lineHeight,
        textAlign = textAlign,
    )
}
