---
phase: 0
title: Planning & Setup
role: Product Manager, UI/UX Designer
dependencies: []
estimated_time: 2-4 hours
---

# Phase 0: Planning & Setup — Agent Workflow

## Prerequisites
- [ ] No prior phases required
- [ ] Clear project idea or domain in mind
- [ ] Node.js, pnpm, and Git installed

## Step-by-Step Execution

### Step 1: Generate Project Ideas
**Action:** `prompt_execution`
**Description:** Execute Prompt 0.1 — brainstorm 5 project ideas matched to skill level. Include AI tools (v0.dev, Claude Code, Cursor) in planning. Select one project to proceed with.

### Step 2: Create Product Requirements Document
**Action:** `write_to_file`
**File:** `docs/PRD.md`
**Description:** Execute Prompt 0.2 — write PRD with user personas, MVP feature list, user stories with acceptance criteria, and Privacy & Compliance section (GDPR/CCPA: data collected, consent mechanism, retention policy, deletion flow).

### Step 3: Create Technical Design Document
**Action:** `write_to_file`
**File:** `docs/TECHNICAL_DESIGN.md`
**Description:** Execute Prompt 0.3 — document architecture, tech stack choices (Option A/B/C for DB, auth, hosting), API versioning strategy (URL prefix recommended), monorepo consideration (Turborepo), and deployment strategy.

### Step 4: Break Down Tasks
**Action:** `write_to_file`
**File:** `docs/TASK_BREAKDOWN.md`
**Description:** Execute Prompt 0.4 — convert PRD into milestones (Foundation → Core Features → Polish → Launch). Each task gets priority (P0-P2) and size estimate (S/M/L).

### Step 5: Create Wireframes
**Action:** `prompt_execution`
**Description:** Execute Prompt 0.5 — design wireframes for all pages including error pages (404, 500), cookie consent banner, and empty states. Use v0.dev or Excalidraw.

### Step 6: Define Design System
**Action:** `write_to_file`
**File:** `docs/DESIGN_SYSTEM.md`
**Description:** Execute Prompt 0.6 — define Tailwind v4 design tokens: oklch colors, spacing scale, typography (next/font), dark mode overrides, sonner toast styles, and `prefers-reduced-motion` considerations.

### Step 7: Review Version Compatibility
**Action:** `review`
**Description:** Execute Prompt 0.7 — review Next.js version compatibility table. Confirm target version and which features (PPR, React Compiler, `after()` API) are available.

## Verification
- [ ] PRD with all sections including privacy/compliance
- [ ] Technical design with architecture decisions documented
- [ ] Task breakdown with milestones and priorities
- [ ] Wireframes for all key pages
- [ ] Design system tokens defined
- [ ] Target Next.js version chosen

## Troubleshooting
- **Issue:** Scope creep — too many MVP features
  **Fix:** Apply MoSCoW method. MVP = 3-5 core features maximum.
- **Issue:** Can't decide between tech options
  **Fix:** Default to best DX for your level. The workflow supports switching later.
