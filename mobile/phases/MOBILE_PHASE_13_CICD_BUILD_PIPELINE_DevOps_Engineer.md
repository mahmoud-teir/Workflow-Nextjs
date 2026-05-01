<a name="phase-m13"></a>
# 📌 MOBILE PHASE M13: CI/CD & BUILD PIPELINE (DevOps Engineer)

> **Build System:** EAS Build (Expo Application Services) — the official, managed build infrastructure for Expo projects.

---

### Prompt M13.1: EAS Build Pipeline

```text
You are a Mobile DevOps Engineer. Set up EAS Build with GitHub Actions for [AppName].

Build targets:
- Development Build: Internal testing with development client
- Preview Build: Internal distribution for QA / stakeholder review
- Production Build: App Store (iOS .ipa) + Play Store (Android .aab)

Required Output Format: Provide complete configuration for:

1. Verify `eas.json` is complete (from Phase M1.2):
```json
{
  "cli": { "version": ">= 14.0.0", "requireCommit": true },
  "build": {
    "development": {
      "developmentClient": true,
      "distribution": "internal",
      "ios": { "simulator": true },
      "android": { "buildType": "apk", "gradleCommand": ":app:assembleDebug" },
      "channel": "development"
    },
    "preview": {
      "distribution": "internal",
      "channel": "preview",
      "ios": { "buildConfiguration": "Release" },
      "android": { "buildType": "apk" }
    },
    "production": {
      "distribution": "store",
      "channel": "production",
      "ios": { "buildConfiguration": "Release", "buildNumber": "auto" },
      "android": { "buildType": "app-bundle", "versionCode": "auto" }
    }
  },
  "submit": {
    "production": {
      "ios": {
        "appleId": "$APPLE_ID",
        "ascAppId": "$ASC_APP_ID",
        "appleTeamId": "$APPLE_TEAM_ID"
      },
      "android": {
        "serviceAccountKeyPath": "$GOOGLE_SERVICE_ACCOUNT_KEY_PATH",
        "track": "internal"
      }
    }
  }
}
```

2. GitHub Actions workflow `.github/workflows/eas-build.yml`:
```yaml
name: EAS Build & Submit

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  lint-and-test:
    name: Lint, Type-check & Test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '22', cache: 'npm' }
      - run: npm ci
      - run: npx tsc --noEmit
      - run: npm test -- --coverage --passWithNoTests
      - name: Upload coverage
        uses: codecov/codecov-action@v4
        if: always()

  build-preview:
    name: EAS Preview Build
    runs-on: ubuntu-latest
    needs: lint-and-test
    if: github.event_name == 'pull_request'
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '22', cache: 'npm' }
      - run: npm ci
      - uses: expo/expo-github-action@v8
        with:
          eas-version: latest
          token: ${{ secrets.EXPO_TOKEN }}
      - name: Build preview (Android)
        run: eas build --profile preview --platform android --non-interactive
        env:
          EXPO_PUBLIC_API_URL: ${{ secrets.PREVIEW_API_URL }}

  build-production:
    name: EAS Production Build & Submit
    runs-on: ubuntu-latest
    needs: lint-and-test
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '22', cache: 'npm' }
      - run: npm ci
      - uses: expo/expo-github-action@v8
        with:
          eas-version: latest
          token: ${{ secrets.EXPO_TOKEN }}
      - name: Build production (iOS + Android)
        run: eas build --profile production --platform all --non-interactive
        env:
          EXPO_PUBLIC_API_URL: ${{ secrets.PRODUCTION_API_URL }}
      - name: Submit to stores
        run: eas submit --profile production --platform all --non-interactive
```

3. EAS Update (OTA) workflow `.github/workflows/eas-update.yml`:
```yaml
name: EAS Update (OTA)

on:
  push:
    branches: [main]

jobs:
  update:
    name: Publish OTA Update
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '22', cache: 'npm' }
      - run: npm ci
      - uses: expo/expo-github-action@v8
        with: { eas-version: latest, token: '${{ secrets.EXPO_TOKEN }}' }
      - name: Publish update
        run: eas update --channel production --message "OTA: ${{ github.event.head_commit.message }}"
```

4. Required GitHub Secrets:
```
EXPO_TOKEN                 # From expo.dev account settings
APPLE_ID                   # Apple ID email
ASC_APP_ID                 # App Store Connect App ID
APPLE_TEAM_ID              # Apple Developer Team ID
GOOGLE_SERVICE_ACCOUNT_KEY # Base64-encoded JSON key
PREVIEW_API_URL            # Preview backend URL
PRODUCTION_API_URL         # Production backend URL
```

⚠️ Common Pitfalls:
- Pitfall: `requireCommit: true` in eas.json failing on CI because of uncommitted changes.
- Solution: Ensure the CI checkout step uses full git history (`fetch-depth: 0`).
- Pitfall: EAS Build timeout on long-running iOS builds.
- Solution: Use `--non-interactive` flag and EAS Build's resource class `large` for faster builds.
```

✅ **Verification Checklist:**
- [ ] Development build installs on physical device.
- [ ] Preview build distributes via EAS internal distribution link.
- [ ] Production build creates `.ipa` and `.aab` artifacts.
- [ ] OTA update publishes and app receives it within 5 minutes.
- [ ] GitHub Actions pipeline passes green on PR.

---

### Prompt M13.2: OTA Updates Strategy

```text
You are a Mobile OTA Update Architect. Design the OTA update strategy for [AppName].

EAS Update enables pushing JS/asset changes without App Store review.
CANNOT be updated via OTA: native code changes, new native modules, app.json config.

Update channels:
- development: Local testing
- preview: QA internal testing
- production: Live users

Required Output Format: Provide complete implementation for:

1. Update channel strategy:
```
main branch push → production channel OTA
PR → preview channel OTA (QA testing)
feature branch → development channel (local)
```

2. In-app update check `lib/updates/index.ts`:
```typescript
import * as Updates from 'expo-updates'
import { Alert } from 'react-native'

export async function checkForOTAUpdate(): Promise<void> {
  if (__DEV__) return  // No OTA in development

  try {
    const update = await Updates.checkForUpdateAsync()
    if (!update.isAvailable) return

    await Updates.fetchUpdateAsync()

    Alert.alert(
      'Update Available',
      'A new version is ready. Restart now for the latest features.',
      [
        { text: 'Later', style: 'cancel' },
        { text: 'Restart', onPress: () => Updates.reloadAsync() },
      ]
    )
  } catch (error) {
    // Silently fail — don't interrupt the user for update errors
    console.warn('OTA check failed:', error)
  }
}
```

3. Trigger update check on app foreground:
```typescript
import { AppState } from 'react-native'
import { checkForOTAUpdate } from '@/lib/updates'

// In app/_layout.tsx
useEffect(() => {
  checkForOTAUpdate()  // Check on initial load

  const subscription = AppState.addEventListener('change', (state) => {
    if (state === 'active') checkForOTAUpdate()  // Check when returning from background
  })
  return () => subscription.remove()
}, [])
```

4. What can and cannot be OTA updated:
```
✅ CAN OTA update:
- JavaScript logic changes
- UI changes (React Native components)
- Asset changes (images, fonts)
- API URL changes
- Feature flags

❌ CANNOT OTA update (requires new native build):
- New Expo plugins (e.g., adding expo-camera)
- Native code changes (Swift, Kotlin, Objective-C, Java)
- Changes to app.json (icons, splash, permissions)
- New native npm packages with native modules
- SDK version upgrades
```
```

✅ **Verification Checklist:**
- [ ] OTA update check runs on app foreground (not just cold start).
- [ ] Update alert shows with "Restart" and "Later" options.
- [ ] JS bundle change (e.g., text update) deploys via OTA without native rebuild.
- [ ] Native module changes trigger a full EAS build (not just OTA).

---

📎 **Related Phases:**
- Prerequisites: [Phase M11: Notifications & Analytics](./MOBILE_PHASE_11_PUSH_NOTIFICATIONS_ANALYTICS_Product_Engineer.md)
- Proceeds to: [Phase M14: App Store Launch](./MOBILE_PHASE_14_APP_STORE_SUBMISSION_LAUNCH_All_Roles.md)
