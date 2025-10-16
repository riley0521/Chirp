package com.rfcoding.chirp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform