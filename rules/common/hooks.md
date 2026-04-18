---
name: hooks
description: Hook types reference and configuration guidance.
origin: ECC
---

# Hooks Reference

## Hook Types

### PreToolUse
Runs BEFORE a tool executes. Can block execution.
```json
{
  "matcher": "Write",
  "hooks": [{ "type": "command", "command": "..." }]
}
```

### PostToolUse
Runs AFTER a tool executes. Cannot block, but can report.
```json
{
  "matcher": "Edit",
  "hooks": [{ "type": "command", "command": "..." }]
}
```

### Stop
Runs when the session ends. Used for cleanup and persistence.
```json
{
  "matcher": "",
  "hooks": [{ "type": "command", "command": "..." }]
}
```

## Matchers
- `"Edit"` — Matches file edit operations
- `"Write"` — Matches file write operations
- `"Bash"` — Matches shell command execution
- `""` — Matches all operations (use for Stop)

## Best Practices
1. Keep hooks under 5 seconds execution time
2. Use `|| true` to prevent hook failures from blocking work
3. Limit output with `| head -N` to avoid context flooding
4. Test hooks locally before adding to settings
5. Use project-level `.claude/settings.json` for project-specific hooks
6. Use `~/.claude/settings.json` for global hooks
