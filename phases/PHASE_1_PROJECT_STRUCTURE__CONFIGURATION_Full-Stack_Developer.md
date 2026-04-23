<a name="phase-1"></a>
# 📌 PHASE 1: PROJECT STRUCTURE & CONFIGURATION (Full-Stack Developer)

> **Next.js Version:** This phase targets **Next.js (latest)**. See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 1.1: Initialize Next.js Project Structure

```text
You are a Principal Full-Stack Developer specializing in modern Next.js architectures. You create rock-solid foundational setups that allow teams to scale quickly without accumulating technical debt.

Project Name: [your-project-name]
Description: [brief description]
Database: [PostgreSQL (Neon/Supabase)/MongoDB/MySQL/Convex]
Authentication: [Better Auth/Auth.js v5/Clerk/custom]
Styling: [Tailwind CSS v4/Material-UI/etc]
Bundler: Turbopack (default)
Package Manager: [npm/pnpm/bun]

Constraints:
- Do NOT use the `src/` directory. Use the root `app/` structure.
- Do NOT configure ESLint if Biome is requested; they conflict.
- Strictly adhere to Tailwind v4 CSS-only setup (no tailwind.config.ts).

Decision Guide (Monorepo vs Single-repo):
- Use single-repo (standard) if this is a standalone web app.
- Use monorepo (Turborepo) ONLY if you explicitly plan to share generic UI components with a React Native app or separate admin dashboard.

Required Output Format:
1. Provide the single command to scaffold the app (`npx create-next-app@latest...`).
2. Provide COMPLETE CODE for each foundational file. Do not omit code with "///...":
   - `.env.example`
   - `next.config.ts`
   - `app/globals.css`
   - `app/layout.tsx` & `app/page.tsx`
   - `lib/utils.ts`
   - `components.json` (shadcn)
   - `package.json` (Must include `postinstall: prisma generate` and `build: prisma generate && next build`)
   - `.gitignore`
   - `biome.json` (or `eslint.config.mjs`)
   - `postcss.config.mjs`
   - `.nvmrc`

Project Structure Target (Standardized):
```

```
app/
├── (auth)/                    # Route Group for auth pages
├── (dashboard)/               # Route Group for dashboard
├── @modal/                    # Parallel Route for modals
├── api/                       # Route Handlers
├── actions/                   # Server Actions
├── error.tsx                  # Error boundary
├── global-error.tsx           # Root error boundary (catches layout errors)
├── loading.tsx                # Loading UI
├── not-found.tsx              # 404 page
├── layout.tsx                 # Root layout
├── page.tsx                   # Home page
└── globals.css                # Global styles with Tailwind v4
components/
├── ui/                        # Shadcn/ui components
├── layout/                    # Layout components (header, footer, sidebar)
├── forms/                     # Form components
└── shared/                    # Shared components
lib/                           # Utils, db, auth, constants
hooks/                         # Custom React hooks
types/                         # TypeScript type definitions
public/                        # Static assets
```

✅ **Verification Checklist:**
- [ ] Run `npm install` (or pnpm/bun equivalent).
- [ ] Ensure `.nvmrc` successfully sets Node to v22+.
- [ ] Run the dev server (`npm run dev`) and verify localhost:3000 loads without Turbopack errors.

---

### Prompt 1.2: Configure Next.js for Production

```text
You are an expert DevOps and Next.js Engineer. Configure the project for optimal production performance, tight security, and strict type safety.

Constraints:
- ONLY enable experimental Next.js flags if strictly necessary for the PRD (PPR, React Compiler).
- Ensure `next.config.ts` is fully statically typed without `any`.
- Environment variable validation must block the build if variables are missing.

Required Output Format: Provide complete TS/JS code for:

1. `next.config.ts`
   - Detail image domains.
   - Explain each experimental flag used.
2. `lib/env.ts` configuring `@t3-oss/env-nextjs`.
   - Separate server and client vars.
3. `tsconfig.json`
   - Target ES2022, strict mode, bundler resolution.
4. `biome.json`
   - Rules for React/Next.js best practices (no unused imports, no dangerous innerHTML).
5. `lib/utils.ts`
   - Setup `cn` tailwind-merge utility.
6. `postcss.config.mjs`
   - Tailwind v4 hook.

⚠️ Common Pitfalls:
- **Pitfall:** `env.ts` is created but never imported, so it doesn't run at build time.
- **Solution:** Import `env.ts` at the top of your `next.config.ts` to guarantee build-time validation.
```

✅ **Verification Checklist:**
- [ ] Run `npm run typecheck` — it should pass.
- [ ] Run `npm run lint` — Biome/ESLint should pass.
- [ ] Remove a required env var and run build — the build MUST crash and output a readable validation error.

---

### Prompt 1.3: Create CLAUDE.md Context File

```text
You are an AI Workflow Architect. Your job is to write instructions that guide other AI agents. Create a comprehensive `CLAUDE.md` (or `.cursorrules` / `.github/copilot-instructions.md`) file.

Constraints:
- This file MUST serve as the strict ground truth for the project's standards.
- Keep it concise but dense with technical directives.

Required Output Format:
Create a markdown file with the following sections:
1. **Tech Stack & Versions**
2. **Development Commands**
3. **Architecture / Project Structure Explanation**
4. **Code Standards & Conventions** (File naming, component vs server action patterns, typescript strictness)
5. **Rendering Strategy**
6. **Authentication Flow**
7. **Database Schema Summary**
8. **Notes for AI Assistants** (e.g., "Default to Server Components", "Use Tailwind v4", "Never use generic 'any'")
```

✅ **Verification Checklist:**
- [ ] File is saved in the root as `CLAUDE.md`.
- [ ] Explicitly states not to use manual memoization (if using React Compiler).

---
📎 **Related Phases:**
- Prerequisites: [Phase 0: Planning & Setup](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md)
- Proceeds to: [Phase 2: Backend Setup](./PHASE_2_BACKEND_SETUP_API_Routes__Server_Actions.md)
