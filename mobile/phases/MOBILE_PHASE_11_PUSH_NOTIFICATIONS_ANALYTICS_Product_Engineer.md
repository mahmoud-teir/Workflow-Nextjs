<a name="phase-m11"></a>
# 📌 MOBILE PHASE M11: PUSH NOTIFICATIONS & ANALYTICS (Product Engineer)

> **Rule:** Post Notifications in Android 13+ (API 33) require runtime permission. Never request permission automatically on first launch.

---

### Prompt M11.1: Firebase Cloud Messaging (FCM) Setup

```text
You are an Android Push Notification Engineer. Implement FCM for [AppName].

Requirements:
- Set up the Firebase dependencies in `libs.versions.toml`.
- Create a `FirebaseMessagingService` to handle background and foreground messages.
- Request the `POST_NOTIFICATIONS` permission (Android 13+) using the pattern from Phase M7.
- Save the FCM token to the backend using the Repository pattern.

Required Output Format: Provide complete code for:

1. Firebase Service `data/remote/FCMService.kt`:
```kotlin
package com.example.app.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.app.MainActivity
import com.example.app.R
import com.example.app.domain.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject lateinit var userRepository: UserRepository
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send the new token to your backend
        scope.launch {
            userRepository.updatePushToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        // Show notification manually if needed (Firebase handles it automatically if app is in background,
        // but this block runs if app is in foreground or it's a data-only payload).
        message.notification?.let {
            sendNotification(it.title, it.body, message.data)
        }
    }

    private fun sendNotification(title: String?, body: String?, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            // Pass deep link data
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = "default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Must be a white/transparent icon
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0+ requires a Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "General Notifications", NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
```

2. Update `AndroidManifest.xml`:
```xml
<service
    android:name=".data.remote.FCMService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>

<!-- Required for Android 13+ -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```
```

---

### Prompt M11.2: Analytics (PostHog Android SDK)

```text
You are a Mobile Analytics Engineer. Implement PostHog analytics for Android.

Requirements:
- Initialize PostHog in the `Application` class.
- Create a helper to track screen views and events.
- Respect opt-out settings if user denies tracking.

Required Output Format: Provide complete code:

1. Initialization in `App.kt`:
```kotlin
package com.example.app

import android.app.Application
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize PostHog
        val config = PostHogAndroidConfig(
            apiKey = BuildConfig.POSTHOG_API_KEY,
            host = "https://app.posthog.com" // or eu.posthog.com
        ).apply {
            captureApplicationLifecycleEvents = true
            captureDeepLinks = true
            captureScreenViews = false // We handle this manually in Compose
        }
        PostHogAndroid.setup(this, config)
    }
}
```

2. Compose Screen Tracking Helper `ui/components/ScreenTracker.kt`:
```kotlin
package com.example.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.posthog.PostHog

@Composable
fun TrackScreen(screenName: String) {
    DisposableEffect(screenName) {
        PostHog.screen(screenTitle = screenName)
        onDispose { }
    }
}

// Usage in a Composable:
// @Composable fun HomeScreen() {
//     TrackScreen("Home")
//     ...
// }
```
```

---

✅ **Verification Checklist:**
- [ ] Notification permission (Android 13+) is requested contextually (e.g., after onboarding).
- [ ] Token is successfully retrieved via `FirebaseMessaging.getInstance().token` or `onNewToken`.
- [ ] App receives background notifications and clicking them opens the app.
- [ ] `TrackScreen` fires events in PostHog.

---

📎 **Related Phases:**
- Prerequisites: [Phase M10: Performance](./MOBILE_PHASE_10_PERFORMANCE_OPTIMIZATION_Mobile_Developer.md)
- Proceeds to: [Phase M14: App Store Launch](./MOBILE_PHASE_14_APP_STORE_SUBMISSION_LAUNCH_All_Roles.md)
