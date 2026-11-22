package com.mobilespace.xrnavi

import platform.UIKit.UIDevice

internal actual fun platformOsInfo(): OsInfo = OsInfo("iOS", UIDevice.currentDevice.systemVersion)
