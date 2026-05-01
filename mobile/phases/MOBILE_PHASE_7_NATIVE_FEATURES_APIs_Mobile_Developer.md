<a name="phase-m7"></a>
# 📌 MOBILE PHASE M7: NATIVE FEATURES & APIs (Android Developer)

> **Rule:** All dangerous permissions MUST be requested at runtime using the `ActivityResultContracts` API. Never request permissions on app launch. Provide a rationale before requesting.

---

### Prompt M7.1: Permissions Flow (Accompanist / ActivityResult)

```text
You are an Android Developer. Implement a reusable permission flow for [AppName].

Requirements:
- Use `rememberLauncherForActivityResult` with `RequestPermission` or `RequestMultiplePermissions`.
- Handle the rationale dialog (showing *why* the app needs the permission before the OS prompt).
- Handle the "Permanently Denied" state (redirect to Settings).

Required Output Format: Provide complete code for:

1. Permission Wrapper Composable `ui/components/PermissionRequest.kt`:
```kotlin
package com.example.app.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun PermissionRequester(
    permission: String,
    rationaleTitle: String,
    rationaleMessage: String,
    onGranted: () -> Unit,
    content: @Composable (requestPermission: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onGranted()
        } else {
            // If denied but shouldn't show rationale, they permanently denied it
            showSettingsDialog = true
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(rationaleTitle) },
            text = { Text(rationaleMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    launcher.launch(permission)
                }) { Text("Allow") }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) { Text("Cancel") }
            }
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Permission Required") },
            text = { Text("You have permanently denied this permission. Please enable it in Settings.") },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    context.openAppSettings()
                }) { Text("Open Settings") }
            }
        )
    }

    content {
        // Provide the trigger back to the caller
        showRationale = true
    }
}

fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivity(intent)
}
```

⚠️ Common Pitfalls:
- Pitfall: Requesting permissions directly without showing a rationale first.
- Solution: Always explain *why* you need the permission in your own UI before triggering the OS dialog. OS dialogs cannot be triggered repeatedly if denied.
```

---

### Prompt M7.2: CameraX Integration

```text
You are an Android Developer. Implement a custom Camera view using CameraX in Compose.

Requirements:
- Use `androidx.camera:camera-camera2` and `camera-view`.
- Implement a `PreviewView` inside an `AndroidView` composable.
- Provide a capture button that takes a photo and saves it to a temporary file.

Required Output Format: Provide complete code for `ui/features/camera/CameraScreen.kt`:

```kotlin
package com.example.app.ui.features.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.Executor

@Composable
fun CameraScreen(onPhotoCaptured: (File) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    imageCapture = ImageCapture.Builder().build()

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageCapture
                        )
                    } catch (exc: Exception) {
                        // Handle binding failure
                    }
                }, executor)
                
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Button(
            onClick = { 
                takePhoto(context, imageCapture, ContextCompat.getMainExecutor(context), onPhotoCaptured) 
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp)
        ) {
            Text("Capture")
        }
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    executor: Executor,
    onPhotoCaptured: (File) -> Unit
) {
    val photoFile = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture?.takePicture(
        outputOptions, executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onPhotoCaptured(photoFile)
            }
            override fun onError(exc: ImageCaptureException) {
                // Handle error
            }
        }
    )
}
```
```

---

### Prompt M7.3: Haptic Feedback & Platform APIs

```text
You are a Compose Developer. Implement Compose-native Haptic Feedback and system Share intents.

Required Output Format: Provide code snippets for:

1. Haptic Feedback (Vibration):
```kotlin
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun HapticButton() {
    val haptic = LocalHapticFeedback.current
    Button(onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        // do action
    }) { Text("Long Press Me") }
}
```

2. Native Share Sheet:
```kotlin
import android.content.Intent
import androidx.compose.ui.platform.LocalContext

@Composable
fun ShareButton(textToShare: String) {
    val context = LocalContext.current
    
    Button(onClick = {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, textToShare)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }) { Text("Share") }
}
```
```

---

✅ **Verification Checklist:**
- [ ] Denying a permission shows the Rationale Dialog on next attempt.
- [ ] Selecting "Don't ask again" handles gracefully (navigates to Settings).
- [ ] CameraX preview displays correctly without rotation/stretching issues.
- [ ] `AndroidView` correctly ties into Compose Lifecycle (`LocalLifecycleOwner`).

---

📎 **Related Phases:**
- Prerequisites: [Phase M6: UI Components](./MOBILE_PHASE_6_UI_COMPONENTS_DESIGN_SYSTEM_Frontend_Developer.md)
- Proceeds to: [Phase M8: State Management](./MOBILE_PHASE_8_STATE_MANAGEMENT_Full_Stack_Mobile.md)
