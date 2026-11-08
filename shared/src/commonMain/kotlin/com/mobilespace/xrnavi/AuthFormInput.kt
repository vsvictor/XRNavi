package com.mobilespace.xrnavi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import xrnavi.shared.generated.resources.*

@Composable
internal fun AuthFormInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    onBlur: () -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier,
    secret: Boolean = false,
    visible: Boolean = false,
    onToggleVisibility: () -> Unit = {},
    trailingIcon: DrawableResource? = null
) {
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    XrnaviTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        error = error,
        onBlur = onBlur,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = if (secret && !visible) PasswordVisualTransformation() else VisualTransformation.None,
    ) { inputHeight ->
        if (secret) {
            IconButton(
                onClick = onToggleVisibility,
                modifier = Modifier.size(inputHeight).clearAndSetSemantics {
                    contentDescription = "${if (visible) "Приховати" else "Показати"}: $label"
                    stateDescription = if (visible) "Пароль показано" else "Пароль приховано"
                    role = Role.Button
                    onClick { onToggleVisibility(); true }
                }
            ) {
                FigmaIcon(
                    light = if (visible) Res.drawable.sign_in_eye
                    else if (dark) Res.drawable.sign_in_eye_off_dark else Res.drawable.sign_in_eye_off_light,
                    dark = if (visible) Res.drawable.sign_in_eye
                    else if (dark) Res.drawable.sign_in_eye_off_dark else Res.drawable.sign_in_eye_off_light,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else if (trailingIcon != null) {
            FigmaIcon(
                light = trailingIcon,
                dark = trailingIcon,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
internal expect fun AuthBackHandler(onBack: () -> Unit)

@Composable
internal expect fun internalAuthPreviewEnabled(): Boolean
