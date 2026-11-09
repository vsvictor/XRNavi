package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import xrnavi.shared.generated.resources.*

/** Navigation and "unavailable" callbacks handed to every additional screen. */
internal class AdditionalActions(
    val open: (AdditionalScreen) -> Unit,
    val back: () -> Unit,
    val section: (DemoSection) -> Unit,
    val unavailable: (String) -> Unit
)
