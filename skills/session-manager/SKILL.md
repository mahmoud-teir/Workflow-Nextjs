---
name: session-manager
description: Manages session persistence between AI coding sessions. Save/resume patterns, memory file management, and context handoff protocol.
origin: ECC
---

# Session Manager Skill

Provides patterns for maintaining context and progress across multiple AI coding sessions.

## When to Activate

- At the start of any new session (RESUME)
- Before ending a session (SAVE)
- When switching between projects
- When context needs to be transferred to a different agent

## Session Lifecycle

### Starting a Session (RESUME)
When starting a new session, always:

1. **Read project state** — Check for existing memory/state files
2. **Check Git status** — `git status`, `git log --oneline -5`
3. **Read task list** — Check for in-progress tasks
4. **Load relevant skills** — Based on current work phase

```
Session Start Checklist:
- [ ] Read CLAUDE.md (if exists)
- [ ] Read WORKING-CONTEXT.md or session state file (if exists)
- [ ] Run `git status` and `git log --oneline -5`
- [ ] Check for in-progress tasks
- [ ] Load skill appropriate for current phase
```

### During a Session (TRACK)
Maintain awareness of:
- Current phase and task
- Files modified since session start
- Decisions made and alternatives considered
- Blocked items and next steps
- Tests that need to be run

### Ending a Session (SAVE)
Before ending, always save state:

```markdown
# Session State — [Date]

## Completed
- [x] Implemented user authentication (app/api/auth/)
- [x] Added login/register pages
- [x] Connected to database

## In Progress
- [/] Dashboard page — basic layout done, needs charts

## Next Steps
- [ ] Add analytics charts to dashboard
- [ ] Implement user settings page
- [ ] Write E2E tests for auth flow

## Key Decisions
- Chose NextAuth.js over Clerk for auth (more control over UI)
- Using Prisma with PostgreSQL (Neon serverless)

## Modified Files
- app/api/auth/[...nextauth]/route.ts
- app/(auth)/login/page.tsx
- app/(auth)/register/page.tsx
- lib/auth.ts
- prisma/schema.prisma

## Environment State
- Branch: feat/user-auth
- Last commit: abc1234 "feat: add auth pages"
- Build status: passing
- Test coverage: 72%
```

## Memory File Patterns

### Working Context File
Save to project root or `.claude/` directory:

```
.claude/
├── memory/
│   ├── session-state.md     # Current session state
│   ├── decisions.md         # Architecture decisions log
│   └── learnings.md         # Patterns discovered
└── settings.json            # Hook configuration
```

### Decision Log Format
```markdown
# Architecture Decisions

## 2024-01-15: Auth Provider
**Decision**: NextAuth.js
**Alternatives**: Clerk, Supabase Auth, Lucia
**Reasoning**: Need custom UI, self-hosted, full control over session management
**Status**: Implemented

## 2024-01-16: Database
**Decision**: Neon PostgreSQL + Prisma
**Alternatives**: Supabase, PlanetScale, Turso
**Reasoning**: Serverless scales to zero, branching for dev, Prisma for type safety
**Status**: Implemented
```

## Context Handoff Protocol

When another agent or session needs to pick up work:

### Minimum Context Package
1. **Current branch and commit** — Where we are in Git
2. **Active task and progress** — What we're doing
3. **Blocked items** — What's preventing progress
4. **Key file paths** — Most important files for context
5. **Test status** — What's passing/failing

### Handoff Format
```
## Context Handoff

### Current State
Branch: `feat/dashboard`
Last Commit: `abc1234 — "feat: add chart components"`
Phase: Phase 5 (Core Feature Development)

### Active Task
Building analytics dashboard with real-time charts.
Chart component is done. Need to:
1. Connect to analytics API
2. Add date range filter
3. Write tests

### Key Files
- `app/(dashboard)/analytics/page.tsx` — Main page
- `components/charts/LineChart.tsx` — Chart component
- `lib/analytics.ts` — Data fetching layer

### Blockers
None currently.
```

## Stop Hook Integration

Configure a Stop hook to automatically save session state:

```json
{
  "hooks": {
    "Stop": [
      {
        "matcher": "",
        "hooks": [{
          "type": "command",
          "command": "echo '## Session ended at '$(date) >> .claude/memory/session-state.md"
        }]
      }
    ]
  }
}
```

---

**Remember**: The best session is one that can be resumed by any agent without asking "where were we?" Save state like you're handing off to a stranger.
