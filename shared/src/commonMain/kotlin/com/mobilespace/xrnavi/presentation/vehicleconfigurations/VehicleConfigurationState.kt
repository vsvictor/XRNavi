package com.mobilespace.xrnavi.presentation.vehicleconfigurations

import androidx.compose.runtime.saveable.listSaver

internal val vehicleConfigurationSelectionsSaver = listSaver<Map<String, String>, String>(
    save = { selections -> selections.flatMap { (vehicleId, configurationId) ->
        listOf(vehicleId, configurationId)
    } },
    restore = { values ->
        values.chunked(2)
            .filter { it.size == 2 }
            .associate { (vehicleId, configurationId) -> vehicleId to configurationId }
    }
)
