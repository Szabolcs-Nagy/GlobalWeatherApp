

# Global Weather App (Compose Multiplatform)

A beautiful, fully animated weather application built with Kotlin Multiplatform and Jetpack Compose. Write once, run seamlessly on both Android and iOS with a fully native feel.

<p align="center">
  <img src="https://github.com/user-attachments/assets/a34ea8e5-ff50-4590-94ff-4cdb6fcd71fd" width="200" alt="App Launch Icon"/>
  <br/>
  <em>Custom vector-based adaptive launcher icons for both platforms.</em>
</p>

## ✨ Features Showcase

Take a look at the dynamic UI that reacts to the current weather with smooth glassmorphism effects, floating orbs, and animated content transitions.

### The Idle Search State

When you first launch the app, you are greeted with a clean, dynamic background waiting for your search.

<p align="center">
  <img src="https://github.com/user-attachments/assets/ac156eca-1c23-41e1-998d-63f2fbac0bc4" width="300" alt="App Idle Screen"/>
</p>

### Dynamic Weather Search

The background gradients and animations seamlessly morph from one state to another based on the live weather data. Watch it in action:

#### ☁️ Cloudy Weather (Los Angeles)
[https://github.com/user-attachments/assets/weather_la.mov](https://github.com/user-attachments/assets/c5a20f7a-64b0-4265-ab31-03c8ed238ce2)

#### ☀️ Sunny Weather (Budapest)
[https://github.com/user-attachments/assets/weather_budapest.mov](https://github.com/user-attachments/assets/88fb1406-b385-4e14-9a74-af3f24b70286)

*(To view these videos on GitHub, simply drag and drop the local `.mov` files from `.github/assets/` into this document using the GitHub web editor.)*

---

## 🛠️ Architecture

It uses:

- **Compose Multiplatform** for shared, fully declarative UI (`App.kt`).
- **Ktor** client for cross-platform networking.
- **WeatherAPI** current weather endpoint (`/v1/current.json`).
- **Kotlinx Serialization** to safely parse JSON responses.

## 🔑 Secrets setup

The WeatherAPI key is strictly kept out of version control and injected at build time via a custom Gradle generation task into a shared `ApiConfig` object.

**Android** — `local.properties` (gitignored):
```properties
weatherApiKey=your_weatherapi_key_here
```

**iOS** — copy the template and fill it in (also gitignored):
```bash
cp iosApp/Configuration/Secrets.xcconfig.template iosApp/Configuration/Secrets.xcconfig
# then edit Secrets.xcconfig with your key
```

The key flows through the build system:
- Android: `local.properties` → `generateApiConfig` gradle task → `ApiConfig.weatherApiKey`
- iOS: `Secrets.xcconfig` → `$(WEATHER_API_KEY)` in `Info.plist` → `generateApiConfig` injected `ApiConfig` object. Both platforms actually just use the generated Kotlin object from the shared module build process!

## 🚀 Run

To build and run the Android app:
```bash
./gradlew :androidApp:assembleDebug
```

For iOS, compile the shared module first, then open Xcode:
```bash
./gradlew :shared:compileKotlinIosSimulatorArm64
open iosApp/iosApp.xcodeproj
```
Then select the `iosApp` scheme and hit `Cmd+R`.

## 🧪 Test

The shared JSON parsing logic and API error extraction are unit tested in the shared module.

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
```
