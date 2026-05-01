<a name="phase-m14"></a>
# 📌 MOBILE PHASE M14: PLAY STORE SUBMISSION & LAUNCH (All Roles)

> **Pre-Launch Gate:** Do not submit until ALL items in the Phase M14 Completion Gate are checked. Google Play review takes 1-7 days. 

---

### Prompt M14.1: Google Play Store Submission Checklist

```text
You are a Google Play Store Submission Specialist. Prepare [AppName] for Android Play Store submission.

Required Output Format: A complete submission checklist:

## 1. Google Play Console Setup
- [ ] Google Play Developer account active ($25 one-time fee)
- [ ] App created in Play Console
- [ ] App type declared: App or Game
- [ ] Free/Paid declared upfront (cannot change Free → Paid later)

## 2. App Content
### Store Listing
- [ ] App Name: [≤50 characters]
- [ ] Short Description: [≤80 characters]
- [ ] Full Description: [≤4000 characters]
- [ ] App Icon: 512×512 PNG (no transparency)
- [ ] Feature Graphic: 1024×500 JPG/PNG (shown at top of listing)

### Screenshots (REQUIRED)
- [ ] Phone: 2-8 screenshots (min 320px, max 3840px, aspect ratio 16:9 or 9:16)
- [ ] Tablet 7": 1-8 screenshots (if tablet supported)
- [ ] Tablet 10": 1-8 screenshots (if tablet supported)

## 3. Privacy & Security
- [ ] Privacy Policy URL provided
- [ ] Data Safety form completed
  - Data collected, shared, encrypted, and deletion options declared
- [ ] Target audience declared (not targeting children unless COPPA compliant)

## 4. Content Rating
- [ ] Content rating questionnaire completed
- [ ] Rating received and applied

## 5. App Release Track
- [ ] Internal testing track (team, up to 100 users)
- [ ] Closed testing (alpha) — limited beta users
- [ ] Open testing (beta) — public beta
- [ ] Production — full release

Start with Internal → Closed → Production for safety.

## 6. Target API Level
- [ ] targetSdkVersion ≥ 34 (required for Play Store 2025)
- [ ] compileSdkVersion ≥ 34
- [ ] Minified and obfuscated (R8 enabled in `build.gradle.kts`)

## 7. Android-Specific Requirements
- [ ] Google Play Billing used for any in-app purchases (no external payment links)
- [ ] App permissions justified in the listing if using: SMS, Call Log, Location
- [ ] Background location requires a separate permission justification form and video
```

---

### Prompt M14.2: App Store Optimization (ASO) & In-App Review

```text
You are an ASO Specialist. Optimize [AppName]'s discoverability and implement the Google Play In-App Review API.

Required Output Format: Provide the implementation code for In-App Reviews:

```kotlin
import com.google.android.play.core.review.ReviewManagerFactory
import android.app.Activity

// Call this function at the RIGHT moment (e.g., after the user completes a core success action, NOT on launch).
fun promptPlayStoreReview(activity: Activity) {
    val reviewManager = ReviewManagerFactory.create(activity)
    val request = reviewManager.requestReviewFlow()
    
    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            // We got the ReviewInfo object
            val reviewInfo = task.result
            val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
            flow.addOnCompleteListener { _ ->
                // The flow has finished. The API does not indicate whether the user 
                // reviewed or not, or even whether the review dialog was shown.
                // Continue app flow normally.
            }
        } else {
            // There was some problem, continue regardless of the result.
        }
    }
}
```

**ASO Rules:**
- App Name should include the primary keyword
- Short Description should communicate the value proposition
- Screenshot 1: Show the core value instantly
- Screenshot 2: Social proof / numbers
- Screenshot 3: Delight / premium feeling moment
```

---

### Prompt M14.3: Launch Checklist — Final Gate

```text
Run this checklist BEFORE submitting to the Play Store:

## 🔴 Blockers (must fix)
- [ ] App bundles as an `.aab` file (not `.apk`)
- [ ] Maestro E2E flows pass on a release build
- [ ] No crashes in Internal testing track
- [ ] HTTPS enforced on all API calls (NetworkSecurityConfig)
- [ ] No sensitive data in raw SharedPreferences (only EncryptedSharedPreferences)
- [ ] Privacy Policy accessible from the app AND Play Store listing
- [ ] Demo credentials provided in the Play Console App Access section
- [ ] Works gracefully offline (does not crash without internet)

## 🟡 Important (fix before launch week)
- [ ] App rating prompt implemented (post-success, not on launch)
- [ ] Push notification permission requested smoothly
- [ ] Crashlytics/PostHog initialized for monitoring
- [ ] Support email / help center URL accessible from app settings

## Post-Launch Monitoring
- Monitor Android Vitals in Play Console for ANRs and Crash rates.
- Target Crash-free session rate > 99%.
- Monitor Day-1 retention.
```

✅ **Phase M14 Completion Gate:**
App is ready to submit when:
- [ ] All 🔴 Blockers resolved
- [ ] Fastlane `build_release` successful
- [ ] Google Play Store listing fully complete
- [ ] Data Safety form verified against actual code

---

## 🤖 ECC Agent Support for Launch

```text
Use these agents for final launch preparation:

/store-specialist — Full Play Store submission workflow
/mobile-security  — Final security audit before submission
/fastlane-builder — Build pipeline execution

Run the verification loop:
→ ./gradlew detekt ktlintCheck
→ ./gradlew testDebugUnitTest
→ maestro test .maestro/
→ bundle exec fastlane build_release
```
