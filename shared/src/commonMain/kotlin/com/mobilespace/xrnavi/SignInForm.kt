package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

@Composable
internal fun SignInForm(
    modifier: Modifier,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onSelectAccount: () -> Unit,
    submitting: Boolean,
    notice: String,
    onSignIn: () -> Unit,
    onDemo: () -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    dark: Boolean
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "З поверненням",
            fontFamily = rememberRobotoCondensedFontFamily(),
            fontSize = 28.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        LabeledInput(
            label = "Електронна пошта",
            value = email,
            onValueChange = onEmailChange,
            placeholder = "",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            trailingIcon = if (dark) Res.drawable.sign_in_mail_dark else Res.drawable.sign_in_mail_light,
            onTrailingClick = onSelectAccount,
            trailingDescription = "Обрати поштовий акаунт",
            readOnly = submitting,
            trailingEnabled = !submitting,
        )
        LabeledInput(
            label = "Пароль",
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSignIn() }),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = if (passwordVisible) Res.drawable.sign_in_eye
                else if (dark) Res.drawable.sign_in_eye_off_dark else Res.drawable.sign_in_eye_off_light,
            onTrailingClick = onTogglePasswordVisibility,
            trailingDescription = if (passwordVisible) "Приховати пароль" else "Показати пароль",
            readOnly = submitting,
        )
        Text(
            text = "Забули пароль?",
            modifier = Modifier
                .align(Alignment.End)
                .clickable(role = Role.Button, onClick = onForgotPassword)
                .padding(vertical = 2.dp),
            fontSize = 12.sp,
            letterSpacing = 0.4.sp,
            color = MaterialTheme.colorScheme.primary
        )
        XrnaviActionButton(
            text = if (submitting) "Вхід…" else "Увійти",
            icon = if (dark) Res.drawable.sign_in_arrow_dark else Res.drawable.sign_in_arrow_light,
            onClick = onSignIn,
            height = 52.dp,
            cornerRadius = 26.dp,
            enabled = !submitting,
        )
        if (notice.isNotBlank()) {
            StatusCard(
                title = "Вхід",
                message = notice
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 1.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.surfaceVariant))
            Text(
                text = "АБО УВІЙТИ З",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.surfaceVariant))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProviderButton(
                modifier = Modifier.weight(1f),
                label = "Apple",
                icon = if (dark) Res.drawable.sign_in_apple_dark else Res.drawable.sign_in_apple_light
            )
            ProviderButton(
                modifier = Modifier.weight(1f),
                label = "Google",
                icon = if (dark) Res.drawable.sign_in_google_dark else Res.drawable.sign_in_google_light
            )
        }
        Text(
            text = "Apple та Google не підключені",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Продовжити без входу · деморежим",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable(role = Role.Button, onClick = onDemo)
                .padding(vertical = 9.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Не маєте акаунта? Зареєструватися",
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onRegister)
                .padding(vertical = 12.dp),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FigmaIcon(
                light = Res.drawable.sign_in_building_light,
                dark = Res.drawable.sign_in_building_dark,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "  Вхід через вашу організацію · не підключено",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
