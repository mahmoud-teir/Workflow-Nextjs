<a name="phase-m1"></a>
# 📌 MOBILE PHASE M1: PROJECT STRUCTURE & CONFIGURATION (Full-Stack Mobile Developer)

> **Expo SDK:** This workflow targets **Expo SDK 52+** with **Managed Workflow**. React Native version: **0.76+ (New Architecture default)**.

---

### Prompt M1.1: Initialize Expo Project

```text
You are a Senior React Native Infrastructure Engineer. Initialize a production-ready Expo project from scratch.

App Name: [AppName]
Bundle ID (iOS): com.[company].[appname]
Package Name (Android): com.[company].[appname]
Platform: [iOS / Android / Both]
Expo Workflow: Managed (default — use Bare only if justified in TDD)

Required Output Format: Provide exact commands and files for:

1. Project initialization:
   ```bash
   npx create-expo-app@latest [AppName] --template blank-typescript
   cd [AppName]
   ```

2. Core dependency installation:
   ```bash
   # Navigation
   npx expo install expo-router

   # Styling
   npm install nativewind
   npm install --save-dev tailwindcss

   # Animations & Gestures
   npx expo install react-native-reanimated react-native-gesture-handler

   # Storage
   npx expo install expo-secure-store expo-sqlite
   npm install drizzle-orm
   npm install --save-dev drizzle-kit

   # State
   npm install zustand @tanstack/react-query

   # Utilities
   npx expo install expo-image expo-constants expo-font expo-system-ui
   npm install zod
   ```

3. `app.json` configuration:
   ```json
   {
     "expo": {
       "name": "[AppName]",
       "slug": "[app-slug]",
       "version": "1.0.0",
       "orientation": "portrait",
       "icon": "./assets/images/icon.png",
       "scheme": "[appscheme]",
       "userInterfaceStyle": "automatic",
       "splash": { "resizeMode": "contain" },
       "ios": {
         "bundleIdentifier": "com.[company].[appname]",
         "supportsTablet": false,
         "buildNumber": "1"
       },
       "android": {
         "adaptiveIcon": { "foregroundImage": "./assets/images/adaptive-icon.png" },
         "package": "com.[company].[appname]",
         "versionCode": 1
       },
       "plugins": [
         "expo-router",
         "expo-secure-store",
         ["expo-sqlite", { "enableFTS": true }]
       ],
       "experiments": { "typedRoutes": true }
     }
   }
   ```

4. `tailwind.config.js` for NativeWind v4:
   ```js
   const { hairlineWidth } = require('nativewind/theme')
   module.exports = {
     content: ['./app/**/*.{js,jsx,ts,tsx}', './components/**/*.{js,jsx,ts,tsx}'],
     presets: [require('nativewind/preset')],
     theme: {
       extend: {
         colors: {
           // Map from MOBILE_DESIGN.md
           primary: '[your-primary-hex]',
           background: '[your-bg-hex]',
           surface: '[your-surface-hex]',
         },
         borderWidth: { hairline: hairlineWidth() },
       },
     },
   }
   ```

5. `babel.config.js`:
   ```js
   module.exports = function(api) {
     api.cache(true)
     return {
       presets: ['babel-preset-expo'],
       plugins: ['nativewind/babel', 'react-native-reanimated/plugin'],
     }
   }
   ```
   ⚠️ `react-native-reanimated/plugin` MUST be the LAST plugin.

6. TypeScript `tsconfig.json`:
   ```json
   {
     "extends": "expo/tsconfig.base",
     "compilerOptions": {
       "strict": true,
       "paths": {
         "@/*": ["./*"],
         "@/components/*": ["./components/*"],
         "@/lib/*": ["./lib/*"],
         "@/store/*": ["./lib/store/*"],
         "@/db/*": ["./lib/db/*"]
       }
     }
   }
   ```

⚠️ Common Pitfalls:
- Pitfall: Adding `react-native-reanimated/plugin` before other Babel plugins.
- Solution: It MUST be last in the `plugins` array.
- Pitfall: Missing `scheme` in `app.json` breaking deep links.
- Solution: Always define a URL scheme matching your app slug.
- Pitfall: NativeWind v4 className not working on custom components.
- Solution: Wrap custom components with `cssInterop` or use `styled()`.
```

✅ **Verification Checklist:**
- [ ] `npx expo start` runs without errors on iOS Simulator and Android Emulator.
- [ ] TypeScript strict mode is enabled (`strict: true`).
- [ ] Path aliases resolve correctly (import from `@/components/...`).
- [ ] NativeWind className applies a color on a test View.
- [ ] Reanimated plugin is last in babel plugins array.

---

### Prompt M1.2: EAS Configuration & Build Setup

```text
You are a React Native DevOps Engineer. Configure EAS (Expo Application Services) for building and submitting the app.

Constraints:
- Use EAS Build for creating iOS (.ipa) and Android (.apk/.aab) builds.
- Use EAS Submit for App Store and Play Store submissions.
- Use EAS Update for OTA (Over-the-Air) updates to existing app builds.
- Store all secrets in EAS Secrets — never hardcode.

Required Output Format: Provide complete configs for:

1. EAS CLI setup:
   ```bash
   npm install -g eas-cli
   eas login
   eas build:configure
   ```

2. `eas.json` — three build profiles:
   ```json
   {
     "cli": { "version": ">= 14.0.0" },
     "build": {
       "development": {
         "developmentClient": true,
         "distribution": "internal",
         "ios": { "simulator": true },
         "android": { "buildType": "apk", "gradleCommand": ":app:assembleDebug" },
         "env": { "APP_ENV": "development" }
       },
       "preview": {
         "distribution": "internal",
         "ios": { "simulator": false },
         "android": { "buildType": "apk" },
         "channel": "preview",
         "env": { "APP_ENV": "preview" }
       },
       "production": {
         "distribution": "store",
         "ios": { "buildNumber": "auto" },
         "android": { "versionCode": "auto", "buildType": "app-bundle" },
         "channel": "production",
         "env": { "APP_ENV": "production" }
       }
     },
     "submit": {
       "production": {
         "ios": { "appleId": "$APPLE_ID", "ascAppId": "$ASC_APP_ID" },
         "android": { "serviceAccountKeyPath": "./google-service-account.json" }
       }
     }
   }
   ```

3. Environment variable handling with `expo-constants`:
   ```typescript
   // lib/config.ts
   import Constants from 'expo-constants'

   const ENV = {
     development: { apiUrl: 'http://localhost:3000/api', debug: true },
     preview: { apiUrl: 'https://api-preview.[domain].com', debug: false },
     production: { apiUrl: 'https://api.[domain].com', debug: false },
   }

   type AppEnv = keyof typeof ENV
   const appEnv = (Constants.expoConfig?.extra?.APP_ENV ?? 'development') as AppEnv

   export const config = ENV[appEnv]
   ```

4. EAS Secrets setup (CLI commands):
   ```bash
   # Set secrets that will be available during build
   eas secret:create --scope project --name API_KEY --value "your-key-here"
   eas secret:create --scope project --name SUPABASE_URL --value "https://..."
   ```

⚠️ Common Pitfalls:
- Pitfall: Forgetting to set `channel` in eas.json — OTA updates won't work.
- Solution: Every non-development build profile must have a `channel`.
- Pitfall: Using process.env for runtime config (it's only available at build time).
- Solution: Use `expo-constants` to expose runtime config via `app.config.js` extra field.
```

✅ **Verification Checklist:**
- [ ] `eas build:configure` completed without errors.
- [ ] `eas.json` has development, preview, and production profiles.
- [ ] `lib/config.ts` correctly switches API URL by environment.
- [ ] No API keys in source code or `app.json`.

---

### Prompt M1.3: Project Folder Architecture

```text
You are a React Native Architect. Create the production-grade folder structure for [AppName].

Required Output Format: The complete folder tree with file responsibilities:

```
[AppName]/
├── app/                          # Expo Router — all screens live here
│   ├── _layout.tsx               # Root layout: fonts, providers, theme
│   ├── +not-found.tsx            # 404 screen
│   ├── (auth)/                   # Unauthenticated stack
│   │   ├── _layout.tsx           # Auth stack layout
│   │   ├── index.tsx             # Welcome / splash redirect
│   │   ├── login.tsx             # Login screen
│   │   ├── register.tsx          # Registration screen
│   │   └── forgot-password.tsx   # Password reset
│   ├── (tabs)/                   # Authenticated tab navigator
│   │   ├── _layout.tsx           # Tab bar configuration
│   │   ├── index.tsx             # Home tab
│   │   ├── [feature].tsx         # Main feature tab
│   │   └── profile.tsx           # Profile/settings tab
│   ├── (modals)/                 # Full-screen modal stack
│   │   ├── _layout.tsx           # Modal layout (no tab bar)
│   │   └── [modal-name].tsx      # Individual modal screens
│   └── [feature]/                # Feature-specific deep screens
│       ├── [id].tsx              # Dynamic detail screen
│       └── [id]/
│           └── edit.tsx          # Edit screen
├── components/
│   ├── ui/                       # Generic design system components
│   │   ├── Button.tsx            # Base button with variants
│   │   ├── Text.tsx              # Typography component
│   │   ├── Input.tsx             # Form input
│   │   ├── Card.tsx              # Surface card
│   │   ├── Badge.tsx             # Status badge
│   │   ├── Skeleton.tsx          # Loading skeleton
│   │   ├── Sheet.tsx             # Bottom sheet wrapper
│   │   ├── Toast.tsx             # Toast notification
│   │   ├── Avatar.tsx            # User avatar
│   │   └── Icon.tsx              # Lucide icon wrapper
│   ├── [feature]/                # Feature-specific components
│   └── layout/                   # Layout components
│       ├── SafeAreaView.tsx       # Safe area wrapper
│       ├── KeyboardAvoidingView.tsx
│       └── ScreenContainer.tsx    # Standard screen wrapper
├── lib/
│   ├── api/                      # API layer
│   │   ├── client.ts             # Axios/Fetch client with interceptors
│   │   ├── hooks/                # TanStack Query hooks
│   │   │   └── use[Feature].ts
│   │   └── [feature].ts          # API functions
│   ├── db/                       # Local database
│   │   ├── client.ts             # SQLite client
│   │   ├── schema.ts             # Drizzle schema
│   │   ├── migrations/           # Drizzle migrations
│   │   └── queries/              # Drizzle query functions
│   ├── store/                    # Zustand stores
│   │   ├── auth.ts               # Auth state
│   │   ├── ui.ts                 # UI state (theme, modals)
│   │   └── [feature].ts          # Feature state
│   ├── auth/                     # Auth utilities
│   │   ├── token.ts              # Token storage (SecureStore)
│   │   └── session.ts            # Session management
│   ├── hooks/                    # Shared custom hooks
│   │   ├── useColorScheme.ts     # Dark/light mode
│   │   ├── useNetworkStatus.ts   # Online/offline detection
│   │   └── use[Hook].ts
│   └── utils/                    # Pure utility functions
│       ├── format.ts             # Date, number, currency formatters
│       ├── validation.ts         # Zod schemas
│       └── constants.ts          # App-wide constants
├── assets/
│   ├── images/                   # Static images
│   │   ├── icon.png              # 1024x1024 App icon
│   │   ├── adaptive-icon.png     # Android adaptive icon foreground
│   │   └── splash.png            # Splash screen
│   └── fonts/                    # Local font files (if not using @expo-google-fonts)
├── app.json                      # Expo configuration
├── eas.json                      # EAS Build/Submit config
├── tailwind.config.js            # NativeWind configuration
├── babel.config.js               # Babel (reanimated last!)
├── tsconfig.json                 # TypeScript config
├── MOBILE_DESIGN.md              # Design system reference
└── .env                          # Local dev only (never commit)
```

Root `app/_layout.tsx` provider setup:
```tsx
import { Stack } from 'expo-router'
import { StatusBar } from 'expo-status-bar'
import { GestureHandlerRootView } from 'react-native-gesture-handler'
import { QueryClientProvider } from '@tanstack/react-query'
import { queryClient } from '@/lib/api/client'
import { useFonts, Inter_400Regular, Inter_700Bold } from '@expo-google-fonts/inter'
import * as SplashScreen from 'expo-splash-screen'
import { useEffect } from 'react'
import '../global.css' // NativeWind

SplashScreen.preventAutoHideAsync()

export default function RootLayout() {
  const [fontsLoaded] = useFonts({ Inter_400Regular, Inter_700Bold })

  useEffect(() => {
    if (fontsLoaded) SplashScreen.hideAsync()
  }, [fontsLoaded])

  if (!fontsLoaded) return null

  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <QueryClientProvider client={queryClient}>
        <StatusBar style="auto" />
        <Stack screenOptions={{ headerShown: false }} />
      </QueryClientProvider>
    </GestureHandlerRootView>
  )
}
```
```

✅ **Verification Checklist:**
- [ ] `GestureHandlerRootView` wraps the entire app at the root level.
- [ ] `QueryClientProvider` is at root level.
- [ ] Fonts load via `useFonts` and splash screen hides on completion.
- [ ] Path aliases `@/*` resolve to correct directories.

---

📎 **Related Phases:**
- Prerequisites: [Phase M0B: HiFi Prototype](./MOBILE_PHASE_0B_HIFI_PROTOTYPE_UI_Designer.md)
- Proceeds to: [Phase M2: Navigation Architecture](./MOBILE_PHASE_2_NAVIGATION_ARCHITECTURE_Mobile_Developer.md)
