---
name: strategic-compact
description: Suggests manual context compaction at logical intervals to preserve context through task phases rather than arbitrary auto-compaction.
origin: ECC
---

# Strategic Compact Skill

Suggests manual context compaction at strategic workflow boundaries rather than relying on arbitrary auto-compaction.

## When to Activate

- Running long sessions approaching context limits (200K+ tokens)
- Working on multi-phase tasks (research → plan → implement → test)
- Switching between unrelated tasks within the same session
- After completing a major milestone and starting new work
- When responses slow down or become less coherent (context pressure)

## Why Strategic Compaction?

Auto-compaction triggers at arbitrary points:
- Often mid-task, losing important context
- No awareness of logical task boundaries
- Can interrupt multi-step operations

Strategic compaction at logical boundaries:
- **After exploration, before execution** — Compact research context, keep implementation plan
- **After completing a milestone** — Fresh start for next phase
- **Before major context shifts** — Clear exploration context before different task

## Compaction Decision Guide

| Phase Transition | Compact? | Why |
|-----------------|----------|-----|
| Research → Planning | ✅ Yes | Research context is bulky; plan is the distilled output |
| Planning → Implementation | ✅ Yes | Plan is in a file or task list; free up context for code |
| Implementation → Testing | ⚠️ Maybe | Keep if tests reference recent code; compact if switching focus |
| Debugging → Next feature | ✅ Yes | Debug traces pollute context for unrelated work |
| Mid-implementation | ❌ No | Losing variable names, file paths, and partial state is costly |
| After a failed approach | ✅ Yes | Clear dead-end reasoning before trying a new approach |
| Phase 0 → Phase 1 | ✅ Yes | Planning docs are saved; start fresh for scaffolding |
| Phase 7 → Phase 8 | ⚠️ Maybe | Keep if security review references test patterns |
| Phase 13 → Phase 14 | ✅ Yes | Fresh context for final pre-launch verification |

## What Survives Compaction

| Persists | Lost |
|----------|------|
| CLAUDE.md / project rules | Intermediate reasoning and analysis |
| Task lists and todo items | File contents previously read |
| Memory files on disk | Multi-step conversation context |
| Git state (commits, branches) | Tool call history and counts |
| Files on disk (code, docs) | Nuanced verbal preferences |

## Best Practices

1. **Compact after planning** — Once plan is finalized in a file, compact to start fresh
2. **Compact after debugging** — Clear error-resolution context before continuing
3. **Don't compact mid-implementation** — Preserve context for related changes
4. **Write before compacting** — Save important context to files before compacting
5. **Use compact with a summary** — Add context: "Focus on implementing auth middleware next"

## Token Optimization Patterns

### Trigger-Table Lazy Loading
Instead of loading all skills at session start, use a trigger table:

| Trigger Keywords | Skill | Load When |
|-----------------|-------|-----------| 
| "test", "tdd", "coverage" | tdd-workflow | User mentions testing |
| "security", "auth", "xss" | security-review | Security-related work |
| "deploy", "ci/cd", "launch" | deployment-patterns | Deployment context |
| "verify", "check", "audit" | verification-loop | Quality gate needed |
| "plan", "design", "architect" | planner-workflow | Planning phase |

### Context Composition Awareness
Monitor what's consuming your context window:
- **CLAUDE.md files** — Always loaded, keep lean (<500 lines)
- **Loaded skills** — Each skill adds 1-5K tokens
- **Conversation history** — Grows with each exchange
- **Tool results** — File reads, search results add bulk

### Deduplication
Common sources of duplicate context:
- Same rules in both user and project scope
- Skills that repeat CLAUDE.md instructions
- Multiple skills covering overlapping domains

---

**Remember**: Strategic compaction is about *when* to reset, not *whether* to reset. The right timing preserves the context that matters and discards what doesn't.
