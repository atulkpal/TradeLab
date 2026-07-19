package com.ashwathai.tradelab.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
