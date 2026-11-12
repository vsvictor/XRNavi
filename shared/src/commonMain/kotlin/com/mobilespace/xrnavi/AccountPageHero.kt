package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.profile_mustang_hero_dark
import xrnavi.shared.generated.resources.profile_mustang_hero_light

@Composable
internal fun AccountPageHero(title: String, onBack: (() -> Unit)? = null) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val window = LocalWindowInfo.current.containerSize
        val landscape = window.width > window.height
        val dark = isDarkXrnaviTheme()
        SignInHero(
            modifier = Modifier.fillMaxWidth().height(
                if (landscape) 196.dp else if (maxWidth < 600.dp) 220.dp else 266.dp,
            ),
            dark = dark,
            image = if (dark) Res.drawable.profile_mustang_hero_dark else Res.drawable.profile_mustang_hero_light,
            horizontalLayout = false,
            title = title,
            onBack = onBack,
            backDescription = "Назад",
        )
    }
}
