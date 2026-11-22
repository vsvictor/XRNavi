package com.mobilespace.xrnavi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.*

@Composable
internal fun RecoveryScreen(
    model: RecoveryViewModel,
    previewEnabled: Boolean,
    onBack: () -> Unit
) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var previewMenu by remember { mutableStateOf(false) }
    val back = {
        focus.clearFocus()
        keyboard?.hide()
        onBack()
    }
    AuthBackHandler(onBack = back)
    if (previewEnabled && previewMenu) {
        AlertDialog(
            onDismissRequest = { previewMenu = false },
            title = { Text("Внутрішній preview S41–S45") },
            text = {
                Column(
                    Modifier.testTag("recovery_preview_menu")
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(recoveryPreviewMessage, fontSize = 12.sp)
                    RecoveryStage.entries.forEach { stage ->
                        TextButton(onClick = {
                            focus.clearFocus()
                            keyboard?.hide()
                            model.openPreview(stage)
                            previewMenu = false
                        }) { Text("${stage.code} · ${stage.title}") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { previewMenu = false }) { Text("Закрити") }
            }
        )
    }
    val dark = isDarkXrnaviTheme()
    val surface = MaterialTheme.colorScheme.background
    val scroll = rememberScrollState()
    val windowSize = LocalWindowInfo.current.containerSize
    val windowHeight = with(LocalDensity.current) { windowSize.height.toDp() }
    val title = if (model.live || model.stage == RecoveryStage.Request) model.stage.title
    else "${model.stage.title} · демо"
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val split = maxWidth > maxHeight
        LaunchedEffect(split) { scroll.scrollTo(0) }
        val heroHeight = when {
            maxHeight < 680.dp -> 196.dp
            maxWidth >= 600.dp -> maxHeight * 0.45f
            else -> 266.dp
        }
        val screenBackground = Modifier.drawBehind {
            if (dark) {
                drawRect(surface)
            } else {
                drawRect(
                    Brush.verticalGradient(
                        colors = listOf(Color.White, surface),
                        startY = if (split) 0f else heroHeight.toPx()
                            .coerceAtMost((size.height - 1f).coerceAtLeast(0f)),
                        endY = size.height.coerceAtLeast(1f),
                    )
                )
            }
        }
        val form: @Composable (Modifier) -> Unit = { modifier ->
            Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.fillMaxWidth()) {
                    RecoveryContent(model, previewEnabled, back) { previewMenu = true }
                }
            }
        }
        key(model.stage) {
          Box(Modifier.fillMaxSize()) {
            if (split) {
                Row(Modifier.fillMaxSize().then(screenBackground).imePadding()) {
                    SignInHero(
                        modifier = Modifier.weight(0.9f).fillMaxHeight(),
                        dark = dark,
                        horizontalLayout = true,
                        title = title,
                        onBack = back
                    )
                    Column(
                        Modifier.weight(1.1f).fillMaxHeight().verticalScroll(scroll)
                            .padding(horizontal = 24.dp, vertical = 18.dp)
                    ) {
                        HeroTitle(title = title, horizontalLayout = true)
                        Spacer(Modifier.height(12.dp))
                        form(Modifier.fillMaxWidth())
                    }
                }
            } else {
                Column(
                    Modifier.fillMaxSize().then(screenBackground).imePadding().verticalScroll(scroll)
                ) {
                    SignInHero(
                        modifier = Modifier.fillMaxWidth().height(heroHeight),
                        dark = dark,
                        horizontalLayout = false,
                        title = title,
                        onBack = back
                    )
                    form(
                        Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                            .padding(top = 10.dp, bottom = 20.dp)
                    )
                }
            }
          }
        }
    }
}
