package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.*

internal class TrackingLocationProvider(
    private val native: LocationProvider,
    private val locations: TrackerLocationBloc,
) : LocationProvider by native {
    override fun fixes(): Flow<DeviceFix?> = locations.state

    override fun refresh() {
        native.refresh()
        locations.refresh()
    }
}
