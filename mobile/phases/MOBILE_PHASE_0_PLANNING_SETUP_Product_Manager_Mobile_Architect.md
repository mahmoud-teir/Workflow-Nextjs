<a name="phase-m0"></a>
# 📌 MOBILE PHASE M0: PLANNING & SETUP (Product Manager, Mobile Architect)

> **Platform:** This workflow targets **iOS 16+** and **Android API 33+** using **Expo SDK 52+** (Managed Workflow). For bare workflow / custom native modules, see Prompt M0.9.

---

### Prompt M0.1: Generate Mobile App Project Ideas

```text
You are an expert Mobile Product Consultant and Senior React Native Architect. I want to build a React Native / Expo mobile app for my portfolio.

About me:
- Skill Level: [beginner/intermediate/advanced]
- Interests: [e.g., fitness, productivity, social, e-commerce, AI-powered]
- Platforms: [iOS only / Android only / Both]
- Time Available: [hours per week / total weeks]
- Goal: [learning / portfolio / actual product / startup MVP]

Tools I plan to use:
- AI-assisted development: Google Antigravity, Claude Code, or Cursor
- UI design: Google Stitch (MCP, mobile device type), Figma
- Backend: Supabase / Convex / custom Next.js API
- Distribution: Expo Go (dev) → EAS Build → App Store / Play Store

Constraints:
- Must be buildable in Expo Managed Workflow (no complex native modules unless justified).
- Must demonstrate mobile-native capabilities (navigation, gestures, offline support, native APIs).
- Must be publishable on the App Store and/or Google Play.

Required Output Format:
1. Suggest 5 mobile app ideas matching my skill level, ranked by Portfolio Impact Score (1-10).
2. For each idea:
   - Description (2-3 sentences)
   - Core Features (3-5 features, highlighting what makes it MOBILE-native)
   - Platform Recommendation: iOS / Android / Both (with reasoning)
   - Difficulty Level (1-5 stars)
   - Estimated Time to Complete
   - Key mobile APIs needed (Camera, Location, Push, Biometrics, etc.)
   - Monetization strategy (free / freemium / subscription / one-time purchase)
   - App Store Category and primary keywords for ASO
   - Why this demonstrates strong React Native skills to employers
```

✅ **Verification Checklist:**
- [ ] You have selected one idea to proceed with.
- [ ] The idea requires at least 2 mobile-native features (not achievable in a plain web app).
- [ ] You understand the App Store / Play Store requirements for this category.

---

### Prompt M0.2: Create Mobile Product Requirements Document (PRD)

```text
You are a Lead Mobile Product Manager with expertise in iOS and Android UX conventions. I want to build [describe your app idea].

Platform Target: [iOS / Android / Both]

Constraints:
- Follow platform-specific design conventions: iOS (Human Interface Guidelines) and Android (Material Design 3).
- Define MVP strictly. Avoid scope creep — mobile apps with too many features feel bloated.
- Consider App Store review guidelines from the beginning (especially for: Payments, User Data, Permissions, Content).

Required Output Format: Create a comprehensive Mobile PRD containing:

## 1. Project Overview & User Personas
   - 2-3 mobile user personas (include usage context: commuting, gym, home, work)
   - Persona device preferences (iPhone 14 Pro vs budget Android, etc.)
## 2. Core Objectives
   - Primary value proposition in one sentence
   - Success metric: what does "successful app" look like after 3 months?
## 3. User Stories (Mobile-Specific)
   - Format: "As a [persona], I want to [action on mobile], so that [benefit]"
   - Include 10-15 critical user stories for MVP.
   - Flag which stories require native device capabilities.
## 4. Core Features (MVP)
   - Screen-by-screen feature list
   - Mark each feature as: [Core] | [Mobile-Native] | [Nice-to-Have]
## 5. Platform-Specific Considerations
   - iOS: Safe Area, Dynamic Island, Face ID, Haptics, App Clips
   - Android: Back gesture, Material You, Widgets, App Shortcuts
## 6. Offline Strategy
   - Which features work offline?
   - What data is cached locally?
   - Sync conflict resolution approach.
## 7. Permission Strategy
   - List every permission required (Camera, Location, Microphone, Contacts, etc.)
   - Justify each permission with the user benefit (App Store requirement).
   - Define the "deny" flow for each permission.
## 8. Push Notification Strategy
   - Types of notifications (transactional, engagement, re-engagement)
   - Opt-in flow (must be user-initiated, not automatic)
## 9. Monetization Model
   - Free / Freemium / Subscription / One-Time Purchase / IAP
   - Price points and tier structure
   - RevenueCat / StoreKit 2 integration plan
## 10. Privacy & Compliance
   - App Store Privacy Nutrition Label requirements
   - GDPR / CCPA for mobile
   - Data Collection Disclosure (required by both stores)
## 11. Success Metrics (KPIs)
   - Day-1 / Day-7 / Day-30 retention targets
   - Core action conversion rate
   - App Store rating target (≥4.5★)
## 12. Out of Scope (MVP)
```

✅ **Verification Checklist:**
- [ ] PRD includes a permission justification for every native API access.
- [ ] Offline behavior is defined for core user flows.
- [ ] Monetization strategy is compatible with App Store / Play Store policies.

---

### Prompt M0.3: Create Mobile Technical Design Document

```text
You are a Principal React Native / Expo Architect. Based on this Mobile PRD:

[Paste your PRD here]

Constraints:
- Target Expo SDK 52+ Managed Workflow unless a specific native module absolutely requires bare workflow.
- Use Expo Router 4 (file-based routing) as the navigation solution.
- Do NOT recommend Redux — use Zustand for global UI state + TanStack Query for server state.
- New Architecture is the default (Fabric + JSI) in RN 0.76+. All library recommendations must be New Architecture compatible.

Required Output Format: Create a Technical Design Document containing:

## 1. Architecture Overview
   - Navigation architecture (Expo Router structure)
   - State management layers (Server state vs UI state vs Persistent state)
   - Offline-first data flow diagram
## 2. Technology Stack Decisions (with ADR justification)
   - Navigation: Expo Router vs React Navigation (justify choice)
   - Styling: NativeWind v4 vs Tamagui vs StyleSheet (justify)
   - Local Storage: Drizzle ORM + SQLite vs WatermelonDB vs MMKV-only (justify)
   - Auth: Expo SecureStore + custom vs Clerk RN SDK vs Supabase Auth
   - Backend: Supabase vs Convex vs custom API (justify)
   - Animations: Reanimated 3 vs Moti (justify)
## 3. Detailed Project Structure
   ```
   app/
   ├── (auth)/         # Auth stack — unauthenticated
   │   ├── login.tsx
   │   └── register.tsx
   ├── (tabs)/         # Main tab navigator — authenticated
   │   ├── index.tsx   # Home tab
   │   ├── explore.tsx
   │   └── profile.tsx
   ├── _layout.tsx     # Root layout (providers, fonts)
   └── +not-found.tsx  # 404 screen
   components/
   ├── ui/             # Generic design system components
   └── [feature]/      # Feature-specific components
   lib/
   ├── api/            # API clients and hooks
   ├── db/             # SQLite schema + Drizzle queries
   ├── store/          # Zustand stores
   └── utils/          # Pure utility functions
   ```
## 4. Database Schema Design (SQLite / Local)
## 5. API Contract Design
   - REST endpoints / GraphQL schema / tRPC routers
   - Request/Response types (Zod schemas)
   - Error envelope format
## 6. Authentication & Authorization Flow
   - Token storage (SecureStore)
   - Token refresh strategy
   - Biometric re-authentication
## 7. Offline Strategy Implementation
   - Optimistic UI updates
   - Sync queue for offline mutations
   - Conflict resolution rules
## 8. Performance Budget
   - Cold start target: <3 seconds
   - Animation: 60fps (no JS thread drops)
   - JS bundle: <50MB initial
   - Image strategy (expo-image, caching)
## 9. Security Considerations
   - Certificate pinning (if required)
   - Root/jailbreak detection
   - Obfuscation for sensitive logic
## 10. Testing Strategy (Mobile-Specific)
   - Unit: Jest + React Native Testing Library
   - E2E: Maestro or Detox (justify choice)
   - Device testing matrix (iOS/Android versions)
```

✅ **Verification Checklist:**
- [ ] All recommended libraries are New Architecture compatible (check `reactnative.directory`).
- [ ] Navigation structure maps cleanly to Expo Router file conventions.
- [ ] Offline strategy has a concrete conflict resolution approach.

---

### Prompt M0.4: Break Down Into Mobile Tasks

```text
You are an Agile Mobile Technical Project Manager. Based on this Technical Design Document:

[Paste your TDD here]

Constraints:
- Tasks must be completable in 2-4 hours.
- Mobile tasks must consider: iOS simulator + Android emulator testing for every UI task.
- Include explicit tasks for App Store / Play Store setup (certificates, provisioning profiles, developer accounts).

Required Output Format: Break into actionable Kanban tasks.

## Task Format:
- **ID**: MOB-001
- **Title**: Action-oriented title
- **Category**: [Setup/Navigation/UI/Backend/Native/Auth/Testing/Store/CI]
- **Priority**: [Critical/High/Medium/Low]
- **Estimated Time**: [hours]
- **Platform**: [iOS / Android / Both]
- **Dependencies**: List task IDs that must be completed first
- **Description**: Detailed what needs to be done
- **Acceptance Criteria**: Checkbox list
- **Phase Reference**: Which mobile phase this maps to (M0-M14)

Group tasks into these Milestones:
- Milestone 1: Foundation (Phase M1-M2) — Project scaffold, navigation
- Milestone 2: Core Backend & Data (Phase M3-M5) — API, DB, Auth
- Milestone 3: UI & Native Features (Phase M6-M8) — Components, state, native APIs
- Milestone 4: Quality & Performance (Phase M9-M10) — Testing, profiling
- Milestone 5: Launch (Phase M11-M14) — Notifications, payments, CI/CD, store
```

✅ **Verification Checklist:**
- [ ] Apple Developer Account + Google Play Console setup tasks are included.
- [ ] Every UI task has "Test on iOS simulator AND Android emulator" in acceptance criteria.
- [ ] Store submission tasks include privacy policy and screenshot requirements.

---

### Prompt M0.5: Mobile Wireframes & Screen Architecture

```text
You are an expert Mobile UI/UX Designer with deep knowledge of iOS HIG and Material Design 3. Create wireframes for [AppName].

Platform: [iOS / Android / Both]

Constraints:
- Use mobile-native navigation patterns: tab bars (iOS), bottom nav (Android), stack navigation.
- Design for thumb reach zones: bottom 60% of screen is the primary action area.
- Every screen must define safe area insets handling.
- All interactive elements must be ≥44pt (iOS) / ≥48dp (Android) touch targets.

Required Output Format:
1. Screen Inventory:
   - List all screens with their navigation path (e.g., `(tabs)/home`, `(auth)/login`)
   - Group by: [Auth Flow] | [Main Flow] | [Settings/Profile] | [Modals/Sheets]

2. Navigation Architecture Diagram:
   - Tab structure (max 5 tabs — iOS HIG recommendation)
   - Stack flows within each tab
   - Modal sheets vs full-screen push

3. Per-Screen ASCII Wireframe OR Stitch Prompt:
   For each primary screen, provide:
   - ASCII wireframe showing layout zones (header, content, bottom action)
   - OR: A Google Stitch (MCP) prompt following IDEA + THEME + CONTENT
     Specify `deviceType: MOBILE` for all Stitch generations.

4. Gesture Map:
   - Swipe gestures per screen (swipe-to-delete, pull-to-refresh, swipe-back)
   - Long-press actions (context menus)
   - Pinch/zoom (if applicable)

5. Platform Divergence Map:
   | Screen | iOS Pattern | Android Pattern |
   |--------|-------------|-----------------|
   | Navigation | Tab bar (bottom) | Bottom Navigation |
   | Settings | Grouped TableView | Material List |
   | Dialogs | Action Sheet | Bottom Sheet / Dialog |
   | Back | Swipe-back gesture | System back button |
```

✅ **Verification Checklist:**
- [ ] All screens handle safe area insets (Dynamic Island, notch, home indicator).
- [ ] Tab bar has ≤5 items.
- [ ] Every touch target is ≥44pt / 48dp.
- [ ] Pull-to-refresh is defined for all data-heavy screens.

---

### Prompt M0.6: Mobile Design System

```text
You are a Principal Mobile Design System Engineer. Create a design system for [AppName] targeting React Native with NativeWind v4.

Platform: [iOS / Android / Both]

Constraints:
- Color system must meet WCAG AA contrast on both light and dark mobile backgrounds.
- Typography must use Expo Google Fonts (`@expo-google-fonts/*`) or system fonts (SF Pro / Roboto).
- Spacing must use a consistent 4pt grid.
- All shadow values must have iOS and Android equivalents.
- Dark mode must be handled via `useColorScheme()` hook.

Required Output Format:

## 1. Color Palette
   - Primary, Secondary, Accent, Error, Warning, Success
   - Light mode values (hex + oklch)
   - Dark mode values (hex + oklch)
   - Semantic color map (background, surface, border, text, textMuted)

## 2. Typography Scale
   - Font: [font name] via `@expo-google-fonts`
   - Scale: Display, Heading, Title, Body, Caption, Label
   - Line heights optimized for mobile reading

## 3. Spacing System
   - Base: 4pt
   - Scale: 4 / 8 / 12 / 16 / 20 / 24 / 32 / 48 / 64
   - NativeWind `theme.extend.spacing` mapping

## 4. Component Tokens
   - Border radius: none / sm (4) / md (8) / lg (16) / full
   - Shadow (iOS elevation): sm / md / lg
   - Button heights: sm (32) / md (44) / lg (56) — all meeting touch target minimum
   - Input height: 44 (iOS) / 48 (Android)

## 5. Motion Tokens
   - Duration: instant (0) / fast (150ms) / normal (300ms) / slow (500ms)
   - Easing: ease-out (default), spring (interactive), ease-in-out (modals)
   - Platform preference: `AccessibilityInfo.isReduceMotionEnabled()`

## 6. Platform Adaptations
   - Hairline border: `StyleSheet.hairlineWidth`
   - Hit slop: `{ top: 10, bottom: 10, left: 10, right: 10 }`
   - Status bar: light / dark content per screen
```

✅ **Verification Checklist:**
- [ ] Dark mode colors are defined for every semantic color token.
- [ ] All button sizes meet minimum touch target requirements.
- [ ] Font loading uses `useFonts` from `@expo-google-fonts`.

---

### Prompt M0.7: Platform & Library Compatibility Matrix

Use this reference to verify library compatibility with Expo SDK 52+ and React Native New Architecture (RN 0.76+):

| Library | New Arch | Expo Managed | Notes |
|---------|----------|--------------|-------|
| Expo Router 4 | ✅ | ✅ | File-based routing — recommended |
| React Navigation 7 | ✅ | ✅ | Use if Expo Router not suitable |
| NativeWind v4 | ✅ | ✅ | CSS-in-JS via Tailwind |
| Tamagui | ✅ | ✅ | Full design system alternative |
| Reanimated 3 | ✅ | ✅ | Required: New Arch by default in SDK 52 |
| Gesture Handler | ✅ | ✅ | Required for Reanimated 3 gestures |
| Skia (Shopify) | ✅ | ✅ | For canvas/custom drawing |
| MMKV | ✅ | ⚠️ Requires plugin | Need `expo-build-properties` |
| Drizzle ORM (SQLite) | ✅ | ✅ | With `expo-sqlite` |
| WatermelonDB | ⚠️ Partial | ❌ Bare only | Better for complex offline sync |
| TanStack Query v5 | ✅ | ✅ | No native dependencies |
| Zustand v5 | ✅ | ✅ | No native dependencies |
| RevenueCat | ✅ | ✅ | Use `react-native-purchases` |
| Expo Notifications | ✅ | ✅ | Managed push — recommended |
| OneSignal | ✅ | ✅ | Alternative push provider |
| Clerk RN SDK | ✅ | ✅ | Managed auth service |
| Supabase JS | ✅ | ✅ | No native dependencies |
| Firebase | ✅ | ⚠️ Partial | Use `@react-native-firebase` (bare) OR Firebase JS SDK (managed) |
| Lottie | ✅ | ✅ | `lottie-react-native` |
| FlashList (Shopify) | ✅ | ✅ | Replace FlatList for performance |
| expo-image | ✅ | ✅ | Replace RN Image — far better caching |
| Maestro | N/A | N/A | E2E testing — platform-agnostic |
| Detox | ✅ | ❌ Bare only | E2E alternative (heavy setup) |

> ⚠️ **Always check** [reactnative.directory](https://reactnative.directory) and the library's GitHub issues before adding a dependency. New Architecture compatibility changed rapidly in 2024-2025.

---

### Prompt M0.8: ECC Mobile Agent Harness Setup

```text
You are a Mobile Development Infrastructure Architect. Set up the Everything Claude Code (ECC) agent harness for a React Native / Expo project.

What is ECC?
ECC is a performance optimization architecture for AI agent harnesses:
1. Skills — Reusable domain knowledge (RN patterns, offline-first, mobile testing).
2. Agents — Specialized subagent definitions (/mobile-planner, /rn-reviewer, etc.).
3. Hooks — Automated lifecycle handlers (typecheck, lint, security scan).
4. Rules — Mandatory mobile-specific coding standards.

Constraints:
- Skills MUST use YAML frontmatter.
- Agents MUST declare `allowed_tools`, `model`, and `trigger`.
- Hooks MUST be JSON templates compatible with `.claude/settings.json`.
- Rules MUST address both iOS and Android considerations.

Required Output Format:
1. Directory structure:
   ```
   .claude/
   ├── settings.json         # Hook configurations
   ├── agents/               # Mobile subagent definitions
   └── skills/               # Project-specific skills
   ```

2. Copy and adapt from the mobile workflow library:
   - Required Skills: `rn-patterns`, `expo-workflow`, `mobile-testing`, `offline-first`
   - Required Agents: `mobile-planner`, `rn-reviewer`, `mobile-security`, `mobile-tdd-guide`
   - Required Hooks: `post-edit-typecheck`, `post-edit-lint`, `pre-commit-security`

3. A `AGENTS.md` or `CLAUDE.md` at project root containing:
   - App tech stack and conventions
   - Active skills and agents
   - Phase tracking table (M0 → M14)
   - Platform targets (iOS/Android versions)

4. Integration with Antigravity artifact system (Implementation Plans, Task Lists, Knowledge Items for mobile-specific patterns).

⚠️ Common Pitfalls:
- Pitfall: Using web-centric skills (nextjs-patterns, seo-specialist) for mobile.
- Solution: Use only mobile-specific skills from the mobile workflow library.
- Pitfall: Not setting up `eas.json` as part of ECC harness setup.
- Solution: Add EAS configuration to the harness setup phase.
```

✅ **Verification Checklist:**
- [ ] `.claude/settings.json` exists with valid hook configurations.
- [ ] `CLAUDE.md` / `AGENTS.md` references mobile tech stack.
- [ ] All required mobile skills have valid YAML frontmatter.
- [ ] `/mobile-planner` correctly invokes the planner subagent.

---

📎 **Related Phases:**
- Proceeds to: [Phase M0B: HiFi Mobile Prototype](./MOBILE_PHASE_0B_HIFI_PROTOTYPE_UI_Designer.md)
- Or: [Phase M1: Project Structure](./MOBILE_PHASE_1_PROJECT_STRUCTURE_CONFIGURATION_Full_Stack_Mobile.md)
