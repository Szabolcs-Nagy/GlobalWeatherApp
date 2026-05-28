package com.coding.global_weather_app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform