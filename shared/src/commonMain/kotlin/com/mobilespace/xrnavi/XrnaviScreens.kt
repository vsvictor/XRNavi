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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import kotlin.math.abs
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.mobilespace.xrnavi.domain.routepreferences.RoutePreferences

@Composable
internal fun SignInScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    notice: String,
    onSignIn: () -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit,
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
                        .padding(horizontal = Constants.DP_24, vertical = 18.dp),
                    email = email,
                    onEmailChange = onEmailChange,
                    password = password,
                    onPasswordChange = onPasswordChange,
                    notice = notice,
                    onSignIn = onSignIn,
                    onRegister = onRegister,
                    onForgotPassword = onForgotPassword,
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
                        .padding(horizontal = Constants.DP_24)
                        .height(
                            when {
                                availableHeight < 680.dp -> 250.dp
                                availableWidth >= 600.dp -> 370.dp
                                else -> 320.dp
                            }
                        )
                        .clip(RoundedCornerShape(Constants.DP_16)),
                    dark = dark,
                    horizontalLayout = false,
                    onToggleTheme = onToggleTheme
                )
                SignInForm(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Constants.DP_24)
                        .padding(top = 10.dp, bottom = Constants.DP_20),
                    email = email,
                    onEmailChange = onEmailChange,
                    password = password,
                    onPasswordChange = onPasswordChange,
                    notice = notice,
                    onSignIn = onSignIn,
                    onRegister = onRegister,
                    onForgotPassword = onForgotPassword,
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
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
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
                .padding(Constants.DP_24),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProductWordmark()
                Text(
                    text = stringResource(Res.string.sign_in_theme),
                    modifier = Modifier
                        .clip(RoundedCornerShape(Constants.DP_16))
                        .clickable(role = Role.Button, onClick = onToggleTheme)
                        .padding(horizontal = 10.dp, vertical = Constants.DP_6),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column {
                Text(
                    text = if (horizontalLayout) {
                        stringResource(Res.string.sign_in_hero_title_multiline)
                    } else {
                        stringResource(Res.string.sign_in_hero_title_singleline)
                    },
                    fontFamily = rememberRobotoCondensedFontFamily(),
                    fontWeight = FontWeight.Bold,
                    fontSize = if (horizontalLayout) 28.sp else 38.sp,
                    lineHeight = if (horizontalLayout) 30.sp else 40.sp,
                    letterSpacing = (-0.45).sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(Res.string.sign_in_hero_subtitle),
                    modifier = Modifier.padding(top = 7.dp),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(Res.string.sign_in_hero_product),
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
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    dark: Boolean
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Constants.DP_12)
    ) {
        Text(
            text = stringResource(Res.string.sign_in_welcome_back),
            fontFamily = rememberRobotoCondensedFontFamily(),
            fontSize = 28.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        LabeledInput(
            label = stringResource(Res.string.sign_in_email),
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
            label = stringResource(Res.string.sign_in_password),
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
            text = stringResource(Res.string.sign_in_forgot_password),
            modifier = Modifier
                .align(Alignment.End)
                .clickable(role = Role.Button, onClick = onForgotPassword)
                .padding(vertical = Constants.DP_2),
            fontSize = 12.sp,
            letterSpacing = 0.4.sp,
            color = MaterialTheme.colorScheme.primary
        )
        XrnaviActionButton(
            text = stringResource(Res.string.sign_in_action),
            icon = if (dark) Res.drawable.sign_in_arrow_dark else Res.drawable.sign_in_arrow_light,
            onClick = onSignIn,
            height = 52.dp,
            cornerRadius = 26.dp
        )
        if (notice.isNotBlank()) {
            StatusCard(
                title = stringResource(Res.string.sign_in_unavailable_title),
                message = notice
            )
        }
        Text(
            text = stringResource(Res.string.sign_in_register_prompt),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onRegister)
                .padding(vertical = Constants.DP_12),
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
                text = stringResource(Res.string.sign_in_organization_unavailable),
                modifier = Modifier.padding(start = Constants.DP_4),
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
    Column(verticalArrangement = Arrangement.spacedBy(Constants.DP_6)) {
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
                .height(Constants.DP_56)
                .clip(RoundedCornerShape(Constants.DP_4))
                .background(MaterialTheme.colorScheme.surface)
                .border(Constants.DP_1, MaterialTheme.colorScheme.outline, RoundedCornerShape(Constants.DP_4))
                .padding(horizontal = 14.dp),
            minLines = 2,
            maxLines = 5,
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
internal fun ProductWordmark(modifier: Modifier = Modifier) {
    val dark = isDarkXrnaviTheme()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Constants.DP_8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FigmaIcon(
            light = Res.drawable.orbit_glyph_light,
            dark = Res.drawable.orbit_glyph_dark,
            modifier = Modifier.size(30.dp),
            contentDescription = stringResource(Res.string.brand_orbit_mark_accessibility)
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
    compact: Boolean = false,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(cornerRadius)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .clip(shape)
            .background(
                if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = if (compact) 10.dp else Constants.DP_16),
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

@Composable
internal fun MapScreen(
    origin: String,
    destination: String,
    routeStops: List<RouteStop>,
    sheetLevel: SheetLevel,
    geoStatus: GeoStatus,
    notice: String,
    onOpenSearch: (SearchTarget) -> Unit,
    onOpenRouteStops: () -> Unit,
    onOpenStopPlaces: () -> Unit,
    onUseLocation: () -> Unit,
    onSheetLevelChange: (SheetLevel) -> Unit,
    onOpenRouteSettings: () -> Unit,
    onToggleTheme: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        MapTopBar(
            onOpenStopPlaces = onOpenStopPlaces,
            onToggleTheme = onToggleTheme
        )
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val layoutWidth = maxWidth
            val layoutHeight = maxHeight
            val showInlineAttribution = layoutHeight < 500.dp && sheetLevel != SheetLevel.Collapsed
            val mapHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
            val density = LocalDensity.current
            val anchors = calculateMapSheetAnchors(
                mapHeightPx = mapHeightPx,
                density = density.density,
                fontScale = density.fontScale,
                inlineAttribution = showInlineAttribution
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
                MapCanvas(modifier = Modifier.fillMaxSize())
                RouteEntryCard(
                    origin = origin,
                    destination = destination,
                    routeStopsCount = routeStops.size,
                    onOriginClick = { onOpenSearch(SearchTarget.Origin) },
                    onDestinationClick = { onOpenSearch(SearchTarget.Destination) },
                    onLocationClick = onUseLocation,
                    onRouteStopsClick = onOpenRouteStops,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .padding(horizontal = Constants.DP_20)
                        .widthIn(max = 620.dp)
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = layoutHeight * 0.63f, end = Constants.DP_20),
                    verticalArrangement = Arrangement.spacedBy(Constants.DP_8)
                ) {
                    MapControlButton(
                        iconLight = Res.drawable.map_locate_light,
                        iconDark = Res.drawable.map_locate_dark,
                        label = stringResource(Res.string.map_origin_my_location),
                        onClick = onUseLocation
                    )
                }
                if (!showInlineAttribution) {
                    val mapStatusDescription = stringResource(
                        Res.string.map_provider_unavailable_accessibility
                    )
                    Text(
                        text = stringResource(Res.string.map_provider_unavailable),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .widthIn(max = 260.dp)
                            .padding(start = Constants.DP_12, end = Constants.DP_12, bottom = sheetHeightDp + Constants.DP_4)
                            .semantics {
                                contentDescription = mapStatusDescription
                            },
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RouteBottomSheet(
                    origin = origin,
                    destination = destination,
                    routeStops = routeStops,
                    geoStatus = geoStatus,
                    notice = notice,
                    level = sheetLevel,
                    showInlineAttribution = showInlineAttribution,
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
                    onOpenRouteStops = onOpenRouteStops,
                    onOpenRouteSettings = onOpenRouteSettings,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .widthIn(max = if (layoutWidth >= 600.dp && layoutHeight >= 600.dp) 680.dp else layoutWidth)
                )
            }
        }
    }
}

@Composable
internal fun RouteSettingsScreen(
    origin: String,
    destination: String,
    preferences: RoutePreferences,
    hasPendingChanges: Boolean,
    canApply: Boolean,
    notice: String,
    onPreferencesChange: (RoutePreferences) -> Unit,
    onBack: () -> Unit,
    onApply: () -> Unit,
    onToggleTheme: () -> Unit
) {
    AuthBackHandler(onBack)
    Column(
        modifier = Modifier.fillMaxSize().imePadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Constants.DP_20, vertical = Constants.DP_12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(Res.string.route_settings_back),
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onBack)
                    .padding(vertical = Constants.DP_8, horizontal = Constants.DP_4),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
            )
            ProductWordmark(modifier = Modifier.weight(1f))
            Text(
                stringResource(Res.string.route_settings_theme),
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onToggleTheme)
                    .padding(vertical = Constants.DP_8, horizontal = Constants.DP_4),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp
            )
        }
        Text(
            stringResource(Res.string.route_settings_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Constants.DP_20)
                .semantics { heading() },
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            stringResource(Res.string.route_settings_unavailable_description),
            modifier = Modifier.padding(start = Constants.DP_20, end = Constants.DP_20, top = Constants.DP_4, bottom = Constants.DP_12),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        BoxWithConstraints(
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            val wideLayout = maxWidth >= 720.dp
            val contentModifier = Modifier
                .widthIn(max = 1040.dp)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (wideLayout) Constants.DP_32 else Constants.DP_20, vertical = Constants.DP_8)
            if (wideLayout) {
                Row(
                    modifier = contentModifier,
                    horizontalArrangement = Arrangement.spacedBy(Constants.DP_16),
                    verticalAlignment = Alignment.Top
                ) {
                    RouteEndpointsCard(
                        modifier = Modifier.weight(1f),
                        origin = origin,
                        destination = destination
                    )
                    RoutePreferenceCard(
                        modifier = Modifier.weight(1f),
                        preferences = preferences,
                        hasPendingChanges = hasPendingChanges && canApply,
                        enabled = canApply,
                        onPreferencesChange = onPreferencesChange
                    )
                }
            } else {
                Column(
                    modifier = contentModifier,
                    verticalArrangement = Arrangement.spacedBy(Constants.DP_12)
                ) {
                    RouteEndpointsCard(
                        origin = origin,
                        destination = destination
                    )
                    RoutePreferenceCard(
                        preferences = preferences,
                        hasPendingChanges = hasPendingChanges && canApply,
                        enabled = canApply,
                        onPreferencesChange = onPreferencesChange
                    )
                }
            }
        }
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = Constants.DP_6) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1040.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = Constants.DP_20, vertical = Constants.DP_12),
                verticalArrangement = Arrangement.spacedBy(Constants.DP_8)
            ) {
                Text(
                    notice.ifBlank {
                        stringResource(Res.string.route_settings_unavailable_description)
                    },
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!canApply) {
                    Text(
                        stringResource(Res.string.route_settings_apply_unavailable),
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                XrnaviActionButton(
                    text = stringResource(Res.string.route_settings_apply),
                    onClick = onApply,
                    enabled = canApply,
                    height = 52.dp,
                    cornerRadius = Constants.DP_12
                )
                Text(
                    stringResource(Res.string.route_settings_cancel),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable(role = Role.Button, onClick = onBack)
                        .padding(vertical = 5.dp, horizontal = Constants.DP_12),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun RouteEndpointsCard(
    origin: String,
    destination: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Constants.DP_16),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Constants.DP_1, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(Constants.DP_16),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                stringResource(Res.string.route_endpoints_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            RouteReadOnlyValue(
                stringResource(Res.string.route_vehicle_origin),
                origin.ifBlank { stringResource(Res.string.route_vehicle_value_unspecified) }
            )
            RouteReadOnlyValue(
                stringResource(Res.string.route_vehicle_destination),
                destination.ifBlank { stringResource(Res.string.route_vehicle_value_unspecified) }
            )
        }
    }
}

@Composable
private fun RouteReadOnlyValue(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Constants.DP_2)) {
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
private fun RoutePreferenceCard(
    preferences: RoutePreferences,
    hasPendingChanges: Boolean,
    onPreferencesChange: (RoutePreferences) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Constants.DP_16),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Constants.DP_1, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(Constants.DP_16),
            verticalArrangement = Arrangement.spacedBy(Constants.DP_4)
        ) {
            Text(
                stringResource(Res.string.route_preferences_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!enabled) {
                Text(
                    stringResource(Res.string.route_preferences_unavailable),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (hasPendingChanges) {
                Text(
                    stringResource(Res.string.route_preferences_pending),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            RoutePreferenceSwitch(
                label = stringResource(Res.string.route_preferences_avoid_tolls),
                checked = preferences.avoidTolls,
                enabled = enabled,
                onCheckedChange = { onPreferencesChange(preferences.copy(avoidTolls = it)) }
            )
            RoutePreferenceSwitch(
                label = stringResource(Res.string.route_preferences_avoid_unpaved),
                checked = preferences.avoidUnpavedRoads,
                enabled = enabled,
                onCheckedChange = { onPreferencesChange(preferences.copy(avoidUnpavedRoads = it)) }
            )
            RoutePreferenceSwitch(
                label = stringResource(Res.string.route_preferences_avoid_ferries),
                checked = preferences.avoidFerries,
                enabled = enabled,
                onCheckedChange = { onPreferencesChange(preferences.copy(avoidFerries = it)) }
            )
            val enabledPreferences = listOfNotNull(
                stringResource(Res.string.route_preferences_tolls)
                    .takeIf { preferences.avoidTolls },
                stringResource(Res.string.route_preferences_unpaved)
                    .takeIf { preferences.avoidUnpavedRoads },
                stringResource(Res.string.route_preferences_ferries)
                    .takeIf { preferences.avoidFerries }
            )
            if (enabled) Text(
                text = if (enabledPreferences.isEmpty()) {
                    stringResource(Res.string.route_preferences_none_selected)
                } else {
                    stringResource(
                        Res.string.route_preferences_enabled_summary,
                        enabledPreferences.joinToString()
                    )
                },
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RoutePreferenceSwitch(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val stateLabel = stringResource(
        if (checked) {
            Res.string.route_preferences_enabled
        } else {
            Res.string.route_preferences_disabled
        }
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Constants.DP_56)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .semantics {
                contentDescription = label
                stateDescription = stateLabel
            }
            .padding(vertical = Constants.DP_8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled
        )
    }
}

@Composable
private fun MapTopBar(
    onOpenStopPlaces: () -> Unit,
    onToggleTheme: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(Constants.DP_56)
            .background(MaterialTheme.colorScheme.background)
    ) {
        val compact = maxWidth < 440.dp
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (compact) Constants.DP_12 else Constants.DP_20),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductWordmark()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (compact) Constants.DP_4 else 10.dp)
            ) {
                TextButton(
                    onClick = onOpenStopPlaces,
                    modifier = Modifier.widthIn(max = 90.dp)
                ) {
                    Text(
                        stringResource(Res.string.map_topbar_stop_places),
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
                Text(
                    text = stringResource(Res.string.map_topbar_theme),
                    modifier = Modifier
                        .clip(RoundedCornerShape(Constants.DP_16))
                        .clickable(role = Role.Button, onClick = onToggleTheme)
                        .padding(horizontal = if (compact) Constants.DP_2 else Constants.DP_6, vertical = 5.dp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun MapCanvas(modifier: Modifier) {
    // TODO: Render the configured platform map SDK here; keep provider keys in platform configuration, never in source.
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        StatusCard(
            title = stringResource(Res.string.map_unavailable_title),
            message = stringResource(Res.string.map_unavailable_message),
            modifier = Modifier.fillMaxWidth().padding(Constants.DP_24)
        )
    }
}

@Composable
private fun RouteEntryCard(
    origin: String,
    destination: String,
    routeStopsCount: Int,
    onOriginClick: () -> Unit,
    onDestinationClick: () -> Unit,
    onLocationClick: () -> Unit,
    onRouteStopsClick: () -> Unit,
    modifier: Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Constants.DP_16),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Constants.DP_1, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = Constants.DP_8)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Constants.DP_40)
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
                    text = origin.ifBlank {
                        stringResource(Res.string.map_origin_my_location)
                    },
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
                    contentDescription = stringResource(Res.string.map_use_location_accessibility)
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(Constants.DP_1)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Constants.DP_40)
                    .clickable(role = Role.Button, onClick = onDestinationClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FigmaIcon(
                    light = Res.drawable.map_destination_marker_light,
                    dark = Res.drawable.map_destination_marker_dark,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = destination.ifBlank {
                        stringResource(Res.string.map_choose_destination)
                    },
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
                        .size(Constants.DP_16)
                        .clickable(role = Role.Button, onClick = onDestinationClick),
                    contentDescription = stringResource(
                        Res.string.map_edit_destination_accessibility
                    )
                )
            }
            Text(
                stringResource(Res.string.map_route_stops_count, routeStopsCount),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onRouteStopsClick)
                    .padding(start = Constants.DP_20, top = 7.dp, bottom = 5.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
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
        shape = RoundedCornerShape(Constants.DP_12),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = Constants.DP_2
    ) {
        Box(contentAlignment = Alignment.Center) {
            FigmaIcon(
                light = iconLight,
                dark = iconDark,
                modifier = Modifier.size(Constants.DP_20),
                contentDescription = label
            )
        }
    }
}

@Composable
private fun RouteBottomSheet(
    origin: String,
    destination: String,
    routeStops: List<RouteStop>,
    geoStatus: GeoStatus,
    notice: String,
    level: SheetLevel,
    showInlineAttribution: Boolean,
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
    onOpenRouteSettings: () -> Unit,
    modifier: Modifier
) {
    val mapStatusDescription = stringResource(Res.string.map_provider_unavailable_accessibility)
    val routeSheetDescription = stringResource(Res.string.route_sheet_accessibility)
    val routeSheetCollapsed = stringResource(Res.string.route_sheet_collapsed)
    val routeSheetPartiallyExpanded = stringResource(Res.string.route_sheet_partially_expanded)
    val routeSheetExpanded = stringResource(Res.string.route_sheet_expanded)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(sheetHeightDp)
            .exportTestTags()
            .nestedScroll(scrollConnection),
        shape = RoundedCornerShape(topStart = Constants.DP_20, topEnd = Constants.DP_20),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = Constants.DP_8
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Constants.DP_20)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .semantics {
                        contentDescription = routeSheetDescription
                        stateDescription = when (level) {
                            SheetLevel.Collapsed -> routeSheetCollapsed
                            SheetLevel.Partial -> routeSheetPartiallyExpanded
                            SheetLevel.Expanded -> routeSheetExpanded
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
                        .size(width = 44.dp, height = Constants.DP_4)
                        .clip(RoundedCornerShape(Constants.DP_2))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            if (showInlineAttribution && level == SheetLevel.Collapsed) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = Constants.DP_8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.map_provider_unavailable),
                        modifier = Modifier
                            .weight(1f)
                            .semantics {
                                contentDescription = mapStatusDescription
                            },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (level == SheetLevel.Expanded) {
                        Text(
                            stringResource(Res.string.route_map_settings),
                            modifier = Modifier
                                .clickable(role = Role.Button, onClick = onOpenRouteSettings)
                                .padding(start = Constants.DP_8, top = Constants.DP_4, bottom = Constants.DP_4),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            if (level == SheetLevel.Expanded && !showInlineAttribution) {
                Text(
                    stringResource(Res.string.route_map_settings),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable(role = Role.Button, onClick = onOpenRouteSettings)
                        .padding(vertical = Constants.DP_4),
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
                        stringResource(Res.string.map_provider_unavailable),
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (level == SheetLevel.Expanded) {
                        Text(
                            stringResource(Res.string.route_map_settings),
                            modifier = Modifier.clickable(role = Role.Button, onClick = onOpenRouteSettings)
                                .padding(vertical = Constants.DP_8),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                when {
                    routeStops.isNotEmpty() -> {
                        StatusCard(
                            title = stringResource(Res.string.route_saved_plan_title),
                            message = stringResource(
                                Res.string.route_saved_plan_message,
                                routeStops.size
                            )
                        )
                    }

                    else -> {
                        Text(
                            text = if (origin.isBlank() || destination.isBlank()) {
                                stringResource(Res.string.route_not_selected)
                            } else {
                                stringResource(Res.string.route_points_selected)
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (origin.isBlank()) {
                                stringResource(Res.string.route_choose_origin_description)
                            } else if (destination.isBlank()) {
                                stringResource(Res.string.route_choose_destination_description)
                            } else {
                                stringResource(Res.string.route_addresses_saved_description)
                            },
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (origin.isNotBlank() && destination.isNotBlank()) {
                            // TODO: Request a route preview from the domain routing use case and show provider ETA/distance only.
                            StatusCard(
                                title = stringResource(Res.string.route_preview_unavailable_title),
                                message = stringResource(Res.string.route_preview_unavailable_message)
                            )
                        }
                        if (notice.isNotBlank()) {
                            StatusCard(
                                title = if (notice == geoStatusMessage(geoStatus)) {
                                    stringResource(Res.string.route_geolocation_unavailable_title)
                                } else {
                                    stringResource(Res.string.route_scenario_status_title)
                                },
                                message = notice
                            )
                        }
                    }

                }
            }
            XrnaviActionButton(
                text = when {
                    routeStops.isNotEmpty() -> stringResource(Res.string.route_action_edit_stops)
                    origin.isBlank() -> stringResource(Res.string.route_action_choose_origin)
                    destination.isBlank() -> stringResource(Res.string.route_action_find_address)
                    else -> stringResource(Res.string.route_preview_unavailable_action)
                },
                onClick = when {
                    routeStops.isNotEmpty() -> onOpenRouteStops
                    origin.isBlank() -> ({ onOpenSearch(SearchTarget.Origin) })
                    destination.isBlank() -> ({ onOpenSearch(SearchTarget.Destination) })
                    else -> ({})
                },
                height = if (level == SheetLevel.Collapsed) 44.dp else 52.dp,
                cornerRadius = if (level == SheetLevel.Collapsed) 10.dp else 14.dp,
                enabled = routeStops.isNotEmpty() || origin.isBlank() || destination.isBlank(),
                modifier = Modifier.padding(top = 7.dp, bottom = Constants.DP_12),
                compact = true
            )
        }
    }
}

@Composable
internal fun SearchScreen(
    origin: String,
    destination: String,
    target: SearchTarget,
    query: String,
    status: SearchStatus,
    onTargetChange: (SearchTarget) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit,
    onPickMap: () -> Unit,
    onOpenSavedPlaces: (SavedPlace?) -> Unit,
    onToggleTheme: () -> Unit
) {
    val cancelSearchDescription = stringResource(Res.string.search_cancel_accessibility)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Constants.DP_56)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = Constants.DP_16),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductWordmark(
                modifier = Modifier
                    .semantics { contentDescription = cancelSearchDescription }
                    .clickable(role = Role.Button, onClick = onBack)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Constants.DP_8)
            ) {
                Text(
                    stringResource(Res.string.search_title),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(Res.string.search_theme),
                    modifier = Modifier
                        .clickable(role = Role.Button, onClick = onToggleTheme)
                        .padding(Constants.DP_4),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Constants.DP_16)
                .padding(top = Constants.DP_8, bottom = Constants.DP_16),
            verticalArrangement = Arrangement.spacedBy(Constants.DP_12)
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
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    stringResource(Res.string.search_saved_places),
                    modifier = Modifier
                        .clickable(role = Role.Button) { onOpenSavedPlaces(null) }
                        .padding(horizontal = Constants.DP_8, vertical = Constants.DP_6),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )
            }
            StatusCard(
                title = stringResource(
                    if (status == SearchStatus.Error) {
                        Res.string.search_unavailable_error_title
                    } else {
                        Res.string.search_unavailable_title
                    }
                ),
                message = stringResource(Res.string.search_unavailable_message)
            )
            SearchMapPicker(onClick = onPickMap)
        }
    }
}

@Composable
internal fun SavedPlacesScreen(
    places: List<SavedPlace>,
    initialEditKey: String?,
    notice: String,
    onBack: () -> Unit,
    onSelectAddress: (SavedPlace) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (SavedPlace) -> Unit,
    onToggleTheme: () -> Unit
) {
    var editingPlace by remember { mutableStateOf<SavedPlace?>(null) }
    var editedLabel by remember { mutableStateOf("") }
    var deletingPlace by remember { mutableStateOf<SavedPlace?>(null) }
    val renameFocusRequester = remember { FocusRequester() }
    LaunchedEffect(initialEditKey, places) {
        val initial = places.firstOrNull { it.key == initialEditKey }
        if (initial != null && editingPlace?.key != initial.key) {
            editingPlace = initial
            editedLabel = initial.label
        }
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = Constants.DP_16, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(Res.string.route_stop_back),
                modifier = Modifier.clickable(role = Role.Button, onClick = onBack)
                    .padding(horizontal = Constants.DP_4, vertical = Constants.DP_8),
                color = MaterialTheme.colorScheme.primary
            )
            ProductWordmark(Modifier.weight(1f))
            Text(
                stringResource(Res.string.route_stop_theme),
                modifier = Modifier.clickable(role = Role.Button, onClick = onToggleTheme)
                    .padding(horizontal = Constants.DP_4, vertical = Constants.DP_8),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = Constants.DP_16, vertical = Constants.DP_8),
            verticalArrangement = Arrangement.spacedBy(Constants.DP_12)
        ) {
            Column(
                Modifier.widthIn(max = 760.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(Constants.DP_12)
            ) {
                Text(
                    stringResource(Res.string.saved_places_title),
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    stringResource(Res.string.saved_places_local_status),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                StatusCard(
                    stringResource(Res.string.saved_places_local_storage_title),
                    stringResource(Res.string.saved_places_local_storage_description)
                )
                if (notice.isNotBlank()) {
                    StatusCard(stringResource(Res.string.saved_places_notice_title), notice)
                }
                if (places.isEmpty()) {
                    StatusCard(
                        stringResource(Res.string.saved_places_empty_title),
                        stringResource(Res.string.saved_places_empty_message)
                    )
                } else {
                    places.forEach { place ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().testTag("saved-place-${place.key}"),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(Constants.DP_1, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(Constants.DP_8)
                            ) {
                                Text(
                                    place.label,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!place.label.equals(place.address, ignoreCase = true)) {
                                    Text(
                                        place.address,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    place.detail,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(Constants.DP_8)
                                ) {
                                    Text(
                                        stringResource(Res.string.saved_places_select_address),
                                        modifier = Modifier.clickable(role = Role.Button) {
                                            onSelectAddress(place)
                                        }.padding(vertical = Constants.DP_8),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp
                                    )
                                }
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(Constants.DP_16)
                                ) {
                                    Text(
                                        stringResource(Res.string.saved_places_rename_label),
                                        modifier = Modifier.clickable(role = Role.Button) {
                                            editingPlace = place
                                            editedLabel = place.label
                                        }.padding(vertical = Constants.DP_6),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        stringResource(Res.string.saved_places_delete),
                                        modifier = Modifier.clickable(role = Role.Button) {
                                            deletingPlace = place
                                        }.padding(vertical = Constants.DP_6),
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
                StatusCard(
                    stringResource(Res.string.saved_places_organization_title),
                    stringResource(Res.string.saved_places_organization_unavailable)
                )
                Text(
                    stringResource(Res.string.saved_places_manual_labels_note),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    editingPlace?.let { place ->
        AlertDialog(
            onDismissRequest = { editingPlace = null },
            title = { Text(stringResource(Res.string.saved_places_rename_dialog_title)) },
            text = {
                val dialogFocusManager = LocalFocusManager.current
                val dialogKeyboardController = LocalSoftwareKeyboardController.current
                LaunchedEffect(place.key) {
                    renameFocusRequester.requestFocus()
                    dialogKeyboardController?.show()
                }
                Column(
                    Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Constants.DP_8)
                ) {
                    Text(place.address)
                    BasicTextField(
                        value = editedLabel,
                        onValueChange = { editedLabel = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                dialogFocusManager.clearFocus(force = true)
                                dialogKeyboardController?.hide()
                            }
                        ),
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.fillMaxWidth().background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(Constants.DP_8)
                        ).padding(Constants.DP_12).focusRequester(renameFocusRequester)
                            .testTag("saved-place-label-input")
                    )
                }
            },
            confirmButton = {
                val dialogFocusManager = LocalFocusManager.current
                val dialogKeyboardController = LocalSoftwareKeyboardController.current
                TextButton(
                    enabled = editedLabel.isNotBlank(),
                    onClick = {
                        dialogFocusManager.clearFocus(force = true)
                        dialogKeyboardController?.hide()
                        onRename(place.key, editedLabel)
                        editingPlace = null
                    }
                ) { Text(stringResource(Res.string.saved_places_save)) }
            },
            dismissButton = {
                val dialogFocusManager = LocalFocusManager.current
                val dialogKeyboardController = LocalSoftwareKeyboardController.current
                TextButton(onClick = {
                    dialogFocusManager.clearFocus(force = true)
                    dialogKeyboardController?.hide()
                    editingPlace = null
                }) { Text(stringResource(Res.string.saved_places_cancel)) }
            }
        )
    }
    deletingPlace?.let { place ->
        AlertDialog(
            onDismissRequest = { deletingPlace = null },
            title = { Text(stringResource(Res.string.saved_places_delete_dialog_title)) },
            text = {
                Text(
                    stringResource(
                        Res.string.saved_places_delete_dialog_message,
                        place.label,
                        place.address
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(place)
                    deletingPlace = null
                }) { Text(stringResource(Res.string.saved_places_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deletingPlace = null }) {
                    Text(stringResource(Res.string.saved_places_cancel))
                }
            }
        )
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
        shape = RoundedCornerShape(Constants.DP_16),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Constants.DP_1, MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(horizontal = Constants.DP_12, vertical = Constants.DP_8)) {
            AddressQueryRow(
                target = SearchTarget.Origin,
                active = target == SearchTarget.Origin,
                value = if (target == SearchTarget.Origin) query else origin,
                placeholder = stringResource(Res.string.address_search_origin_placeholder),
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
                    .height(Constants.DP_1)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            AddressQueryRow(
                target = SearchTarget.Destination,
                active = target == SearchTarget.Destination,
                value = if (target == SearchTarget.Destination) query else destination,
                placeholder = stringResource(Res.string.address_search_destination_placeholder),
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
    val clearSearchDescription = stringResource(
        Res.string.address_search_clear_accessibility
    )
    val searchAddressDescription = stringResource(
        Res.string.address_search_submit_accessibility
    )
    val addressFieldDescription = stringResource(
        if (target == SearchTarget.Origin) {
            Res.string.address_search_origin_field_accessibility
        } else {
            Res.string.address_search_destination_field_accessibility
        }
    )
    val originPointDescription = stringResource(
        Res.string.address_search_origin_accessibility
    )
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
                    .padding(start = Constants.DP_1)
                    .size(10.dp)
                    .clip(CircleShape)
                    .border(Constants.DP_2, MaterialTheme.colorScheme.onSurface, CircleShape)
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
                    .padding(start = if (target == SearchTarget.Origin) Constants.DP_1 else 0.dp)
                    .size(if (target == SearchTarget.Origin) Constants.DP_12 else 14.dp)
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
                            contentDescription = addressFieldDescription
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
                    .semantics { contentDescription = clearSearchDescription }
                    .clickable(role = Role.Button) { onValueChange("") }
                    .padding(horizontal = Constants.DP_8),
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
                contentDescription = searchAddressDescription,
                tint = if (dark) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
            )
        }
        if (target == SearchTarget.Origin && !active) {
            FigmaIcon(
                light = Res.drawable.search_swap_light,
                dark = Res.drawable.search_swap_light,
                modifier = Modifier
                    .padding(start = Constants.DP_8)
                    .size(18.dp),
                contentDescription = originPointDescription
            )
        }
    }
}

@Composable
private fun SearchMapPicker(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Constants.DP_48)
            .clip(RoundedCornerShape(Constants.DP_12))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                Constants.DP_1,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(Constants.DP_12)
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FigmaIcon(
            light = Res.drawable.search_pick_map_light,
            dark = Res.drawable.search_pick_map_dark,
            modifier = Modifier.size(Constants.DP_20)
        )
        Text(
            stringResource(Res.string.address_search_pick_map),
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
            modifier = Modifier.size(Constants.DP_16),
            contentDescription = null
        )
    }
}

@Composable
internal fun RouteStopsScreen(
    origin: String,
    destination: String,
    stops: List<RouteStop>,
    notice: String,
    canSave: Boolean,
    onAdd: () -> Unit,
    onReplace: (String) -> Unit,
    onRemove: (String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onToggleTheme: () -> Unit
) {
    AuthBackHandler(onCancel)
    val editorScrollState = rememberScrollState()
    LaunchedEffect(stops) { editorScrollState.scrollTo(0) }
    Column(modifier = Modifier.fillMaxSize().imePadding().exportTestTags()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Constants.DP_16, vertical = Constants.DP_12),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(Res.string.route_stops_cancel),
                modifier = Modifier.clickable(role = Role.Button, onClick = onCancel).padding(Constants.DP_8),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                stringResource(Res.string.route_stops_title),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                stringResource(Res.string.route_stops_theme),
                modifier = Modifier.clickable(role = Role.Button, onClick = onToggleTheme).padding(Constants.DP_8),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().testTag("route-stops-content").verticalScroll(editorScrollState)
                .padding(horizontal = Constants.DP_16, vertical = Constants.DP_8),
            verticalArrangement = Arrangement.spacedBy(Constants.DP_12)
        ) {
            StatusCard(
                stringResource(Res.string.route_stops_origin_readonly),
                origin.ifBlank { stringResource(Res.string.route_stops_not_selected) },
                modifier = Modifier.testTag("route-stops-origin")
            )
            StatusCard(
                stringResource(Res.string.route_stops_destination_readonly),
                destination.ifBlank { stringResource(Res.string.route_stops_not_selected) },
                modifier = Modifier.testTag("route-stops-destination")
            )
            Text(
                stringResource(Res.string.route_stops_intermediate_count, stops.size),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (stops.isEmpty()) {
                StatusCard(
                    stringResource(Res.string.route_stops_none_title),
                    stringResource(Res.string.route_stops_empty_message)
                )
            } else {
                stops.forEachIndexed { index, stop ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().testTag("route-stop-${stop.stopId}"),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(Constants.DP_1, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(Constants.DP_8)
                        ) {
                            Text(
                                stringResource(Res.string.route_stops_number, index + 1) +
                                    stop.placeName?.let { " · $it" }.orEmpty(),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(stop.address, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                stop.detail,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(Constants.DP_4)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    RouteStopAction(
                                        stringResource(Res.string.route_stops_move_up),
                                        index > 0
                                    ) { onMoveUp(stop.stopId) }
                                    RouteStopAction(
                                        stringResource(Res.string.route_stops_move_down),
                                        index < stops.lastIndex
                                    ) {
                                        onMoveDown(stop.stopId)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    RouteStopAction(
                                        stringResource(Res.string.route_stops_replace_address)
                                    ) {
                                        onReplace(stop.stopId)
                                    }
                                    RouteStopAction(stringResource(Res.string.route_stops_delete)) {
                                        onRemove(stop.stopId)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            RouteStopAction(stringResource(Res.string.route_stops_add), onClick = onAdd)
            if (notice.isNotBlank()) {
                StatusCard(stringResource(Res.string.route_stops_notice_title), notice)
            }
            Text(
                stringResource(Res.string.route_stops_disclaimer),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Constants.DP_16, vertical = Constants.DP_12),
            horizontalArrangement = Arrangement.spacedBy(Constants.DP_12)
        ) {
            RouteStopAction(
                stringResource(Res.string.route_stops_cancel),
                modifier = Modifier.weight(1f),
                onClick = onCancel
            )
            RouteStopAction(
                stringResource(Res.string.route_stops_save),
                enabled = canSave,
                modifier = Modifier.weight(1f),
                onClick = onSave
            )
        }
    }
}

@Composable
internal fun RouteStopSearchScreen(
    stopId: String,
    query: String,
    savedPlaces: List<SavedPlace>,
    onQueryChange: (String) -> Unit,
    onSelectSavedPlace: (SavedPlace) -> Unit,
    onBack: () -> Unit,
    onOpenStopPlaces: () -> Unit,
    onToggleTheme: () -> Unit
) {
    AuthBackHandler(onBack)
    val normalizedQuery = query.trim()
    val savedMatches = savedPlaces.filter {
        normalizedQuery.isBlank() ||
            it.label.contains(normalizedQuery, ignoreCase = true) ||
            it.address.contains(normalizedQuery, ignoreCase = true) ||
            it.detail.contains(normalizedQuery, ignoreCase = true)
    }
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Constants.DP_16, vertical = Constants.DP_12),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(Res.string.route_stop_back),
                modifier = Modifier.clickable(role = Role.Button, onClick = onBack).padding(Constants.DP_8),
                color = MaterialTheme.colorScheme.primary
            )
            Text(stringResource(Res.string.route_stop_search_title), fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(Res.string.route_stop_theme),
                modifier = Modifier.clickable(role = Role.Button, onClick = onToggleTheme).padding(Constants.DP_8),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = Constants.DP_16),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                stringResource(Res.string.route_stop_search_target, stopId),
                fontSize = 12.sp
            )
            Text(
                stringResource(Res.string.route_stop_places),
                modifier = Modifier.fillMaxWidth().clickable(
                    role = Role.Button, onClick = onOpenStopPlaces
                ).padding(vertical = Constants.DP_12),
                color = MaterialTheme.colorScheme.primary
            )
            Surface(
                shape = RoundedCornerShape(Constants.DP_12),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(Constants.DP_1, MaterialTheme.colorScheme.outlineVariant)
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text(
                               stringResource(Res.string.route_stop_search_saved_hint),
                               color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        inner()
                    }
                )
            }
            Text(
                stringResource(Res.string.route_stop_saved_places),
                fontWeight = FontWeight.SemiBold
            )
            savedMatches.forEach { place ->
                RouteStopSearchResult(
                    title = place.label,
                    detail = if (place.label.equals(place.address, ignoreCase = true)) {
                        place.detail
                    } else {
                        stringResource(
                            Res.string.route_stop_address_detail,
                            place.address,
                            place.detail
                        )
                    }
                ) { onSelectSavedPlace(place) }
            }
            if (savedMatches.isEmpty()) {
                StatusCard(
                    stringResource(Res.string.route_stop_no_saved_title),
                    stringResource(Res.string.route_stop_no_saved_message)
                )
            }
        }
    }
}

@Composable
private fun RouteStopSearchResult(title: String, detail: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(Constants.DP_12),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Constants.DP_1, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(Constants.DP_12), verticalArrangement = Arrangement.spacedBy(Constants.DP_4)) {
            Text(title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(detail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RouteStopAction(
    label: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Text(
        label,
        modifier = modifier
            .then(if (enabled) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(vertical = Constants.DP_8),
        fontSize = 12.sp,
        color = if (enabled) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant
    )
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
            .clip(RoundedCornerShape(Constants.DP_12))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(Constants.DP_12),
        verticalArrangement = Arrangement.spacedBy(Constants.DP_4)
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
