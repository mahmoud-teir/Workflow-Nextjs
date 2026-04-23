# CLAUDE.md — ECC Agent Harness Configuration

> This file is the meta-configuration for AI coding agents (Claude Code, Google Antigravity, Cursor).
> It serves as the entry point for understanding the project structure, conventions, and available automation.

## Project Overview

This is a **21-phase Next.js development workflow library** enhanced with the **Everything Claude Code (ECC)** agent harness architecture. It provides machine-executable skills, specialized agents, automated hooks, and mandatory rules for production-grade full-stack development.

**Tech Stack Context:** Next.js (App Router) · React 19 · TypeScript · Tailwind CSS v4 · Biome · Vitest · Playwright

## Architecture

```
workflows/
├── phases/           # 21-phase development workflow (Phases 0-20)
├── skills/           # ECC skills — reusable workflow knowledge
│   ├── tdd-workflow/
│   ├── security-review/
│   ├── verification-loop/
│   ├── strategic-compact/
│   ├── planner-workflow/
│   ├── code-review/
│   ├── nextjs-patterns/
│   ├── deployment-patterns/
│   ├── session-manager/
│   ├── continuous-learning/
│   ├── karpathy-guidelines/
│   ├── db-schema-design/
│   ├── rules-generator/
│   └── telemetry-integration/
├── agents/           # ECC agents — specialized subagent definitions
│   ├── planner.md
│   ├── code-reviewer.md
│   ├── security-reviewer.md
│   ├── tdd-guide.md
│   ├── build-error-resolver.md
│   ├── e2e-runner.md
│   ├── seo-specialist.md
│   ├── performance-optimizer.md
│   ├── db-architect.md
│   ├── devops-engineer.md
│   └── api-architect.md
├── hooks/            # Lifecycle event handlers (JSON templates)
│   ├── post-edit-format.json
│   ├── post-edit-typecheck.json
│   ├── pre-edit-security-scan.json
│   ├── stop-session-save.json
│   └── stop-console-log-audit.json
├── rules/            # Mandatory project-wide constraints
│   ├── common/       # Language-agnostic rules
│   ├── typescript/   # TypeScript-specific rules
│   └── security/     # Server Actions security constraints
├── docs/             # Guides and reference documentation
└── stitch-skills/    # Google Stitch design integration
```

## Agent Quick Reference

| Command | Agent | Purpose |
|---------|-------|---------|
| `/planner` | Planner | Structured task decomposition and planning |
| `/code-reviewer` | Code Reviewer | Severity-based code review with confidence filtering |
| `/security-reviewer` | Security Reviewer | Full security audit (auth, CSP, env vars, inputs) |
| `/tdd-guide` | TDD Guide | RED→GREEN→REFACTOR test-driven development |
| `/build-error-resolver` | Build Error Resolver | Diagnose and fix build/type/lint errors |
| `/e2e-runner` | E2E Runner | Execute and triage Playwright test suites |
| `/seo-specialist` | SEO Specialist | Metadata, sitemap, structured data audit |
| `/performance-optimizer` | Performance Optimizer | Core Web Vitals, bundle size, rendering optimization |
| `/db-architect` | DB Architect | Schema design, indexing, RLS, and migration safety |
| `/devops` | DevOps Engineer | CI/CD pipelines, Docker, GitHub Actions automation |
| `/api-architect` | API Architect | Type-safe contracts, Zod schemas, API documentation |
| `/verify` | — (Skill) | 6-gate verification loop (build→types→lint→test→security→diff) |

## Core Principles

1. **Agent-First** — Delegate to specialized agents; don't do everything in one prompt
2. **Test-Driven** — Write tests before implementation (RED→GREEN→REFACTOR)
3. **Security-First** — Auth checks, input validation, and secret scanning are non-negotiable
4. **Verify Everything** — Run the verification loop after every significant change
5. **Plan Before Execute** — Use the planner agent for architectural decisions

## Rules Summary

### Always
- Validate all inputs with Zod schemas
- Use Server Components by default; `'use client'` only when needed
- Run `tsc --noEmit` before committing
- Write tests for business logic (≥80% coverage target)
- Use `<Link>` for internal navigation, `<Image>` for images

### Never
- Hardcode secrets or API keys in source files
- Use `eval()`, `Function()`, or `dangerouslySetInnerHTML` without sanitization
- Skip the verification loop before merging
- Use `any` type — prefer `unknown` with runtime narrowing
- Commit `console.log` statements to production code

## Hook Configuration

To activate hooks in your project, copy the hook templates to `.claude/settings.json`:

```json
{
  "hooks": {
    "PostToolUse": [
      { "matcher": "Edit|Write", "command": "npx biome check --write ${file}" },
      { "matcher": "Edit|Write", "command": "npx tsc --noEmit --pretty 2>&1 | head -50" }
    ],
    "PreToolUse": [
      { "matcher": "Edit|Write", "command": "bash -c 'grep -nE \"(NEXT_PUBLIC_.*SECRET|eval\\(|password\\s*=)\" ${file} && exit 1 || exit 0'" }
    ]
  }
}
```

## Phase Tracking

| Phase | Name | ECC Integration | Status |
|-------|------|-----------------|--------|
| 0 | Planning & Setup | Prompt 0.11 (ECC Setup) | ✅ |
| 1-6 | Build Phases | Skills + Rules | ✅ |
| 7 | Testing & QA | TDD Skill + Agents | ✅ |
| 8 | Security & Automation | Security Skill + Hooks | ✅ |
| 9-13 | Polish Phases | Rules + Patterns | ✅ |
| 14 | Pre-Launch Checklist | Prompts 14.11 + 14.12 | ✅ |
| 15-20 | Post-Launch Phases | Continuous Learning | ✅ |

## Getting Started

1. Clone this repository alongside your project
2. Follow **Phase 0, Prompt 0.11** to set up the ECC agent harness
3. Copy required skills and agents to your `.claude/` directory
4. Activate hooks in `.claude/settings.json`
5. Start with `/planner` to decompose your first milestone
