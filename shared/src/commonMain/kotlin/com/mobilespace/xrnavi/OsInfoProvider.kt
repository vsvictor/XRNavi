package com.mobilespace.xrnavi

/** Operating system sent with `auth.login` as `device.os`. */
internal data class OsInfo(val name: String, val version: String)

internal fun interface OsInfoProvider {
    fun osInfo(): OsInfo
}

/** Reads the OS of the running platform (`Android` or `iOS`). */
internal class PlatformOsInfoProvider : OsInfoProvider {
    override fun osInfo(): OsInfo = platformOsInfo()
}

internal expect fun platformOsInfo(): OsInfo
