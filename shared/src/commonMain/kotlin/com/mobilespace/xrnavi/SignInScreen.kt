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
internal fun SignInScreen(
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
    onForgotPassword: () -> Unit
) {
    val dark = isDarkXrnaviTheme()
    val surface = MaterialTheme.colorScheme.background
    val formScroll = rememberScrollState()
    val windowSize = LocalWindowInfo.current.containerSize
    val windowHeight = with(LocalDensity.current) { windowSize.height.toDp() }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val availableWidth = maxWidth
        val splitLayout = windowSize.width > windowSize.height
        val heroHeight = when {
            windowHeight < 680.dp -> 210.dp
            availableWidth >= 600.dp -> 330.dp
            else -> 280.dp
        }
        val screenBackground = Modifier.drawBehind {
            if (dark) {
                drawRect(surface)
            } else {
                drawRect(
                    Brush.verticalGradient(
                        colors = listOf(Color.White, surface),
                        startY = if (splitLayout) 0f else heroHeight.toPx()
                            .coerceAtMost((size.height - 1f).coerceAtLeast(0f)),
                        endY = size.height.coerceAtLeast(1f),
                    )
                )
            }
        }
        if (splitLayout) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .then(screenBackground)
                    .imePadding()
            )
            {
                SignInHero(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight(),
                    dark = dark,
                    horizontalLayout = true
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
                    passwordVisible = passwordVisible,
                    onTogglePasswordVisibility = onTogglePasswordVisibility,
                    onSelectAccount = onSelectAccount,
                    submitting = submitting,
                    notice = notice,
                    onSignIn = onSignIn,
                    onDemo = onDemo,
                    onRegister = onRegister,
                    onForgotPassword = onForgotPassword,
                    dark = dark
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(screenBackground)
                    .imePadding()
                    .verticalScroll(formScroll)
            ) {
                SignInHero(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(heroHeight),
                    dark = dark,
                    horizontalLayout = false
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
                    passwordVisible = passwordVisible,
                    onTogglePasswordVisibility = onTogglePasswordVisibility,
                    onSelectAccount = onSelectAccount,
                    submitting = submitting,
                    notice = notice,
                    onSignIn = onSignIn,
                    onDemo = onDemo,
                    onRegister = onRegister,
                    onForgotPassword = onForgotPassword,
                    dark = dark
                )
            }
        }
    }
}
