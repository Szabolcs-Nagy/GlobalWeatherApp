package com.coding.global_weather_app

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val CURRENT_WEATHER_URL = "https://api.weatherapi.com/v1/current.json"

class WeatherApiService(
    private val client: HttpClient = createHttpClient(),
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun fetchCurrentWeather(query: String): Result<WeatherData> {
        val city = query.trim()
        if (city.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter a city name."))
        }

        return runCatching {
            val response = client.get(CURRENT_WEATHER_URL) {
                parameter("key", ApiConfig.weatherApiKey)
                parameter("q", city)
                parameter("aqi", "no")
            }

            val rawBody = response.body<String>()
            parseWeatherDataFromPayload(
                payload = rawBody,
                json = json,
                isSuccess = response.status.isSuccess(),
            )
        }.recoverCatching {
            throw IllegalStateException(it.message ?: "Unable to load weather right now.")
        }
    }
}

data class WeatherData(
    val locationName: String,
    val country: String,
    val localTime: String,
    val conditionText: String,
    val iconUrl: String,
    val temperatureC: Double,
    val feelsLikeC: Double,
    val humidity: Int,
    val windKph: Double,
)

internal fun parseWeatherDataFromPayload(
    payload: String,
    json: Json,
    isSuccess: Boolean,
): WeatherData {
    val root = json.parseToJsonElement(payload).jsonObject

    if (!isSuccess) {
        throw IllegalStateException(root.errorMessage() ?: "Request failed.")
    }

    return root.toWeatherData()
}

private fun JsonObject.toWeatherData(): WeatherData {
    val location = this["location"]?.jsonObject ?: error("Missing location data")
    val current = this["current"]?.jsonObject ?: error("Missing current weather data")
    val condition = current["condition"]?.jsonObject ?: error("Missing weather condition data")

    return WeatherData(
        locationName = location.string("name") ?: error("Missing city name"),
        country = location.string("country") ?: error("Missing country"),
        localTime = location.string("localtime") ?: "N/A",
        conditionText = condition.string("text") ?: "N/A",
        iconUrl = condition.string("icon").orEmpty().normalizeIconUrl(),
        temperatureC = current.double("temp_c") ?: 0.0,
        feelsLikeC = current.double("feelslike_c") ?: 0.0,
        humidity = current.int("humidity") ?: 0,
        windKph = current.double("wind_kph") ?: 0.0,
    )
}

private fun JsonObject.errorMessage(): String? {
    val error = this["error"]?.jsonObject ?: return null
    return error.string("message")
}

private fun JsonObject.string(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull

private fun JsonObject.double(key: String): Double? =
    this[key]?.jsonPrimitive?.doubleOrNull

private fun JsonObject.int(key: String): Int? =
    this[key]?.jsonPrimitive?.intOrNull

private fun String.normalizeIconUrl(): String {
    return when {
        startsWith("//") -> "https:$this"
        startsWith("http") -> this
        else -> ""
    }
}

