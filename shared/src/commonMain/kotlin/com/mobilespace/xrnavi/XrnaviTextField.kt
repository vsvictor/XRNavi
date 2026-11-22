package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Єдине поле вводу застосунку: вигляд і налаштування як на екрані входу. */
@Composable
internal fun XrnaviTextField(
    label: String?,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: String? = null,
    supportingText: String? = null,
    onBlur: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    trailing: @Composable (inputHeight: androidx.compose.ui.unit.Dp) -> Unit = {},
) {
    val inputHeight = with(LocalDensity.current) { 20.sp.toDp() + 24.dp }
    val shape = RoundedCornerShape(4.dp)
    val borderColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
    var wasFocused by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (label != null) {
            Text(
                text = label,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .onFocusChanged {
                    if (wasFocused && !it.isFocused) onBlur()
                    wasFocused = it.isFocused
                }
                .semantics {
                    if (label != null) contentDescription = label
                    if (error != null) error(error)
                }
                .fillMaxWidth()
                .heightIn(min = inputHeight)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, borderColor, shape)
                .padding(horizontal = 14.dp),
            singleLine = singleLine,
            readOnly = readOnly,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Left,
                letterSpacing = 0.25.sp,
                fontFamily = rememberInterFontFamily()
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                    trailing(inputHeight)
                }
            }
        )
        val note = error ?: supportingText
        if (note != null) {
            Text(
                note,
                color = if (error != null) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.semantics {
                    if (error != null) liveRegion = LiveRegionMode.Polite
                }
            )
        }
    }
}
