package com.coding.global_weather_app

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

actual fun platformHttpClientEngineFactory(): HttpClientEngineFactory<*> = OkHttp
