<a name="phase-m14"></a>
# 📌 MOBILE PHASE M14: APP STORE SUBMISSION & LAUNCH (All Roles)

> **Pre-Launch Gate:** Do not submit until ALL items in the Phase M14 Completion Gate are checked. App Store review takes 1-3 days. Play Store review takes 1-7 days. Rejections reset the clock.

---

### Prompt M14.1: App Store Connect (iOS) Submission Checklist

```text
You are an App Store Submission Specialist. Prepare [AppName] for iOS App Store submission.

Required Output Format: A complete submission checklist:

## 1. Developer Account Setup
- [ ] Apple Developer Program membership active ($99/year)
- [ ] App ID created in Apple Developer Portal (matching bundle identifier)
- [ ] Certificates and provisioning profiles configured in EAS
- [ ] App Store Connect record created with correct Bundle ID

## 2. App Information (App Store Connect)
### Basic Info
- [ ] App Name: [Must be unique, ≤30 characters]
- [ ] Subtitle: [≤30 characters — key value proposition]
- [ ] Bundle ID: com.[company].[appname]
- [ ] SKU: [unique identifier for your records]
- [ ] Primary Language: [English/other]
- [ ] Primary Category: [e.g., Productivity / Health & Fitness]
- [ ] Secondary Category: [optional]

### App Privacy (Privacy Nutrition Label — REQUIRED)
- [ ] Data types collected declared (location, contacts, usage data, etc.)
- [ ] Purpose of collection stated for each data type
- [ ] Whether data is linked to identity
- [ ] Privacy Policy URL: https://[domain]/privacy

## 3. Ratings & Content
- [ ] Age Rating completed (questionnaire in App Store Connect)
- [ ] Content appropriate for declared age group
- [ ] No hidden or restricted features for reviewers

## 4. App Review Information
- [ ] Demo Account credentials provided (if app requires login)
  - Username: review@[domain].com
  - Password: [strong-password]
- [ ] Review Notes explaining any non-obvious features
- [ ] Contact information for reviewer questions

## 5. App Store Listing (ASO)
### Screenshots (REQUIRED SIZES)
- [ ] iPhone 6.9" (iPhone 15 Pro Max): 1320×2868px — 3-10 screenshots
- [ ] iPhone 6.7" (iPhone 14 Plus): 1284×2778px — 3-10 screenshots
- [ ] iPad 13" (iPad Pro): 2064×2752px — 3-10 screenshots (if iPad supported)
- [ ] Screenshots show actual app UI (no mockup frames)
- [ ] First screenshot shows the most compelling feature

### App Preview Video (Optional but recommended)
- [ ] 15-30 seconds, no audio required
- [ ] Shows real app UI, not animated graphics
- [ ] Correct dimensions for target device

### Description
- [ ] First 3 lines (above "more"): Hook, value prop, CTA
- [ ] Full description ≤4000 characters
- [ ] Keywords ≤100 characters (comma-separated, no spaces)
- [ ] No competitor names in keywords (review rejection risk)

## 6. Technical Requirements
- [ ] Minimum iOS version set (recommend iOS 16+)
- [ ] Supports latest iOS SDK
- [ ] No use of private APIs (App Store rejection)
- [ ] All HTTP requests use HTTPS (ATS compliance)
- [ ] Crash-free rate >99.5% in TestFlight beta

## 7. Subscription Compliance (if applicable)
- [ ] "Restore Purchases" button visible
- [ ] Subscription terms clearly displayed near purchase button
- [ ] Auto-renewal disclosure present
- [ ] Cancellation instructions mentioned

## 8. TestFlight Beta Testing
- [ ] Internal testers (team) have tested all flows
- [ ] External beta (100+ users) completed if available
- [ ] All TestFlight feedback addressed
- [ ] Crash-free session rate >99%
```

---

### Prompt M14.2: Google Play Store Submission Checklist

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
- [ ] Data Safety form completed (new for Android — must match actual data collection)
  - Data collected, shared, encrypted, and deletion options declared
- [ ] Target audience declared (not targeting children unless COPPA compliant)

## 4. Content Rating
- [ ] Content rating questionnaire completed (similar to iOS age rating)
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
- [ ] 64-bit support (required — AAB handles this automatically)

## 7. Android-Specific Requirements
- [ ] Google Play Billing used for any in-app purchases (no external payment links)
- [ ] App permissions justified in the listing if using: SMS, Call Log, Contacts, Location
- [ ] Background location requires separate permission justification form
```

---

### Prompt M14.3: App Store Optimization (ASO)

```text
You are an ASO (App Store Optimization) Specialist. Optimize [AppName]'s discoverability.

App Category: [your category]
Primary users: [from PRD personas]
Key competitors: [list 2-3 competitor apps]

Required Output Format:

## 1. Keyword Research
Generate 3 keyword tiers:
- Tier 1 (High intent, high volume): [5 keywords]
- Tier 2 (Medium intent, medium volume): [10 keywords]
- Tier 3 (Long-tail, low competition): [15 keywords]

Rules:
- Include the app's primary use case keyword
- Include problem-focused keywords (e.g., "track my calories" not just "calorie tracker")
- Avoid competitor brand names
- iOS keywords field: 100 character limit, comma-separated, no spaces

## 2. App Name & Subtitle Optimization
- App Name should include the primary keyword
- Subtitle should communicate the value proposition
- Example: "[AppName]: [Primary Keyword]" / "[Value Prop in ≤30 chars]"

## 3. Screenshot Strategy
First screenshot (most important):
- Show the core value in the first 2 seconds of viewing
- Include a compelling headline overlay
- Use your primary brand color as the background

Remaining screenshots:
- Screenshot 2: Second most valuable feature
- Screenshot 3: Social proof / numbers / stats
- Screenshot 4: Key differentiator from competitors
- Screenshot 5: Delight / premium feeling moment

## 4. Rating & Review Strategy
- [ ] In-app rating prompt implemented (at the right moment — after user achieves something)
- [ ] Prompt timing: [after user completes their first success action, not on launch]
- [ ] Handle negative feedback: route to support email first

```typescript
import { requestReview } from 'expo-store-review'
import StoreReview from 'react-native-store-review'

// Request review at the right moment
async function promptReview() {
  const isAvailable = await StoreReview.isAvailable()
  if (isAvailable) {
    StoreReview.requestReview()
  }
}

// Call after user first completes core action:
await markPostAsComplete()
promptReview()  // Show review request after success
```
```

---

### Prompt M14.4: Launch Checklist — Final Gate

```text
Run this checklist BEFORE submitting to App Store / Play Store:

## 🔴 Blockers (must fix)
- [ ] All Maestro E2E flows pass on physical iOS device
- [ ] All Maestro E2E flows pass on physical Android device
- [ ] Cold start < 3 seconds on mid-range Android (Pixel 6a or similar)
- [ ] No crashes in TestFlight beta / Play internal testing (>99.5% crash-free)
- [ ] HTTPS enforced on all API calls
- [ ] No sensitive data in AsyncStorage (only SecureStore)
- [ ] Restore Purchases works (iOS requirement)
- [ ] Privacy Policy accessible from the app AND App Store listing
- [ ] All required permissions have NSXxxUsageDescription strings
- [ ] Demo credentials provided for App Store reviewer
- [ ] No Lorem Ipsum in the app
- [ ] App works fully offline for core features

## 🟡 Important (fix before launch week)
- [ ] App rating prompt implemented (post-success, not on launch)
- [ ] Push notification opt-in rate tracked
- [ ] Analytics initialized (PostHog) for post-launch monitoring
- [ ] Error tracking set up (Sentry for React Native)
- [ ] Support email / help center URL accessible from app settings
- [ ] EAS Update configured for rapid hotfix capability
- [ ] At least 5 screenshots per device size

## 🟢 Nice-to-Have (can do post-launch)
- [ ] App Preview Video on App Store listing
- [ ] Localized listings for top markets
- [ ] App Clips / Instant Apps configured
- [ ] Widget extension

## Post-Launch Monitoring
- Monitor crash-free rate: should stay >99.5%
- Monitor Day-1 retention: target >40%
- Monitor App Store rating: target >4.5★
- First OTA update ready: prepare a hotfix within 24h if critical bugs discovered
- Social media / community engagement post ready for launch day
```

✅ **Phase M14 Completion Gate:**
App is ready to submit when:
- [ ] All 🔴 Blockers resolved
- [ ] Production EAS Build successful (iOS .ipa + Android .aab)
- [ ] App Store Connect record fully complete
- [ ] Google Play Store listing fully complete
- [ ] TestFlight beta + Play internal testing completed

---

## 🤖 ECC Agent Support for Launch

```text
Use these agents for final launch preparation:

/store-specialist — Full App Store & Play Store submission workflow
/mobile-security  — Final security audit before submission
/rn-reviewer      — Code review for any last-minute fixes
/eas-builder      — Build pipeline verification

Run the verification loop:
→ npm test -- --coverage (≥80%)
→ maestro test .maestro/ (all flows pass)
→ npx tsc --noEmit (no type errors)
→ eas build --profile production (successful build)
→ /store-specialist (submission checklist)
```

---

📎 **Phase M14 is the final phase.** After launch:
- Monitor crash rates and ratings daily for the first 2 weeks.
- Prepare OTA hotfixes for critical bugs.
- Plan your first feature update based on user feedback.
- Return to Phase M11 for push notification campaigns.
- Return to Phase M12 for subscription optimization.
