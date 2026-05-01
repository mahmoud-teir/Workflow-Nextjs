<a name="phase-m0b"></a>
# 📌 MOBILE PHASE M0B: HIGH-FIDELITY MOBILE PROTOTYPE (UI/UX Designer)

> **Position in Workflow:** Run after **Phase M0** (PRD + wireframes approved) and before **Phase M1** (project scaffolding).
> **Tools Covered:** Google Stitch (MCP — `deviceType: MOBILE`), Figma, prototype → production translation.

---

## 🧭 Why a Dedicated Mobile HiFi Prototype Phase?

Mobile wireframes answer *"what goes where."* A high-fidelity mobile prototype answers:
- Does the visual hierarchy feel native on a 390×844px screen?
- Do gestures (swipe, long-press, pull-to-refresh) feel intuitive?
- Does the bottom safe area and Dynamic Island handling look correct?
- Can a stakeholder tap through a realistic flow on their phone and sign off before engineers build it?

Catching these answers in a prototype costs **minutes**. Catching them in production code costs **sprints**.

---

## 🗺️ Prototype Tooling Decision Tree

```
Do you need a multi-screen interactive flow?
├── YES → Use Google Stitch MCP (Prompt M0B.2) — generates mobile-first screens
│         Use deviceType: MOBILE for all generations
└── NO, just one complex component →
        Describe it precisely and use AI to generate the JSX directly

Is this for client stakeholders?
├── YES → Export Stitch screens → Figma (Prompt M0B.4) for clickable presentation
└── NO → Use Stitch HTML as visual reference for React Native implementation
```

---

### Prompt M0B.1: Mobile Prototype Scope & Screen Inventory

```text
You are a Senior Mobile Product Designer running a pre-development prototype scoping session.

My app is: [AppName] — [one paragraph from PRD]
Target platform: [iOS / Android / Both]
Primary device target: [iPhone 14 Pro (390×844) / Samsung Galaxy S24 (360×800)]

My approved wireframes cover: [list screen names from M0.5]

Constraints:
- Prototype only the screens on the CRITICAL USER PATH (onboarding → core feature loop).
- Do NOT prototype settings, account management, or admin screens at this stage.
- Each screen must account for: safe area insets, keyboard-avoiding behavior, scroll behavior.
- Design for the "thumb zone" — primary actions should be reachable with one hand.

Required Output Format:

## 1. Prototype Scope
   - Screens to prototype with one-line reason each.
   - Screens NOT worth prototyping (explain why).

## 2. Mobile User Flow Map
   - Step-by-step flow from app launch → core feature completion.
   - Mark: [Animated Transition] | [Sheet Modal] | [Full-Screen Push] | [Tab Switch].

## 3. Mobile Component Inventory
   For each screen, list components as:
   [NEW] needs designing | [SHARED] reused | [NATIVE] use platform default (e.g., DatePicker)
   Mark components that need gesture handling.

## 4. Mobile Prototype Fidelity Checklist
   - [ ] Real copy (no Lorem Ipsum)
   - [ ] Real data shapes (realistic names, numbers, images)
   - [ ] Safe area insets defined (top + bottom)
   - [ ] Keyboard-avoiding behavior defined for input screens
   - [ ] Dark mode: Yes / No for prototype
   - [ ] Platform: iOS-first / Android-first / Both
```

✅ **Verification Checklist:**
- [ ] ≤8 screens in prototype scope.
- [ ] Every screen specifies safe area handling.
- [ ] Bottom navigation / tab structure is finalized.

---

### Prompt M0B.2: Google Stitch (MCP) — Mobile Screen Generation

> **Tool:** Google Stitch via MCP server (`mcp_StitchMCP_generate_screen_from_text`)
> **Critical Setting:** Always set `deviceType: "MOBILE"` for all mobile screen generations.

```text
Generate a high-fidelity mobile screen for [AppName].

## Screen: [ScreenName]
Purpose: [What the user does on this screen]

## Design System (from Phase M0.6)
- Primary color: [hex / oklch]
- Background: [hex / oklch]
- Accent: [hex / oklch]
- Font: [font name]
- Border radius: [value]
- Design language: [minimal / bold / editorial / glassmorphism / brutalist]
- Platform: [iOS / Android]

## Content Requirements
[List the exact UI elements this screen needs:]
- Header: [app name / back button / action icons]
- Primary content: [description of what's shown]
- Interactive elements: [buttons, inputs, toggles, etc.]
- Bottom area: [CTA button / tab bar / floating action button]

## Mobile-Specific Constraints
- Status bar: [light / dark content]
- Safe area: Show top (44pt) and bottom (34pt) safe insets
- Tab bar: [Show / Hide — specify which tab is active]
- Gesture indicators: [Pull-to-refresh arrow / Swipe hint / etc.]
- Platform conventions:
  - iOS: Rounded corners, blur effects, SF Pro font
  - Android: Material You, Roboto, bottom sheet patterns
- NO Lorem Ipsum — use real [app-specific] content
- NO generic stock imagery — use placeholder with realistic dimensions

## Interaction Notes (for prototype)
- Primary CTA button position: [bottom / middle / floating]
- Navigation type: [Stack push / Tab switch / Modal sheet]
```

**MCP Stitch Tool Usage Pattern:**

```
Tool: mcp_StitchMCP_generate_screen_from_text
Parameters:
  projectId: [your-stitch-project-id]
  deviceType: "MOBILE"   ← CRITICAL: always set this for mobile
  prompt: [paste the prompt above]
```

✅ **Verification Checklist:**
- [ ] `deviceType: MOBILE` was set for every generation.
- [ ] Safe area insets are visible in the generated screen.
- [ ] No Lorem Ipsum in the generated content.
- [ ] Platform conventions match target (iOS or Android).

---

### Prompt M0B.3: Mobile Design System Extraction (MOBILE_DESIGN.md)

> After generating screens in Stitch, extract the design system into a `MOBILE_DESIGN.md` file.

```text
You are a Design System Engineer extracting design tokens from generated mobile screens.

Review the Stitch-generated screens for [AppName] and produce a MOBILE_DESIGN.md file.

## Required Sections:

### 1. Color Palette
Extract all colors used in the screens and organize as:
- Primary: [hex] — used for primary buttons, key interactive elements
- Background: [hex] — main screen background
- Surface: [hex] — cards, sheets, modals
- Border: [hex] — dividers, input borders
- TextPrimary: [hex]
- TextSecondary: [hex]
- Success: [hex]
- Error: [hex]
- Warning: [hex]

Light mode / Dark mode variants for each.

### 2. Typography
- Font Family: [name]
- Scale:
  | Token | Size | Weight | Line Height | Usage |
  |-------|------|--------|-------------|-------|
  | display | 34sp | 700 | 40 | Onboarding hero |
  | h1 | 28sp | 700 | 34 | Screen titles |
  | h2 | 22sp | 600 | 28 | Section headers |
  | body | 17sp | 400 | 24 | Body text |
  | caption | 13sp | 400 | 18 | Meta, timestamps |
  | label | 15sp | 500 | 20 | Button labels |

### 3. Spacing (4pt grid)
- xs: 4 | sm: 8 | md: 16 | lg: 24 | xl: 32 | 2xl: 48

### 4. Component Tokens
- Button height: [px]
- Input height: [px]
- Card border radius: [px]
- Card shadow: [iOS shadowColor, Android elevation]
- Tab bar height: 49 (iOS) / 56 (Android)
- Navigation bar height: 44 (iOS) / 56 (Android)

### 5. Screen Structure Template
(Safe area zones for all screens)
- Status bar height: 44-59 (iPhone) / varies (Android)
- Top safe area: [value]
- Bottom safe area: 34 (iPhone with home indicator) / 0 (Android with nav bar)
- Content area: screen height - status bar - tab bar - bottom safe area

Produce this as a clean Markdown document for placement at the project root.
```

✅ **Verification Checklist:**
- [ ] `MOBILE_DESIGN.md` is generated and placed at project root.
- [ ] All screen colors are extracted and named semantically.
- [ ] Typography scale includes mobile-appropriate sizes (no web-scale headings).

---

### Prompt M0B.4: Mobile Interaction States — Full State Matrix

```text
You are a Senior Mobile UX Engineer conducting a UI state audit for [AppName].

For every screen in the prototype, define the complete mobile state matrix:

| State | Description | Mobile Treatment |
|-------|-------------|-----------------|
| Default | Normal loaded state | Full content |
| Loading | Data fetching | Skeleton cards (NOT spinners for lists) |
| Empty | No data (first-run) | Illustration + CTA button |
| Error | Network/server failure | Inline error + Retry button |
| Offline | No internet connection | Banner + cached content |
| Refreshing | Pull-to-refresh active | RefreshControl spinner at top |
| Success | Action completed | Toast / Snackbar (bottom) |
| Disabled | Permission denied or prerequisites unmet | Greyed + explanation tooltip |
| Keyboard Visible | Input focused | Content scrolls up, not hidden |
| Permission Prompt | Native permission dialog | Custom pre-prompt → System dialog |

## Mobile-Specific Rules:
- Loading states for LISTS must use Skeleton UI (content-shaped placeholders), NEVER a centered ActivityIndicator.
- Exception: Full-screen transitions may use a minimal loading overlay.
- Empty states MUST include an actionable CTA (not just "No items yet").
- Error states MUST offer a "Retry" button AND a way to go back.
- Offline states MUST show cached data if available, with a "You're offline" banner.
- Keyboard handling: Test every screen with the keyboard open — content must NOT be hidden behind the keyboard.

## Required Output:
For each screen: a Markdown table of all applicable states.
Then: identify MISSING states and generate code for each using React Native.

Mobile code examples for each state type:
- Skeleton: `<Skeleton width="100%" height={80} borderRadius={8} />`
- Empty: Custom component with illustration + CTA
- Offline: `useNetInfo()` from `@react-native-community/netinfo`
- RefreshControl: `<ScrollView refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}>`
```

✅ **Verification Checklist:**
- [ ] No list screen uses ActivityIndicator as the primary loading state.
- [ ] Every empty state has a CTA button.
- [ ] Keyboard behavior is defined for every screen with text inputs.
- [ ] Offline state is defined for every screen that fetches data.

---

### Prompt M0B.5: Mobile Prototype → Production Translation Guide

```text
You are a Senior React Native Architect. Given the approved prototype for [AppName], generate the production translation guide.

## Prototype Summary
[Describe: screens generated, key design decisions, platform target]

## Translation Rules

### 1. Screen → Route Mapping
| Stitch Screen | Expo Router Path | Component Type | Notes |
|---------------|-----------------|----------------|-------|
| Home | (tabs)/index.tsx | Screen | Has pull-to-refresh |
| Profile | (tabs)/profile.tsx | Screen | Requires auth |
| Detail | [id]/detail.tsx | Screen | Receives id param |
| Settings Modal | modal/settings.tsx | Modal Sheet | dismissible |

### 2. Component Classification
For each component in the prototype:
- Server: [Not applicable in React Native — all components are client-side]
- Stateless: Pure display component — no hooks
- Stateful: Uses useState / useReducer locally
- Connected: Uses Zustand store or TanStack Query
- Native: Wraps a native module (Camera, Location, etc.)

### 3. Mock Data → Real Data Mapping
For each screen's mock data:
- Define the TypeScript interface
- Identify: TanStack Query `useQuery` | Zustand selector | SQLite (Drizzle) query | Direct prop

### 4. Design Token → NativeWind / StyleSheet Mapping
Map MOBILE_DESIGN.md tokens to:
- NativeWind v4 `tailwind.config.js` theme extensions
- `StyleSheet.create` objects for non-NativeWind cases
- `useColorScheme()` hook for dark mode

### 5. Animation Inventory
| Animation | Implementation | Notes |
|-----------|----------------|-------|
| Screen transitions | Expo Router default | Customize with `screenOptions` |
| List item enter | Reanimated 3 `FadeIn` | Use `entering` prop |
| Button press | Reanimated 3 `useAnimatedStyle` | Scale + opacity |
| Modal sheet | `react-native-bottom-sheet` | Gesture-driven |
| Skeleton loading | `moti` `MotiView` | Pulse animation |

### 6. Gesture Implementation Map
| Gesture | Library | Component |
|---------|---------|-----------|
| Swipe-to-delete | Gesture Handler `Swipeable` | List items |
| Pull-to-refresh | Built-in `RefreshControl` | ScrollView/FlatList |
| Pinch-to-zoom | Gesture Handler `PinchGestureHandler` | Image viewer |
| Long-press menu | Gesture Handler `LongPressGestureHandler` | Cards |
```

✅ **Verification Checklist:**
- [ ] Every screen maps to an Expo Router file path.
- [ ] All animations use Reanimated 3 (not Animated API).
- [ ] `MOBILE_DESIGN.md` tokens are mapped to NativeWind theme.
- [ ] Gesture interactions are specified for all interactive list items.

---

### Prompt M0B.6: Stakeholder Mobile Prototype Review

```text
You are a Mobile Product Designer facilitating a prototype review session.

## Review Context
- Reviewers: [e.g., "client, PM, 1 engineer"]
- Prototype format: [Stitch screens / Figma / Video walkthrough]
- Device: [iPhone 14 Pro / Android Pixel 8]
- Review goal: [sign off on navigation / validate core flow / check brand alignment]

## Mobile-Specific Feedback Triage

| Priority | Criteria | Action |
|----------|----------|--------|
| 🔴 Blocker | Core flow broken, gesture conflicts, app crashes | Fix before sign-off |
| 🟡 Iteration | Layout preference, copy tweaks, color adjustment | Fix in this phase |
| 🟢 Backlog | Polish, edge cases, secondary screens | Defer to M6 or M7 |
| ❌ Rejected | Scope creep, technically infeasible, store guideline violation | Document and decline |

## Mobile-Specific Review Checklist
- [ ] Core user flow completable in ≤3 taps from app launch
- [ ] Navigation is intuitive without explanation
- [ ] Text is readable without zooming (minimum 13pt)
- [ ] Touch targets are large enough (can be tapped without precision)
- [ ] Loading states are present on data-heavy screens
- [ ] The app feels "native" to the platform (iOS or Android)
- [ ] Dark mode (if applicable) looks polished

## Sign-Off Criteria
Prototype is approved when:
- [ ] All 🔴 Blockers resolved
- [ ] Stakeholders complete the primary flow without confusion
- [ ] Design is consistent with MOBILE_DESIGN.md
- [ ] At least one engineer has reviewed for native feasibility
```

✅ **Phase M0B Completion Gate:**
Before proceeding to Phase M1, confirm ALL:
- [ ] All prototype screens complete and navigable.
- [ ] Interaction states defined for critical screens.
- [ ] Stakeholder sign-off received.
- [ ] `MOBILE_DESIGN.md` extracted and at project root.
- [ ] `Prototype → Production Translation Guide` (M0B.5) complete.
- [ ] No 🔴 Blockers open.

---

📎 **Related Phases:**
- Prerequisites: [Phase M0: Planning & Setup](./MOBILE_PHASE_0_PLANNING_SETUP_Product_Manager_Mobile_Architect.md)
- Proceeds to: [Phase M1: Project Structure](./MOBILE_PHASE_1_PROJECT_STRUCTURE_CONFIGURATION_Full_Stack_Mobile.md)
