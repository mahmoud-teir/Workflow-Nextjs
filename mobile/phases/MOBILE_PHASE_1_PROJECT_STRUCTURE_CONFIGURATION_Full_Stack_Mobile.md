<a name="phase-m1"></a>
# 📌 MOBILE PHASE M1: PROJECT STRUCTURE & CONFIGURATION (Android Engineer)

> **Rule:** All Native Android projects MUST use Gradle Version Catalogs (`libs.versions.toml`) and strictly separate code by feature or layer.

---

### Prompt M1.1: Project Initialization & Version Catalogs

```text
You are a Lead Android Engineer. Initialize the Native Android project structure and set up Gradle Version Catalogs.

Dependencies needed:
- Core: AndroidX Core KTX, Lifecycle Runtime, Activity Compose
- UI: Jetpack Compose (BOM), Material 3, UI Tooling, UI Test Manifest
- Navigation: Navigation Compose, kotlinx-serialization
- DI: Dagger Hilt
- Network: Retrofit, OkHttp Logging Interceptor
- Database: Room
- Async: Kotlin Coroutines

Required Output Format: Provide complete code for:

1. `gradle/libs.versions.toml`:
```toml
[versions]
agp = "8.3.0"
kotlin = "1.9.22"
coreKtx = "1.12.0"
junit = "4.13.2"
junitVersion = "1.1.5"
espressoCore = "3.5.1"
lifecycleRuntimeKtx = "2.7.0"
activityCompose = "1.8.2"
composeBom = "2024.02.00"
hilt = "2.50"
hiltNavigationCompose = "1.2.0"
room = "2.6.1"
retrofit = "2.9.0"
okhttp = "4.12.0"
coroutines = "1.8.0"
navigationCompose = "2.7.7"
serialization = "1.6.3"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }

# Navigation
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serialization" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }

# Network
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }

# Coroutines
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

[plugins]
androidApplication = { id = "com.android.application", version.ref = "agp" }
jetbrainsKotlinAndroid = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "1.9.22-1.0.17" }
serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

2. App-level `build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.example.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.10" }
}

dependencies {
    // Implement all the libraries from the TOML catalog here
    implementation(libs.androidx.core.ktx)
    // ...
}
```
```

---

### Prompt M1.2: Directory Structure & Core Application Class

```text
You are an Android Architect. Set up the package structure and the required core Application class.

Constraints:
- Use feature-based packaging (e.g., `ui/auth/`, `ui/home/`, `data/`, `di/`).
- Initialize Dagger Hilt in the Application class.
- Update `AndroidManifest.xml` to point to the new Application class.

Required Output Format: Provide complete code for:

1. Directory Structure (Linux tree format):
```
src/main/java/com/example/app/
├── App.kt                # Custom Application class
├── MainActivity.kt       # Single Activity
├── di/                   # Hilt Modules
│   ├── AppModules.kt
│   └── NetworkModules.kt
├── data/                 # Data Layer
│   ├── local/            # Room DAOs, Entities, DataStore
│   ├── remote/           # Retrofit Interfaces
│   └── repository/       # Repository Implementations
├── domain/               # Domain Layer (Optional)
│   ├── model/            # Business Models
│   └── repository/       # Repository Interfaces
├── ui/                   # Presentation Layer
│   ├── theme/            # Compose Theme, Color, Type
│   ├── components/       # Shared Composables
│   ├── auth/             # Feature: Auth
│   │   ├── LoginScreen.kt
│   │   └── AuthViewModel.kt
│   ├── home/             # Feature: Home
│   └── navigation/       # Navigation Graphs
```

2. `App.kt`
```kotlin
package com.example.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Timber, Firebase, Crashlytics, etc.
    }
}
```

3. Update `AndroidManifest.xml`:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:name=".App"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.App">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

4. `MainActivity.kt` setup with Compose and Hilt:
```kotlin
package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.app.ui.theme.AppTheme
import com.example.app.ui.navigation.AppNavigation

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                AppNavigation()
            }
        }
    }
}
```

⚠️ Common Pitfalls:
- Pitfall: Forgetting `@HiltAndroidApp` in `App.kt` or `@AndroidEntryPoint` in `MainActivity.kt`.
- Solution: Hilt will crash immediately on launch without these annotations.
- Pitfall: Hardcoding dependency versions in `build.gradle.kts` instead of the Version Catalog.
- Solution: Always add dependencies to `libs.versions.toml` first.
```

---

✅ **Verification Checklist:**
- [ ] `libs.versions.toml` is created and correctly linked.
- [ ] Gradle sync completes successfully.
- [ ] `App.kt` has `@HiltAndroidApp`.
- [ ] `AndroidManifest.xml` references `.App`.
- [ ] App builds and runs successfully, displaying a blank screen or basic theme.

---

📎 **Related Phases:**
- Proceeds to: [Phase M2: Navigation Architecture](./MOBILE_PHASE_2_NAVIGATION_ARCHITECTURE_Android_Architect.md)
