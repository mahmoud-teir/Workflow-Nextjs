# workflows — Next.js Development Workflow Prompts

## Directory Overview

This directory contains a **comprehensive, phase-gated workflow system** for building production-grade Next.js full-stack applications. It is not a software project itself — it is a **structured library of AI prompts and agent-executable workflows** that guide developers (or AI agents) through 21 development phases (Phase 0 through Phase 20), from initial planning to post-launch error handling.

Each phase is written from the perspective of a specific role (Product Manager, Full-Stack Developer, DevOps Engineer, Security Expert, etc.) and contains detailed, copy-paste-ready prompts designed to be fed into AI coding assistants like Claude Code, Cursor, or GitHub Copilot.

## Key Files and Structure

```
workflows/
├── README.md                                  # General GitHub profile readme (unrelated content)
├── phases.zip                                 # Archived copy of the phases directory
├── QWEN.md                                    # This file
└── phases/
    ├── PHASE_0_PLANNING__SETUP_*.md           # Prompts 0.1–0.7: ideation, PRD, tech design, task breakdown, wireframes, design system, version compat
    ├── PHASE_1_PROJECT_STRUCTURE__*.md        # Prompts 1.1–1.3: project init, next.config.ts, CLAUDE.md template
    ├── PHASE_2_BACKEND_SETUP_*.md             # API Routes & Server Actions
    ├── PHASE_3_DATABASE_MODELS__*.md          # Database schema & ORM integration
    ├── PHASE_4_AUTHENTICATION__*.md           # Auth & authorization
    ├── PHASE_5_FRONTEND_DEVELOPMENT_*.md      # UI component development
    ├── PHASE_6_ADVANCED_FEATURES_*.md         # Advanced Next.js features
    ├── PHASE_7_TESTING_QA__*.md               # Testing strategy (Vitest + Playwright)
    ├── PHASE_8_SECURITY_AUTOMATION_*.md       # DevSecOps & security hardening
    ├── PHASE_9_ACCESSIBILITY__*.md            # a11y & i18n
    ├── PHASE_10_PERFORMANCE_OPTIMIZATION_*.md # Frontend/backend performance
    ├── PHASE_11_DEVOPS__INFRASTRUCTURE_*.md   # Infrastructure setup
    ├── PHASE_12_OBSERVABILITY__MONITORING_*.md# Monitoring & observability
    ├── PHASE_13_DEPLOYMENT__CICD_*.md         # CI/CD pipelines (GitHub Actions)
    ├── PHASE_14_PRE-LAUNCH_CHECKLIST_*.md     # Pre-launch checklist
    ├── PHASE_15_AI__LLM_INTEGRATION_*.md      # Vercel AI SDK, LLM integration
    ├── PHASE_16_PAYMENT__SUBSCRIPTION_*.md    # Stripe payments & subscriptions
    ├── PHASE_17_MOBILE__PWA_*.md              # Mobile & PWA development
    ├── PHASE_18_ANALYTICS__FEATURE_FLAGS_*.md # Analytics & feature flags
    ├── PHASE_19_API_DOCUMENTATION__*.md       # API docs & versioning
    ├── PHASE_20_ERROR_HANDLING__*.md          # Error handling & resilience
    └── agent_workflows/                        # Agent-executable step-by-step workflows
        ├── 00-planning-setup-*.md
        ├── 01-project-structure-*.md
        └── ... (one per phase)
```

### File Naming Convention

- **`PHASE_N_TITLE__ROLE.md`** — Human-readable prompt files with detailed code templates, configuration examples, and instructions.
- **`agent_workflows/N-title.md`** — Machine-executable workflow files with YAML front matter (`phase`, `title`, `role`, `dependencies`, `estimated_time`) and structured step-by-step actions (`prompt_execution`, `write_to_file`, `review`).

## Technology Stack Referenced

The workflows target a modern (2025–2026) Next.js stack:

| Category | Technology |
|---|---|
| Framework | Next.js (latest stable, with Next.js 14/15/16+ compatibility table) |
| React | 19+ (`useActionState`, `useOptimistic`, `use()`, React Compiler) |
| Language | TypeScript (strict mode) |
| Node.js | 22+ (pinned in `.nvmrc`) |
| Styling | Tailwind CSS v4 (CSS-based config, `@theme` directive, `oklch()` colors) |
| UI Components | shadcn/ui |
| Database | PostgreSQL (Neon/Supabase), MongoDB, or Convex |
| ORM | Prisma or Drizzle ORM |
| Auth | Better Auth, Auth.js v5, or Clerk |
| Email | Resend + React Email |
| AI | Vercel AI SDK (OpenAI, Anthropic) |
| Payments | Stripe |
| Linting | Biome (preferred) or ESLint flat config |
| Testing | Vitest (unit) + Playwright (E2E) |
| Deployment | Vercel or AWS |
| CI/CD | GitHub Actions |
| Analytics | PostHog or Vercel Analytics |
| Error Tracking | Sentry |
| Bundler | Turbopack |

## How to Use

1. **Start at Phase 0.** Follow prompts sequentially — each phase builds on the previous one.
2. **Copy prompts into your AI assistant.** Each `Prompt X.Y` block is designed to be pasted directly into Claude Code, Cursor, Copilot, or similar tools.
3. **Fill in bracketed placeholders.** Replace `[your-project-name]`, `[database type]`, etc. with your specific values before executing prompts.
4. **Use agent_workflows/ for automation.** The `agent_workflows/` subdirectory contains structured workflows with prerequisites, step-by-step actions, file targets, and verification checklists — suitable for automated agent execution.
5. **Reference the version compatibility table.** Phase 0.7 contains a detailed matrix of feature availability across Next.js 14, 15, and 16+.

## Development Conventions Embedded in the Workflows

- **Server Components by default** — only add `'use client'` when necessary (interactivity, hooks, browser APIs).
- **Server Actions for mutations** — preferred over API routes for form handling and data mutations.
- **Zod for all input validation** — forms, API routes, and Server Actions.
- **Tailwind CSS v4 syntax** — CSS-based config via `@import "tailwindcss"` and `@theme` directive.
- **React Compiler** — handles memoization automatically; do not manually use `useMemo`/`useCallback`.
- **Strict TypeScript** — no `any`, prefer `type` over `interface`, strict mode enabled.
- **File naming** — kebab-case for files, PascalCase for components, camelCase for utilities.
- **Commit format** — Conventional Commits (`feat:`, `fix:`, `chore:`, `docs:`).
- **Error handling** — `error.tsx`, `global-error.tsx`, `not-found.tsx` per route; graceful degradation.

## Phases at a Glance

| Phase | Title | Role |
|---|---|---|
| 0 | Planning & Setup | Product Manager, UI/UX Designer |
| 1 | Project Structure & Configuration | Full-Stack Developer |
| 2 | Backend Setup: API Routes & Server Actions | Full-Stack Developer |
| 3 | Database Models & Integration | Database Architect |
| 4 | Authentication & Authorization | Security Expert |
| 5 | Frontend Development | Frontend Developer |
| 6 | Advanced Features | Full-Stack Developer |
| 7 | Testing & QA | Testing Engineer |
| 8 | Security & Automation | DevSecOps |
| 9 | Accessibility & Internationalization | UI/UX Designer, Frontend Developer |
| 10 | Performance Optimization | Frontend, Backend, DevOps |
| 11 | DevOps & Infrastructure | DevOps Engineer |
| 12 | Observability & Monitoring | DevOps, SRE |
| 13 | Deployment & CI/CD | DevOps Engineer |
| 14 | Pre-Launch Checklist | All Roles |
| 15 | AI & LLM Integration | AI Engineer |
| 16 | Payment & Subscription System | Full-Stack Engineer |
| 17 | Mobile & PWA | Frontend Engineer |
| 18 | Analytics & Feature Flags | Product Engineer |
| 19 | API Documentation & Versioning | Backend Engineer |
| 20 | Error Handling & Resilience | Full-Stack Engineer |

## Key Code Patterns by Phase

### Phase 18 — Analytics (PostHog)

> **PostHog cookie consent must block initialization.** Analytics should not fire until explicit user consent is granted.

```tsx
// components/consent-banner.tsx
'use client'
import { useEffect } from 'react'
import posthog from 'posthog-js'

export function ConsentBanner() {
  useEffect(() => {
    const consent = getConsent()
    if (consent === 'granted') {
      posthog.init(/* ... */)
    } else {
      posthog.opt_out_capturing()
    }
  }, [])

  // ... banner UI
}
```

### Phase 19 — API Documentation (Scalar)

> **Scalar — correct import for Next.js 15+.** Use `@scalar/nextjs-api-reference`, not the generic package.

```typescript
// app/api/docs/ui/route.ts
import { ApiReference } from '@scalar/nextjs-api-reference'

export const GET = ApiReference({
  spec: { url: '/api/docs' },
})
```

### Phase 20 — Error Handling

> **`lib/errors.ts` — export `AppError` base class.** All application errors should extend this for consistent status code and error code propagation.

```typescript
// lib/errors.ts
export class AppError extends Error {
  constructor(
    message: string,
    public code: string,
    public statusCode = 500,
  ) {
    super(message)
    this.name = 'AppError'
  }
}
```
