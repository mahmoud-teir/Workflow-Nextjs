---
name: typescript-hooks
description: TypeScript-specific PostToolUse hook configurations.
origin: ECC
stack: TypeScript, Biome
---

# TypeScript Hooks

## Recommended PostToolUse Hooks for TypeScript Projects

### Format on Save
```json
{
  "matcher": "Edit",
  "hooks": [{
    "type": "command",
    "command": "npx biome format --write \"$TOOL_INPUT_FILE_PATH\" 2>/dev/null || true"
  }]
}
```

### Type Check After Edit
```json
{
  "matcher": "Edit",
  "hooks": [{
    "type": "command",
    "command": "npx tsc --noEmit --pretty 2>&1 | head -20 || true"
  }]
}
```

### Console.log Warning
```json
{
  "matcher": "Edit",
  "hooks": [{
    "type": "command",
    "command": "grep -n 'console\\.log' \"$TOOL_INPUT_FILE_PATH\" 2>/dev/null && echo 'WARNING: console.log detected' || true"
  }]
}
```

## Installation
Add to `.claude/settings.json` or `~/.claude/settings.json` under `hooks.PostToolUse[]`.
