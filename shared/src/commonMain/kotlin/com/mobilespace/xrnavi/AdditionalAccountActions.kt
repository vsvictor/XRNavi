package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

/** Building blocks shared by the demo account screens (garage, profile, vehicle, settings). */
@Composable
internal fun AdditionalAccountActions(
    destinations: List<AdditionalScreen>,
    onOpenAdditional: ((AdditionalScreen) -> Unit)?
) {
    if (onOpenAdditional == null) return
    destinations.forEach { destination ->
        AccountAction(destination.title, "Відкрити · локальний приклад") {
            onOpenAdditional(destination)
        }
    }
}
