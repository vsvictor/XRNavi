package com.mobilespace.xrnavi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.abs
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun SignInScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    notice: String,
    onSignIn: () -> Unit,
    onDemo: () -> Unit,
    onToggleTheme: () -> Unit
) {
    val dark = isDarkXrnaviTheme()
    val surface = MaterialTheme.colorScheme.background
    val formScroll = rememberScrollState()
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(surface)
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight
        val splitLayout = availableWidth > availableHeight
        if (splitLayout) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            )
            {
                SignInHero(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight(),
                    dark = dark,
                    horizontalLayout = true,
                    onToggleTheme = onToggleTheme
                )
                SignInForm(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .verticalScroll(formScroll)
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    email = email,
                    onEmailChange = onEmailChange,
                    password = password,
                    onPasswordChange = onPasswordChange,
                    notice = notice,
                    onSignIn = onSignIn,
                    onDemo = onDemo,
                    dark = dark
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(formScroll)
            ) {
                SignInHero(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(
                            when {
                                availableHeight < 680.dp -> 250.dp
                                availableWidth >= 600.dp -> 370.dp
                                else -> 320.dp
                            }
                        )
                        .clip(RoundedCornerShape(16.dp)),
                    dark = dark,
                    horizontalLayout = false,
                    onToggleTheme = onToggleTheme
                )
                SignInForm(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 10.dp, bottom = 20.dp),
                    email = email,
                    onEmailChange = onEmailChange,
                    password = password,
                    onPasswordChange = onPasswordChange,
                    notice = notice,
                    onSignIn = onSignIn,
                    onDemo = onDemo,
                    dark = dark
                )
            }
        }
    }
}

@Composable
private fun SignInHero(
    modifier: Modifier,
    dark: Boolean,
    horizontalLayout: Boolean,
    onToggleTheme: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProductWordmark()
                Text(
                    text = "Тема",
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(role = Role.Button, onClick = onToggleTheme)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
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
                    text = "Навігація для кожного, хто за кермом.",
                    modifier = Modifier.padding(top = 7.dp),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Продукт Mobile space",
                    modifier = Modifier.padding(top = 5.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun SignInForm(
    modifier: Modifier,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    notice: String,
    onSignIn: () -> Unit,
    onDemo: () -> Unit,
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
            trailingIcon = if (dark) Res.drawable.sign_in_mail_dark else Res.drawable.sign_in_mail_light
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
            visualTransformation = PasswordVisualTransformation(),
            trailingIcon = if (dark) Res.drawable.sign_in_eye_off_dark else Res.drawable.sign_in_eye_off_light
        )
        Text(
            text = "Забули пароль?",
            modifier = Modifier
                .align(Alignment.End)
                .clickable(role = Role.Button, onClick = onSignIn)
                .padding(vertical = 2.dp),
            fontSize = 12.sp,
            letterSpacing = 0.4.sp,
            color = MaterialTheme.colorScheme.primary
        )
        XrnaviActionButton(
            text = "Увійти",
            icon = if (dark) Res.drawable.sign_in_arrow_dark else Res.drawable.sign_in_arrow_light,
            onClick = onSignIn,
            height = 52.dp,
            cornerRadius = 26.dp
        )
        if (notice.isNotBlank()) {
            StatusCard(
                title = "Авторизація недоступна",
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
private fun LabeledInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions,
    trailingIcon: DrawableResource,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
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
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(horizontal = 14.dp),
            singleLine = true,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 20.sp,
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
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                    FigmaIcon(
                        light = trailingIcon,
                        dark = trailingIcon,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        )
    }
}

@Composable
private fun ProviderButton(
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
private fun ProductWordmark(modifier: Modifier = Modifier) {
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
private fun XrnaviActionButton(
    text: String,
    icon: DrawableResource? = null,
    onClick: () -> Unit,
    height: Dp,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val shape = RoundedCornerShape(cornerRadius)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = if (compact) 10.dp else 16.dp),
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
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
internal fun MapScreen(
    preview: Boolean,
    demoMode: Boolean,
    origin: String,
    destination: String,
    sheetLevel: SheetLevel,
    geoStatus: GeoStatus,
    notice: String,
    vehicleClass: String,
    vehicleMode: String,
    onVehicleClassChange: (String) -> Unit,
    onOpenSearch: (SearchTarget) -> Unit,
    onUseLocation: () -> Unit,
    onSheetLevelChange: (SheetLevel) -> Unit,
    onPreview: () -> Unit,
    onStartRoute: () -> Unit,
    onBack: () -> Unit,
    onToggleTheme: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        MapTopBar(
            demoMode = demoMode,
            preview = preview,
            vehicleMode = vehicleMode,
            onBack = onBack,
            onToggleTheme = onToggleTheme
        )
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val layoutWidth = maxWidth
            val layoutHeight = maxHeight
            val showInlineAttribution =
                layoutHeight < 500.dp && sheetLevel != SheetLevel.Collapsed
            val mapHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
            val density = LocalDensity.current
            val anchors = calculateMapSheetAnchors(
                mapHeightPx = mapHeightPx,
                density = density.density,
                fontScale = density.fontScale
            )
            val minHeightPx = anchors.collapsed
            val partialHeightPx = anchors.partial
            val maxHeightPx = anchors.expanded
            var sheetHeightPx by remember(mapHeightPx) {
                mutableFloatStateOf(anchors.heightFor(sheetLevel))
            }
            val sheetHeightDp = with(density) { sheetHeightPx.toDp() }
            val sheetScrollState = rememberScrollState()

            LaunchedEffect(sheetLevel, minHeightPx, partialHeightPx, maxHeightPx) {
                sheetHeightPx = anchors.heightFor(sheetLevel)
            }

            val scrollConnection = remember(mapHeightPx, minHeightPx, partialHeightPx, maxHeightPx) {
                object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        if (available.y >= 0f || sheetHeightPx >= maxHeightPx) return Offset.Zero
                        val consumed = minOf(-available.y, maxHeightPx - sheetHeightPx)
                        sheetHeightPx += consumed
                        return Offset(0f, -consumed)
                    }

                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        if (
                            available.y <= 0f ||
                            sheetScrollState.value != 0 ||
                            sheetHeightPx <= minHeightPx
                        ) return Offset.Zero
                        val used = minOf(available.y, sheetHeightPx - minHeightPx)
                        sheetHeightPx -= used
                        return Offset(0f, used)
                    }

                    override suspend fun onPreFling(available: Velocity): Velocity {
                        val anchors = listOf(
                            SheetLevel.Collapsed to minHeightPx,
                            SheetLevel.Partial to partialHeightPx,
                            SheetLevel.Expanded to maxHeightPx
                        )
                        val target = when {
                            available.y < -500f -> anchors.firstOrNull {
                                it.second > sheetHeightPx + 1f
                            }?.first
                            available.y > 500f && sheetScrollState.value == 0 -> anchors
                                .lastOrNull { it.second < sheetHeightPx - 1f }
                                ?.first
                            else -> anchors.minByOrNull { abs(it.second - sheetHeightPx) }?.first
                        }
                        if (target != null) {
                            onSheetLevelChange(target)
                            return Velocity(0f, available.y)
                        }
                        return Velocity.Zero
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                MapCanvas(
                    modifier = Modifier.fillMaxSize(),
                    preview = preview
                )
                RouteEntryCard(
                    origin = origin,
                    destination = destination,
                    onOriginClick = { onOpenSearch(SearchTarget.Origin) },
                    onDestinationClick = { onOpenSearch(SearchTarget.Destination) },
                    onLocationClick = onUseLocation,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .widthIn(max = 620.dp)
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = layoutHeight * 0.63f, end = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MapControlButton(
                        iconLight = Res.drawable.map_layers_light,
                        iconDark = Res.drawable.map_layers_dark,
                        label = "Шари мапи",
                        onClick = { }
                    )
                    MapControlButton(
                        iconLight = Res.drawable.map_locate_light,
                        iconDark = Res.drawable.map_locate_dark,
                        label = "Моє місцезнаходження",
                        onClick = onUseLocation
                    )
                }
                if (!showInlineAttribution) {
                    Text(
                        text = if (layoutWidth >= 600.dp) {
                            "Демо-мапа · Mapbox / OpenStreetMap не підключені"
                        } else {
                            "Демо-мапа · Mapbox /\nOpenStreetMap не підключені"
                        },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .widthIn(
                                max = if (layoutWidth >= 600.dp) {
                                    minOf(480.dp, layoutWidth * 0.4f)
                                } else {
                                    148.dp
                                }
                            )
                            .padding(start = 12.dp, end = 12.dp, bottom = sheetHeightDp + 4.dp)
                            .semantics {
                                contentDescription =
                                    "Демонстраційна мапа. Mapbox і OpenStreetMap не підключені."
                            },
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RouteBottomSheet(
                    preview = preview,
                    origin = origin,
                    destination = destination,
                    geoStatus = geoStatus,
                    notice = notice,
                    level = sheetLevel,
                    showInlineAttribution = showInlineAttribution,
                    vehicleClass = vehicleClass,
                    onVehicleClassChange = onVehicleClassChange,
                    sheetHeightDp = sheetHeightDp,
                    scrollConnection = scrollConnection,
                    scrollState = sheetScrollState,
                    minHeightPx = minHeightPx,
                    partialHeightPx = partialHeightPx,
                    maxHeightPx = maxHeightPx,
                    currentHeightPx = { sheetHeightPx },
                    updateHeight = { sheetHeightPx = it },
                    onLevelChange = onSheetLevelChange,
                    onOpenSearch = onOpenSearch,
                    onPreview = onPreview,
                    onStartRoute = onStartRoute,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .widthIn(max = if (layoutWidth >= 600.dp && layoutHeight >= 600.dp) 680.dp else layoutWidth)
                )
            }
        }
        BottomNavigation()
    }
}

@Composable
private fun MapTopBar(
    demoMode: Boolean,
    preview: Boolean,
    vehicleMode: String,
    onBack: () -> Unit,
    onToggleTheme: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
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
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (demoMode) "Демо · $vehicleMode" else vehicleMode,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Тема",
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(role = Role.Button, onClick = onToggleTheme)
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MapCanvas(
    modifier: Modifier,
    preview: Boolean
) {
    val dark = isDarkXrnaviTheme()
    val land = if (dark) Color(0xFF111D2A) else Color(0xFFF1F5F9)
    val park = if (dark) Color(0xFF1C352F) else Color(0xFFDFEBD9)
    BoxWithConstraints(
        modifier = modifier.background(land)
    ) {
        val w = maxWidth
        val h = maxHeight

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
        MapPlaceLabel("Київ", w * 0.764f, h * 0.176f, strong = true)
        MapPlaceLabel("Житомир", w * 0.555f, h * 0.276f)
        MapPlaceLabel("Рівне", w * 0.274f, h * 0.315f)
        MapPlaceLabel("Львів", w * 0.042f, h * 0.358f, strong = true)
        MapPlaceLabel("М-06 / E40", w * 0.565f, h * 0.333f, size = 10.sp, strong = true)
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
            RouteDurationChip(
                modifier = Modifier
                    .offset(x = w * 0.47f, y = h * 0.454f)
            )
        }
    }
}

@Composable
private fun MapRoadLayer(
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
private fun MapPlaceLabel(
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
private fun RouteDurationChip(modifier: Modifier) {
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
private fun RouteEntryCard(
    origin: String,
    destination: String,
    onOriginClick: () -> Unit,
    onDestinationClick: () -> Unit,
    onLocationClick: () -> Unit,
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
        }
    }
}

@Composable
private fun MapControlButton(
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
private fun RouteBottomSheet(
    preview: Boolean,
    origin: String,
    destination: String,
    geoStatus: GeoStatus,
    notice: String,
    level: SheetLevel,
    showInlineAttribution: Boolean,
    vehicleClass: String,
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
    onPreview: () -> Unit,
    onStartRoute: () -> Unit,
    modifier: Modifier
) {
    val dark = isDarkXrnaviTheme()
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(sheetHeightDp)
            .nestedScroll(scrollConnection),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Box(
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
            if (showInlineAttribution) {
                Text(
                    text = "Демо-мапа · Mapbox / OpenStreetMap не підключені",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .semantics {
                            contentDescription =
                                "Демонстраційна мапа. Mapbox і OpenStreetMap не підключені."
                        },
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when {
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
                                title = if (geoStatus == GeoStatus.NotConfigured) {
                                    "Геолокація не підключена"
                                } else {
                                    "Стан сценарію"
                                },
                                message = notice
                            )
                        }
                    }

                    level == SheetLevel.Collapsed -> {
                        Row(
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
                                    "Mustang GT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    else -> {
                        if (notice.isNotBlank()) {
                            StatusCard(title = "Стан маршруту", message = notice)
                        }
                        VehicleSummaryCard()
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
                        if (level == SheetLevel.Expanded) {
                            Text(
                                "Параметри маршруту та альтернативи ›",
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .clickable(role = Role.Button, onClick = onStartRoute)
                                    .padding(vertical = 2.dp),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            XrnaviActionButton(
                text = when {
                    preview -> "Почати маршрут"
                    origin.isBlank() -> "Вибрати початкову точку"
                    destination.isBlank() -> "Знайти адресу"
                    else -> "Переглянути маршрут"
                },
                icon = if (preview) Res.drawable.route_navigation_dark else null,
                onClick = when {
                    preview -> onStartRoute
                    origin.isBlank() -> ({ onOpenSearch(SearchTarget.Origin) })
                    destination.isBlank() -> ({ onOpenSearch(SearchTarget.Destination) })
                    else -> onPreview
                },
                height = if (level == SheetLevel.Collapsed) 44.dp else 52.dp,
                cornerRadius = if (level == SheetLevel.Collapsed) 10.dp else 14.dp,
                modifier = Modifier.padding(top = 7.dp, bottom = 12.dp),
                compact = true
            )
        }
    }
}

@Composable
private fun VehicleSummaryCard() {
    val dark = isDarkXrnaviTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(
                if (dark) Res.drawable.mustang_thumbnail_dark else Res.drawable.mustang_thumbnail_light
            ),
            contentDescription = "Демонстраційне фото Ford Mustang GT",
            contentScale = ContentScale.Crop,
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
                "Ford Mustang GT",
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                lineHeight = 25.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Легковий · 1,4 м · 1,8 т",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
private fun VehicleClassSelector(
    selected: String,
    onSelected: (String) -> Unit
) {
    val types = listOf("Легкове", "Вантажне", "Спецтехніка")
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

@Composable
private fun BottomNavigation() {
    val tabs = listOf(
        Triple("Мапа", Res.drawable.nav_map_light, Res.drawable.nav_map_dark),
        Triple("Гараж", Res.drawable.nav_garage_light, Res.drawable.nav_garage_dark),
        Triple("Налаштування", Res.drawable.nav_settings_light, Res.drawable.nav_settings_dark),
        Triple("Профіль", Res.drawable.nav_profile_light, Res.drawable.nav_profile_dark)
    )
    Row(
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == 0
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(enabled = selected, role = Role.Tab) { },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                FigmaIcon(
                    light = tab.second,
                    dark = tab.third,
                    modifier = Modifier.size(19.dp),
                    tint = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    tab.first,
                    modifier = Modifier.padding(top = 4.dp),
                    fontSize = 11.sp,
                    color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
internal fun SearchScreen(
    demoMode: Boolean,
    origin: String,
    destination: String,
    target: SearchTarget,
    query: String,
    status: SearchStatus,
    vehicleMode: String,
    onVehicleModeChange: (String) -> Unit,
    onTargetChange: (SearchTarget) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit,
    onPickMap: () -> Unit,
    onSelectPlace: (DemoPlace) -> Unit,
    onToggleTheme: () -> Unit
) {
    val matchingPlaces = if (query.isBlank()) {
        demoPlaces.take(3)
    } else {
        demoPlaces.filter {
            it.title.contains(query.trim(), ignoreCase = true) ||
                it.detail.contains(query.trim(), ignoreCase = true)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductWordmark(
                modifier = Modifier.clickable(role = Role.Button, onClick = onBack)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    if (demoMode) "Демо-адреси" else "Пошук",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Тема",
                    modifier = Modifier
                        .clickable(role = Role.Button, onClick = onToggleTheme)
                        .padding(4.dp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AddressSearchCard(
                origin = origin,
                destination = destination,
                target = target,
                query = query,
                onTargetChange = onTargetChange,
                onQueryChange = onQueryChange,
                onSearch = onSearch
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    when {
                        !demoMode -> "ПОШУК НЕ ПІДКЛЮЧЕНО"
                        query.isBlank() -> "НЕЩОДАВНІ АДРЕСИ · ДЕМО"
                        else -> "ЗНАЙДЕНІ АДРЕСИ · ДЕМО"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (status == SearchStatus.Results && demoMode) {
                    Text(
                        "${matchingPlaces.size} результат${if (matchingPlaces.size == 1) "" else "и"}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            when (status) {
                SearchStatus.Idle -> {
                    if (demoMode) {
                        matchingPlaces.take(2).forEach { place ->
                            SearchPlaceRow(place, recent = true) { onSelectPlace(place) }
                        }
                    } else {
                        StatusCard(
                            title = "Адреси недоступні",
                            message = "Пошуковий провайдер не налаштований. Введений текст не надсилається."
                        )
                    }
                }

                SearchStatus.Loading -> AsyncState(
                    title = "Шукаємо адресу…",
                    message = "Пошук демонстраційних адрес",
                    loading = true
                )

                SearchStatus.Empty -> AsyncState(
                    title = "Нічого не знайдено",
                    message = "У локальному демо-наборі немає відповідної адреси."
                )

                SearchStatus.Error -> AsyncState(
                    title = "Пошук не підключено",
                    message = "Geocoding provider відсутній. Запит залишився лише в цьому екрані.",
                    actionLabel = "Повторити",
                    onAction = onSearch
                )

                SearchStatus.Results -> {
                    if (!demoMode) {
                        AsyncState(
                            title = "Живі результати недоступні",
                            message = "Цей екран не виконує мережевий пошук."
                        )
                    } else if (matchingPlaces.isEmpty()) {
                        AsyncState(
                            title = "Нічого не знайдено",
                            message = "Змініть запит або виберіть адресу з нещодавніх."
                        )
                    } else {
                        matchingPlaces.forEach { place ->
                            SearchPlaceRow(place, recent = false) { onSelectPlace(place) }
                        }
                    }
                }
            }
            SearchMapPicker(onClick = onPickMap)
            SearchVehicleCard(
                mode = vehicleMode,
                onModeChange = onVehicleModeChange
            )
        }
        BottomNavigation()
    }
}

@Composable
private fun AddressSearchCard(
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
private fun AddressQueryRow(
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
                        .focusRequester(focusRequester),
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
private fun SearchPlaceRow(
    place: DemoPlace,
    recent: Boolean,
    onClick: () -> Unit
) {
    val dark = isDarkXrnaviTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
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
private fun SearchMapPicker(onClick: () -> Unit) {
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
private fun SearchVehicleCard(
    mode: String,
    onModeChange: (String) -> Unit
) {
    val dark = isDarkXrnaviTheme()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 68.dp)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(
                    if (dark) Res.drawable.mustang_thumbnail_dark else Res.drawable.mustang_thumbnail_light
                ),
                contentDescription = "Демонстраційне фото Ford Mustang GT",
                contentScale = ContentScale.Crop,
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
                    "Ford Mustang GT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    "Легковий · 1,4 м · 1,8 т",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FigmaIcon(
                light = Res.drawable.search_car_light,
                dark = Res.drawable.search_car_dark,
                modifier = Modifier.size(18.dp),
                contentDescription = "Транспорт"
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
private fun AsyncState(
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
private fun StatusCard(
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
private fun FigmaIcon(
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
private fun isDarkXrnaviTheme(): Boolean =
    MaterialTheme.colorScheme.background == Color(0xFF0B1220)
