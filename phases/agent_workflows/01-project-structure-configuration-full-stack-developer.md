---
phase: 1
title: Project Structure & Configuration
role: Full-Stack Developer
dependencies: [Phase 0]
estimated_time: 1-2 hours
---

# Phase 1: Project Structure & Configuration — Agent Workflow

## Prerequisites
- [ ] Phase 0 completed (PRD, tech design, design system)
- [ ] Node.js 20+ installed
- [ ] pnpm installed (`corepack enable`)

## Step-by-Step Execution

### Step 1: Create Next.js Project
**Action:** `run_command`
```bash
pnpm create next-app@latest my-app --typescript --tailwind --app --no-src-dir --import-alias "@/*"
cd my-app
echo "20" > .nvmrc
```

### Step 2: Create Directory Structure
**Action:** `run_command`
```bash
mkdir -p app/\(marketing\) app/\(app\) app/api components/ui hooks lib/validations db actions types public/icons
```

### Step 3: Write Environment Validation
**Action:** `write_to_file`
**File:** `lib/env.ts`
**Description:** Type-safe env validation with `@t3-oss/env-nextjs` and Zod.
```bash
pnpm add @t3-oss/env-nextjs zod
```

### Step 4: Write Biome Configuration
**Action:** `write_to_file`
**File:** `biome.json`
**Description:** Biome 2.x config with schema 2.0.0, security rules enabled, organize imports.
```bash
pnpm add -D @biomejs/biome
```

### Step 5: Write Next.js Configuration
**Action:** `write_to_file`
**File:** `next.config.ts`
**Description:** Config with experimental flags (ppr, reactCompiler, after) and stability caveats as comments.

### Step 6: Write Environment Example
**Action:** `write_to_file`
**File:** `.env.example`
**Description:** Template with all required env vars (DATABASE_URL, BETTER_AUTH_SECRET, NEXT_PUBLIC_APP_URL, etc.).

### Step 7: Write Global CSS
**Action:** `write_to_file`
**File:** `app/globals.css`
**Description:** Tailwind v4 CSS with `@theme` directive, oklch color tokens, `.dark` class overrides, and `prefers-reduced-motion` query.

### Step 8: Write Root Layout
**Action:** `write_to_file`
**File:** `app/layout.tsx`
**Description:** Root layout with `next/font`, ThemeProvider, Toaster (sonner), metadata.

### Step 9: Write Utility Helpers
**Action:** `write_to_file`
**File:** `lib/utils.ts`
**Description:** `cn()` helper using `clsx` + `tailwind-merge`.
```bash
pnpm add clsx tailwind-merge
```

### Step 10: Write CLAUDE.md
**Action:** `write_to_file`
**File:** `CLAUDE.md`
**Description:** Project intelligence file with stack overview, conventions, file patterns, and common commands.

### Step 11: Update package.json
**Action:** `edit_file`
**File:** `package.json`
**Description:** Add `engines` field, scripts (dev, build, lint, format, check, db:push, db:seed), and verify dependencies.

### Step 12: Install Core Dependencies
**Action:** `run_command`
```bash
pnpm add next-themes sonner
pnpm add -D @types/node
```

## Verification
- [ ] `pnpm dev` starts without errors
- [ ] `pnpm build` completes successfully
- [ ] `pnpm biome check .` passes
- [ ] Directory structure matches Phase 1 spec
- [ ] `.env.example` has all required variables

## Troubleshooting
- **Issue:** `next.config.ts` not recognized
  **Fix:** Ensure Next.js 15+ is installed. Earlier versions use `.mjs` or `.js`.
- **Issue:** Tailwind v4 `@theme` not working
  **Fix:** Ensure `tailwindcss@4` is installed, not v3. V4 uses CSS-based config, not `tailwind.config.ts`.
