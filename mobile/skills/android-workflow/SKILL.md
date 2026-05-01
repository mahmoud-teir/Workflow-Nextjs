---
name: android-workflow
description: General Native Android project architecture, Gradle configuration, and Hilt DI.
origin: Mobile ECC
stack: Android Studio, Gradle Version Catalogs, Dagger Hilt
---

# Android Workflow & Architecture Skill

## Directory Structure
```
ui/             # Presentation layer (Compose, ViewModels, Theme)
domain/         # Business logic (Models, UseCases, Interfaces)
data/           # Data layer (Room, Retrofit, Repositories)
di/             # Dagger Hilt modules
```

## Gradle Version Catalogs
Always define dependencies in `gradle/libs.versions.toml`.
Never hardcode versions in `build.gradle.kts`.

## Dagger Hilt
- `App.kt` MUST have `@HiltAndroidApp`
- `MainActivity.kt` MUST have `@AndroidEntryPoint`
- `ViewModels` MUST have `@HiltViewModel`

## Coroutines
- UI runs on Main thread.
- Network and Database MUST run on `Dispatchers.IO`.
- Use `viewModelScope.launch { ... }` in ViewModels.
