# MOBILE_CLAUDE.md — ECC Agent Harness Configuration (Native Android)

> This file is the meta-configuration for AI coding agents (Google Antigravity, Claude Code, Cursor) for **Native Android app development**.
> It serves as the entry point for understanding the Jetpack Compose project structure, conventions, and available automation.

## Project Overview

This is a **15-phase Native Android development workflow library** enhanced with the **Everything Claude Code (ECC)** agent harness architecture. It provides machine-executable skills, specialized agents, automated hooks, and mandatory rules for production-grade Android app development.

**Tech Stack Context:** Kotlin · Jetpack Compose (Material 3) · MVVM Architecture · Dagger Hilt · Navigation Compose · Retrofit + Coroutines/Flow · Room Database · Firebase

## Architecture

```
mobile/
├── MOBILE_README.md      # Main documentation (start here)
├── MOBILE_CLAUDE.md      # This file — agent meta-configuration
├── phases/               # 15-phase Android development workflow
│   ├── MOBILE_PHASE_0_*  # Planning & Setup
│   ├── MOBILE_PHASE_0B_* # HiFi Mobile Prototype
│   ├── MOBILE_PHASE_1_*  # Project Structure & Version Catalogs
│   ├── MOBILE_PHASE_2_*  # Navigation Compose
│   ├── MOBILE_PHASE_3_*  # Network (Retrofit/Firebase)
│   ├── MOBILE_PHASE_4_*  # Database (Room/DataStore)
│   ├── MOBILE_PHASE_5_*  # Security & Biometrics
│   ├── MOBILE_PHASE_6_*  # Compose UI & Material 3
│   ├── MOBILE_PHASE_7_*  # Native APIs (Camera/Location)
│   ├── MOBILE_PHASE_8_*  # ViewModels & StateFlow
│   ├── MOBILE_PHASE_9_*  # Testing (JUnit/Compose/Maestro)
│   ├── MOBILE_PHASE_10_* # Performance & Baseline Profiles
│   ├── MOBILE_PHASE_11_* # FCM & Analytics
│   ├── MOBILE_PHASE_12_* # Play Billing / RevenueCat
│   ├── MOBILE_PHASE_13_* # CI/CD (Fastlane)
│   └── MOBILE_PHASE_14_* # Play Store Launch
├── agents/               # ECC agents — specialized subagent definitions
│   ├── mobile-planner.md
│   ├── compose-reviewer.md
│   ├── store-specialist.md
│   ├── mobile-security.md
│   ├── performance-profiler.md
│   ├── compose-tdd-guide.md
│   └── fastlane-builder.md
├── skills/               # ECC skills — reusable Android workflow knowledge
│   ├── compose-patterns/
│   ├── android-workflow/
│   ├── store-submission/
│   ├── mobile-security/
│   ├── offline-first/
│   ├── mobile-testing/
│   ├── mobile-performance/
│   └── mobile-verification-loop/
└── hooks/                # Lifecycle event handlers (JSON templates)
    ├── post-edit-lint.json
    ├── pre-commit-security.json
    └── stop-session-save.json
```

## Agent Quick Reference

| Command | Agent | Purpose |
|---------|-------|---------|
| `/mobile-planner` | Mobile Planner | Structured task decomposition for Android features |
| `/compose-reviewer` | Compose Reviewer | Jetpack Compose code quality & recomposition checks |
| `/store-specialist` | Store Specialist | Google Play Store submission automation |
| `/mobile-security` | Mobile Security | EncryptedSharedPreferences, OWASP Top 10 |
| `/performance-profiler` | Perf Profiler | Baseline Profiles, R8 rules, LazyColumn analysis |
| `/compose-tdd-guide` | Compose TDD Guide | JUnit + Compose Test Rule + Maestro TDD workflow |
| `/fastlane-builder` | Fastlane Builder | Fastlane / GitHub Actions pipeline automation |
| `/verify` | — (Skill) | 5-gate Android verification loop (detekt, ktlint, tests) |

## Core Principles

1. **Unidirectional Data Flow (UDF)** — State flows down, events flow up.
2. **ViewModel Hygiene** — ViewModels never know about the View or Context. Use `SavedStateHandle`.
3. **Compose Performance** — Avoid unstable parameters. Use `remember` and `collectAsStateWithLifecycle`.
4. **Offline-First** — Repository pattern handles local DB (Room) vs remote network fetching.
5. **Security by Default** — Never use raw SharedPreferences for tokens; use EncryptedSharedPreferences.
6. **Main-Safety** — Suspend functions must be safe to call from the Main thread (use `withContext(Dispatchers.IO)`).

## Rules Summary

### Always
- Use `collectAsStateWithLifecycle()` when collecting flows in Compose to avoid background resource leaks.
- Pass plain data classes and lambdas to Composables, NOT ViewModels.
- Run heavy data operations or DB queries in `Dispatchers.IO`.
- Use Dagger Hilt for Dependency Injection (`@HiltViewModel`, `@AndroidEntryPoint`).
- Use Version Catalogs (`libs.versions.toml`) for dependency management.
- Provide `contentDescription` for all meaningful Images/Icons for accessibility.

### Never
- Never pass `Context` into ViewModels. If absolutely needed, use `@ApplicationContext`.
- Never perform network or database operations on the UI thread.
- Never use legacy Android Views unless absolutely necessary (Interop).
- Never hardcode strings or dimensions; use `stringResource` and `dimensionResource`.
- Never put API keys in code repository. Use `local.properties` or CI secrets.

## Getting Started

1. Start a new Android Studio project with "Empty Compose Activity".
2. Follow **Phase M0, Prompt M0.8** to set up the ECC agent harness.
3. Copy required skills and agents to your `.claude/` directory.
4. Activate hooks in `.claude/settings.json`.
5. Start with `/mobile-planner` to decompose your first milestone.
