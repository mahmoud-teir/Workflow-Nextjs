<a name="phase-1"></a>
# 📌 PHASE 1: PROJECT STRUCTURE & CONFIGURATION (Full-Stack Developer)

> **Next.js Version:** This phase targets **Next.js (latest)**. See [Phase 0.7](./PHASE_0_PLANNING__SETUP_Product_Manager_UIUX_Designer.md#prompt-07) for the version compatibility table.

---

### Prompt 1.1: Initialize Next.js Project Structure

```text
You are a Senior Full-Stack Developer. I want to create a professional Next.js project structure with App Router.

Project Name: [your-project-name]
Description: [brief description]
Database: [PostgreSQL (Neon/Supabase)/MongoDB/MySQL/Convex]
Authentication: [Better Auth/Auth.js v5/Clerk/custom]
Styling: [Tailwind CSS v4/Material-UI/etc]
Bundler: Turbopack (default)
Package Manager: [npm/pnpm/bun]

Required:
1. Provide complete commands to set up the project
2. Create all essential files with starter code
3. Follow modern best practices (2025-2026)

Provide COMPLETE CODE for each file:

1. **.env.example** - Include all required environment variables with example values
2. **.env.local** - Gitignored, local overrides (document in .env.example)
3. **next.config.ts** - Next.js configuration with TypeScript support
4. **app/globals.css** - Tailwind CSS v4 configuration using @import "tailwindcss" and @theme directive
5. **app/layout.tsx** - Root layout with all providers
6. **app/page.tsx** - Home page
7. **lib/utils.ts** - Utility functions
8. **components.json** - For shadcn/ui if using
9. **package.json** - With all necessary dependencies and scripts
10. **.gitignore** - Comprehensive ignore rules
11. **CLAUDE.md** - Complete AI context file (see Prompt 1.3)
12. **biome.json** - Biome configuration (or eslint.config.mjs if using ESLint flat config)
13. **postcss.config.mjs** - PostCSS config for Tailwind CSS v4 (`@tailwindcss/postcss`)
14. **.nvmrc** - Pin Node.js version (e.g., 22)
15. **.dockerignore** - Docker ignore rules (if using containers)

For each file, write the COMPLETE, PRODUCTION-READY code with:
- Proper TypeScript types (strict mode)
- Clear comments only where logic is non-obvious
- Modern best practices (2025-2026)
- Error handling where applicable

Project Structure (standardized — no src/ directory):
```

```
app/
├── (auth)/                    # Route Group for auth pages
│   ├── login/page.tsx
│   ├── register/page.tsx
│   └── layout.tsx
├── (dashboard)/               # Route Group for dashboard
│   ├── dashboard/page.tsx
│   ├── settings/page.tsx
│   └── layout.tsx
├── @modal/                    # Parallel Route for modals
│   └── (.)item/[id]/page.tsx  # Intercepting Route
├── api/                       # Route Handlers
│   └── health/route.ts
├── actions/                   # Server Actions
│   └── example.ts
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
lib/
├── db.ts                      # Database connection
├── auth.ts                    # Auth configuration
├── utils.ts                   # Utility functions (cn(), formatDate, etc.)
├── validations/               # Zod schemas
└── constants.ts               # App constants
hooks/                         # Custom React hooks
types/                         # TypeScript type definitions
public/                        # Static assets
emails/                        # React Email templates (if using Resend)
```

---

### Prompt 1.2: Configure Next.js for Production

```text
You are a Next.js expert. Configure the project for optimal production performance.

Required configurations:

## 1. next.config.ts - Complete TypeScript Configuration:
```

```typescript
import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  // Turbopack is the default bundler in latest Next.js
  // No explicit config needed unless customizing loaders

  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'your-domain.com',
      },
    ],
    formats: ['image/webp', 'image/avif'],
  },

  // Feature flags — check Phase 0.7 compatibility table before enabling
  experimental: {
    // Partial Prerendering (stable in Next.js 16+, experimental in 15)
    ppr: true,
    // React Compiler — automatic memoization (stable in 16+, experimental in 15)
    reactCompiler: true,
    // after() API for background tasks (stable in 16+, experimental in 15)
    after: true,
  },

  // Security headers
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: securityHeaders,
      },
    ]
  },

  // Redirects and rewrites if needed
  async redirects() {
    return []
  },
  async rewrites() {
    return []
  },
}

export default nextConfig
```

```text
## 2. Security Headers Configuration:
Create security headers for CSP, HSTS, etc. (detailed in Phase 8).

## 3. Environment Validation:
Create lib/env.ts using `@t3-oss/env-nextjs` with Zod for type-safe environment variables.
```

```typescript
// lib/env.ts
import { createEnv } from '@t3-oss/env-nextjs'
import { z } from 'zod'

export const env = createEnv({
  server: {
    DATABASE_URL: z.string().url(),
    DIRECT_DATABASE_URL: z.string().url().optional(),
    AUTH_SECRET: z.string().min(32),
    RESEND_API_KEY: z.string().startsWith('re_').optional(),
    STRIPE_SECRET_KEY: z.string().startsWith('sk_').optional(),
    STRIPE_WEBHOOK_SECRET: z.string().startsWith('whsec_').optional(),
  },
  client: {
    NEXT_PUBLIC_APP_URL: z.string().url(),
    NEXT_PUBLIC_POSTHOG_KEY: z.string().optional(),
  },
  runtimeEnv: {
    DATABASE_URL: process.env.DATABASE_URL,
    DIRECT_DATABASE_URL: process.env.DIRECT_DATABASE_URL,
    AUTH_SECRET: process.env.AUTH_SECRET,
    RESEND_API_KEY: process.env.RESEND_API_KEY,
    STRIPE_SECRET_KEY: process.env.STRIPE_SECRET_KEY,
    STRIPE_WEBHOOK_SECRET: process.env.STRIPE_WEBHOOK_SECRET,
    NEXT_PUBLIC_APP_URL: process.env.NEXT_PUBLIC_APP_URL,
    NEXT_PUBLIC_POSTHOG_KEY: process.env.NEXT_PUBLIC_POSTHOG_KEY,
  },
})
```

```text
## 4. Middleware Configuration:
Create middleware.ts for authentication, i18n, and logging.

## 5. TypeScript Configuration:
Update tsconfig.json for strict type checking with modern settings:
```

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "lib": ["dom", "dom.iterable", "esnext"],
    "allowJs": true,
    "skipLibCheck": true,
    "strict": true,
    "noEmit": true,
    "esModuleInterop": true,
    "module": "esnext",
    "moduleResolution": "bundler",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "jsx": "preserve",
    "incremental": true,
    "plugins": [{ "name": "next" }],
    "paths": {
      "@/*": ["./*"]
    }
  },
  "include": ["next-env.d.ts", "**/*.ts", "**/*.tsx", ".next/types/**/*.ts"],
  "exclude": ["node_modules"]
}
```

```text
## 6. Biome Configuration (recommended over ESLint + Prettier):
```

```json
{
  "$schema": "https://biomejs.dev/schemas/2.0.0/schema.json",
  "organizeImports": { "enabled": true },
  "linter": {
    "enabled": true,
    "rules": {
      "recommended": true,
      "complexity": {
        "noExcessiveCognitiveComplexity": "warn"
      },
      "correctness": {
        "noUnusedImports": "error",
        "useExhaustiveDependencies": "warn"
      },
      "suspicious": {
        "noExplicitAny": "warn"
      },
      "style": {
        "useConst": "error",
        "noNonNullAssertion": "warn"
      },
      "security": {
        "noDangerouslySetInnerHtml": "warn"
      }
    }
  },
  "formatter": {
    "enabled": true,
    "indentStyle": "space",
    "indentWidth": 2,
    "lineWidth": 100
  },
  "javascript": {
    "formatter": {
      "quoteStyle": "single",
      "semicolons": "asNeeded"
    }
  },
  "files": {
    "ignore": [
      "node_modules",
      ".next",
      "dist",
      "*.gen.ts",
      "components/ui/**"
    ]
  }
}
```

```text
## 7. Utility Functions (lib/utils.ts):
```

```typescript
// lib/utils.ts
import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
```

```bash
# Required dependencies
pnpm add clsx tailwind-merge
```

```text
## 8. PostCSS Configuration (required for Tailwind CSS v4):
```

```javascript
// postcss.config.mjs
export default {
  plugins: {
    '@tailwindcss/postcss': {},
  },
}
```

```bash
# Tailwind v4 uses @tailwindcss/postcss instead of the old tailwindcss/postcss plugin
pnpm add -D @tailwindcss/postcss
```

```text
## 9. Package.json Template:
```

```json
{
  "name": "your-project-name",
  "version": "0.1.0",
  "private": true,
  "engines": {
    "node": ">=22.0.0"
  },
  "scripts": {
    "dev": "next dev --turbopack",
    "build": "next build",
    "start": "next start",
    "lint": "biome check .",
    "lint:fix": "biome check --write .",
    "format": "biome format --write .",
    "typecheck": "tsc --noEmit",
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:coverage": "vitest --coverage",
    "test:e2e": "playwright test",
    "db:generate": "prisma generate",
    "db:push": "prisma db push",
    "db:migrate": "prisma migrate dev",
    "db:migrate:prod": "prisma migrate deploy",
    "db:seed": "tsx prisma/seed.ts",
    "db:studio": "prisma studio",
    "postinstall": "prisma generate"
  }
}
```

```text
Alternative: If using ESLint, use the new flat config format (eslint.config.mjs) with @next/eslint-plugin-next.

Write complete, production-ready configuration files.
```

---

### Prompt 1.3: Create CLAUDE.md Context File

```text
Create a comprehensive CLAUDE.md file in the root directory. This file teaches AI assistants everything about this Next.js project.

Include:

# [Project Name] - Next.js Project

## Project Overview
[One paragraph describing what this project does and its purpose]

## Tech Stack & Versions
- Next.js: [version] (latest stable)
- React: 19+
- TypeScript: 5.7+
- Node.js: 22+ (pinned in .nvmrc)
- Database: [type and version]
- ORM: [Prisma/Drizzle/Convex]
- Authentication: [Better Auth/Auth.js v5/Clerk]
- Styling: Tailwind CSS v4 + Shadcn/ui
- State Management: [Zustand/Jotai/React Context] (minimal — React 19 reduces need)
- Bundler: Turbopack (default)
- Linting: Biome (or ESLint flat config)
- Testing: Vitest + Playwright
- Deployment: [Vercel/AWS/etc]
- AI SDK: [Vercel AI SDK if applicable]
- Email: [Resend + React Email if applicable]
- Analytics: [PostHog/Vercel Analytics if applicable]
- Error Tracking: [Sentry if applicable]

## Development Commands
All npm/pnpm/bun scripts and their purposes:
- `dev` — Start dev server with Turbopack
- `build` — Production build
- `lint` — Run Biome checks
- `typecheck` — TypeScript type checking
- `test` — Run Vitest unit tests
- `test:e2e` — Run Playwright E2E tests
- `db:migrate` — Run database migrations
- `db:seed` — Seed database with test data
- `db:studio` — Open database GUI

## Project Structure
Complete folder structure with explanations:
- app/ — App Router structure (Route Groups, Parallel Routes, Intercepting Routes)
- components/ — Reusable components (Server & Client)
- lib/ — Utilities, configurations, services
- hooks/ — Custom React hooks
- types/ — TypeScript type definitions
- prisma/ or drizzle/ — Database schema and migrations
- emails/ — React Email templates
- public/ — Static assets
- e2e/ — Playwright E2E tests
- tests/ — Vitest test setup

## Code Standards & Conventions
- General Rules
- Naming Conventions (files: kebab-case, components: PascalCase, utils: camelCase)
- Component Patterns (Server vs Client Components — default to Server)
- Route Handler Patterns
- Server Actions Patterns (primary for mutations)
- Error Handling (error.tsx, global-error.tsx, not-found.tsx)
- TypeScript Patterns (strict mode, no any, prefer type over interface for unions)

## Rendering Strategy
- Which pages use SSG, SSR, ISR, CSR, PPR
- Streaming patterns
- Suspense boundary strategy

## Authentication Flow
How authentication works in this project

## Database Schema
List all models with key fields

## Environment Variables
All required .env variables (reference .env.example)

## Common Patterns
- Data fetching in Server Components (async/await, no useEffect)
- Form handling with Server Actions + useActionState
- Optimistic updates with useOptimistic
- Cache invalidation with revalidatePath/revalidateTag
- Error handling patterns
- Loading states with Suspense + Skeleton components

## Git Workflow
Branch naming, commit format (conventional commits):
- feat: new feature
- fix: bug fix
- chore: maintenance
- docs: documentation

## Notes for AI Assistants
- Always use Server Components by default
- Only add 'use client' when needed (interactivity, hooks, browser APIs)
- Use Server Actions for data mutations, not API routes
- Use Tailwind CSS v4 syntax (CSS-based config, @theme directive)
- Use Biome for linting/formatting (not ESLint + Prettier)
- Use Vitest for unit tests, Playwright for E2E
- searchParams and params are Promises in Next.js 15+ (must await)
- React Compiler handles memoization — do NOT use useMemo/useCallback manually
- Use `cn()` utility for conditional class names (from lib/utils.ts)
- Prefer sonner for toast notifications
- Use Zod for ALL input validation (forms, API, server actions)

Use clear Markdown formatting with proper headings and bullet points.
```

---
