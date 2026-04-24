<a name="phase-0b"></a>
# 📌 PHASE 0B: HIGH-FIDELITY PROTOTYPE (UI/UX Designer, Frontend Developer)

> **Position in Workflow:** Run this phase **after Phase 0** (PRD + wireframes approved) and **before Phase 1** (project scaffolding). The goal is to produce a clickable, realistic prototype that stakeholders can validate before a single line of production code is written.

> **Tools Covered:** mcp Stitch tool, v0.dev, Vercel AI SDK UI, Framer, and the prototype-to-production translation pipeline.

---

## 🧭 Why a Dedicated HiFi Prototype Phase?

Wireframes (Phase 0.5) answer *"what goes where."* A high-fidelity prototype answers:

- Does the visual hierarchy feel right at full fidelity?
- Do the interaction states (loading, error, empty, hover) make sense before engineers build them?
- Can a stakeholder/client click through a realistic flow and sign off **before** the backend exists?
- Which components are genuinely reusable vs. one-off?

Catching these answers in a prototype costs **minutes**. Catching them in production code costs **sprints**.

---

## 🗺️ Prototype Tooling Decision Tree

```
Do you need a full, multi-screen interactive flow?
├── YES → Use mcp Stitch tool (Prompt 0B.2) — generates a full React app prototype
│         Then optionally refine complex components with v0.dev (Prompt 0B.3)
└── NO, just one complex component →
        Use v0.dev (Prompt 0B.3) directly

Is this prototype for internal dev alignment only?
├── YES → mcp Stitch tool (faster, code-native, no export friction)
└── NO, for external stakeholders/clients →
        Polish in Framer (Prompt 0B.5) for pixel-perfect presentation
        OR export mcp Stitch tool prototype to a hosted URL

Are you designing a data-heavy dashboard or table?
└── Add Prompt 0B.4 (Interaction States) — cover all data states explicitly
```

---

### Prompt 0B.1: Prototype Scope & Screen Inventory

```text
You are a Senior Product Designer running a pre-development prototype scoping session.

My project is: [ProjectName] — [one paragraph description from PRD]

My approved wireframes cover these screens: [list screen names from Prompt 0.5 output]

Constraints:
- Focus only on screens that are on the critical user path (first-run experience, core feature loop).
- Do NOT prototype admin-only CRUD screens at this stage — those can be scaffolded directly in Phase 5.
- Each screen must be paired with its interaction states (see Prompt 0B.4).

Required Output Format:

## 1. Prototype Scope (What to Prototype)
   - List the screens to prototype with a one-line reason why each is on the critical path.
   - Flag any screens that are NOT worth prototyping (simple forms, settings pages, etc.) and explain why.

## 2. User Flow Map
   - A step-by-step flow (numbered list) showing how a first-time user moves from Landing → Core Feature.
   - Annotate decision points (e.g., "If not logged in → redirect to /auth").
   - Identify which transitions should have loading states vs. instant navigation.

## 3. Component Inventory (Pre-Build Analysis)
   - For each screen, list the UI components needed.
   - Mark each as: [NEW] (needs designing) | [SHARED] (reused across screens) | [SHADCN] (use library default).
   - Highlight components that are complex enough to warrant a standalone v0.dev prompt.

## 4. Prototype Fidelity Checklist
   - [ ] Real copy (no Lorem Ipsum) — pulled from PRD user stories.
   - [ ] Real data shapes (e.g., actual product names, realistic numbers, real image placeholders).
   - [ ] Responsive breakpoints: mobile-first at 375px, tablet at 768px, desktop at 1280px.
   - [ ] Dark mode: Yes / No decision for the prototype stage.
   - [ ] Animations: Specify which transitions are critical to validate (page, modal, toast) vs. deferred.
```

✅ **Verification Checklist:**
- [ ] You have a list of ≤8 screens to prototype (more than this suggests scope creep).
- [ ] Every screen on the list is on the critical user path — no "nice to have" screens.
- [ ] Component inventory is complete and [SHARED] components are identified.

---

### Prompt 0B.2: mcp Stitch tool — Full Interactive Prototype

> **Tool:** mcp Stitch tool
> **What it does:** Generates a fully interactive, multi-screen React prototype from a natural language prompt. Output is real JSX/TSX you can run and hand to developers.

```text
You are an expert UI/UX designer and React developer. Build a complete, high-fidelity, interactive prototype for [ProjectName].

## Project Context
- Product type: [SaaS / e-commerce / content platform / internal tool / etc.]
- Target users: [from PRD personas]
- Core value proposition: [one sentence from PRD]

## Screens to Build (from Prompt 0B.1 scope)
[List each screen name + its purpose, e.g.:]
1. Landing Page — Communicates value prop, CTA leads to signup.
2. Onboarding / Sign Up — [Auth.js / Clerk / Better Auth] style, 2 steps max.
3. Dashboard (Authenticated) — Main feature hub, shows [X, Y, Z] data.
4. [Feature Screen] — [description].
5. Settings — Profile, notifications, billing stub.

## Design System
[Paste your DESIGN.md content here if it exists from Phase 0.8, otherwise:]
- Color palette: Primary [hex], Background [hex], Surface [hex], Text [hex]
- Font: [font name] — headings [weight], body [weight]
- Border radius: [sm/md/lg]
- Design language: [minimal / bold / editorial / brutalist / corporate / etc.]

## Interaction Requirements
- Navigation between all screens must work (use React Router or internal state).
- Include at least one modal/dialog (e.g., confirm action, detail drawer).
- Include at least one toast/notification pattern.
- Include hover states on all interactive elements.
- The Dashboard must show a realistic data table or list with at least 5 rows of realistic data.
- Include a mobile hamburger navigation for screens <768px.

## Quality Constraints
- NO Lorem Ipsum. Use real copy derived from the product's purpose.
- NO placeholder metrics ("99.9% uptime", "10,000+ users") unless they are realistic for this product.
- NO Inter font / pure black backgrounds / neon gradients unless it matches the brand.
- Use Tailwind CSS utility classes. Do NOT use inline styles.
- Use shadcn/ui component primitives where applicable (Button, Input, Card, Dialog, DropdownMenu, Table, Badge, Tabs).
- The result must look like a real product, not an AI-generated template.

## Deliverable
A single React file (or component tree) I can run immediately. Include:
- All screens as distinct components.
- A top-level `App` component with routing/navigation between screens.
- Realistic mock data defined as constants at the top of the file.
- Inline comments marking "PRODUCTION NOTE:" where real API calls will replace mock data.
```

✅ **Verification Checklist:**
- [ ] All screens from the scope list are present and navigable.
- [ ] No Lorem Ipsum or placeholder metrics exist in the prototype.
- [ ] Mobile layout has been viewed at 375px width — no broken overflow.
- [ ] At least one modal, one toast, and one table/list are present.
- [ ] `PRODUCTION NOTE:` comments mark every location where real data will come from.

---

### Prompt 0B.3: v0.dev — Complex Component Deep Dive

> **Tool:** [v0.dev](https://v0.dev)
> **When to use:** A specific component from your inventory (Prompt 0B.1) is complex enough that it deserves focused iteration — e.g., a multi-step form, a drag-and-drop kanban, a data table with inline editing, a command palette (⌘K), or an advanced chart.

```text
Build a production-ready [component name] component for a Next.js 15 App Router project.

## Component Specification
- **Name:** [ComponentName]
- **Purpose:** [What it does in one sentence]
- **Location in app:** [Which screen it lives on, e.g., "main dashboard, above the data table"]

## Functional Requirements
[List each behavior the component must support, e.g.:]
- Sortable columns (click header to sort asc/desc, visual indicator on active sort column).
- Row selection with checkbox (single and select-all).
- Inline editing: clicking a cell enters edit mode, Escape cancels, Enter/blur saves.
- Pagination: 10/25/50 rows per page selector + prev/next controls.
- Empty state: Custom illustration + CTA when no data exists.
- Loading state: Skeleton rows while data fetches (use `Suspense`-compatible pattern).
- Error state: Inline error banner with retry button.

## Design Constraints
- Tailwind CSS v4, shadcn/ui primitives only.
- Must be accessible: keyboard-navigable, ARIA roles for table, visible focus rings.
- Must support dark mode via `next-themes` / `data-[theme]` class.
- Match this color palette: [paste relevant tokens from DESIGN.md].
- No animation libraries beyond Tailwind transitions for this component.

## Technical Constraints
- Export as a single `'use client'` React component.
- Props interface must be fully TypeScript typed (no `any`).
- Data must be passed in via props — no internal fetching. Include a realistic mock data fixture for demonstration.
- Include a `isLoading: boolean` and `error: string | null` prop to control states externally.

## Expected Output
- The complete component code.
- A usage example showing how to integrate it into a Next.js Server Component page via a Client wrapper.
- A list of follow-up iteration suggestions for Phase 5 (production implementation).
```

✅ **Verification Checklist:**
- [ ] Component renders all three states: loaded, loading skeleton, error.
- [ ] Empty state is designed (not just "No data found" text).
- [ ] TypeScript props are fully typed with no `any`.
- [ ] Component is accessible via keyboard alone (tab, enter, arrow keys where applicable).
- [ ] A `PRODUCTION NOTE:` marks where the mock data fixture should be replaced.

---

### Prompt 0B.4: Interaction States — The Full State Matrix

> This is the most commonly skipped step in prototyping and the most expensive to fix later. Every screen and component has more than one state.

```text
You are a Senior Frontend Engineer conducting a UI state audit for [ProjectName].

Review the prototype from Prompt 0B.2 and for each screen/component listed below, generate the complete state matrix.

## Screens & Components to Audit
[Paste screen/component list from Prompt 0B.1]

For each item, define the following states:

| State | Description | Visual Treatment |
|-------|-------------|-----------------|
| **Default** | Normal loaded state with data | — |
| **Loading** | Data is fetching / action is in-flight | Skeleton UI (not spinner) |
| **Empty** | No data exists yet (first-run or after deletion) | Illustration + CTA |
| **Error** | Fetch failed or action failed | Inline error banner, retry available |
| **Partial / Degraded** | Some data loaded, some failed | Partial render + warning |
| **Success / Confirmation** | Action completed successfully | Toast + optimistic UI update |
| **Disabled** | User lacks permission or prerequisites not met | Greyed + tooltip explaining why |
| **Hover / Focus** | Interactive element is targeted | Subtle highlight, cursor change |
| **Active / Selected** | Item is selected or currently active | Distinct visual treatment |
| **Overflow** | Content exceeds container bounds | Truncation + "show more" or scroll |

## Constraints
- Every loading state MUST use skeleton UI (content-shaped placeholders), never a centered spinner.
  Exception: Full-page route transitions may use a minimal top progress bar (NProgress pattern).
- Empty states MUST include a clear CTA — never just "No items found."
- Error states MUST offer a recovery path (retry button, contact support link, or navigation escape).
- Optimistic updates MUST be used for: likes, toggles, status changes, inline edits. Do NOT wait for server confirmation before updating UI.

## Required Output
For each screen/component: a Markdown table showing all applicable states and their visual treatments.
Then: identify which states are MISSING from the current prototype and generate the code for each missing state.
```

✅ **Verification Checklist:**
- [ ] No screen uses a full-page centered spinner for loading.
- [ ] Every list/table has an empty state with a CTA.
- [ ] Every destructive action (delete, cancel, revoke) has a confirmation dialog.
- [ ] Form validation shows inline field-level errors, not just a top-level alert.
- [ ] At least 3 optimistic UI updates are implemented in the prototype.

---

### Prompt 0B.5: Prototype → Production Translation Guide

> After stakeholder sign-off on the prototype, this prompt generates the handoff document that bridges the prototype and Phase 1 (Project Structure) + Phase 5 (Frontend Development).

```text
You are a Senior Frontend Architect. Given the approved high-fidelity prototype for [ProjectName], generate the production translation guide.

## Prototype Summary
[Briefly describe the prototype: screens, key components, design decisions made]

## Translation Rules

### 1. Component Architecture Plan
For each component in the prototype:
- Classify as: Server Component | Client Component | Shared (used in both).
- Identify the 'use client' boundary (the outermost component that needs interactivity).
- Map to file path in the Next.js App Router structure (e.g., `app/dashboard/_components/DataTable.tsx`).

### 2. Mock Data → Real Data Mapping
For each `PRODUCTION NOTE:` comment in the prototype:
- Define the expected API shape (TypeScript interface).
- Identify whether data comes from: Server Action | Route Handler | Direct DB query (RSC) | Client fetch (SWR/React Query).
- Note any pagination, caching (`revalidate`), or real-time (`use server` + `revalidatePath`) requirements.

### 3. Design Token Extraction
Extract all color values, spacing, typography, and border radius decisions from the prototype and generate:
- A complete `app/globals.css` with Tailwind v4 `@theme` block.
- A `components.json` for shadcn/ui initialization that matches the design.
- Any custom CSS variables not covered by Tailwind.

### 4. Animation Inventory
List every animation/transition in the prototype and classify:
- CSS transition only → keep as Tailwind utility.
- Needs layout animation (entering/exiting DOM) → use `framer-motion` `AnimatePresence`.
- Needs spring physics or gesture → use `framer-motion` `motion.*`.
- Needs scroll-triggered → use `framer-motion` `useInView` or `useScroll`.

### 5. Phase 5 Prompt Augmentation
Generate 3-5 additions to include in Prompt 5.1 (UI Components) that are specific to THIS prototype's decisions (e.g., custom component patterns discovered, non-standard layout needs, specific animation interactions).

## Output Format
A structured Markdown handoff document suitable for sharing with the development team or pasting into a project wiki.
```

✅ **Verification Checklist:**
- [ ] Every `PRODUCTION NOTE:` in the prototype maps to a specific data-fetching strategy.
- [ ] `app/globals.css` `@theme` block is generated and consistent with the prototype.
- [ ] Every `'use client'` boundary is justified (hooks, events, browser APIs).
- [ ] The animation inventory has no "figure it out later" items.
- [ ] Phase 5 prompt augmentations are written and ready to paste.

---

### Prompt 0B.6: Stakeholder Review & Prototype Iteration

> Use this prompt to run structured review sessions and manage feedback without derailing the design.

```text
You are a Senior Product Designer facilitating a prototype review session for [ProjectName].

## Review Context
- Reviewers: [e.g., "2 engineers, 1 non-technical client, 1 PM"]
- Prototype URL / file: [link or description]
- Review goal: [e.g., "Sign off on core user flow" | "Validate navigation patterns" | "Check brand alignment"]

## Feedback Triage Rules
Categorize all feedback into:

| Priority | Criteria | Action |
|----------|----------|--------|
| 🔴 **Blocker** | Core user flow broken, confusing UX that will cause drop-off | Fix before sign-off |
| 🟡 **Iteration** | Design preference, copy tweaks, minor layout adjustments | Fix in this phase |
| 🟢 **Backlog** | Nice-to-have, polish, edge cases | Defer to Phase 5 or Phase 6 |
| ❌ **Rejected** | Scope creep, contradicts PRD, technically infeasible in MVP | Document and decline with reason |

## Prompt Instructions
Take the following feedback notes from the review session and:
1. Categorize each item using the triage rules above.
2. For 🔴 Blockers: Generate the updated component/screen code immediately.
3. For 🟡 Iterations: List the changes needed with file references.
4. For 🟢 Backlog items: Write them as GitHub Issues in the format:
   `## [Feature/Polish]: [Title]\n**Context:** ...\n**Acceptance Criteria:** ...`
5. For ❌ Rejected items: Write a one-sentence rationale to share with the reviewer.

## Feedback Notes
[Paste raw feedback here]

## Sign-Off Criteria
The prototype is approved when:
- [ ] All 🔴 Blockers are resolved.
- [ ] Stakeholders can complete the primary user flow without asking questions.
- [ ] The design is consistent with the brand/tone defined in Phase 0.8 DESIGN.md.
- [ ] At least one non-designer (engineer or PM) has reviewed for technical feasibility.
```

✅ **Phase 0B Completion Gate:**
Before proceeding to Phase 1 (Project Structure), confirm ALL of the following:
- [ ] All screens in the prototype scope are complete and navigable.
- [ ] All interaction states (loading, empty, error) are present on critical screens.
- [ ] Stakeholder sign-off has been received on the core user flow.
- [ ] Prototype → Production translation guide (Prompt 0B.5) is complete.
- [ ] `app/globals.css` design tokens are extracted and ready.
- [ ] No 🔴 Blocker feedback items remain open.

---

## 🔗 Phase Connections

| From | To | What carries forward |
|------|----|----------------------|
| Phase 0.5 (Wireframes) | → Phase 0B | Screen inventory + user flow |
| Phase 0.8 (Google Stitch) | → Phase 0B.2 | `DESIGN.md` design tokens for prototype prompt |
| Phase 0B | → Phase 1 | `globals.css` tokens, component file map |
| Phase 0B | → Phase 5 | Prototype code as reference, augmented Prompt 5.1 |
| Phase 0B | → Phase 7 | Interaction states become test cases (e.g., "empty state renders CTA") |
| Phase 0B | → Phase 9 | A11y issues discovered in prototype are pre-logged |

---

## 🛠️ Tool Quick Reference

| Tool | URL | Best For | Output |
|------|-----|----------|--------|
| **mcp Stitch tool** | mcp Stitch tool | Full multi-screen interactive app prototype | React JSX, runnable immediately |
| **v0.dev** | v0.dev | Single complex component iteration | shadcn/ui + Tailwind component |
| **Google Stitch** | stitch.withgoogle.com | Visual design ideation, `DESIGN.md` generation | HTML screens + design system |
| **Framer** | framer.com | Client-presentable polished prototype, no-code animation | Hosted prototype URL |
| **Excalidraw** | excalidraw.com | Quick layout sketches before HiFi | Wireframe export |

> **Avoid using Figma for this phase** unless your team has a dedicated designer with component libraries set up. For AI-assisted development workflows, code-native tools (mcp Stitch tool, v0.dev) produce prototype assets that translate directly into Phase 5 without a design-to-code conversion step.
