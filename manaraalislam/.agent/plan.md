# Project Plan

Build the 'Manara Al-Islam' application, a comprehensive Islamic guide including a structured school, library, and daily tools. The app must be stable, adaptive, and follow Material Design 3 guidelines.

## Project Brief

Manara Al-Islam (Lighthouse of Islam) - Project Brief

Manara Al-Islam is a comprehensive Islamic educational and lifestyle platform.

### Features
* **Structured Islamic School (Level 1 MVP):** Guided learning path (Quran, Hadith, Ethics). Video lessons, infographics, quizzes, certificates.
* **Integrated Digital Reference Library:** Quran reader (search, bookmarks, Tafsir), Daily Athkar.
* **Smart Islamic Tools:** Prayer timings (location-based), Hijri/Gregorian calendar, event countdown.
* **Gamified Engagement:** Points, badges, level-ups.
* **Personalized User Profiles:** Email/Guest login (random ID/image for guests). Interest selection. Progress sync.

### High-Level Tech Stack
* Language: Kotlin
* UI: Jetpack Compose, Material Design 3, Compose Material Adaptive.
* Navigation: Jetpack Navigation 3.
* Async: Coroutines & Flow.
* Networking: Retrofit & Kotlinx Serialization.
* Media: Coil & ExoPlayer.

### Specific User Constraints (Cheat Sheet):
- Use actual newline characters, not `\n` in Kotlin strings.
- Delete Room DB in debug if schema changes: `if (BuildConfig.DEBUG) { appContext.deleteDatabase("database_name") }`.
- Enable `buildConfig = true` in `app/build.gradle.kts`.
- Load API keys from `local.properties`.
- Use Gemini 1.5 Pro (3.1 Pro) for reasoning tasks.

## Implementation Steps

