<a name="phase-0"></a>
# 📌 PHASE 0: PLANNING & SETUP (Product Manager, UI/UX Designer)

> **Next.js Version:** This workflow targets **Next.js (latest)**. See [Prompt 0.7](#prompt-07) for a version compatibility reference.

---

### Prompt 0.1: Generate Project Ideas

```text
You are an expert digital product consultant. I want to build a Next.js full-stack project to add to my portfolio.

About me:
- Skill Level: [beginner/intermediate/advanced]
- Interests: [e.g., e-commerce, education, health, social media, AI-powered apps]
- Time Available: [hours per week / total weeks]
- Goal: [learning / portfolio / actual product]

Tools I plan to use for development:
- AI-assisted development: Claude Code, Cursor, or GitHub Copilot
- UI prototyping: v0.dev (Vercel's AI UI generator), Figma, or Excalidraw
- Project management: Linear, GitHub Projects, or Notion

Required Output:
1. Suggest 5 Next.js project ideas matching my skill level
2. For each idea, provide:
   - Description (2-3 sentences)
   - Core Features (3-5 features)
   - Difficulty Level (1-5 stars)
   - Estimated Time to Complete
   - Why this project is good for portfolio
   - Recommended rendering strategy (SSR, SSG, ISR, PPR, or hybrid)
   - AI-powered features that could be added (if applicable)
   - Monetization potential (free tier, subscription, one-time purchase)

Rank ideas from easiest to most challenging.
```

---

### Prompt 0.2: Create Product Requirements Document (PRD)

```text
You are a professional Product Manager at a tech company. I want to build [describe your project idea] using Next.js.

Required: Create a comprehensive Product Requirements Document containing:

## 1. Project Overview
## 2. Core Objectives
## 3. User Stories
   - Format: "As a [role], I want to [action], so that [benefit]"
   - Include at least 10 user stories for MVP
## 4. Core Features (MVP)
## 5. Future Features (Post-MVP)
## 6. Rendering Strategy Decisions:
   - Which pages use Static Generation (SSG)?
   - Which pages use Server-Side Rendering (SSR)?
   - Which pages use Incremental Static Regeneration (ISR)?
   - Which pages use Client-Side Rendering (CSR)?
   - Which pages use Partial Prerendering (PPR)?
   - Which pages use Edge Runtime?
   - Which pages use Streaming SSR with Suspense?
## 7. AI-Powered Features (if applicable):
   - AI-powered search or recommendations
   - Chatbot or assistant features
   - Content generation or summarization
   - Image generation or processing
   - Structured data extraction
## 8. Constraints & Assumptions
## 9. Success Metrics (quantifiable KPIs)
## 10. Out of Scope
## 11. Privacy & Compliance Requirements
   - GDPR / CCPA considerations
   - Cookie consent requirements
   - Data retention policies
   - User data export/deletion (right to erasure)

Use clear Markdown formatting with proper headings and bullet points.
```

---

### Prompt 0.3: Create Technical Design Document

```text
You are a Senior Software Architect specializing in Next.js. Based on this PRD:

[Paste your PRD here]

Required: Create a Technical Design Document containing:

## 1. Architecture Overview
   - App Router architecture and patterns
   - Server Components vs Client Components strategy
   - Route Handlers vs Server Actions strategy
   - Middleware usage plan
   - Parallel Routes and Intercepting Routes plan
   - Route Groups organization
## 2. Technology Stack Decisions
   - Next.js version (latest stable — see Phase 0.7 compatibility table)
   - React version (19+)
   - Database: PostgreSQL (Neon/Supabase), MongoDB, Convex, or other
   - ORM: Prisma, Drizzle ORM, or direct database driver (unless using Convex)
   - Authentication: Better Auth, Auth.js v5, Clerk, or custom
   - Email: Resend with React Email
   - UI Library: Tailwind CSS v4, Shadcn/ui, or other
   - State Management: Zustand, Jotai, or React Context (note: React 19 reduces need for state management)
   - AI Integration: Vercel AI SDK (if applicable)
   - Payments: Stripe (if applicable)
   - File Upload: UploadThing, Vercel Blob, or Cloudinary
   - Deployment: Vercel, AWS, or other
   - Bundler: Turbopack (default in latest Next.js)
   - Linting: Biome (recommended) or ESLint flat config
   - Testing: Vitest + Playwright
   - Analytics: PostHog (open-source) or Vercel Analytics
   - Error Tracking: Sentry
## 3. Detailed Project Structure
   - App Router structure (app/, components/, lib/, etc.)
   - Route Groups, Parallel Routes, Intercepting Routes
   - Public assets organization
## 4. Database Schema Design
## 5. Route Handlers Specifications (if using Route Handlers)
## 6. Server Actions Design (primary approach for mutations)
## 7. Authentication & Authorization Flow
## 8. Error Handling Strategy
   - Error boundaries (error.tsx, global-error.tsx, not-found.tsx)
   - Retry logic for external API calls
   - Graceful degradation patterns
## 9. Security Considerations
## 10. Performance Optimization Strategy (including PPR)
## 11. Testing Strategy (Vitest + Playwright)
## 12. AI Integration Strategy (if applicable)
## 13. API Documentation & Versioning Strategy
   - OpenAPI/Swagger documentation approach
   - API versioning strategy (URL prefix, header, or query param)
   - SDK generation plan (if public API)
## 14. Monorepo Considerations (if applicable)
   - Turborepo setup for shared packages
   - Shared UI component library
   - Shared TypeScript types and validation schemas

Justify each technology choice with reasoning.
```

---

### Prompt 0.4: Break Down Into Tasks

```text
You are an Agile Project Manager. Based on this Technical Design Document:

[Paste your Technical Design Document here]

Required: Break the project into actionable tasks in Kanban format.

## Task Format:
For each task:
- **ID**: TASK-001
- **Title**: Clear, action-oriented title
- **Category**: [Setup/Backend/Frontend/Database/Auth/Features/AI/Testing/Deployment/Security/DevOps]
- **Priority**: [Critical/High/Medium/Low]
- **Estimated Time**: [hours]
- **Dependencies**: List task IDs that must be completed first
- **Description**: Detailed what needs to be done
- **Acceptance Criteria**: How do we know it's complete? (checklist)
- **Technical Notes**: Hints, gotchas, resources
- **Phase Reference**: Which workflow phase this maps to (Phase 1-20)

Order tasks by dependencies so they can be completed sequentially.
Create at least 30-50 tasks for a medium-sized project.

Group tasks into milestones:
- **Milestone 1**: Project Setup & Infrastructure (Phase 1-3)
- **Milestone 2**: Core Features & Auth (Phase 4-6)
- **Milestone 3**: Testing & Security (Phase 7-8)
- **Milestone 4**: Polish & Optimization (Phase 9-10)
- **Milestone 5**: Deployment & Launch (Phase 11-14)
- **Milestone 6**: Advanced Features (Phase 15-20)
```

---

### Prompt 0.5: Create Wireframes for Project

```text
You are a UI/UX Designer. Create wireframes for [ProjectName] using Next.js.

Required Output:
1. Sketch main pages/screens:
   - Landing/Home Page
   - Login/Register Pages (including OAuth buttons, magic link, passkey option)
   - Dashboard/User Panel
   - Resource Management Pages (List, Detail, Form)
   - Settings & Profile Page
   - Notifications/Alerts Page
   - AI Chat/Assistant Interface (if applicable)
   - Error pages (404, 500, maintenance)
2. Consider Next.js-specific features:
   - Navigation patterns (client-side vs server-side)
   - Loading states and Suspense boundaries with skeletons
   - Error boundaries (error.tsx)
   - Streaming UI patterns with React Suspense
   - Parallel Routes for split-view layouts
   - Intercepting Routes for modals
   - Cookie consent banner placement
3. Include layout structure:
   - Root layout (app/layout.tsx)
   - Nested layouts for different sections
   - Route Groups for layout organization
   - Template components for transitions
4. Provide notes for each wireframe:
   - User flow explanation
   - Navigation hierarchy
   - CTA (Call-to-Action) positions
   - Responsive breakpoints (mobile, tablet, desktop)
5. Output:
   - ASCII-style wireframe OR image URL placeholder for AI generation
   - Annotations for key components
   - Consider using v0.dev to generate initial component prototypes
```

---

### Prompt 0.6: Create UI Design System

```text
You are a UI/UX Designer. Create a comprehensive UI Design system for [ProjectName] using Next.js and Tailwind CSS v4.

Required Output:
1. Color Palette
   - Primary, Secondary, Accent, Background, Text colors
   - Light & Dark Mode variants using CSS custom properties
   - Tailwind CSS v4 theme configuration (CSS-based, not JS config)
   - Use oklch() color space for perceptually uniform colors
2. Typography
   - Font families for headings, body, buttons
   - Font sizes, weights, line heights
   - Arabic & Latin font pairing if multilingual
   - next/font configuration for Google Fonts or local fonts
3. Components
   - Buttons, Inputs, Forms
   - Cards, Tables, Modals, Alerts
   - Navigation (Header, Footer, Sidebar)
   - Notifications, Toasts (sonner), Loading indicators
   - Shadcn/ui component integration plan (if using)
   - Skeleton components for loading states
4. Layout & Spacing
   - Container widths, margins, paddings
   - Grid system (desktop, tablet, mobile)
   - Tailwind CSS v4 spacing scale
5. Design Tokens
   - CSS custom properties for theming (Tailwind v4 approach)
   - @theme directive configuration
6. Accessibility
   - Contrast ratios (WCAG 2.2 AA minimum)
   - Focus states (focus-visible)
   - ARIA labels
   - Keyboard navigation
   - Reduced motion preferences (@media prefers-reduced-motion)
7. Motion & Animation
   - Framer Motion integration
   - Micro-interactions
   - Page transitions
   - View Transitions API
   - Respect prefers-reduced-motion

Include Tailwind CSS v4 configuration snippets using the new CSS-based config approach.
```

---

<a name="prompt-07"></a>
### Prompt 0.7: Version Compatibility Reference

Use this table to determine which features are available in your Next.js version. Always check the [Next.js release notes](https://nextjs.org/blog) for the most current information.

| Feature | Next.js 14 | Next.js 15 | Next.js 16+ | Notes |
|---|---|---|---|---|
| App Router | ✅ Stable | ✅ Stable | ✅ Stable | Default since 13.4 |
| Server Actions | ✅ Stable | ✅ Stable | ✅ Stable | `'use server'` directive |
| Turbopack (dev) | ⚡ Beta | ✅ Stable | ✅ Default | `--turbopack` flag (14-15), default (16+) |
| Turbopack (build) | ❌ | 🧪 Alpha | ✅ Stable | Production builds |
| Partial Prerendering (PPR) | 🧪 Experimental | 🧪 Experimental | ✅ Stable | `experimental.ppr` in config |
| React 19 | ❌ | ✅ Stable | ✅ Stable | `useActionState`, `useOptimistic`, `use()` |
| React Compiler | ❌ | 🧪 Experimental | ✅ Stable | Auto-memoization, no manual `useMemo`/`useCallback` |
| `after()` API | ❌ | 🧪 Experimental | ✅ Stable | Background tasks after response |
| `forbidden()` / `unauthorized()` | ❌ | 🧪 Experimental | ✅ Stable | Auth-specific error helpers |
| `next.config.ts` | ❌ | ✅ Stable | ✅ Stable | TypeScript config file |
| Async `searchParams` / `params` | ❌ | ✅ Required | ✅ Required | Must `await` in 15+ |
| `unstable_cache` | ✅ | ✅ | ⚠️ May rename | May become `cache()` — add stability caveats |
| `cacheLife` / `cacheTag` | ❌ | 🧪 Experimental | ✅ Stable | Replaces `unstable_cache` |
| View Transitions | ❌ | ❌ | 🧪 Experimental | CSS View Transitions API |
| Middleware (enhanced) | ✅ | ✅ | ✅ | Route matching, headers, rewrites |
| `instrumentation.ts` | ✅ Stable | ✅ Stable | ✅ Stable | OpenTelemetry, monitoring setup |
| Static Indicator | ❌ | ✅ | ✅ | Dev mode visual indicator |

**React 19 Hooks Reference:**

| Hook | Purpose | Replaces |
|---|---|---|
| `useActionState` | Form state with Server Actions | `useFormState` (deprecated) |
| `useOptimistic` | Optimistic UI updates | Manual state + rollback |
| `use()` | Unwrap promises/context in render | `useEffect` + `useState` for data |
| `useFormStatus` | Pending state for forms | Manual `isPending` state |

> **Tip:** Always run `npx next info` to check your current Next.js and React versions, and consult the [Next.js Upgrade Guide](https://nextjs.org/docs/app/building-your-application/upgrading) when moving between major versions.
