package com.coding.global_weather_app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ── Gradient palettes per weather condition ───────────────────────────────────

private val GRADIENT_SUNNY   = Color(0xFFFFB300) to Color(0xFFE64A19)
private val GRADIENT_PARTLY  = Color(0xFF039BE5) to Color(0xFF0277BD)
private val GRADIENT_CLOUDY  = Color(0xFF607D8B) to Color(0xFF37474F)
private val GRADIENT_RAINY   = Color(0xFF1E88E5) to Color(0xFF0D47A1)
private val GRADIENT_STORMY  = Color(0xFF1C1C2E) to Color(0xFF4A148C)
private val GRADIENT_SNOWY   = Color(0xFF81D4FA) to Color(0xFF4FC3F7)
private val GRADIENT_FOGGY   = Color(0xFF90A4AE) to Color(0xFF546E7A)
private val GRADIENT_DEFAULT = Color(0xFF3949AB) to Color(0xFF1A237E)

private fun conditionGradient(condition: String): Pair<Color, Color> {
    val w = condition.lowercase()
    return when {
        "thunder" in w || "storm" in w            -> GRADIENT_STORMY
        "blizzard" in w                            -> GRADIENT_SNOWY
        "snow" in w || "sleet" in w || "ice" in w -> GRADIENT_SNOWY
        "heavy rain" in w || "torrential" in w     -> GRADIENT_STORMY
        "rain" in w || "drizzle" in w || "shower" in w -> GRADIENT_RAINY
        "fog" in w || "mist" in w || "haze" in w  -> GRADIENT_FOGGY
        "overcast" in w || "cloudy" in w           -> GRADIENT_CLOUDY
        "partly" in w                              -> GRADIENT_PARTLY
        "sunny" in w || "clear" in w               -> GRADIENT_SUNNY
        else                                       -> GRADIENT_DEFAULT
    }
}

private fun conditionEmoji(condition: String): String {
    val w = condition.lowercase()
    return when {
        "thunder" in w || "storm" in w             -> "⛈️"
        "blizzard" in w                             -> "❄️"
        "snow" in w || "sleet" in w || "ice" in w  -> "🌨️"
        "heavy rain" in w || "torrential" in w      -> "🌧️"
        "drizzle" in w                              -> "🌦️"
        "rain" in w || "shower" in w                -> "🌧️"
        "fog" in w || "mist" in w || "haze" in w   -> "🌫️"
        "overcast" in w                             -> "☁️"
        "cloudy" in w || "partly" in w              -> "⛅"
        "sunny" in w || "clear" in w                -> "☀️"
        else                                        -> "🌡️"
    }
}

// ── Screen state ──────────────────────────────────────────────────────────────

private sealed interface WeatherScreenState {
    data object Idle    : WeatherScreenState
    data object Loading : WeatherScreenState
    data class  Success(val weather: WeatherData) : WeatherScreenState
    data class  Error(val message: String)        : WeatherScreenState
}

// ── Root ──────────────────────────────────────────────────────────────────────

@Composable
fun App() {
    val service = remember { WeatherApiService() }
    val scope   = rememberCoroutineScope()

    var city  by remember { mutableStateOf("") }
    var state by remember { mutableStateOf<WeatherScreenState>(WeatherScreenState.Idle) }

    val (targetTop, targetBot) = when (val s = state) {
        is WeatherScreenState.Success -> conditionGradient(s.weather.conditionText)
        else                          -> GRADIENT_DEFAULT
    }

    // Background colours morph smoothly when weather changes
    val animTop by animateColorAsState(targetTop, tween(1400), label = "bgTop")
    val animBot by animateColorAsState(targetBot, tween(1400), label = "bgBot")

    fun search() {
        scope.launch {
            state = WeatherScreenState.Loading
            state = service.fetchCurrentWeather(city).fold(
                onSuccess = { WeatherScreenState.Success(it) },
                onFailure = { WeatherScreenState.Error(it.message ?: "Something went wrong.") },
            )
        }
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(animTop, animBot))),
        ) {
            FloatingOrbs(tint = animTop)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(56.dp))

                AppTitle()

                Spacer(Modifier.height(24.dp))

                SearchBar(city = city, onChange = { city = it }, onSearch = ::search)

                AnimatedContent(
                    targetState      = state,
                    transitionSpec   = {
                        (fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 6 })
                            .togetherWith(fadeOut(tween(200)))
                    },
                    contentAlignment = Alignment.TopCenter,
                    modifier         = Modifier.fillMaxSize(),
                    label            = "weatherState",
                ) { s ->
                    when (s) {
                        WeatherScreenState.Idle       -> IdleContent()
                        WeatherScreenState.Loading    -> LoadingContent()
                        is WeatherScreenState.Error   -> ErrorContent(s.message)
                        is WeatherScreenState.Success -> WeatherDisplay(s.weather)
                    }
                }
            }
        }
    }
}

// ── Title ─────────────────────────────────────────────────────────────────────

@Composable
private fun AppTitle() {
    Text(
        text          = "Global Weather",
        fontSize      = 28.sp,
        fontWeight    = FontWeight.Bold,
        color         = Color.White,
        letterSpacing = 0.5.sp,
    )
}

// ── Floating background orbs ──────────────────────────────────────────────────

@Composable
private fun FloatingOrbs(tint: Color) {
    val inf = rememberInfiniteTransition(label = "orbs")
    val a by inf.animateFloat(0f, 1f,
        infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse), "orbA")
    val b by inf.animateFloat(1f, 0f,
        infiniteRepeatable(tween(13000, easing = LinearEasing), RepeatMode.Reverse), "orbB")

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier.size(300.dp)
                .offset(x = (-60 + 80 * a).dp, y = (30 + 60 * b).dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.25f))
        )
        Box(
            Modifier.size(220.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (40 - 70 * b).dp, y = (-60 + 50 * a).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )
        Box(
            Modifier.size(120.dp)
                .align(Alignment.CenterEnd)
                .offset(x = (20 - 40 * a).dp, y = (80 * b - 40).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
        )
    }
}

// ── Search bar ────────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(city: String, onChange: (String) -> Unit, onSearch: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(40.dp),
        color    = Color.White.copy(alpha = 0.18f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(start = 20.dp, end = 8.dp),
        ) {
            TextField(
                value           = city,
                onValueChange   = onChange,
                placeholder     = { Text("City name…", color = Color.White.copy(0.6f)) },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor        = Color.White,
                    unfocusedTextColor      = Color.White,
                    cursorColor             = Color.White,
                ),
                modifier = Modifier.weight(1f),
            )
            Surface(
                onClick  = onSearch,
                shape    = CircleShape,
                color    = Color.White.copy(0.25f),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🔍", fontSize = 18.sp)
                }
            }
            Spacer(Modifier.width(4.dp))
        }
    }
}

// ── Idle ──────────────────────────────────────────────────────────────────────

@Composable
private fun IdleContent() {
    val inf = rememberInfiniteTransition(label = "idleFloat")
    val yOff by inf.animateFloat(-8f, 8f,
        infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse), "idleY")

    Column(
        modifier            = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🌍", fontSize = 90.sp, modifier = Modifier.offset(y = yOff.dp))
        Spacer(Modifier.height(20.dp))
        Text(
            text       = "Search for any city\nto see live weather",
            color      = Color.White.copy(0.75f),
            textAlign  = TextAlign.Center,
            fontSize   = 16.sp,
            lineHeight = 26.sp,
        )
    }
}

// ── Loading ───────────────────────────────────────────────────────────────────

@Composable
private fun LoadingContent() {
    val inf = rememberInfiniteTransition(label = "loading")
    val pulse by inf.animateFloat(0.8f, 1.2f,
        infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse), "pulse")
    val alpha by inf.animateFloat(0.5f, 1.0f,
        infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse), "alpha")

    Column(
        modifier            = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🌀", fontSize = 64.sp, modifier = Modifier.scale(pulse))
        Spacer(Modifier.height(20.dp))
        Text("Fetching weather…", color = Color.White.copy(alpha), fontSize = 16.sp)
    }
}

// ── Error ─────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorContent(message: String) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("😶‍🌫️", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(0.18f)) {
            Text(
                text      = message,
                color     = Color.White,
                textAlign = TextAlign.Center,
                fontSize  = 15.sp,
                modifier  = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
        }
    }
}

// ── Weather success ───────────────────────────────────────────────────────────

@Composable
private fun WeatherDisplay(weather: WeatherData) {
    // Temperature animates its numeric value when result changes
    val animTemp by animateFloatAsState(
        targetValue   = weather.temperatureC.toFloat(),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label         = "temp",
    )

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Weather emoji with entrance scale animation
        var emojiVisible by remember(weather.conditionText) { mutableStateOf(false) }
        LaunchedEffect(weather.conditionText) { emojiVisible = true }

        AnimatedVisibility(
            visible = emojiVisible,
            enter   = scaleIn(tween(600, easing = FastOutSlowInEasing)) + fadeIn(tween(600)),
            exit    = scaleOut() + fadeOut(),
        ) {
            Text(conditionEmoji(weather.conditionText), fontSize = 96.sp)
        }

        Spacer(Modifier.height(4.dp))

        // Giant animated temperature
        Text(
            text          = "${animTemp.roundToInt()}°",
            fontSize      = 96.sp,
            fontWeight    = FontWeight.Thin,
            color         = Color.White,
            letterSpacing = (-3).sp,
        )

        // Condition label
        Text(
            text       = weather.conditionText,
            fontSize   = 22.sp,
            fontWeight = FontWeight.Light,
            color      = Color.White.copy(0.90f),
        )

        Spacer(Modifier.height(8.dp))

        // Location pill
        Surface(shape = RoundedCornerShape(40.dp), color = Color.White.copy(0.20f)) {
            Text(
                text     = "📍  ${weather.locationName}, ${weather.country}",
                color    = Color.White,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp),
            )
        }

        Spacer(Modifier.height(44.dp))

        StatCardRow(weather)

        Spacer(Modifier.height(32.dp))
    }
}

// ── Stat cards ────────────────────────────────────────────────────────────────

private data class Stat(val emoji: String, val value: String, val label: String)

@Composable
private fun StatCardRow(weather: WeatherData) {
    val stats = listOf(
        Stat("🌡️", "${weather.feelsLikeC}°C",        "Feels like"),
        Stat("💧", "${weather.humidity}%",             "Humidity"),
        Stat("💨", "${weather.windKph} km/h",          "Wind"),
        Stat("🕐", weather.localTime.takeLast(5),      "Local time"),
    )
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(stats, key = { it.label }) { stat -> StatCard(stat) }
    }
}

@Composable
private fun StatCard(stat: Stat) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(600)) + scaleIn(tween(600, easing = FastOutSlowInEasing)),
    ) {
        Surface(
            shape    = RoundedCornerShape(28.dp),
            color    = Color.White.copy(alpha = 0.18f),
            modifier = Modifier.width(104.dp),
        ) {
            Column(
                modifier            = Modifier.padding(vertical = 20.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(stat.emoji, fontSize = 30.sp)
                Text(
                    text       = stat.value,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White,
                    fontSize   = 15.sp,
                    textAlign  = TextAlign.Center,
                    maxLines   = 1,
                )
                Text(
                    text      = stat.label,
                    fontSize  = 11.sp,
                    color     = Color.White.copy(0.65f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

