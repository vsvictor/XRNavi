package com.mobilespace.xrnavi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import kotlin.math.abs
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun SignInHero(
    modifier: Modifier,
    dark: Boolean,
    horizontalLayout: Boolean
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(if (dark) Res.drawable.mustang_hero_dark else Res.drawable.mustang_hero_light),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (horizontalLayout) {
                        Brush.horizontalGradient(
                            colorStops = if (dark) {
                                arrayOf(
                                    0f to Color(0x660B1220),
                                    0.5f to Color.Transparent,
                                    0.78f to Color(0xCC0B1220),
                                    1f to Color(0xFF0B1220)
                                )
                            } else {
                                arrayOf(
                                    0f to Color(0x66F6F8FC),
                                    0.45f to Color.Transparent,
                                    0.78f to Color(0xF7FFFFFF),
                                    1f to Color.White
                                )
                            }
                        )
                    } else {
                        Brush.verticalGradient(
                            colorStops = if (dark) {
                                arrayOf(
                                    0f to Color(0x660B1220),
                                    0.4f to Color.Transparent,
                                    0.64f to Color(0xCC0B1220),
                                    1f to Color(0xFF0B1220)
                                )
                            } else {
                                arrayOf(
                                    0f to Color(0xDDF6F8FC),
                                    0.23f to Color.Transparent,
                                    0.44f to Color.Transparent,
                                    0.6f to Color(0xF7FFFFFF),
                                    1f to Color.White
                                )
                            }
                        )
                    }
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ProductWordmark()
            Column {
                Text(
                    text = if (horizontalLayout) {
                        "ТВІЙ РИТМ.\nТВІЙ МАРШРУТ."
                    } else {
                        "ТВІЙ РИТМ. ТВІЙ МАРШРУТ."
                    },
                    fontFamily = rememberRobotoCondensedFontFamily(),
                    fontWeight = FontWeight.Bold,
                    fontSize = if (horizontalLayout) 28.sp else 38.sp,
                    lineHeight = if (horizontalLayout) 30.sp else 40.sp,
                    letterSpacing = (-0.45).sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Навігація для кожного",
                    modifier = Modifier.padding(top = 7.dp),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

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

@Composable
internal fun ProviderButton(
    modifier: Modifier,
    label: String,
    icon: DrawableResource
) {
    Row(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(12.dp)
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FigmaIcon(light = icon, dark = icon, modifier = Modifier.size(18.dp))
        Text(
            text = "  $label",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
internal fun ProductWordmark(modifier: Modifier = Modifier) {
    val dark = isDarkXrnaviTheme()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FigmaIcon(
            light = Res.drawable.orbit_glyph_light,
            dark = Res.drawable.orbit_glyph_dark,
            modifier = Modifier.size(30.dp),
            contentDescription = "Знак Mobile Space 01 Orbit"
        )
        Text(
            text = "XRNavi",
            fontFamily = rememberSpaceGroteskFontFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            letterSpacing = (-0.35).sp,
            color = if (dark) Color(0xFFF2F5FA) else Color(0xFF172033)
        )
    }
}

@Composable
internal fun XrnaviActionButton(
    text: String,
    icon: DrawableResource? = null,
    onClick: () -> Unit,
    height: Dp,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(cornerRadius)
    Button(
        onClick = onClick,
        modifier = modifier
            .clearAndSetSemantics {
                contentDescription = text
                role = Role.Button
                if (enabled) {
                    onClick(label = null) {
                        onClick()
                        true
                    }
                } else {
                    disabled()
                }
            }
            .fillMaxWidth()
            .heightIn(min = height),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(
            horizontal = if (compact) 10.dp else 16.dp,
            vertical = 0.dp
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                FigmaIcon(
                    light = icon,
                    dark = icon,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = text,
                fontFamily = rememberInterFontFamily(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.1.sp,
                color = if (enabled) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}



@Composable
internal fun RouteVehicleCard(
    vehicle: DemoVehicleProfile?,
    origin: String,
    destination: String,
    onOpenGarage: () -> Unit,
    onOpenConfiguration: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Демоавтомобіль",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (vehicle == null) {
                Text(
                    "Автомобіль не вибрано. Спочатку оберіть демопрофіль у гаражі.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    vehicle.name,
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Категорія: ${vehicle.category}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Конфігурація: ${vehicle.configurationName ?: "Не задано"}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    vehicle.specification,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Це локальні демонстраційні дані. Маса не є підтвердженим допустимим лімітом; технічні характеристики не редагуються тут.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RouteReadOnlyValue("Початок", origin.ifBlank { "Не задано" })
            RouteReadOnlyValue("Призначення", destination.ifBlank { "Не задано" })
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Garage · змінити авто",
                    modifier = Modifier
                        .clickable(role = Role.Button, onClick = onOpenGarage)
                        .padding(vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "S16 · конфігурація",
                    modifier = Modifier
                        .clickable(role = Role.Button, onClick = onOpenConfiguration)
                        .padding(vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
internal fun RouteReadOnlyValue(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
internal fun RoutePreferenceCard(
    preferences: RoutePreferences,
    hasPendingChanges: Boolean,
    onPreferencesChange: (RoutePreferences) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Переваги маршруту",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (hasPendingChanges) {
                Text(
                    "Зміни ще не застосовано",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            RoutePreferenceSwitch(
                label = "Уникати платних доріг",
                checked = preferences.avoidTolls,
                onCheckedChange = { onPreferencesChange(preferences.copy(avoidTolls = it)) }
            )
            RoutePreferenceSwitch(
                label = "Уникати ґрунтових доріг",
                checked = preferences.avoidUnpavedRoads,
                onCheckedChange = { onPreferencesChange(preferences.copy(avoidUnpavedRoads = it)) }
            )
            RoutePreferenceSwitch(
                label = "Уникати поромів",
                checked = preferences.avoidFerries,
                onCheckedChange = { onPreferencesChange(preferences.copy(avoidFerries = it)) }
            )
            val enabledPreferences = listOfNotNull(
                "платні дороги".takeIf { preferences.avoidTolls },
                "ґрунтові дороги".takeIf { preferences.avoidUnpavedRoads },
                "пороми".takeIf { preferences.avoidFerries }
            )
            Text(
                text = if (enabledPreferences.isEmpty()) {
                    "Обмеження не вибрано"
                } else {
                    "Увімкнено: ${enabledPreferences.joinToString()}"
                },
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun RoutePreferenceSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
            .clearAndSetSemantics {
                contentDescription = label
                stateDescription = if (checked) "Увімкнено" else "Вимкнено"
                toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
                onClick(label) {
                    onCheckedChange(!checked)
                    true
                }
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = null
        )
    }
}

@Composable
internal fun MapTopBar(
    demoMode: Boolean,
    preview: Boolean,
    vehicleMode: String,
    onBack: () -> Unit,
    onOpenStopPlaces: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenExplanation: (() -> Unit)? = null
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        val compact = maxWidth < 440.dp
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (compact) 12.dp else 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (preview) {
                    Text(
                        text = "‹",
                        modifier = Modifier
                            .clickable(role = Role.Button, onClick = onBack)
                            .padding(end = 8.dp),
                        fontSize = 27.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                ProductWordmark()
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 10.dp)
            ) {
                TextButton(
                    onClick = onOpenStopPlaces,
                    modifier = Modifier.widthIn(max = 90.dp)
                ) {
                    Text("Місця зупинки", fontSize = 11.sp, lineHeight = 14.sp)
                }
                Text(
                    text = when {
                        compact && demoMode -> "Демо"
                        demoMode -> "Демо · $vehicleMode"
                        else -> vehicleMode
                    },
                    modifier = Modifier
                        .semantics {
                            contentDescription = if (demoMode) {
                                "Демо · $vehicleMode"
                            } else {
                                vehicleMode
                            }
                        },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Тема",
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(role = Role.Button, onClick = onToggleTheme)
                        .padding(horizontal = if (compact) 2.dp else 6.dp, vertical = 5.dp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
                )
                if (onOpenExplanation != null) IconButton(onClick = onOpenExplanation) {
                    AccountIcon(AccountIcon.Help, contentDescription = "Чому обрано цей шлях · приклад")
                }
            }
        }
    }
}

@Composable
internal fun MapCanvas(
    modifier: Modifier,
    preview: Boolean,
    compact: Boolean = false
) {
    val dark = isDarkXrnaviTheme()
    val showScaledMapLabels = LocalDensity.current.fontScale < 1.5f
    val land = if (dark) Color(0xFF111D2A) else Color(0xFFF1F5F9)
    val park = if (dark) Color(0xFF1C352F) else Color(0xFFDFEBD9)
    BoxWithConstraints(
        modifier = modifier.clipToBounds().background(land)
    ) {
        val w = maxWidth
        val h = if (compact) maxHeight * 2.3f else maxHeight

        Image(
            painter = painterResource(if (dark) Res.drawable.map_river_dark else Res.drawable.map_river_light),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .offset(x = w * 0.783f, y = h * 0.035f)
                .size(width = w * 0.209f, height = h * 0.636f)
        )
        Box(
            Modifier
                .offset(x = w * 0.037f, y = h * 0.194f)
                .size(width = w * 0.274f, height = h * 0.049f)
                .clip(RoundedCornerShape(18.dp))
                .background(park)
        )
        Box(
            Modifier
                .offset(x = w * 0.249f, y = h * 0.415f)
                .size(width = w * 0.204f, height = h * 0.087f)
                .clip(RoundedCornerShape(18.dp))
                .background(park)
        )
        Box(
            Modifier
                .offset(x = w * 0.684f, y = h * 0.076f)
                .size(width = w * 0.137f, height = h * 0.049f)
                .clip(RoundedCornerShape(18.dp))
                .background(park)
        )
        Box(
            Modifier
                .offset(x = w * 0.659f, y = h * 0.517f)
                .size(width = w * 0.187f, height = h * 0.067f)
                .clip(RoundedCornerShape(18.dp))
                .background(park)
        )
        MapRoadLayer(
            image = if (dark) Res.drawable.map_road_casing_dark else Res.drawable.map_road_casing_light,
            width = w,
            height = h
        )
        MapRoadLayer(
            image = if (dark) Res.drawable.map_road_network_dark else Res.drawable.map_road_network_light,
            width = w,
            height = h
        )
        if (preview) {
            Image(
                painter = painterResource(if (dark) Res.drawable.map_route_halo_dark else Res.drawable.map_route_halo_light),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .offset(x = w * 0.112f, y = h * 0.209f)
                    .size(width = w * 0.769f, height = h * 0.186f)
            )
            Image(
                painter = painterResource(if (dark) Res.drawable.map_planned_route_dark else Res.drawable.map_planned_route_light),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .offset(x = w * 0.112f, y = h * 0.209f)
                    .size(width = w * 0.769f, height = h * 0.186f)
            )
        }
        if (showScaledMapLabels && !compact) {
            MapPlaceLabel("Київ", w * 0.764f, h * 0.176f, strong = true)
            MapPlaceLabel("Житомир", w * 0.555f, h * 0.276f)
            MapPlaceLabel("Рівне", w * 0.274f, h * 0.315f)
            MapPlaceLabel("Львів", w * 0.042f, h * 0.358f, strong = true)
            MapPlaceLabel("М-06 / E40", w * 0.565f, h * 0.333f, size = 10.sp, strong = true)
        }
        if (preview) {
            Image(
                painter = painterResource(if (dark) Res.drawable.map_origin_pin_dark else Res.drawable.map_origin_pin_light),
                contentDescription = "Початкова точка демо-маршруту",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .offset(x = w * 0.863f, y = h * 0.202f)
                    .size(width = w * 0.035f, height = h * 0.019f)
            )
            Image(
                painter = painterResource(if (dark) Res.drawable.map_destination_pin_dark else Res.drawable.map_destination_pin_light),
                contentDescription = "Кінцева точка демо-маршруту",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .offset(x = w * 0.089f, y = h * 0.387f)
                    .size(width = w * 0.045f, height = h * 0.024f)
            )
            if (showScaledMapLabels) {
                RouteDurationChip(
                    modifier = Modifier
                        .offset(x = w * 0.47f, y = h * 0.454f)
                )
            }
        }
    }
}

@Composable
internal fun MapRoadLayer(
    image: DrawableResource,
    width: Dp,
    height: Dp
) {
    Box(
        modifier = Modifier
            .offset(x = width * (-0.05f), y = height * 0.0148f)
            .size(width = width * 1.12f, height = height * 0.656f)
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = width * 0.1999f,
                    top = height * 0.123f,
                    end = width * 0.2669f,
                    bottom = height * 0.2293f
                )
        )
    }
}

@Composable
internal fun MapPlaceLabel(
    text: String,
    x: Dp,
    y: Dp,
    size: androidx.compose.ui.unit.TextUnit = 12.sp,
    strong: Boolean = false
) {
    Text(
        text = text,
        modifier = Modifier.offset(x = x, y = y),
        fontSize = size,
        fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal,
        color = if (strong) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
    )
}

@Composable
internal fun RouteDurationChip(modifier: Modifier) {
    Text(
        text = "6 год 20 хв",
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
internal fun RouteEntryCard(
    origin: String,
    destination: String,
    routeStopsCount: Int,
    onOriginClick: () -> Unit,
    onDestinationClick: () -> Unit,
    onLocationClick: () -> Unit,
    onRouteStopsClick: () -> Unit,
    modifier: Modifier
) {
    val dark = isDarkXrnaviTheme()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable(role = Role.Button, onClick = onOriginClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface)
                )
                Text(
                    text = origin.ifBlank { "Моє місцезнаходження" },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    maxLines = 1,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FigmaIcon(
                    light = Res.drawable.map_swap_light,
                    dark = Res.drawable.map_swap_dark,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(role = Role.Button, onClick = onLocationClick),
                    contentDescription = "Використати місцезнаходження"
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable(role = Role.Button, onClick = onDestinationClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FigmaIcon(
                    light = Res.drawable.map_destination_marker_light,
                    dark = Res.drawable.map_destination_marker_dark,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = destination.ifBlank { "Вибрати адресу призначення" },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    maxLines = 1,
                    fontSize = 13.sp,
                    fontWeight = if (destination.isBlank()) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (destination.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                FigmaIcon(
                    light = Res.drawable.map_edit_light,
                    dark = Res.drawable.map_edit_dark,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(role = Role.Button, onClick = onDestinationClick),
                    contentDescription = "Редагувати адресу призначення"
                )
            }
            Text(
                "Зупинки маршруту · $routeStopsCount",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onRouteStopsClick)
                    .padding(start = 20.dp, top = 7.dp, bottom = 5.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
internal fun MapControlButton(
    iconLight: DrawableResource,
    iconDark: DrawableResource,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(44.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            FigmaIcon(
                light = iconLight,
                dark = iconDark,
                modifier = Modifier.size(20.dp),
                contentDescription = label
            )
        }
    }
}

@Composable
internal fun RouteBottomSheet(
    preview: Boolean,
    origin: String,
    destination: String,
    routeStops: List<RouteStop>,
    geoStatus: GeoStatus,
    notice: String,
    level: SheetLevel,
    showInlineAttribution: Boolean,
    vehicleClass: String,
    selectedVehicle: DemoVehicleProfile?,
    onVehicleClassChange: (String) -> Unit,
    sheetHeightDp: Dp,
    scrollConnection: NestedScrollConnection,
    scrollState: androidx.compose.foundation.ScrollState,
    minHeightPx: Float,
    partialHeightPx: Float,
    maxHeightPx: Float,
    currentHeightPx: () -> Float,
    updateHeight: (Float) -> Unit,
    onLevelChange: (SheetLevel) -> Unit,
    onOpenSearch: (SearchTarget) -> Unit,
    onOpenRouteStops: () -> Unit,
    onPreview: () -> Unit,
    onStartRoute: () -> Unit,
    onOpenRouteSettings: () -> Unit,
    sheetContent: (@Composable ColumnScope.() -> Unit)? = null,
    fixedPane: Boolean = false,
    modifier: Modifier
) {
    val dark = isDarkXrnaviTheme()
    val navigationUnavailable =
        preview && notice.startsWith("Маршрут і навігацію не підключено")
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(sheetHeightDp)
            .exportTestTags()
            .then(if (fixedPane) Modifier else Modifier.nestedScroll(scrollConnection)),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = if (fixedPane || sheetContent != null) 16.dp else 0.dp)
        ) {
            if (sheetContent == null && !fixedPane) Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .semantics {
                        contentDescription = "Панель маршруту"
                        stateDescription = when (level) {
                            SheetLevel.Collapsed -> "Згорнута"
                            SheetLevel.Partial -> "Частково розгорнута"
                            SheetLevel.Expanded -> "Розгорнута"
                        }
                    }
                    .clickable(role = Role.Button) {
                        onLevelChange(nextSheetLevel(level))
                    }
                    .pointerInput(minHeightPx, maxHeightPx) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                updateHeight(
                                    (currentHeightPx() - dragAmount)
                                        .coerceIn(minHeightPx, maxHeightPx)
                                )
                            },
                            onDragEnd = {
                                val anchors = listOf(
                                    SheetLevel.Collapsed to minHeightPx,
                                    SheetLevel.Partial to partialHeightPx,
                                    SheetLevel.Expanded to maxHeightPx
                                )
                                val level = anchors.minByOrNull {
                                    abs(it.second - currentHeightPx())
                                }?.first ?: SheetLevel.Collapsed
                                onLevelChange(level)
                            }
                        )
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .size(width = 44.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            if (showInlineAttribution && level == SheetLevel.Collapsed) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Демо-мапа · Mapbox / OpenStreetMap не підключені",
                        modifier = Modifier
                            .weight(1f)
                            .semantics {
                                contentDescription =
                                    "Демонстраційна мапа. Mapbox і OpenStreetMap не підключені."
                            },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (level == SheetLevel.Expanded) {
                        Text(
                            "Параметри маршруту та альтернативи ›",
                            modifier = Modifier
                                .clickable(role = Role.Button, onClick = onOpenRouteSettings)
                                .padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            if (sheetContent == null && level == SheetLevel.Expanded && !showInlineAttribution) {
                Text(
                    "Параметри маршруту та альтернативи ›",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable(role = Role.Button, onClick = onOpenRouteSettings)
                        .padding(vertical = 4.dp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("route-sheet-content")
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (showInlineAttribution && level != SheetLevel.Collapsed) {
                    Text(
                        "Демо-мапа · Mapbox / OpenStreetMap не підключені",
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (sheetContent == null && level == SheetLevel.Expanded) {
                        Text(
                            "Параметри маршруту та альтернативи ›",
                            modifier = Modifier.clickable(role = Role.Button, onClick = onOpenRouteSettings)
                                .padding(vertical = 8.dp),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                when {
                    sheetContent != null -> sheetContent()
                    routeStops.isNotEmpty() -> {
                        StatusCard(
                            title = "План збережено локально",
                            message = "Багатоточковий перегляд ще не підключено. " +
                                "Збережено проміжних зупинок: ${routeStops.size}. " +
                                "Лінія маршруту, відстань і час для цього плану не розраховуються."
                        )
                    }

                    !preview -> {
                        Text(
                            text = if (origin.isBlank() || destination.isBlank()) {
                                "Маршрут не вибрано"
                            } else {
                                "Точки маршруту вибрано"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (origin.isBlank()) {
                                "Оберіть початкову адресу або введіть її вручну."
                            } else if (destination.isBlank()) {
                                "Оберіть адресу призначення для демо-перегляду."
                            } else {
                                "Адреси збережено. Попередній перегляд не розраховувався."
                            },
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (notice.isNotBlank()) {
                            StatusCard(
                                title = if (notice == geoStatusMessage(geoStatus)) {
                                    "Геолокація не підключена"
                                } else if (vehicleClassUnavailableMessage(vehicleClass) == notice) {
                                    "Профіль транспорту недоступний"
                                } else {
                                    "Стан сценарію"
                                },
                                message = notice
                            )
                        }
                        if (level != SheetLevel.Collapsed) {
                            if (demoVehicleForClass(vehicleClass) != null) {
                                if (selectedVehicle != null) {
                                    VehicleSummaryCard(selectedVehicle)
                                }
                            }
                            VehicleClassSelector(
                                selected = vehicleClass,
                                onSelected = onVehicleClassChange
                            )
                        }
                    }

                    level == SheetLevel.Collapsed -> {
                        if (vehicleClassUnavailableMessage(vehicleClass) == null) Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 1.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "6 год 20 хв",
                                modifier = Modifier.weight(1f),
                                fontFamily = rememberRobotoCondensedFontFamily(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Column(
                                modifier = Modifier.width(100.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    "540 км",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    selectedVehicle?.name ?: "Транспорт",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = "Профіль для $vehicleClass недоступний",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        if (notice.isNotBlank()) {
                            StatusCard(title = "Стан маршруту", message = notice)
                        }
                        if (vehicleClassUnavailableMessage(vehicleClass) == null) {
                            if (selectedVehicle != null) {
                                VehicleSummaryCard(selectedVehicle)
                            }
                        } else {
                            StatusCard(
                                title = "Профіль транспорту недоступний",
                                message = vehicleClassUnavailableMessage(vehicleClass).orEmpty()
                            )
                        }
                        VehicleClassSelector(
                            selected = vehicleClass,
                            onSelected = onVehicleClassChange
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "6 год 20 хв",
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "540 км",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "М-06 · орієнтовні дані демо-фікстури",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (sheetContent == null) XrnaviActionButton(
                text = when {
                    navigationUnavailable -> "Маршрут і навігацію не підключено · лише демо"
                    preview -> "Почати маршрут"
                    routeStops.isNotEmpty() -> "Редагувати зупинки"
                    origin.isBlank() -> "Вибрати початкову точку"
                    destination.isBlank() -> "Знайти адресу"
                    vehicleClassUnavailableMessage(vehicleClass) != null -> "Профіль недоступний"
                    else -> "Переглянути маршрут"
                },
                icon = if (preview && !navigationUnavailable) {
                    Res.drawable.route_navigation_dark
                } else {
                    null
                },
                onClick = when {
                    navigationUnavailable -> ({})
                    preview -> onStartRoute
                    routeStops.isNotEmpty() -> onOpenRouteStops
                    origin.isBlank() -> ({ onOpenSearch(SearchTarget.Origin) })
                    destination.isBlank() -> ({ onOpenSearch(SearchTarget.Destination) })
                    else -> onPreview
                },
                height = if (level == SheetLevel.Collapsed) 44.dp else 52.dp,
                cornerRadius = if (level == SheetLevel.Collapsed) 10.dp else 14.dp,
                modifier = Modifier.padding(top = 7.dp, bottom = 12.dp),
                compact = true,
                enabled = !navigationUnavailable
            )
        }
    }
}

@Composable
internal fun VehicleSummaryCard(vehicle: DemoVehicleProfile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DemoVehicleImage(
            vehicle = vehicle,
            contentDescription = "Демонстраційне зображення ${vehicle.name}",
            thumbnail = true,
            modifier = Modifier
                .size(width = 94.dp, height = 64.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                vehicle.name,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                lineHeight = 25.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                vehicle.specification,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            vehicle.configurationName?.let {
                Text(
                    "Конфігурація: $it",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            "⌄",
            modifier = Modifier.padding(start = 4.dp),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun VehicleClassSelector(
    selected: String,
    onSelected: (String) -> Unit,
    title: String = "Категорія демо-профілю"
) {
    val types = listOf("Легкове", "Вантажне", "Спецтехніка")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            title,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        types.forEach { type ->
            val active = selected == type
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (active) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .border(
                        1.dp,
                        if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .semantics {
                        contentDescription = type
                        this.selected = active
                    }
                    .clickable(role = Role.Button) { onSelected(type) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FigmaIcon(
                    light = Res.drawable.vehicle_car_light,
                    dark = when (type) {
                        "Вантажне" -> Res.drawable.vehicle_truck_dark
                        "Спецтехніка" -> Res.drawable.vehicle_special_dark
                        else -> Res.drawable.vehicle_car_dark
                    },
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = type,
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .clearAndSetSemantics { },
                    fontSize = 9.sp,
                    color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
        }
    }
}

@Composable
internal fun BottomNavigation(
    selectedSection: DemoSection,
    onSectionChange: (DemoSection) -> Unit,
    rail: Boolean = false,
    compactRail: Boolean = false,
    accountStyle: Boolean = false
) {
    val styledRail = rail && accountStyle
    val tabs = listOf(
        Triple("Мапа", Res.drawable.nav_map_light, Res.drawable.nav_map_dark),
        Triple("Гараж", Res.drawable.nav_garage_light, Res.drawable.nav_garage_dark),
        Triple("Налаштування", Res.drawable.nav_settings_light, Res.drawable.nav_settings_dark),
        Triple("Профіль", Res.drawable.nav_profile_light, Res.drawable.nav_profile_dark)
    )
    val items: @Composable (Modifier) -> Unit = { itemModifier ->
        tabs.forEachIndexed { index, tab ->
            val section = when (index) {
                0 -> DemoSection.Map
            1 -> DemoSection.Garage
            2 -> DemoSection.Settings
            3 -> DemoSection.Profile
            else -> DemoSection.Map
        }
            val active = section == selectedSection
            val tabModifier = itemModifier
                    .semantics {
                        selected = active
                        if (rail && compactRail) contentDescription = tab.first
                    }
                    .clickable(role = Role.Tab) { onSectionChange(section) }
                    .then(
                        if (styledRail && !compactRail && !active)
                            Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface)
                        else Modifier
                    )
                    .padding(horizontal = 2.dp, vertical = 4.dp)
            val icon: @Composable () -> Unit = {
                FigmaIcon(
                    light = tab.second,
                    dark = tab.third,
                    modifier = Modifier.size(
                        if (styledRail && !compactRail) 24.dp
                        else if (styledRail) 21.dp else 19.dp
                    ),
                    tint = if (active && styledRail && !compactRail) MaterialTheme.colorScheme.onSurface
                    else if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val label: @Composable () -> Unit = {
                if (!rail || !compactRail) Text(
                    tab.first,
                    modifier = Modifier.padding(
                        top = 4.dp
                    ),
                    fontSize = if (styledRail && index == 2) 7.sp
                    else if (styledRail) 12.sp else if (rail) 9.sp else 11.sp,
                    lineHeight = if (styledRail && index == 2) 11.sp
                    else if (styledRail) 16.sp else androidx.compose.ui.unit.TextUnit.Unspecified,
                    letterSpacing = if (styledRail) 0.4.sp else androidx.compose.ui.unit.TextUnit.Unspecified,
                    color = if (active && styledRail) MaterialTheme.colorScheme.onSurface
                    else if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (styledRail) 1 else 2
                )
            }
            Column(
                tabModifier, horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { icon(); label() }
        }
    }
    if (rail) {
        Column(
            Modifier.width(if (compactRail) 56.dp else 88.dp)
                .fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant)
                .then(if (accountStyle && !compactRail) Modifier.padding(horizontal = 8.dp, vertical = 24.dp) else Modifier)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(if (accountStyle && !compactRail) 22.dp else 12.dp)
        ) { items(Modifier.fillMaxWidth().heightIn(min = if (accountStyle && !compactRail) 72.dp else 70.dp)) }
    } else Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(Modifier.weight(1f).fillMaxHeight())
    }
}





@Composable
internal fun AddressSearchCard(
    origin: String,
    destination: String,
    target: SearchTarget,
    query: String,
    onTargetChange: (SearchTarget) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    val originFocusRequester = remember { FocusRequester() }
    val destinationFocusRequester = remember { FocusRequester() }
    var requestedFocusTarget by remember { mutableStateOf<SearchTarget?>(null) }
    LaunchedEffect(target, requestedFocusTarget) {
        if (requestedFocusTarget == target) {
            when (target) {
                SearchTarget.Origin -> originFocusRequester.requestFocus()
                SearchTarget.Destination -> destinationFocusRequester.requestFocus()
            }
            requestedFocusTarget = null
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            AddressQueryRow(
                target = SearchTarget.Origin,
                active = target == SearchTarget.Origin,
                value = if (target == SearchTarget.Origin) query else origin,
                placeholder = "Моє місцезнаходження",
                onActivate = {
                    requestedFocusTarget = SearchTarget.Origin
                    onTargetChange(SearchTarget.Origin)
                },
                onValueChange = onQueryChange,
                onSearch = onSearch,
                showSearch = target == SearchTarget.Origin,
                focusRequester = originFocusRequester
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            AddressQueryRow(
                target = SearchTarget.Destination,
                active = target == SearchTarget.Destination,
                value = if (target == SearchTarget.Destination) query else destination,
                placeholder = "Введіть адресу",
                onActivate = {
                    requestedFocusTarget = SearchTarget.Destination
                    onTargetChange(SearchTarget.Destination)
                },
                onValueChange = onQueryChange,
                onSearch = onSearch,
                showSearch = target == SearchTarget.Destination,
                focusRequester = destinationFocusRequester
            )
        }
    }
}

@Composable
internal fun AddressQueryRow(
    target: SearchTarget,
    active: Boolean,
    value: String,
    placeholder: String,
    onActivate: () -> Unit,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    showSearch: Boolean,
    focusRequester: FocusRequester
) {
    val dark = isDarkXrnaviTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .then(
                if (active) {
                    Modifier
                } else {
                    Modifier.clickable(role = Role.Button) {
                        onActivate()
                    }
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (target == SearchTarget.Origin && value.isBlank() && !active) {
            Box(
                Modifier
                    .padding(start = 1.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            )
        } else {
            FigmaIcon(
                light = if (target == SearchTarget.Origin) {
                    Res.drawable.search_origin_light
                } else {
                    Res.drawable.search_pin_light
                },
                dark = if (target == SearchTarget.Origin) {
                    Res.drawable.search_origin_dark
                } else {
                    Res.drawable.search_pin_dark
                },
                modifier = Modifier
                    .padding(start = if (target == SearchTarget.Origin) 1.dp else 0.dp)
                    .size(if (target == SearchTarget.Origin) 12.dp else 14.dp)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        ) {
            if (active) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .semantics {
                            contentDescription = if (target == SearchTarget.Origin) {
                                "Початкова адреса"
                            } else {
                                "Адреса призначення"
                            }
                        },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = if (value.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isBlank()) {
                                Text(
                                    placeholder,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            } else {
                Text(
                    text = value.ifBlank { placeholder },
                    fontSize = 13.sp,
                    fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (value.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1
                )
            }
        }
        if (active && value.isNotBlank()) {
            Text(
                "×",
                modifier = Modifier
                    .semantics { contentDescription = "Очистити пошуковий запит" }
                    .clickable(role = Role.Button) { onValueChange("") }
                    .padding(horizontal = 8.dp),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (showSearch && value.isNotBlank()) {
            FigmaIcon(
                light = Res.drawable.search_search_dark,
                dark = Res.drawable.search_search_dark,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(role = Role.Button, onClick = onSearch),
                contentDescription = "Шукати адресу",
                tint = if (dark) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
            )
        }
        if (target == SearchTarget.Origin && !active) {
            FigmaIcon(
                light = Res.drawable.search_swap_light,
                dark = Res.drawable.search_swap_light,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(18.dp),
                contentDescription = "Початкова точка"
            )
        }
    }
}

@Composable
internal fun SearchPlaceRow(
    place: DemoPlace,
    recent: Boolean,
    isSaved: Boolean,
    onClick: () -> Unit,
    onSave: () -> Unit
) {
    val dark = isDarkXrnaviTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(0.dp)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(start = 0.dp)
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            FigmaIcon(
                light = if (recent) Res.drawable.search_recent_light else Res.drawable.search_pin_light,
                dark = if (recent) Res.drawable.search_recent_dark else Res.drawable.search_pin_dark,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                place.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                place.detail,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Text(
            if (isSaved) "Збережено" else "Зберегти",
            modifier = Modifier
                .clickable(role = Role.Button, onClick = onSave)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp
        )
        FigmaIcon(
            light = Res.drawable.search_chevron_light,
            dark = Res.drawable.search_chevron_dark,
            modifier = Modifier
                .padding(end = 4.dp)
                .size(16.dp),
            contentDescription = "Вибрати адресу"
        )
    }
}

@Composable
internal fun SearchMapPicker(onClick: () -> Unit) {
    val dark = isDarkXrnaviTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(12.dp)
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FigmaIcon(
            light = Res.drawable.search_pick_map_light,
            dark = Res.drawable.search_pick_map_dark,
            modifier = Modifier.size(20.dp)
        )
        Text(
            "Вибрати точку на мапі",
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        FigmaIcon(
            light = Res.drawable.search_chevron_light,
            dark = Res.drawable.search_chevron_dark,
            modifier = Modifier.size(16.dp),
            contentDescription = null
        )
    }
}

@Composable
internal fun SearchVehicleCard(
    vehicleClass: String,
    vehicle: DemoVehicleProfile?,
    mode: String,
    onModeChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (vehicleClassUnavailableMessage(vehicleClass) == null && vehicle != null) Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 68.dp)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DemoVehicleImage(
                vehicle = vehicle,
                contentDescription = "Демонстраційне зображення ${vehicle.name}",
                thumbnail = true,
                modifier = Modifier
                    .size(width = 94.dp, height = 64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    vehicle.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    vehicle.specification,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                vehicle.configurationName?.let {
                    Text(
                        "Конфігурація: $it",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            FigmaIcon(
                light = Res.drawable.search_car_light,
                dark = Res.drawable.search_car_dark,
                modifier = Modifier.size(18.dp),
                contentDescription = "Транспорт"
            )
        } else {
            StatusCard(
                title = "Профіль транспорту недоступний",
                message = vehicleClassUnavailableMessage(vehicleClass).orEmpty()
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Особистий", "Корпоративний").forEach { item ->
                val selected = item == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable(role = Role.Button) { onModeChange(item) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        item,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}





@Composable
internal fun RouteStopSearchResult(title: String, detail: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(detail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun RouteStopAction(
    label: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Text(
        label,
        modifier = modifier
            .then(if (enabled) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(vertical = 8.dp),
        fontSize = 12.sp,
        color = if (enabled) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
internal fun AsyncState(
    title: String,
    message: String,
    loading: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (loading) {
            Text(
                "···",
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            message,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actionLabel != null && onAction != null) {
            Text(
                actionLabel,
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onAction)
                    .padding(vertical = 4.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
internal fun StatusCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            message,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun FigmaIcon(
    light: DrawableResource,
    dark: DrawableResource,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color? = null
) {
    Image(
        painter = painterResource(if (isDarkXrnaviTheme()) dark else light),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        colorFilter = tint?.let { ColorFilter.tint(it) },
        modifier = modifier
    )
}

@Composable
internal fun isDarkXrnaviTheme(): Boolean =
    MaterialTheme.colorScheme.background == Color(0xFF0B1220)
