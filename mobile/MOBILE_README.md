# 🤖 Native Android / Jetpack Compose Development Workflow

> **The Everything Claude Code (ECC) Blueprint for Native Android**
> 
> This is a comprehensive, AI-executable library of prompts and patterns designed to take a Native Android application from a blank canvas to Google Play Store submission using **Jetpack Compose**. It leverages Google Antigravity, Claude Code, or Cursor to automate the heavy lifting of Android development.

---

## 🎯 What is this?

This is a **15-phase framework** for building production-ready Native Android apps. Instead of chaotic, disjointed prompting, this workflow provides a structured path for AI agents.

### The Stack
- **UI Toolkit:** Jetpack Compose (Material Design 3)
- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF)
- **Dependency Injection:** Dagger Hilt
- **Networking:** Retrofit + Kotlin Coroutines / Flow
- **Local Database:** Room Database
- **Local Key-Value:** Jetpack DataStore (Preferences)
- **Navigation:** Navigation Compose
- **Backend/Auth:** Firebase (Auth, Firestore, Cloud Messaging)
- **Testing:** JUnit 4/5, Compose Test Rule, MockK, Maestro (E2E)
- **CI/CD:** GitHub Actions + Fastlane

---

## 🏗️ The 15-Phase Android Workflow

The workflow is broken into strict, gated phases. **Do not skip phases.** 

| Phase | Goal | Agent Role | Key Artifacts |
|-------|------|------------|---------------|
| **M0** | Planning & Setup | Product Manager / Architect | PRD, Kanban Tasks, Tech Design |
| **M0B** | HiFi Prototype | UI/UX Designer | Stitch MCP generations, State Matrix |
| **M1** | Project Structure | Android Engineer | Gradle Version Catalogs, Hilt Setup |
| **M2** | Navigation | Android Architect | Navigation Compose graph, Deep Links |
| **M3** | Backend & API | Data Layer Engineer | Retrofit, Repository Pattern, Firebase |
| **M4** | Database & Offline| Offline-First Architect | Room Entities/DAOs, DataStore |
| **M5** | Auth & Security | Security Expert | EncryptedSharedPreferences, Biometrics |
| **M6** | UI Components | UI Engineer | Compose Modifiers, Material 3, Animations|
| **M7** | Native Features | Android Developer | Accompanist Permissions, Camera, Location|
| **M8** | State Management | UI Architect | ViewModels, StateFlow, SavedStateHandle |
| **M9** | Testing & QA | QA Engineer | Compose UI Tests, Maestro E2E |
| **M10** | Performance | Performance Specialist | Baseline Profiles, LazyColumn optimization |
| **M11** | Push & Analytics | Product Engineer | Firebase Cloud Messaging, PostHog |
| **M12** | Payments & IAP | Full-Stack Engineer | RevenueCat / Google Play Billing |
| **M13** | CI/CD | DevOps Engineer | Fastlane, GitHub Actions, R8 rules |
| **M14** | Play Store Launch | Submission Specialist | Play Console checklists, ASO |

---

## 🚀 How to Use This Workflow

### 1. Initializing the Agent Harness
Before starting development, set up the agent context so your AI coding assistant understands the Android stack.
1. Run **Prompt M0.8** to configure the `.claude/` directory.
2. Ensure `MOBILE_CLAUDE.md` is present at the project root.

### 2. Executing Phases
1. Open the relevant phase file (e.g., `MOBILE_PHASE_1_...`).
2. Copy the prompt block.
3. Paste it into your AI agent (Claude Code, Cursor, Antigravity).
4. **Enforce the Verification Checklist** at the bottom of the prompt before moving to the next one.

### 3. Using Android Subagents
You have access to specialized subagents. Invoke them via chat:
- `/compose-planner` → Break down a new Compose feature before coding.
- `/compose-reviewer` → Review code for Compose anti-patterns (unnecessary recompositions).
- `/compose-tdd-guide` → Enforce RED→GREEN→REFACTOR with Compose UI testing.
- `/mobile-security` → Audit for OWASP Mobile Top 10 vulnerabilities.
- `/fastlane-builder` → Manage Fastlane scripts and Gradle builds.

---

## ⚠️ Core Native Android Rules

1. **State flows down, events flow up.** Use `StateFlow` in ViewModels and `collectAsStateWithLifecycle()` in Compose.
2. **Never pass ViewModels to Composables.** Pass only the state class and a lambda for events: `MyScreen(state = uiState, onEvent = viewModel::onEvent)`.
3. **Avoid Unnecessary Recompositions.** Use `remember` and immutable data classes. Avoid putting inline unstable objects in Composable parameter lists.
4. **Never run blocking operations on the Main thread.** Always use `Dispatchers.IO` for database or network calls.
5. **Handle Process Death.** Use `SavedStateHandle` in ViewModels to preserve critical transient state.
6. **Strict Security.** Never use `SharedPreferences` for tokens; always use `EncryptedSharedPreferences`.
