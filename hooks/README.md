# Hooks System

Hooks are automated quality gates that run at lifecycle events during AI coding sessions. They enforce standards without manual intervention.

## Hook Types

| Type | When | Use Case |
|------|------|----------|
| `PreToolUse` | Before a tool runs | Block dangerous operations, validate inputs |
| `PostToolUse` | After a tool runs | Auto-format, type-check, security scan |
| `Stop` | When session ends | Save state, audit logs, cleanup |

## Installation

Copy the desired hook JSON files to your project's Claude settings:

```json
// .claude/settings.json (project-level)
// or ~/.claude/settings.json (global)
{
  "hooks": {
    "PostToolUse": [
      // ... paste hook configs here
    ],
    "Stop": [
      // ... paste hook configs here
    ]
  }
}
```

## Available Hooks

### Post-Edit Hooks
| Hook | File | Purpose |
|------|------|---------|
| Format on Save | `post-edit-format.json` | Auto-format after file edits |
| Type Check | `post-edit-typecheck.json` | Run tsc after TypeScript edits |

### Pre-Edit Hooks
| Hook | File | Purpose |
|------|------|---------|
| Security Scan | `pre-edit-security-scan.json` | Check for secrets before writes |

### Stop Hooks
| Hook | File | Purpose |
|------|------|---------|
| Session Save | `stop-session-save.json` | Save session state to memory file |
| Console Log Audit | `stop-console-log-audit.json` | Scan for leftover console.log |

## Hook Execution Model

```
User Action
    │
    ▼
PreToolUse Hooks ──── Block? ──── Stop execution
    │                               
    ▼ (pass)
Tool Executes
    │
    ▼
PostToolUse Hooks ──── Log warnings, auto-fix
    │
    ▼
Continue
```

## Best Practices

1. **Keep hooks fast** — Under 5 seconds execution time
2. **Don't block unnecessarily** — Use warnings, not blocks, for non-critical issues
3. **Scope narrowly** — Use matchers to target specific tools (Edit, Write, Bash)
4. **Test hooks locally** — Verify hook behavior before adding to settings
5. **Log, don't crash** — Hooks should fail gracefully
