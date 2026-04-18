<a name="phase-0"></a>
# 📌 PHASE 0: PLANNING & SETUP (Product Manager, UI/UX Designer)

> **Next.js Version:** This workflow targets **Next.js (latest)**. See [Prompt 0.7](#prompt-07) for a version compatibility reference.

---

### Prompt 0.1: Generate Project Ideas

```text
You are an expert Digital Product Consultant and Senior Software Architect. You have a deep understanding of modern web application trends, technical feasibility, and portfolio-building strategies. I want to build a Next.js full-stack project to add to my portfolio.

About me:
- Skill Level: [beginner/intermediate/advanced]
- Interests: [e.g., e-commerce, education, health, social media, AI-powered apps]
- Time Available: [hours per week / total weeks]
- Goal: [learning / portfolio / actual product]

Tools I plan to use for development:
- AI-assisted development: **Google Antigravity**, Claude Code, Cursor, or GitHub Copilot
- AI-powered UI design: **Google Stitch** (stitch.withgoogle.com), v0.dev
- UI prototyping: Figma or Excalidraw
- Project management: Linear, GitHub Projects, or Notion

Constraints:
- Avoid ideas that require complex hardware integrations or unreleased APIs.
- Must be buildable within the stated time frame.
- Must demonstrate full-stack capabilities (auth, database, API, frontend).

Required Output Format:
1. Suggest 5 Next.js project ideas matching my skill level, ranked by Portfolio Impact Score (1-10).
2. For each idea, provide:
   - Description (2-3 sentences)
   - Core Features (3-5 features)
   - Difficulty Level (1-5 stars)
   - Estimated Time to Complete
   - Why this project is good for a portfolio (specific skills demonstrated)
   - Recommended rendering strategy (SSR, SSG, ISR, PPR, or hybrid)
   - AI-powered features that could be added (if applicable)
   - Tech Stack Compatibility Matrix summary (briefly note best ORM/auth pairs for this idea)
   - Monetization potential (free tier, subscription, one-time purchase)

Decision Guide:
- Rank ideas placing the highest balance of "impressiveness" vs "achievability" first.
```

✅ **Verification Checklist:**
- [ ] You have selected one project idea to proceed with.
- [ ] The idea fits your realistic time constraints.
- [ ] You understand the high-level rendering strategy required.

---

### Prompt 0.2: Create Product Requirements Document (PRD)

```text
You are a Lead Product Manager with expertise in agile methodologies and Next.js applications. I want to build [describe your project idea] using Next.js.

Constraints:
- Do not include features that are technically incompatible with standard web browsers.
- Focus the MVP strictly on core value delivery. Post-MVP can contain nice-to-haves.

Required Output Format: Create a comprehensive Product Requirements Document containing:

## 1. Project Overview & User Personas
   - Briefly define 2-3 target user personas.
## 2. Core Objectives
## 3. User Stories
   - Format: "As a [role], I want to [action], so that [benefit]"
   - Include exactly 10-15 critical user stories for MVP.
## 4. Core Features (MVP)
## 5. Future Features (Post-MVP)
## 6. Rendering Strategy Decisions:
   - Which pages use Static Generation (SSG)?
   - Which pages use Server-Side Rendering (SSR)?
   - Which pages use Client-Side Rendering (CSR)?
   - Which pages use Partial Prerendering (PPR)?
## 7. AI-Powered Features (if applicable):
## 8. Data Flow Diagrams Requirement (Conceptual)
   - Describe verbally how data moves from user input -> API -> Database -> AI -> Output.
## 9. Constraints & Assumptions
## 10. Success Metrics (quantifiable KPIs)
## 11. Out of Scope
## 12. Privacy & Compliance Requirements
   - GDPR / CCPA, Cookie consent, Data retention.
```

✅ **Verification Checklist:**
- [ ] The PRD clearly defines what is in the MVP vs what is deferred.
- [ ] Requirements mapping to user stories is consistent.
- [ ] You have reviewed the rendering strategies with Next.js capabilities in mind.

---

### Prompt 0.3: Create Technical Design Document

```text
You are a Principal Next.js Architect. You excel at designing scalable, maintainable, and highly performant web applications using the latest Next.js features (App Router, Server Components). Based on this PRD:

[Paste your PRD here]

Constraints:
- Do NOT recommend Pages Router. Strictly stick to App Router.
- Do NOT recommend traditional Redux unless strictly necessary; prefer React 19 native state/context or Zustand.
- Ensure authentication recommendations align with Next.js edge compatibility if middleware is used.

Required Output Format: Create a Technical Design Document containing:

## 1. Architecture Overview
   - Server Components vs Client Components strategy
   - Route Handlers vs Server Actions strategy
## 2. Technology Stack Decisions
   - Ensure you use the Phase 0.7 compatibility matrix.
   - Include an Architecture Decision Record (ADR) style justification for: Database, ORM, Auth, Styling.
## 3. Detailed Project Structure
   - Example folder tree highlighting Route Groups.
## 4. Database Schema Design (High-Level)
## 5. Next.js Patterns Specifications
   - Server Actions Design (primary approach for mutations)
## 6. Authentication & Authorization Flow
## 7. Error Handling & Resilience Strategy
## 8. Security Considerations (OWASP alignment)
## 9. Performance Optimization Strategy (including PPR)
## 10. Testing Strategy
## 11. API Documentation Strategy
## 12. Tradeoff Analysis
   - Specifically note the drawbacks of the chosen stack (e.g., "Prisma has higher cold start times in serverless, so we will use Prisma Accelerate").
```

✅ **Verification Checklist:**
- [ ] Architecture aligns with the PRD requirements.
- [ ] Next.js version capabilities (e.g., React 19) are considered fully.

---

### Prompt 0.4: Break Down Into Tasks

```text
You are an Agile Technical Project Manager and Scrum Master. Based on this Technical Design Document:

[Paste your Technical Design Document here]

Constraints:
- Tasks must be small enough to be completed in 2-4 hours.
- Tasks must have clear dependencies.
- Avoid vague "Implement feature X" descriptions; be highly specific.

Required Output Format: Break the project into actionable tasks in Kanban format.

## Task Format (For each task):
- **ID**: TASK-001
- **Title**: Clear, action-oriented title
- **Category**: [Setup/Backend/Frontend/Database/Auth/Features/AI/Testing/Deployment/Security/DevOps]
- **Priority**: [Critical/High/Medium/Low]
- **Estimated Time**: [hours]
- **Dependencies**: List task IDs that must be completed first
- **Description**: Detailed what needs to be done
- **Risk Assessment**: Identify any technical risks specific to this task.
- **Acceptance Criteria**: Checkbox list to prove completion.
- **Phase Reference**: Which workflow phase this maps to (Phase 1-20)

Group tasks into these Milestones:
- Milestone 1: Infrastructure & Auth (Phase 1-4)
- Milestone 2: Core UI & Database (Phase 3, 5)
- Milestone 3: Advanced Features & AI (Phase 6, 15, 16)
- Milestone 4: Security, QA, Perf (Phase 7-10)
- Milestone 5: Launch & Obs (Phase 11-14, 18-20)
```

✅ **Verification Checklist:**
- [ ] Task IDs are unique and sequential.
- [ ] No circular dependencies exist.
- [ ] Time estimates align with your stated availability.

---

### Prompt 0.5: Create Wireframes for Project

```text
You are an expert UI/UX Designer specializing in Next.js applications and component-driven design. Create wireframes for [ProjectName].

Constraints:
- Designs must account for Server Component boundaries (where interactive client components sit inside static server layouts).
- Must adhere to a mobile-first responsive approach.
- Assume use of `lucide-react` for icons and `shadcn/ui` for primitives.

Required Output Format:
1. Sketch main pages/screens (ASCII-style wireframe OR detailed description for a **Google Stitch** / v0.dev prompt):
   - Landing/Home
   - Auth (Login/Register)
   - Dashboard
   - 2 Core Feature Pages
2. Information Architecture & Navigation hierarchy
3. Next.js Layout Mapping:
   - Map screens to Next.js route structures (e.g., `app/(dashboard)/layout.tsx`)
   - Note where Parallel or Intercepting routes are used for modals/split-views.
4. Loading & Error States:
   - Define skeleton structures for `loading.tsx` boundaries.
5. Responsive Matrix:
   - Define a mobile-first layout strategy using intrinsic CSS grids/flexbox, `fr` units, and fluid typography rather than relying on an excessive amount of fixed-pixel media queries.
6. (Optional) Google Stitch Design Prompt:
   - Using the IDEA + THEME + CONTENT formula, write 3-5 structured Stitch prompts to generate high-fidelity screens for each primary view.
```

✅ **Verification Checklist:**
- [ ] Wireframes account for loading states (Suspense boundaries).
- [ ] Layout mapping correctly uses Next.js app router conventions.

---

### Prompt 0.6: Create UI Design System

```text
You are a Principal UI/UX Engineer. Create a comprehensive UI Design system for [ProjectName] using Next.js and Tailwind CSS v4.

Constraints:
- Use Tailwind v4 CSS-based configuration (no `tailwind.config.js` unless explicitly necessary for legacy plugins).
- Do not use generic names like "blue"; use semantic tokens (e.g., `primary`, `accent`).
- Must pass WCAG 2.2 AA accessibility standards.

Required Output Format:
1. Color Palette (oklch)
   - Light & Dark mode mapping matrices.
2. Typography
   - Recommendations for Google Fonts via `next/font`.
3. Design Token Naming Convention
   - E.g., `--color-primary-500` mapping strategy.
4. Component Variant Matrix
   - Define button variants (default, outline, ghost) and sizes.
5. Motion & Animation Strategy
   - Define default transition durations and Framer Motion spring configs.
6. Accessibility (a11y) Baseline
   - Focus ring styling, screen reader text strategies (`sr-only`), reduced motion toggles (`prefers-reduced-motion`).
```

✅ **Verification Checklist:**
- [ ] Design tokens leverage `@theme` from Tailwind v4.
- [ ] Light/Dark mode variables are properly isolated.
- [ ] Font imports align with Next.js optimization practices.

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
| Partial Prerendering | 🧪 Exp | 🧪 Exp | ✅ Stable | `experimental.ppr` in config |
| React 19 | ❌ | ✅ Stable | ✅ Stable | `useActionState`, `useOptimistic`, `use()` |
| React Compiler | ❌ | 🧪 Exp | ✅ Stable | Auto-memoization |
| `after()` API | ❌ | 🧪 Exp | ✅ Stable | Background tasks after response |
| `next.config.ts` | ❌ | ✅ Stable | ✅ Stable | TypeScript config file |
| Async Params | ❌ | ✅ Req | ✅ Req | Must `await searchParams` in 15+ |

**React 19 Hooks Reference:**

| Hook | Purpose | Replaces |
|---|---|---|
| `useActionState` | Form state with Server Actions | `useFormState` (deprecated) |
| `useOptimistic` | Optimistic UI updates | Manual state + rollback |
| `use()` | Unwrap promises/context in render | `useEffect` + `useState` for data |

> **⚠️ Common Pitfalls:**
> 1. **Forgetting to await params:** In Next 15+, `searchParams` and `params` are Promises. Failing to `await` them causes runtime errors.
> 2. **Over-using client components:** Default to Server Components. Only drop `'use client'` down the tree to the specific interactive nodes.

---

### Prompt 0.11: ECC Agent Harness Setup

```text
You are a Development Infrastructure Architect. Set up the Everything Claude Code (ECC) agent harness architecture for a new Next.js project to enable machine-executable, agentic development workflows.

What is ECC?
ECC is a performance optimization architecture for AI agent harnesses. It organizes development knowledge into four layers:
1. **Skills** — Reusable, domain-specific workflow knowledge (TDD, security review, verification loops).
2. **Agents** — Specialized subagent definitions with scoped tools and model configurations.
3. **Hooks** — Automated lifecycle event handlers (format-on-save, type-check-on-edit, security scan).
4. **Rules** — Mandatory project-wide constraints and coding standards.

These layers are consumed by AI coding agents (Claude Code, Google Antigravity, Cursor) to enforce production-grade quality automatically.

Constraints:
- Skills MUST use YAML frontmatter for machine-readability.
- Agents MUST declare `allowed_tools`, `model`, and `trigger` fields.
- Hooks MUST be JSON templates compatible with `.claude/settings.json`.
- Rules MUST be organized by domain (`common/`, `typescript/`).

Required Output Format:
1. Directory structure creation:
   ```
   .claude/
   ├── settings.json         # Hook configurations
   ├── agents/               # Subagent definitions
   └── skills/               # Project-specific skills
   ```
2. Copy and adapt from the workflow library:
   - **Required Skills:** `tdd-workflow`, `security-review`, `verification-loop`, `planner-workflow`
   - **Required Agents:** `planner`, `code-reviewer`, `security-reviewer`, `tdd-guide`
   - **Required Hooks:** `post-edit-format`, `post-edit-typecheck`, `pre-edit-security-scan`
   - **Required Rules:** `development-workflow`, `security`, `testing`
3. A `CLAUDE.md` file at the project root serving as the meta-configuration, referencing:
   - Project tech stack and conventions
   - Active skills and agents
   - Phase tracking table
4. Integration with the Antigravity artifact system (Implementation Plans, Task Lists, Knowledge Items).

⚠️ Common Pitfalls:
- **Pitfall:** Installing ALL skills/agents when only a subset is needed, bloating agent context.
- **Solution:** Start with the 4 required skills and 4 required agents. Add domain-specific ones (e.g., `seo-specialist`, `e2e-runner`) only when reaching the relevant phase.
- **Pitfall:** Not adapting hook JSON templates to the project's actual tool paths (e.g., `biome` vs `eslint`).
- **Solution:** Review each hook's `matcher` and `command` fields against the project's `package.json` scripts.
```

✅ **Verification Checklist:**
- [ ] `.claude/settings.json` exists and contains valid hook configurations.
- [ ] `CLAUDE.md` exists at the project root with tech stack and phase tracking.
- [ ] All required skills have valid YAML frontmatter (parse with `yq` or equivalent).
- [ ] Running `/planner` in the agent harness correctly invokes the planner subagent.

**📚 ECC Skill References:**
| Workflow Step | Skill | Agent |
|---|---|---|
| Planning | [`planner-workflow`](../skills/planner-workflow/SKILL.md) | [`planner`](../agents/planner.md) |
| Code Review | [`code-review`](../skills/code-review/SKILL.md) | [`code-reviewer`](../agents/code-reviewer.md) |
| Security Audit | [`security-review`](../skills/security-review/SKILL.md) | [`security-reviewer`](../agents/security-reviewer.md) |
| Test-Driven Dev | [`tdd-workflow`](../skills/tdd-workflow/SKILL.md) | [`tdd-guide`](../agents/tdd-guide.md) |
| Verification | [`verification-loop`](../skills/verification-loop/SKILL.md) | — |
| Context Management | [`strategic-compact`](../skills/strategic-compact/SKILL.md) | — |
| Session Persistence | [`session-manager`](../skills/session-manager/SKILL.md) | — |

---
📎 **Related Phases:**
- Proceeds to: [Phase 1: Project Structure & Configuration](./PHASE_1_PROJECT_STRUCTURE__CONFIGURATION_Full-Stack_Developer.md)

---

<a name="prompt-08"></a>
### Prompt 0.8: Google Stitch — AI-Powered Design Ideation & DESIGN.md

```text
You are a Design Agent Specialist using Google Stitch (stitch.withgoogle.com) to accelerate UI ideation for a Next.js project.

What is Google Stitch?
Stitch is Google's AI-powered design tool that generates high-fidelity UI screens from natural language prompts. It outputs a `DESIGN.md` file — a plain-text, agent-readable design system document that serves as the visual "source of truth" alongside your code.

Key Concepts:
- **Prompt Formula:** IDEA + THEME + CONTENT
  - IDEA: High-level project goal (e.g., "A fitness tracking dashboard")
  - THEME: Visual direction using adjectives (e.g., "Sleek, dark mode, neon green accents")
  - CONTENT: Specific UI elements/data (e.g., "Weekly progress chart, calorie counter, workout list")
- **DESIGN.md:** A living artifact that defines your color palette, typography, spacing, and component patterns. Design agents (Stitch) read it; coding agents (Claude, Cursor) consume it to maintain visual consistency.
- **Iterative Refinement:** Start with a broad prompt, then refine screen-by-screen. Apply the "one major change at a time" rule for best results.

Constraints:
- Generate screens for both **Web** and **Mobile App** device types to cover responsive needs.
- Use Stitch's design variation feature to explore 2-3 alternatives before committing to a direction.
- Export the `DESIGN.md` and place it at the project root so coding agents can reference it automatically.
- When downloading artifacts, cross-reference the generated HTML/CSS with your Tailwind v4 design tokens — do NOT blindly copy raw inline styles.

Required Output Format:
1. Five structured Stitch prompts (one per primary screen: Landing, Auth, Dashboard, Detail View, Settings) following the IDEA + THEME + CONTENT formula.
2. A `DESIGN.md` file defining:
   - Color palette (with oklch/hex values)
   - Typography scale (font families, sizes, weights)
   - Spacing system (base unit, scale)
   - Component patterns (cards, buttons, inputs, navigation)
3. A mapping table from Stitch's generated screens → Next.js route files.

⚠️ Common Pitfalls:
- **Pitfall:** Treating Stitch output as final production code. Stitch generates HTML for design visualization — it is NOT optimized for React Server Components or Next.js routing.
- **Solution:** Use Stitch strictly for **design ideation and `DESIGN.md` generation**. Translate its visual output into your component library manually or via coding agents.
- **Pitfall:** Generating all screens in one massive prompt, resulting in inconsistent styling across pages.
- **Solution:** Generate one screen at a time. Let Stitch build a visual identity from the first screen, then iterate subsequent screens referencing the established `DESIGN.md`.
```

✅ **Verification Checklist:**
- [ ] `DESIGN.md` exists at the project root and contains color, typography, spacing, and component sections.
- [ ] Each primary screen has at least 2 design variations explored in Stitch before finalizing.
- [ ] The design tokens in `DESIGN.md` are consistent with your `globals.css` / Tailwind `@theme` definitions.

---

### Prompt 0.9: Stitch SDK & MCP Integration (Agent Automation)

```text
You are a Design Automation Engineer. Integrate Google Stitch's SDK and MCP (Model Context Protocol) server into an agent-driven development workflow.

Integration Options:
1. **MCP Server (Remote):** Stitch operates as a Remote MCP Server, allowing AI agents (Cursor, VSCode, custom agents) to programmatically create projects, generate/edit screens, and extract design systems.
2. **SDK (`@google/stitch-sdk/ai`):** Direct TypeScript integration with the Vercel AI SDK for programmatic design generation.

Constraints:
- Authentication must use persistent **API Keys** for CI/headless workflows, or OAuth browser flow for interactive use.
- Agent workflows must follow a create → generate → iterate → download pipeline.
- Downloaded artifacts (ZIP) contain generated code and optimized image assets — extract images to `public/` and reference in Next.js components.

Required Output Format: Provide code for:
1. SDK setup: Installing `@google/stitch-sdk/ai` and initializing the client with API key authentication.
2. Agent workflow script: A Node.js script that:
   - Creates a new Stitch project
   - Generates 3 screens from text prompts
   - Downloads artifacts (code + assets)
   - Extracts the `DESIGN.md` to the project root
3. MCP configuration: JSON config for adding Stitch as a remote MCP server in Cursor/VSCode settings.
4. `callTool` examples: Using `stitch.callTool("create_project", {...})` and `stitch.callTool("generate_screen_from_text", {...})` for lower-level agent control.

⚠️ Common Pitfalls:
- **Pitfall:** Using Stitch SDK output (HTML) directly as React components without adaptation.
- **Solution:** Stitch supports translation to React, Vue, Angular, Flutter, SwiftUI, and Jetpack Compose — but always validate the output against your component library and design system before integration.
```

✅ **Verification Checklist:**
- [ ] Run the agent workflow script end-to-end. Verify it creates a project, generates screens, and downloads a ZIP artifact successfully.
- [ ] Verify Stitch appears as an available MCP tool in your IDE after adding the config.
- [ ] The extracted `DESIGN.md` matches the visual identity of the generated screens.

---

### Prompt 0.10: Google Antigravity — Artifact-Driven AI Development Workflow

```text
You are a Development Workflow Architect. Structure this project's AI-assisted development around Google Antigravity's artifact system to maximize transparency, reviewability, and iterative progress.

What is Google Antigravity?
Antigravity (antigravity.google) is Google's agentic AI coding assistant. It operates in a **Planning Mode** that produces structured artifacts for asynchronous human review, enabling a human-in-the-loop workflow.

Key Artifact Types:
1. **Implementation Plan** — A detailed technical design document the agent creates before making code changes. It includes proposed file modifications, architectural decisions, and open questions. The user must review and approve ("Proceed") before the agent executes.
2. **Task List** — A live markdown checklist (`task.md`) tracking research, implementation, and verification steps. Items are marked `[ ]` (pending), `[/]` (in progress), or `[x]` (complete) as work progresses.
3. **Walkthrough** — A post-execution summary (`walkthrough.md`) documenting what changed, what was tested, and validation results. Includes embedded screenshots and browser recordings.
4. **Knowledge Items (KIs)** — Persistent memory extracted from conversations. KIs inform future agent responses with project-specific patterns, gotchas, and architectural decisions. Stored in the agent's knowledge directory.
5. **Browser Recordings** — WebP video captures of browser interactions (UI testing, visual verification) that can be embedded in walkthroughs.
6. **Screenshots** — Visual captures of UI states for review and comparison.

Workflow Integration with This Prompt Library:
- When starting a new phase, instruct Antigravity to create an **Implementation Plan** artifact before writing any code.
- Use the **Task List** artifact to track progress through each phase's prompts (e.g., "Prompt 5.1: ✅, Prompt 5.2: 🔄, Prompt 5.3: ⬜").
- After completing a phase, instruct Antigravity to produce a **Walkthrough** artifact summarizing deliverables.
- Leverage **Knowledge Items** to persist cross-phase decisions (e.g., "We chose Better Auth over Clerk because..." or "Our DAL pattern uses...").

Constraints:
- Always operate in Planning Mode for phases that involve architectural decisions (Phases 0, 1, 3, 4, 8).
- For execution-heavy phases (Phases 5, 6, 7), toggle to direct execution after the plan is approved.
- Ensure every Implementation Plan references the specific Phase and Prompt number from this library (e.g., "Implementing Phase 5, Prompt 5.3: URL State Management").

Required Output Format:
1. An `AGENTS.md` file at the project root defining:
   - The project's tech stack and conventions
   - File structure and naming patterns
   - Which phases are complete and which are in progress
   - Key architectural decisions for the agent to reference
2. A phase-tracking table mapping each workflow phase to its artifact status:
   | Phase | Implementation Plan | Task List | Walkthrough | Status |
   |-------|-------------------|-----------|-------------|--------|
   | 0     | ✅ Approved        | ✅ Complete | ✅ Written   | Done   |
   | 1     | 🔄 In Review       | 🔄 Active  | ⬜ Pending   | Active |
3. A Knowledge Item seeding strategy: List 5-10 project-specific KIs to create after Phase 0 completes (e.g., "Auth Provider Choice", "Database Schema Conventions", "Component Architecture Patterns").

⚠️ Common Pitfalls:
- **Pitfall:** Skipping the Implementation Plan and jumping straight to code generation, resulting in misaligned architecture.
- **Solution:** Always require an approved Implementation Plan before any code changes in architectural phases. Use Antigravity's "Always Proceed" toggle only for trivial tasks.
- **Pitfall:** Not seeding Knowledge Items early, causing the agent to lose context across conversations.
- **Solution:** After Phase 0, explicitly instruct the agent to extract and save KIs for tech stack, conventions, and architectural decisions.
```

✅ **Verification Checklist:**
- [ ] `AGENTS.md` exists at the project root and is referenced by the AI agent.
- [ ] Phase-tracking table accurately reflects current progress.
- [ ] At least 5 Knowledge Items are seeded after completing Phase 0.
