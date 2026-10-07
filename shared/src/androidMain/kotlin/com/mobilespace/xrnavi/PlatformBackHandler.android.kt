package com.mobilespace.xrnavi

import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

@Composable
internal actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    val view = LocalView.current
    DisposableEffect(view, enabled, onBack) {
        val listener = android.view.View.OnKeyListener { _, keyCode, event ->
            if (enabled && keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                onBack()
                true
            } else {
                false
            }
        }
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener(listener)
        onDispose { view.setOnKeyListener(null) }
    }
}
