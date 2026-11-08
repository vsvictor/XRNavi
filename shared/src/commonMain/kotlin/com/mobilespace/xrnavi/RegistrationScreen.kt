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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.*

@Composable
internal fun RegistrationScreen(
    viewModel: RegistrationViewModel,
    onBack: () -> Unit,
    onToggleTheme: () -> Unit
) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var showDocuments by remember { mutableStateOf(false) }
    val back = {
        focus.clearFocus()
        keyboard?.hide()
        onBack()
    }
    AuthBackHandler(onBack = back)
    if (showDocuments) {
        AlertDialog(
            onDismissRequest = { showDocuments = false },
            title = { Text("Документи недоступні") },
            text = { Text(registrationDocumentsMessage) },
            confirmButton = {
                TextButton(onClick = { showDocuments = false }) { Text("Зрозуміло") }
            }
        )
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val split = maxWidth >= 600.dp && maxWidth > maxHeight
        val tablet = minOf(maxWidth, maxHeight) >= 600.dp
        val density = LocalDensity.current
        val keyboardHeight = with(density) { WindowInsets.ime.getBottom(this).toDp() }
        val compactHeader = maxHeight - keyboardHeight < 240.dp
        val scroll = rememberScrollState()
        Column(Modifier.fillMaxSize().imePadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = back, modifier = Modifier.semantics {
                    contentDescription = "Повернутися до входу"
                }) { Text("‹", fontSize = 28.sp) }
                Column(Modifier.weight(1f).padding(vertical = if (compactHeader) 0.dp else 8.dp)) {
                    Text(
                        "Створити акаунт",
                        fontFamily = rememberRobotoCondensedFontFamily(),
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compactHeader) 22.sp else 26.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (!compactHeader) {
                        Text(
                            "Ваш шлях починається тут",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(onClick = onToggleTheme) { Text("Тема") }
            }
            if (split) {
                Row(
                    Modifier.weight(1f).padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    if (!compactHeader) {
                        RegistrationHero(
                            Modifier.weight(1f).fillMaxHeight().padding(bottom = 16.dp),
                            compact = false
                        )
                    }
                    RegistrationForm(
                        viewModel,
                        Modifier.weight(1f).fillMaxHeight().verticalScroll(scroll)
                            .padding(bottom = 24.dp),
                        onDocuments = { showDocuments = true },
                        onBack = back
                    )
                }
            } else {
                Column(
                    Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RegistrationHero(
                        Modifier.fillMaxWidth().height(if (tablet) 260.dp else 110.dp),
                        compact = !tablet
                    )
                    Spacer(Modifier.height(16.dp))
                    RegistrationForm(
                        viewModel,
                        Modifier.widthIn(max = 540.dp).fillMaxWidth().padding(bottom = 24.dp),
                        onDocuments = { showDocuments = true },
                        onBack = back
                    )
                }
            }
        }
    }
}
