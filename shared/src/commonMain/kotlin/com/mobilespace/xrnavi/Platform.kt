package com.mobilespace.xrnavi

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform