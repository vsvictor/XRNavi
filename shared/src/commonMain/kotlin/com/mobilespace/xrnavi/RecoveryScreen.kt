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
    onBack: () -> Unit,
    onToggleTheme: () -> Unit
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
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val split = maxWidth >= 600.dp && maxWidth > maxHeight
        val tablet = minOf(maxWidth, maxHeight) >= 600.dp
        val density = LocalDensity.current
        val imeHeight = with(density) { WindowInsets.ime.getBottom(this).toDp() }
        val short = maxHeight - imeHeight < 240.dp
        Column(Modifier.fillMaxSize().imePadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = back,
                    modifier = Modifier.semantics { contentDescription = "Повернутися до входу" }
                ) { Text("‹", fontSize = 28.sp) }
                Column(Modifier.weight(1f).padding(vertical = if (short) 0.dp else 8.dp)) {
                    ProductWordmark()
                    if (!short) {
                        Text(
                            "Mobile space",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(onClick = onToggleTheme) { Text("Тема") }
            }
            key(model.stage) {
                val scroll = rememberScrollState()
                if (split) {
                    Row(
                        Modifier.weight(1f).padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!short) {
                            RecoveryPhoto(
                                Modifier.weight(1f).fillMaxHeight().padding(vertical = 24.dp)
                            )
                        }
                        Column(
                            Modifier.weight(1f).fillMaxHeight().verticalScroll(scroll)
                                .padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            RecoveryContent(model, previewEnabled, back) { previewMenu = true }
                        }
                    }
                } else {
                    Column(
                        Modifier.weight(1f).verticalScroll(scroll)
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!short) {
                            RecoveryPhoto(
                                Modifier.fillMaxWidth().height(if (tablet) 270.dp else 176.dp)
                            )
                            Spacer(Modifier.height(28.dp))
                        }
                        Column(
                            Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(bottom = 24.dp)
                        ) {
                            RecoveryContent(model, previewEnabled, back) { previewMenu = true }
                        }
                    }
                }
            }
        }
    }
}
