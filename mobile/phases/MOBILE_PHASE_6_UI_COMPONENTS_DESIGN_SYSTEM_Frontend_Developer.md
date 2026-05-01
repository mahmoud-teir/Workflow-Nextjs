<a name="phase-m6"></a>
# 📌 MOBILE PHASE M6: UI COMPONENTS & DESIGN SYSTEM (UI Engineer)

> **Rule:** Use Material Design 3 (`androidx.compose.material3`). Never hardcode hex colors or text sizes directly in Modifiers; always use `MaterialTheme.colorScheme` and `MaterialTheme.typography`.

---

### Prompt M6.1: Compose Theme Setup (Material 3)

```text
You are a Lead Compose UI Engineer. Define the Jetpack Compose Material 3 theme for [AppName].

Requirements:
- Define Light and Dark color schemes.
- Define a custom Typography scale using a Google Font (e.g., via `google-fonts` provider or bundled TTF).
- Provide the main `AppTheme` composable wrapper.

Required Output Format: Provide complete code for:

1. Colors `ui/theme/Color.kt`:
```kotlin
package com.example.app.ui.theme

import androidx.compose.ui.graphics.Color

val PrimaryLight = Color(0xFF0061A4)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFD1E4FF)
// ... define rest of M3 tokens

val PrimaryDark = Color(0xFF9ECAFF)
val OnPrimaryDark = Color(0xFF003258)
val PrimaryContainerDark = Color(0xFF00497D)
// ... define rest of M3 tokens
```

2. Theme `ui/theme/Theme.kt`:
```kotlin
package com.example.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    // ...
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    // ...
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set true to allow Android 12+ wallpaper colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

⚠️ Common Pitfalls:
- Pitfall: Forgetting to update the Status Bar color to match the Compose theme background.
- Solution: The `SideEffect` block with `WindowCompat` handles this gracefully.
```

---

### Prompt M6.2: Core UI Components

```text
You are a Compose UI Specialist. Create 3 reusable core components using Material 3.

Components to create:
1. `PrimaryButton` — wraps `Button` with standard padding and a loading state.
2. `AppTextField` — wraps `OutlinedTextField` with error state handling and standard styling.
3. `EmptyStateView` — a reusable component for lists with no data (Icon + Text + Action Button).

Required Output Format: Provide complete code for `ui/components/CoreComponents.kt`:

```kotlin
package com.example.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text)
        }
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
// Add EmptyStateView similarly...
```
```

---

### Prompt M6.3: Compose Animations

```text
You are an Android Animation Expert. Implement a standard transition and a layout animation in Compose.

Requirements:
- Create an `AnimatedVisibility` wrapper for items entering/exiting a list.
- Show how to animate a state change (e.g., a color transitioning smoothly).

Required Output Format: Provide code snippets demonstrating:

```kotlin
// 1. Enter / Exit Animations (e.g., for a Toast or Card)
@Composable
fun FadingCard(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card { /* Content */ }
    }
}

// 2. State-driven Animation
@Composable
fun ToggleHeart(isLiked: Boolean, onToggle: () -> Unit) {
    // Animates color smoothly when state changes
    val tint by animateColorAsState(
        targetValue = if (isLiked) Color.Red else Color.Gray,
        label = "heartColor"
    )
    
    IconButton(onClick = onToggle) {
        Icon(Icons.Default.Favorite, contentDescription = null, tint = tint)
    }
}
```

⚠️ Common Pitfalls:
- Pitfall: Triggering animations inside the View layer based on transient events instead of State.
- Solution: Compose animations are driven by State variables. Always declare a state (`val visible by ...`) and use `animate*AsState` or `AnimatedVisibility`.
```

---

✅ **Verification Checklist:**
- [ ] Dark mode and Light mode switch correctly based on system settings.
- [ ] Status bar color matches the theme background.
- [ ] `PrimaryButton` shows a spinner when `isLoading = true` and is unclickable.
- [ ] Animations use `animateXAsState` rather than legacy View Property Animators.

---

📎 **Related Phases:**
- Prerequisites: [Phase M5: Security](./MOBILE_PHASE_5_AUTHENTICATION_SECURITY_Security_Expert.md)
- Proceeds to: [Phase M7: Native Features & APIs](./MOBILE_PHASE_7_NATIVE_FEATURES_APIs_Android_Developer.md)
