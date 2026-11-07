@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.mobilespace.xrnavi

import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.NSObject

class IosLocationProvider : LocationProvider {
    private val manager = CLLocationManager()
    private val mutablePermission = MutableStateFlow(readPermission())
    private val mutableService = MutableStateFlow(readService())
    private var onFix: ((DeviceFix) -> Unit)? = null

    private val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) = refresh()

        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            (didUpdateLocations.lastOrNull() as? CLLocation)?.let { publish(it) }
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) = refresh()
    }

    init {
        manager.delegate = delegate
        manager.desiredAccuracy = kCLLocationAccuracyBest
    }

    override val permission: StateFlow<LocationPermission> = mutablePermission
    override val serviceEnabled: StateFlow<Boolean> = mutableService

    override fun refresh() {
        mutablePermission.value = readPermission()
        mutableService.value = readService()
    }

    override fun requestPermission() {
        if (manager.authorizationStatus == 0) manager.requestWhenInUseAuthorization()
    }

    override fun openAppSettings() = openSettings()
    override fun openLocationSettings() = openSettings()

    override fun fixes(): Flow<DeviceFix> = callbackFlow {
        onFix = { trySend(it) }
        manager.location?.let { publish(it) }
        manager.startUpdatingLocation()
        awaitClose {
            manager.stopUpdatingLocation()
            onFix = null
        }
    }

    private fun publish(location: CLLocation) {
        val point = location.coordinate.useContents { GeoPoint(latitude, longitude) }
        onFix?.invoke(DeviceFix(point, (location.timestamp.timeIntervalSinceReferenceDate + platform.Foundation.NSTimeIntervalSince1970 * 1000).toLong()))
    }

    private fun readService() = CLLocationManager.locationServicesEnabled()

    private fun readPermission(): LocationPermission = when (manager.authorizationStatus) {
        kCLAuthorizationStatusAuthorizedAlways, kCLAuthorizationStatusAuthorizedWhenInUse -> LocationPermission.Granted
        kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> LocationPermission.PermanentlyDenied
        else -> LocationPermission.NotDetermined
    }

    private fun openSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any?>(), null)
    }
}
