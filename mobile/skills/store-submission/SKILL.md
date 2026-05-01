---
name: store-submission
description: Use this skill when preparing for App Store or Google Play Store submission. Covers screenshot requirements, metadata optimization, review guidelines, and submission automation with EAS Submit.
origin: Mobile ECC
stack: EAS Submit, App Store Connect, Google Play Console
---

# App Store Submission Skill

## iOS App Store Requirements (2025)

### Screenshots
| Device | Size | Required |
|--------|------|----------|
| iPhone 6.9" (15 Pro Max) | 1320×2868 | Yes |
| iPhone 6.7" (14 Plus) | 1284×2778 | Yes (or use 6.9") |
| iPad 13" Pro | 2064×2752 | Required if iPad supported |
| iPad 11" Air | 1668×2388 | Optional |

**Rules:**
- 3-10 screenshots per device
- Show actual app UI (no device frames required)
- No misleading content
- First screenshot must be most compelling

### App Review Guidelines Highlights
1. **IAP**: Any digital goods/services MUST use StoreKit. No external payment links.
2. **Permissions**: Every permission must have a clear user benefit stated in the prompt.
3. **Privacy**: Must have a Privacy Policy URL.
4. **Login**: If login required, provide test credentials in review notes.
5. **Minimum functionality**: App must be more than a simple website wrapper.
6. **No placeholder content**: No Lorem Ipsum in screenshots or the app itself.

### Privacy Nutrition Label (Required)
Declare ALL data types collected:
- Contact info (name, email, phone)
- Health & fitness (if applicable)
- Financial info (payment details)
- Location (precise or coarse)
- Usage data (product interactions, crash data)
- Diagnostics (crash logs, performance data)

### Metadata Limits
| Field | Limit |
|-------|-------|
| App Name | 30 characters |
| Subtitle | 30 characters |
| Keywords | 100 characters (comma-separated) |
| Description | 4000 characters |
| Support URL | Required |
| Marketing URL | Optional |
| Privacy Policy URL | Required |

## Google Play Store Requirements (2025)

### Screenshots
| Device | Size |
|--------|------|
| Phone | Min 320px, max 3840px, ≤8 screenshots |
| Tablet 7" | Same size range (if supported) |
| Tablet 10" | Same size range (if supported) |
| Feature Graphic | 1024×500 JPG/PNG (required) |
| App Icon | 512×512 PNG (required) |

### API Level Requirements
- `targetSdkVersion` ≥ 34 (required for 2025 submissions)
- Must support 64-bit (AAB format handles this automatically)

### Data Safety Form (New for Android)
Must declare:
- Data types collected
- Whether data is shared with third parties
- Whether data is encrypted in transit
- Whether users can request data deletion

### Play Store Metadata Limits
| Field | Limit |
|-------|-------|
| App Name | 50 characters |
| Short Description | 80 characters |
| Full Description | 4000 characters |

## EAS Submit Automation

### Setup
```bash
# Configure App Store Connect
eas submit:configure --platform ios

# Configure Play Store
eas submit:configure --platform android
```

### Submit iOS
```bash
eas submit --profile production --platform ios
# Requires: APPLE_ID, ASC_APP_ID, APPLE_TEAM_ID as EAS Secrets
```

### Submit Android
```bash
eas submit --profile production --platform android
# Requires: Google Service Account JSON key
```

### Submit Both
```bash
eas submit --profile production --platform all --non-interactive
```

## ASO (App Store Optimization) Strategy

### Keyword Research
1. Research competitor app names and keywords
2. Use AppFollow, Sensor Tower, or AppFigures for keyword data
3. Target: High relevance + medium competition keywords
4. Include: Problem-focused keywords ("track my [X]") not just category keywords

### Review Prompt Best Practices
```typescript
import StoreReview from 'react-native-store-review'

// Prompt at the RIGHT moment:
// ✅ After user achieves first success
// ✅ After positive interaction (5th session)
// ❌ NOT on app launch
// ❌ NOT after crash or error

async function promptReviewAtRightMoment() {
  const sessionsCount = await getSessionCount()
  if (sessionsCount === 5) {
    const isAvailable = await StoreReview.isAvailable()
    if (isAvailable) StoreReview.requestReview()
  }
}
```

## Common Rejection Reasons & Fixes

| Rejection Reason | Fix |
|-----------------|-----|
| Missing NSXxxUsageDescription | Add to `app.json` infoPlist |
| External payment link | Remove ALL links to web checkout on iOS |
| No demo account | Add credentials to App Store review notes |
| Lorem Ipsum in app | Replace with real content |
| Guideline 5.1.1 — Data collection | Complete Privacy Nutrition Label |
| App too simple | Add more functionality or rich UI |
| Broken features | Test on real device, not just simulator |
| Missing Restore Purchases | Add RestorePurchases button to paywall |
| Privacy Policy not found | Verify URL is publicly accessible |

## Pre-Submission Final Checklist

```bash
# 1. Increment version
# In app.json: version, ios.buildNumber, android.versionCode

# 2. Typecheck
npx tsc --noEmit

# 3. Tests
npm test -- --passWithNoTests

# 4. Production build
eas build --profile production --platform all --non-interactive

# 5. Submit
eas submit --profile production --platform all --non-interactive
```
