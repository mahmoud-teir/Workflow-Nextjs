# 🚀 Next.js Full-Stack Development Workflows

[![Next.js 15](https://img.shields.io/badge/Next.js-15+-black?logo=next.js)](https://nextjs.org/)
[![React 19](https://img.shields.io/badge/React-19-blue?logo=react)](https://react.dev/)
[![Tailwind v4](https://img.shields.io/badge/Tailwind-v4-38B2AC?logo=tailwind-css)](https://tailwindcss.com/)
[![ECC Architecture](https://img.shields.io/badge/ECC-Agent%20Harness-purple)](./CLAUDE.md)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)

A comprehensive, phase-gated **AI prompt library** enhanced with the **Everything Claude Code (ECC)** agent harness architecture for building production-grade Next.js applications. Each phase contains ready-to-use prompts designed for AI coding assistants (**Google Antigravity**, Claude Code, Cursor, GitHub Copilot) that guide you from initial idea to post-launch resilience.

> **22 phases · 100+ prompts · 10 skills · 8 agents · 5 hooks · 12 rules · 2025–2026 stack**

---

## 📋 Quick Start

### Use in a New or Existing Project

```bash
# Clone into your project
git clone https://github.com/<your-username>/nextjs-workflows.git workflows

# Or add as a subtree to track updates
git subtree add --prefix=workflows https://github.com/<your-username>/nextjs-workflows.git main
```

Then reference the prompts from `workflows/phases/` when working with your AI assistant.

### How It Works

1. **Start at Phase 0** — follow phases sequentially
2. **Copy prompts into your AI assistant** — each `Prompt X.Y` block is ready to paste
3. **Fill in `[bracketed]` placeholders** with your project specifics
4. **Follow the phase's deliverables** — each phase lists what should be produced

### 🤖 With ECC Agent Harness (Recommended)

1. **Set up the harness** — Follow **Prompt 0.11** in Phase 0 to initialize skills, agents, hooks, and rules
2. **Use agent commands** — Invoke specialized agents like `/planner`, `/tdd-guide`, `/security-reviewer`
3. **Automated quality gates** — Hooks auto-format, type-check, and security-scan on every edit
4. **Launch readiness** — Run `/verify` for the 6-gate verification loop before shipping

---

## 🏗️ Architecture

```
workflows/
├── CLAUDE.md             # 🎯 Agent meta-configuration (start here)
├── phases/               # 📖 21-phase development workflow
│   ├── PHASE_0_*.md      #    Planning & Setup (with ECC Prompt 0.11)
│   ├── PHASE_7_*.md      #    Testing & QA (with TDD agent integration)
│   ├── PHASE_8_*.md      #    Security (with security agent integration)
│   ├── PHASE_14_*.md     #    Pre-Launch (with Prompts 14.11 & 14.12)
│   └── ...               #    Phases 1-20
├── skills/               # 🧠 ECC Skills — reusable workflow knowledge
│   ├── tdd-workflow/     #    RED→GREEN→REFACTOR methodology
│   ├── security-review/  #    Auth, CSP, env var, input validation
│   ├── verification-loop/#    6-gate quality pipeline
│   ├── planner-workflow/ #    Structured planning methodology
│   ├── code-review/      #    Severity-based code review
│   ├── nextjs-patterns/  #    App Router, Server Components, Server Actions
│   ├── deployment-patterns/#  Pre-deployment checklist & CI/CD
│   ├── strategic-compact/#    Context management for long sessions
│   ├── session-manager/  #    Persistence and context handoff
│   └── continuous-learning/#  Knowledge capture and improvement
├── agents/               # 🤖 ECC Agents — specialized subagent definitions
│   ├── planner.md        #    Task decomposition and planning
│   ├── code-reviewer.md  #    Code quality with confidence filtering
│   ├── security-reviewer.md#  Full security audit
│   ├── tdd-guide.md      #    Test-driven development guide
│   ├── build-error-resolver.md# Build/type/lint error diagnosis
│   ├── e2e-runner.md     #    Playwright test execution
│   ├── seo-specialist.md #    SEO and metadata audit
│   └── performance-optimizer.md# Core Web Vitals optimization
├── hooks/                # ⚡ Lifecycle event handlers (JSON templates)
│   ├── post-edit-format.json#  Auto-format with Biome on edit
│   ├── post-edit-typecheck.json# Auto type-check on edit
│   ├── pre-edit-security-scan.json# Block hardcoded secrets
│   ├── stop-session-save.json#  Persist session state on exit
│   └── stop-console-log-audit.json# Flag console.log on exit
├── rules/                # 📏 Mandatory project-wide constraints
│   ├── common/           #    7 language-agnostic rules
│   └── typescript/       #    5 TypeScript-specific rules
├── docs/                 # 📚 Guides and reference
│   ├── SECURITY_GUIDE.md #    Comprehensive security reference
│   └── CONTRIBUTING.md   #    How to contribute
└── stitch-skills/        # 🎨 Google Stitch design integration
```

---

## 🗂️ Phases

### 🗺️ Visual Workflow

```mermaid
graph TD
    %% Core Setup
    A[0: Planning & Setup] --> A2[0B: HiFi Prototype]
    A2 --> B[1: Project Structure]
    B --> C[2: Backend Setup]
    C --> D[3: Database Models]
    D --> E[4: Authentication]
    E --> F[5: Frontend Dev]
    
    %% Testing & Security
    F --> G[7: Testing & QA]
    G --> H[8: Security]
    
    %% Advanced Features (Branch)
    F -.-> I[6: Advanced Features]
    F -.-> J[9: A11y & i18n]
    F -.-> K[17: Mobile & PWA]

    %% Production Prep
    H --> L[10: Performance]
    L --> M[11: DevOps]
    M --> N[12: Observability]
    N --> O[13: CI/CD]
    O --> P[14: Pre-Launch]

    %% Add-ons (Optional Branches)
    P -.-> Q[15: AI Integration]
    P -.-> R[16: Subscriptions]
    P -.-> S[18: Analytics]
    P -.-> T[19: API Docs]
    P -.-> U[20: Error Resilience]
    
    classDef phase fill:#0f172a,stroke:#3b82f6,stroke-width:2px,color:#fff;
    classDef branch fill:#1e293b,stroke:#a855f7,stroke-width:2px,color:#fff,stroke-dasharray: 5 5;
    
    class A,A2,B,C,D,E,F,G,H,L,M,N,O,P phase;
    class I,J,K,Q,R,S,T,U branch;
```

### 🧭 Which Phase Do I Need?

**Starting a new project?**
Begin firmly at **Phase 0** and proceed sequentially through **Phase 5**. These are the critical path foundations for every Next.js application.

**Need secure user logins?**
Jump to **Phase 4 (Authentication)** to see patterns for Auth.js, Clerk, and Better Auth.

**App is slow? Need better Core Web Vitals?**
You need **Phase 10 (Performance Optimization)** for caching strategies, bundle analysis, and image optimization.

**Deploying to production tomorrow?**
Pause development and run through **Phase 14 (Pre-Launch Checklist)**, **Phase 8 (Security)**, and **Phase 12 (Observability)** immediately.

**Adding new revenue streams or paid plans?**
Head straight to **Phase 16 (Payment & Subscription)** for Stripe integration and webhooks.

**Trying to add ChatGPT-like features?**
Use **Phase 15 (AI & LLM Integration)** for Vercel AI SDK integration, RAG pipelines, and tool calling.

**Want AI to design your UI before you code?**
Use **Phase 0.8 (Google Stitch)** to generate high-fidelity screens and a `DESIGN.md` design system from natural language prompts.

### Phase Directory

| # | Phase | Role | Description |
|---|---|---|---|
| 0 | [Planning & Setup](phases/PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md) | Product Manager, UI/UX Designer | Ideation, PRD, tech design, wireframes, **Google Stitch**, design system, **ECC Setup** |
| 0B | [High-Fidelity Prototype](phases/PHASE_0B_HIGH_FIDELITY_PROTOTYPE_UI_Designer_Frontend_Developer.md) | UI/UX Designer, Frontend Developer | **Claude Design**, v0.dev, interaction states, stakeholder sign-off, prototype→production translation |
| 1 | [Project Structure & Config](phases/PHASE_1_PROJECT_STRUCTURE__CONFIGURATION_Full-Stack_Developer.md) | Full-Stack Developer | Project init, `next.config.ts`, Biome, Tailwind v4 |
| 2 | [Backend: API Routes & Server Actions](phases/PHASE_2_BACKEND_SETUP_API_Routes__Server_Actions.md) | Full-Stack Developer | Route Handlers, Server Actions, validation |
| 3 | [Database Models & Integration](phases/PHASE_3_DATABASE_MODELS__INTEGRATION_Database_Architect.md) | Database Architect | Schema design, Prisma/Drizzle, migrations |
| 4 | [Authentication & Authorization](phases/PHASE_4_AUTHENTICATION__AUTHORIZATION_Security_Expert.md) | Security Expert | Auth flow, OAuth, session management |
| 5 | [Frontend Development](phases/PHASE_5_FRONTEND_DEVELOPMENT_Frontend_Developer.md) | Frontend Developer | Components, layouts, forms, state |
| 6 | [Advanced Features](phases/PHASE_6_ADVANCED_FEATURES_Full-Stack_Developer.md) | Full-Stack Developer | PPR, streaming, intercepting routes |
| 7 | [Testing & QA](phases/PHASE_7_TESTING_QA__Testing_Engineer.md) | Testing Engineer | Vitest, Playwright, coverage, **TDD agents** |
| 8 | [Security & Automation](phases/PHASE_8_SECURITY_AUTOMATION_DevSecOps.md) | DevSecOps | Headers, CSP, rate limiting, **security agents** |
| 9 | [Accessibility & i18n](phases/PHASE_9_ACCESSIBILITY__INTERNATIONALIZATION_UIUX_Designer_Frontend_Developer.md) | UI/UX, Frontend | WCAG 2.2, RTL, locales, reduced motion |
| 10 | [Performance Optimization](phases/PHASE_10_PERFORMANCE_OPTIMIZATION_Frontend_Backend_DevOps.md) | Frontend, Backend, DevOps | Caching, bundle analysis, image opt |
| 11 | [DevOps & Infrastructure](phases/PHASE_11_DEVOPS__INFRASTRUCTURE_DevOps_Engineer.md) | DevOps Engineer | Infra, CDN, edge, databases |
| 12 | [Observability & Monitoring](phases/PHASE_12_OBSERVABILITY__MONITORING_DevOps_SRE.md) | DevOps, SRE | Logging, tracing, alerting |
| 13 | [Deployment & CI/CD](phases/PHASE_13_DEPLOYMENT__CICD_DevOps_Engineer.md) | DevOps Engineer | GitHub Actions, preview deploys |
| 14 | [Pre-Launch Checklist](phases/PHASE_14_PRE-LAUNCH_CHECKLIST_All_Roles.md) | All Roles | Final checks, **verification loop**, **launch readiness** |
| 15 | [AI & LLM Integration](phases/PHASE_15_AI__LLM_INTEGRATION_AI_Engineer.md) | AI Engineer | Vercel AI SDK, tool calling, RAG |
| 16 | [Payment & Subscription](phases/PHASE_16_PAYMENT__SUBSCRIPTION_SYSTEM_Full-Stack_Engineer.md) | Full-Stack Engineer | Stripe, webhooks, billing portal |
| 17 | [Mobile & PWA](phases/PHASE_17_MOBILE__PWA_Frontend_Engineer.md) | Frontend Engineer | Manifest, offline, install prompts |
| 18 | [Analytics & Feature Flags](phases/PHASE_18_ANALYTICS__FEATURE_FLAGS_Product_Engineer.md) | Product Engineer | PostHog (consent-gated), flags |
| 19 | [API Documentation & Versioning](phases/PHASE_19_API_DOCUMENTATION__VERSIONING_Backend_Engineer.md) | Backend Engineer | Scalar, OpenAPI, SDK generation |
| 20 | [Error Handling & Resilience](phases/PHASE_20_ERROR_HANDLING__RESILIENCE_Full-Stack_Engineer.md) | Full-Stack Engineer | AppError class, retries, fallbacks |

---

## 🤖 ECC Agent Harness

The **Everything Claude Code (ECC)** architecture transforms this prompt library into a machine-executable framework with four layers:

### Agent Quick Reference

| Command | Agent | Purpose | Phase |
|---------|-------|---------|-------|
| `/planner` | [Planner](agents/planner.md) | Structured task decomposition | 0 |
| `/code-reviewer` | [Code Reviewer](agents/code-reviewer.md) | Severity-based code review | All |
| `/security-reviewer` | [Security Reviewer](agents/security-reviewer.md) | Full security audit | 8, 14 |
| `/tdd-guide` | [TDD Guide](agents/tdd-guide.md) | RED→GREEN→REFACTOR | 7 |
| `/build-error-resolver` | [Build Error Resolver](agents/build-error-resolver.md) | Build/type error diagnosis | All |
| `/e2e-runner` | [E2E Runner](agents/e2e-runner.md) | Playwright test execution | 7 |
| `/seo-specialist` | [SEO Specialist](agents/seo-specialist.md) | SEO and metadata audit | 14 |
| `/performance-optimizer` | [Performance Optimizer](agents/performance-optimizer.md) | Core Web Vitals optimization | 10, 14 |

### Skills

| Skill | Description | Used By |
|-------|-------------|---------|
| [tdd-workflow](skills/tdd-workflow/SKILL.md) | RED→GREEN→REFACTOR with coverage gates | TDD Guide |
| [security-review](skills/security-review/SKILL.md) | Auth, CSP, env var, input validation | Security Reviewer |
| [verification-loop](skills/verification-loop/SKILL.md) | 6-gate quality pipeline | All Agents |
| [planner-workflow](skills/planner-workflow/SKILL.md) | Structured planning methodology | Planner |
| [code-review](skills/code-review/SKILL.md) | Severity-based code review | Code Reviewer |
| [nextjs-patterns](skills/nextjs-patterns/SKILL.md) | App Router, Server Components | Performance Optimizer |
| [deployment-patterns](skills/deployment-patterns/SKILL.md) | Pre-deployment checklist & CI/CD | SEO Specialist |
| [strategic-compact](skills/strategic-compact/SKILL.md) | Context management | — |
| [session-manager](skills/session-manager/SKILL.md) | Persistence and handoff | — |
| [continuous-learning](skills/continuous-learning/SKILL.md) | Knowledge capture | — |

### Hooks (Automated Lifecycle Events)

| Hook | Event | Effect |
|------|-------|--------|
| [post-edit-format](hooks/post-edit-format.json) | After file edit | Auto-format with Biome |
| [post-edit-typecheck](hooks/post-edit-typecheck.json) | After file edit | Run `tsc --noEmit` |
| [pre-edit-security-scan](hooks/pre-edit-security-scan.json) | Before file write | Block hardcoded secrets |
| [stop-session-save](hooks/stop-session-save.json) | Session end | Persist session state |
| [stop-console-log-audit](hooks/stop-console-log-audit.json) | Session end | Flag `console.log` |

---

## 🛠️ Target Tech Stack

| Category | Technology |
|---|---|
| **Framework** | Next.js 15/16+ (App Router) |
| **React** | 19+ (`useActionState`, `useOptimistic`, `use()`) |
| **Language** | TypeScript (strict) |
| **Node.js** | 22+ |
| **Styling** | Tailwind CSS v4 (`@theme`, `oklch()`) |
| **UI** | shadcn/ui |
| **Database** | PostgreSQL (Neon/Supabase), MongoDB, Convex |
| **ORM** | Prisma / Drizzle |
| **Auth** | Better Auth / Auth.js v5 / Clerk |
| **Email** | Resend + React Email |
| **AI** | Vercel AI SDK (OpenAI, Anthropic) |
| **Design** | Google Stitch (`DESIGN.md`, MCP, SDK) |
| **Payments** | Stripe |
| **Linting** | Biome |
| **Testing** | Vitest + Playwright |
| **Deployment** | Vercel / AWS |
| **CI/CD** | GitHub Actions |
| **Analytics** | PostHog |
| **Bundler** | Turbopack |

---

## 🔑 Key Patterns

### PostHog — Consent-Gated Init (Phase 18)

```tsx
// components/consent-banner.tsx
useEffect(() => {
  const consent = getConsent()
  if (consent === 'granted') posthog.init(/* ... */)
  else posthog.opt_out_capturing()
}, [])
```

### Scalar API Docs — Next.js 15+ Import (Phase 19)

```typescript
// app/api/docs/ui/route.ts
import { ApiReference } from '@scalar/nextjs-api-reference'
export const GET = ApiReference({ spec: { url: '/api/docs' } })
```

### AppError Base Class (Phase 20)

```typescript
// lib/errors.ts
export class AppError extends Error {
  constructor(message: string, public code: string, public statusCode = 500) {
    super(message)
    this.name = 'AppError'
  }
}
```

### Google Stitch — DESIGN.md (Phase 0.8)

```markdown
<!-- DESIGN.md — placed at project root -->
# Design System
## Colors
- Primary: oklch(0.7 0.15 250)
- Background: oklch(0.15 0.01 260)
## Typography
- Heading: "Inter", sans-serif, 700
- Body: "Inter", sans-serif, 400
## Spacing
- Base: 4px, Scale: 4/8/12/16/24/32/48/64
```

---

## 🤖 Using with AI Agents

### Google Antigravity (Recommended)

Antigravity's **Planning Mode** with **artifacts** (Implementation Plans, Task Lists, Walkthroughs, Knowledge Items) maps directly to this library's phase structure. See **Prompt 0.10** for the full integration guide.

```
Read ./phases/PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md
and execute Prompt 0.1 for my project.
```

Antigravity will automatically produce an Implementation Plan artifact for review before writing code.

### With ECC Agent Harness (Any Agent)

```
Read ./CLAUDE.md for project context, then:
1. /planner — to decompose the first milestone
2. /tdd-guide — to write tests before implementation
3. /verify — to run the 6-gate quality pipeline
```

### Claude Code / Cursor / Copilot

Point to a phase file and execute:

```
Read ./phases/PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md
and execute Prompt 0.1 for my project.
```

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [CLAUDE.md](CLAUDE.md) | Agent meta-configuration — start here for AI agents |
| [Security Guide](docs/SECURITY_GUIDE.md) | Comprehensive security reference with code patterns |
| [Contributing](docs/CONTRIBUTING.md) | How to add skills, agents, hooks, and rules |
| [Hooks README](hooks/README.md) | Hook configuration and adaptation guide |

---

## 📄 License

MIT
