package com.coding.global_weather_app

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory

expect fun platformHttpClientEngineFactory(): HttpClientEngineFactory<*>

fun createHttpClient(): HttpClient = HttpClient(platformHttpClientEngineFactory())
