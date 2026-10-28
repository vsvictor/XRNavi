package com.mobilespace.xrnavi

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
import org.jetbrains.compose.resources.painterResource
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
    leadingIcon: @Composable (() -> Unit)? = null
) {
    var wasFocused by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth().onFocusChanged {
                if (wasFocused && !it.isFocused) onBlur()
                wasFocused = it.isFocused
            }.semantics {
                contentDescription = label
                if (error != null) error(error)
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontFamily = rememberInterFontFamily(),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            isError = error != null,
            shape = RoundedCornerShape(4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                errorContainerColor = MaterialTheme.colorScheme.surface
            ),
            visualTransformation = if (secret && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            leadingIcon = leadingIcon,
            trailingIcon = if (secret) {
                {
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.semantics {
                            contentDescription =
                                "${if (visible) "Приховати" else "Показати"}: $label"
                            stateDescription =
                                if (visible) "Пароль показано" else "Пароль приховано"
                        }
                    ) {
                        Icon(
                            painterResource(
                                if (MaterialTheme.colorScheme.background.red < 0.5f)
                                    Res.drawable.sign_in_eye_off_dark
                                else Res.drawable.sign_in_eye_off_light
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null
        )
        if (error != null) {
            Text(
                error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
    }
}

@Composable
internal fun AuthInfo(message: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Text(
            message,
            modifier = Modifier.padding(14.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal expect fun AuthBackHandler(onBack: () -> Unit)

@Composable
internal expect fun internalAuthPreviewEnabled(): Boolean
