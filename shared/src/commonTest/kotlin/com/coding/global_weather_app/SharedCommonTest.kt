package com.coding.global_weather_app

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SharedCommonTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parsesWeatherPayload() {
        val payload =
            """
            {
              "location": {
                "name": "Budapest",
                "country": "Hungary",
                "localtime": "2026-05-28 20:55"
              },
              "current": {
                "temp_c": 18.4,
                "feelslike_c": 17.6,
                "humidity": 63,
                "wind_kph": 12.1,
                "condition": {
                  "text": "Partly cloudy",
                  "icon": "//cdn.weatherapi.com/weather/64x64/day/116.png"
                }
              }
            }
            """.trimIndent()

        val result = parseWeatherDataFromPayload(payload, json = json, isSuccess = true)

        assertEquals("Budapest", result.locationName)
        assertEquals("Hungary", result.country)
        assertEquals("Partly cloudy", result.conditionText)
        assertEquals(18.4, result.temperatureC)
    }

    @Test
    fun throwsApiErrorMessageWhenRequestFails() {
        val payload =
            """
            {
              "error": {
                "message": "No matching location found."
              }
            }
            """.trimIndent()

        val error = assertFailsWith<IllegalStateException> {
            parseWeatherDataFromPayload(payload, json = json, isSuccess = false)
        }

        assertEquals("No matching location found.", error.message)
    }
}