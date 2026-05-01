# MOBILE_CLAUDE.md — ECC Agent Harness Configuration (Mobile)

> This file is the meta-configuration for AI coding agents (Google Antigravity, Claude Code, Cursor) for **mobile app development**.
> It serves as the entry point for understanding the React Native / Expo project structure, conventions, and available automation.

## Project Overview

This is a **15-phase React Native / Expo mobile development workflow library** enhanced with the **Everything Claude Code (ECC)** agent harness architecture. It provides machine-executable skills, specialized agents, automated hooks, and mandatory rules for production-grade mobile app development targeting iOS and Android.

**Tech Stack Context:** Expo SDK 52+ · React Native 0.76+ (New Architecture) · TypeScript · Expo Router 4 · NativeWind v4 · Zustand · TanStack Query · Drizzle ORM · EAS Build

## Architecture

```
mobile/
├── MOBILE_README.md      # Main documentation (start here)
├── MOBILE_CLAUDE.md      # This file — agent meta-configuration
├── phases/               # 15-phase mobile development workflow
│   ├── MOBILE_PHASE_0_*  # Planning & Setup
│   ├── MOBILE_PHASE_0B_* # HiFi Mobile Prototype
│   ├── MOBILE_PHASE_1_*  # Project Structure
│   ├── MOBILE_PHASE_2_*  # Navigation Architecture
│   ├── MOBILE_PHASE_3_*  # Backend & API Integration
│   ├── MOBILE_PHASE_4_*  # Database & Offline Storage
│   ├── MOBILE_PHASE_5_*  # Authentication & Security
│   ├── MOBILE_PHASE_6_*  # UI Components & Design System
│   ├── MOBILE_PHASE_7_*  # Native Features & APIs
│   ├── MOBILE_PHASE_8_*  # State Management
│   ├── MOBILE_PHASE_9_*  # Testing & QA
│   ├── MOBILE_PHASE_10_* # Performance Optimization
│   ├── MOBILE_PHASE_11_* # Push Notifications & Analytics
│   ├── MOBILE_PHASE_12_* # Payments & IAP
│   ├── MOBILE_PHASE_13_* # CI/CD & Build Pipeline
│   └── MOBILE_PHASE_14_* # App Store Submission & Launch
├── agents/               # ECC agents — specialized subagent definitions
│   ├── mobile-planner.md
│   ├── rn-reviewer.md
│   ├── store-specialist.md
│   ├── mobile-security.md
│   ├── performance-profiler.md
│   ├── mobile-tdd-guide.md
│   └── eas-builder.md
├── skills/               # ECC skills — reusable mobile workflow knowledge
│   ├── rn-patterns/
│   ├── expo-workflow/
│   ├── store-submission/
│   ├── mobile-security/
│   ├── offline-first/
│   ├── mobile-testing/
│   ├── mobile-performance/
│   └── mobile-verification-loop/
└── hooks/                # Lifecycle event handlers (JSON templates)
    ├── post-edit-typecheck.json
    ├── post-edit-lint.json
    ├── pre-commit-security.json
    ├── stop-session-save.json
    └── stop-console-audit.json
```

## Agent Quick Reference

| Command | Agent | Purpose |
|---------|-------|---------|
| `/mobile-planner` | Mobile Planner | Structured task decomposition for mobile features |
| `/rn-reviewer` | RN Reviewer | React Native code quality with mobile-specific patterns |
| `/store-specialist` | Store Specialist | App Store & Play Store submission automation |
| `/mobile-security` | Mobile Security | SecureStore, biometrics, certificate pinning audit |
| `/performance-profiler` | Perf Profiler | FPS profiling, memory leaks, bundle size analysis |
| `/mobile-tdd-guide` | Mobile TDD Guide | Jest + RNTL + Maestro TDD workflow |
| `/eas-builder` | EAS Builder | EAS Build/Submit pipeline automation |
| `/verify` | — (Skill) | 5-gate mobile verification loop |

## Core Principles

1. **New Architecture First** — Use Fabric renderer + JSI; avoid legacy bridge patterns
2. **Platform Parity** — Test on real iOS and Android devices before every release
3. **Offline-First** — Assume network is unreliable; design for offline gracefully
4. **Performance Budget** — 60fps animations; <3s cold start; <50MB initial JS bundle
5. **Security by Default** — Never store sensitive data in AsyncStorage; use Keychain/SecureStore
6. **Test-Driven** — Write tests before implementation; Maestro E2E covers critical flows

## Rules Summary

### Always
- Use `expo-secure-store` for sensitive data (tokens, credentials), never `AsyncStorage`
- Validate all API responses with Zod schemas before storing in state
- Use `useSharedValue` + `useAnimatedStyle` for animations (runs on UI thread, no JS bridge)
- Wrap navigation calls in try/catch; handle deep link edge cases
- Use `data-testid` attributes on every interactive element for Maestro selectors
- Test on physical devices before submitting to App Store / Play Store
- Use `expo-image` instead of RN's built-in `Image` for better caching and performance

### Never
- Hardcode API keys or secrets in source files or `app.json`
- Use `AsyncStorage` for sensitive data (tokens, PII) — use `expo-secure-store`
- Use `ScrollView` for long lists — use `FlashList` or `FlatList` with `keyExtractor`
- Commit `.env` files; use `EAS Secrets` for CI/CD environment variables
- Use `console.log` in production builds (use `expo-constants` to guard dev-only logging)
- Use `TouchableOpacity` from core RN — prefer `Pressable` or gesture-handler `TouchableOpacity`

## Hook Configuration

```json
{
  "hooks": {
    "PostToolUse": [
      { "matcher": "Edit|Write", "command": "npx tsc --noEmit --pretty 2>&1 | head -50" },
      { "matcher": "Edit|Write", "command": "npx expo-doctor 2>&1 | head -20" }
    ],
    "PreToolUse": [
      { "matcher": "Edit|Write", "command": "grep -nE '(API_KEY|SECRET|PASSWORD)\\s*=' ${file} && exit 1 || exit 0" }
    ]
  }
}
```

## Phase Tracking

| Phase | Name | ECC Integration | Status |
|-------|------|-----------------|--------|
| M0 | Planning & Setup | Prompt M0.8 (ECC Setup) | ✅ |
| M0B | HiFi Mobile Prototype | Stitch MCP (MOBILE device type) | ✅ |
| M1 | Project Structure | Rules + Patterns | ✅ |
| M2 | Navigation | RN Patterns Skill | ✅ |
| M3 | Backend & API | API Patterns + Zod | ✅ |
| M4 | Database & Offline | Offline-First Skill | ✅ |
| M5 | Authentication | Mobile Security Skill | ✅ |
| M6 | UI Components | RN Patterns Skill | ✅ |
| M7 | Native Features | Native APIs | ✅ |
| M8 | State Management | Zustand + TanStack | ✅ |
| M9 | Testing & QA | Mobile Testing Skill | ✅ |
| M10 | Performance | Mobile Perf Skill | ✅ |
| M11 | Notifications & Analytics | PostHog / Firebase | ✅ |
| M12 | Payments & IAP | RevenueCat + StoreKit 2 | ✅ |
| M13 | CI/CD | EAS Builder Agent | ✅ |
| M14 | App Store Launch | Store Specialist Agent | ✅ |

## Getting Started

1. Clone this repository alongside your Expo project
2. Follow **Phase M0, Prompt M0.8** to set up the ECC agent harness
3. Copy required skills and agents to your `.claude/` directory
4. Activate hooks in `.claude/settings.json`
5. Start with `/mobile-planner` to decompose your first milestone
