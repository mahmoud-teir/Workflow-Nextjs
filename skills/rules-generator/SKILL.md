---
name: rules-generator
description: Generates a consolidated .cursorrules or .claude/settings.json file by merging all rules from the rules/ directory into a single, project-ready configuration file.
---

# Rules Generator Skill

Generates a ready-to-use rules file for AI coding assistants (Cursor, Claude Code) by consolidating all rules from the `rules/` directory.

## When to Use

- Starting a new project and need a `.cursorrules` file
- Updating rules after adding new constraints
- Generating `.claude/settings.json` for Claude Code projects

## Process

### 1. Scan All Rules
Read every `.md` file in:
- `rules/common/` — Language-agnostic rules
- `rules/typescript/` — TypeScript-specific rules
- `rules/security/` — Security constraints (if exists)

### 2. Extract Rule Statements
From each file, extract the actionable rules (bullet points, numbered lists, code constraints). Ignore explanatory prose.

### 3. Generate Output

#### For Cursor (`.cursorrules`)
Create a single markdown file at the project root:

```markdown
# Project Rules

## Code Quality
- [extracted from rules/common/]

## TypeScript
- [extracted from rules/typescript/]

## Security
- [extracted from rules/security/]

## Project-Specific
- Use Next.js App Router (no Pages Router)
- Use Tailwind CSS v4 for styling
- Use Biome for formatting and linting
- Use Prisma for database access
- All Server Actions must validate with Zod
- All Server Actions must check authorization first
```

#### For Claude Code (`.claude/settings.json`)
```json
{
  "permissions": {
    "allow": [
      "Bash(npm run *)",
      "Bash(npx prisma *)",
      "Bash(pnpm *)"
    ],
    "deny": [
      "Bash(rm -rf *)",
      "Bash(sudo *)"
    ]
  },
  "hooks": {}
}
```

### 4. Inject Hooks (Optional)
If the user wants automated hooks, copy the relevant hook JSON from `hooks/` into the generated settings file.

## Verification
- [ ] Generated file is valid (no syntax errors)
- [ ] All rules from `rules/` are represented
- [ ] No duplicate or conflicting rules
- [ ] File is placed at project root
