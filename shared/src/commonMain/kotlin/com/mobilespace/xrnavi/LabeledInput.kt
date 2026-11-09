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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import xrnavi.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource

@Composable
internal fun LabeledInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions,
    trailingIcon: DrawableResource,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTrailingClick: (() -> Unit)? = null,
    trailingDescription: String = "",
    trailingEnabled: Boolean = true,
    readOnly: Boolean = false,
) {
    val inputHeight = with(LocalDensity.current) { 20.sp.toDp() + 24.dp }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .semantics { contentDescription = label }
                .fillMaxWidth()
                .height(inputHeight)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(horizontal = 14.dp),
            singleLine = true,
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
                    if (onTrailingClick != null) {
                        IconButton(
                            onClick = onTrailingClick,
                            enabled = trailingEnabled,
                            modifier = Modifier.size(inputHeight).clearAndSetSemantics {
                                contentDescription = trailingDescription
                                role = Role.Button
                                if (trailingEnabled) {
                                    onClick {
                                        onTrailingClick()
                                        true
                                    }
                                } else {
                                    disabled()
                                }
                            },
                        ) {
                            FigmaIcon(
                                light = trailingIcon,
                                dark = trailingIcon,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        FigmaIcon(
                            light = trailingIcon,
                            dark = trailingIcon,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        )
    }
}
