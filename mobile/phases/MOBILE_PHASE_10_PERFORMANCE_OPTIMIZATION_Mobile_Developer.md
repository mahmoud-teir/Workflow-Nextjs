<a name="phase-m10"></a>
# 📌 MOBILE PHASE M10: PERFORMANCE OPTIMIZATION (Performance Specialist)

> **Rule:** Never measure Android performance in Debug mode. Debug mode contains heavy tooling overhead. Only measure performance in a `Release` build with R8 enabled.

---

### Prompt M10.1: Compose List Optimization (LazyColumn)

```text
You are a Jetpack Compose Performance Expert. Optimize the main list view.

Requirements:
- Ensure all items in `LazyColumn` use a stable `key`.
- Verify that item content type is used if lists have varying UI components.
- Ensure the item data class is `@Stable` or immutable.

Required Output Format: Provide complete optimized code:

```kotlin
// 1. Data classes must be immutable (val only) and ideally primitive or String
@Immutable
data class FeedItem(
    val id: String,
    val title: String,
    val type: Int
)

// 2. LazyColumn implementation
@Composable
fun FeedList(items: List<FeedItem>, onUserClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = items,
            // REQUIRED: Provide a unique key to prevent recomposition when order changes
            key = { item -> item.id },
            // OPTIONAL: Provide contentType to pool composables efficiently
            contentType = { item -> item.type }
        ) { item ->
            // Use remember to avoid allocating new lambdas on every recomposition
            val currentOnClick = rememberUpdatedState(onUserClick)
            
            FeedCard(
                item = item,
                onClick = { currentOnClick.value(item.id) }
            )
        }
    }
}
```

⚠️ Common Pitfalls:
- Pitfall: `LazyColumn` scrolling drops frames (<60fps).
- Solution: Missing `key` means Compose destroys and recreates every item on scroll instead of moving them.
- Pitfall: Passing a `List<T>` to a Composable where `T` is an interface or class from another module not marked `@Stable`.
- Solution: Compose treats generic `List` as unstable. Wrap it in a stable class or use KotlinX Immutable Collections (`ImmutableList`).
```

---

### Prompt M10.2: R8 / ProGuard Setup

```text
You are an Android DevOps Engineer. Configure R8 (ProGuard) to shrink and optimize the app.

Requirements:
- Enable `isMinifyEnabled` and `isShrinkResources` in `build.gradle.kts`.
- Provide basic ProGuard rules for common libraries (Retrofit, Coroutines).

Required Output Format:

1. Update `app/build.gradle.kts`:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        // Optionally configure a custom signing config here
    }
}
```

2. Standard `app/proguard-rules.pro`:
```text
# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-keep,allowoptimization class * {
    @kotlinx.serialization.Serializable *;
    @kotlinx.serialization.Serializer *;
}

# Dagger Hilt
-keep,allowobfuscation,allowshrinking interface dagger.hilt.internal.GeneratedEntryPoint
```
```

---

### Prompt M10.3: Baseline Profiles (Cold Start Optimization)

```text
You are an Android Performance Engineer. Implement Baseline Profiles to pre-compile the app's critical path, reducing cold start times by up to 30%.

Requirements:
- Use the Macrobenchmark and Baseline Profile Gradle plugin.
- Create a generator class to trace the main app launch.

Required Output Format: Provide instructions and code:

1. Add the Baseline Profile module using Android Studio:
   `File -> New -> New Module -> Baseline Profile Generator`
   
2. The Generator Code (in the new module):
```kotlin
package com.example.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineRule = BaselineProfileRule()

    @Test
    fun generateProfile() {
        baselineRule.collect(
            packageName = "com.example.app",
            profileBlock = {
                // 1. Start the app
                pressHome()
                startActivityAndWait()

                // 2. Perform critical path actions (e.g., scrolling main list)
                device.waitForIdle()
                val list = device.findObject(By.res("main_list"))
                if (list != null) {
                    list.setGestureMargin(device.displayWidth / 5)
                    list.scroll(androidx.test.uiautomator.Direction.DOWN, 1f)
                }
            }
        )
    }
}
```

3. Generate the profile:
   Run the `generateBaselineProfile` Gradle task. The output `baseline-prof.txt` will be automatically placed in `app/src/main/baselineProfiles/`.
```

---

✅ **Verification Checklist:**
- [ ] No `LazyColumn` items are missing `key` properties.
- [ ] Running a Release build (`./gradlew assembleRelease`) successfully completes without missing class crashes (R8 rules are correct).
- [ ] Baseline Profiles are generated and included in the APK.

---

📎 **Related Phases:**
- Prerequisites: [Phase M9: Testing](./MOBILE_PHASE_9_TESTING_QA_QA_Engineer.md)
- Proceeds to: [Phase M11: Push Notifications & Analytics](./MOBILE_PHASE_11_PUSH_NOTIFICATIONS_ANALYTICS_Product_Engineer.md)
