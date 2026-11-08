package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
internal fun ProfileAuthActions(onExit: () -> Unit, onRegister: () -> Unit, onRecovery: () -> Unit) {
    AccountCard {
        CardHeading("Вхід і реєстрація")
        AccountBody("Ці дії закривають демонстраційні дані та поточну сесію входу.")
        TextButton(onClick = onExit) { Text("Вийти з деморежиму") }
        TextButton(onClick = onRegister) { Text("Відкрити реєстрацію") }
        TextButton(onClick = onRecovery) { Text("Відновлення пароля") }
    }
}
