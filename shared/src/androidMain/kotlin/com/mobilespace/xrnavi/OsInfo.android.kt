package com.mobilespace.xrnavi

import android.os.Build

internal actual fun platformOsInfo(): OsInfo = OsInfo("Android", Build.VERSION.RELEASE ?: Build.VERSION.SDK_INT.toString())
