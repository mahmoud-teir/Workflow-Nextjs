# 📌 MOBILE PHASE M0: PLANNING & SETUP (Product Manager / Android Architect)

> **Goal:** Define the product requirements, user personas, technical architecture, and establish the Everything Claude Code (ECC) AI harness for Native Android.

---

### Prompt M0.1: Product Requirements Document (PRD)

```text
You are an expert Mobile Product Manager. Create a comprehensive Product Requirements Document (PRD) for a Native Android application with the following core concept:

App Concept: [Describe your app idea here]
Target Audience: [Who is this for?]
Key Value Proposition: [What problem does it solve?]

Constraints:
- Must follow Native Android / Material Design 3 conventions
- Must have an offline-first architecture

Please generate a PRD that includes:
1. Executive Summary
2. User Personas (minimum 2)
3. User Stories (Agile format: As a [persona], I want to [action] so that [benefit])
4. Core Features (MVP Scope)
5. Out of Scope (Post-MVP)
6. Non-Functional Requirements (Performance, Security, Android-specifics)
```

---

### Prompt M0.2: Feature Breakdown & Task Estimation

```text
You are a Lead Android Engineer. Based on the PRD above, break down the MVP into a phased implementation plan.

Please provide:
1. A phased rollout strategy (Phase 1, Phase 2, Phase 3)
2. For Phase 1 (MVP), break down the features into actionable developer tasks.
3. For each task, estimate the complexity (Small, Medium, Large) assuming the use of Jetpack Compose, Hilt, and Room.
4. Identify any Android-specific technical risks (e.g., Background processing limitations, Doze mode, complex permissions) and mitigation strategies.
```

---

### Prompt M0.3: Database Schema Design (Room)

```text
You are an Android Data Architect. Based on the PRD, design the local database schema using Room Database patterns.

Constraints:
- The app must be offline-first.
- All entities must have a client-side generated UUID (String).
- Include `createdAt`, `updatedAt`, and `syncedAt` for sync tracking.
- Do not use foreign keys if syncing with a NoSQL remote database (like Firestore), use string relations instead.
- If using a relational remote DB, define foreign keys with appropriate cascade rules.

Output:
1. Provide the Kotlin `@Entity` data classes.
2. Provide the `@Dao` interfaces with basic CRUD operations and Flows.
3. Provide an ERD (Entity Relationship Diagram) using Mermaid syntax.
```

---

### Prompt M0.4: API / Backend Design

```text
You are a Backend Architect. Based on the PRD and Database Schema, design the RESTful API / Firebase structure needed to support the mobile app.

Constraints:
- Outline the authentication strategy (Firebase Auth or custom JWT).
- Define the endpoints / Firestore collections needed.
- If REST: Provide a swagger/openapi-style summary of endpoints, request bodies, and response structures.
- If Firebase: Provide the Firestore Security Rules concept and Collection structure.
- Consider data pagination for lists.
```

---

### Prompt M0.5: Android Architecture & Tech Stack

```text
You are a Principal Android Architect. Define the complete technical stack and architecture for this Native Android app.

Constraints:
- UI: Jetpack Compose
- Architecture: MVVM + UDF (Unidirectional Data Flow)
- DI: Dagger Hilt
- Local DB: Room
- Network: Retrofit + OkHttp (or Ktor)
- Async: Kotlin Coroutines & Flow
- Navigation: Navigation Compose (Type-safe)

Provide:
1. A detailed list of libraries to be used (with justification).
2. The recommended package structure (e.g., feature-based vs layer-based).
3. A Mermaid diagram illustrating the Unidirectional Data Flow (UI -> ViewModel -> Repository -> Network/DB -> Flow -> UI).
```

---

### Prompt M0.6: User Flow & App Architecture

```text
You are a Mobile UX Architect. Map out the user flow and screen hierarchy for the Android app based on the PRD.

Output:
1. A text-based tree representing the screen hierarchy and navigation graph (e.g., Auth Graph, Main App Graph, Bottom Navigation items).
2. A Mermaid Flowchart mapping the user's journey from app launch (Splash -> Onboarding -> Auth -> Main).
3. Identify which screens require specific Android permissions (e.g., Camera, Location, Post Notifications).
```

---

### Prompt M0.7: Write Tasks to task.md

```text
You are an Agile Scrum Master. Take the developer tasks from Prompt M0.2 and the architectural decisions from M0.5, and generate a comprehensive `task.md` file.

Use this format:
# Implementation Plan

- `[ ]` **Phase 1: Project Setup**
  - `[ ]` Initialize Android Studio project
  - `[ ]` Set up Version Catalogs (libs.versions.toml)
  - `[ ]` Configure Hilt, Room, Retrofit
- `[ ]` **Phase 2: Database & Network**
  - `[ ]` ...
- `[ ]` **Phase 3: Auth & Onboarding**
  - `[ ]` ...
- `[ ]` **Phase 4: Core Features**
  - `[ ]` ...

Place this content in a markdown code block so I can copy it easily.
```

---

### Prompt M0.8: Set Up Everything Claude Code (ECC) Agent Harness

```text
You are a DevOps Automation Engineer. Let's set up the ECC agent harness for this Android project.

Please generate the bash commands to:
1. Create a `.claude` directory at the root of the project.
2. Create `.claude/agents`, `.claude/skills`, and `.claude/hooks` subdirectories.
3. Create a `.claude/settings.json` file to enable the hooks.
4. Provide instructions on which agent and skill files I should copy from the `mobile/` template directory into the new project's `.claude/` structure based on our tech stack decisions.

Constraints:
- Include a pre-commit hook concept (e.g., detekt/ktlint) in the settings.json template.
- Ensure the setup is compatible with Claude Code / Cursor.
```

---

✅ **Phase M0 Completion Gate:**
- [ ] PRD is defined and approved.
- [ ] Database schema (Room) is defined.
- [ ] Tech stack (Compose, Hilt, Room) is finalized.
- [ ] Screen hierarchy and Navigation graph are mapped.
- [ ] `task.md` is populated with initial tasks.
- [ ] The `.claude/` ECC agent harness directory is created and configured.
