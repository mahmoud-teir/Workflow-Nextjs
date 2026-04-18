# Contributing Guide

> How to contribute to the Next.js Development Workflow Library with ECC Agent Harness.

## Overview

This project uses the **Everything Claude Code (ECC)** architecture. Contributions fall into these categories:

| Category | Directory | Format | Review Agent |
|----------|-----------|--------|-------------|
| Skills | `skills/<name>/SKILL.md` | Markdown + YAML frontmatter | `/code-reviewer` |
| Agents | `agents/<name>.md` | Markdown + YAML frontmatter | `/code-reviewer` |
| Hooks | `hooks/<name>.json` | JSON | `/security-reviewer` |
| Rules | `rules/<domain>/<name>.md` | Markdown + YAML frontmatter | `/code-reviewer` |
| Phases | `phases/PHASE_<N>_*.md` | Markdown | `/code-reviewer` |
| Docs | `docs/<name>.md` | Markdown | — |

---

## Adding a New Skill

### 1. Create the Directory and File

```bash
mkdir -p skills/my-new-skill
touch skills/my-new-skill/SKILL.md
```

### 2. Use the Required YAML Frontmatter

```yaml
---
name: my-new-skill
version: 1.0.0
description: >
  One-line description of what the skill does.
triggers:
  - keyword-or-command-that-activates-this-skill
category: development | testing | security | deployment | meta
requires:
  - list-of-tool-capabilities-needed (e.g., Bash, Read, Write)
---
```

### 3. Structure the Skill Content

```markdown
# Skill Name

## Purpose
What problem does this skill solve?

## Workflow
Step-by-step instructions the agent must follow.

## Patterns
Code patterns with copy-pasteable examples.

## Anti-Patterns
What to avoid.

## Verification
How to confirm the skill was executed correctly.
```

### 4. Validate

```bash
# Check YAML frontmatter parses correctly
head -20 skills/my-new-skill/SKILL.md | yq -f extract '.'
```

---

## Adding a New Agent

### 1. Create the File

```bash
touch agents/my-agent.md
```

### 2. Use the Required YAML Frontmatter

```yaml
---
name: my-agent
version: 1.0.0
description: >
  What this agent specializes in.
trigger: /my-agent
model: claude-sonnet-4-20250514
allowed_tools:
  - Read
  - Bash(npm run test)
  - Bash(npx biome check)
skills:
  - skill-name-1
  - skill-name-2
---
```

### 3. Required Fields

| Field | Required | Description |
|-------|----------|-------------|
| `name` | ✅ | Unique agent identifier |
| `version` | ✅ | Semantic version |
| `description` | ✅ | Purpose statement |
| `trigger` | ✅ | Slash command to invoke (e.g., `/my-agent`) |
| `model` | ✅ | LLM model to use |
| `allowed_tools` | ✅ | Scoped tool access list |
| `skills` | ✅ | Skills this agent consumes |

### 4. Structure the Agent Content

```markdown
# Agent Name

## Role
Precise definition of what this agent does.

## Instructions
Detailed instructions for the LLM when operating as this agent.

## Output Format
Expected output structure.

## Constraints
What the agent must NOT do.
```

---

## Adding a New Hook

### 1. Create the File

```bash
touch hooks/my-hook.json
```

### 2. Use the Required JSON Structure

```json
{
  "hook_name": "my-hook",
  "description": "What this hook does",
  "event": "PreToolUse | PostToolUse | Stop",
  "matcher": "Tool pattern to match (e.g., Edit|Write)",
  "command": "bash -c 'command to execute'",
  "timeout_ms": 10000,
  "on_failure": "warn | block",
  "variables": {
    "${file}": "The file being edited"
  }
}
```

### 3. Hook Events

| Event | When It Fires | Use Case |
|-------|--------------|----------|
| `PreToolUse` | Before the agent writes/edits a file | Security scanning, validation |
| `PostToolUse` | After the agent writes/edits a file | Formatting, type checking |
| `Stop` | When the agent session ends | Session state persistence, auditing |

### 4. Security Review

All hooks that execute shell commands **must** be reviewed by the security-reviewer agent before merging:

```
/security-reviewer -- Review the hook at hooks/my-hook.json for command injection risks
```

---

## Adding a New Rule

### 1. Choose the Domain

- `rules/common/` — Language-agnostic rules (git, testing, security, performance)
- `rules/typescript/` — TypeScript/Next.js-specific rules

### 2. Create the File

```bash
touch rules/common/my-rule.md
# or
touch rules/typescript/my-rule.md
```

### 3. Use the Required YAML Frontmatter

```yaml
---
name: my-rule
category: common | typescript
severity: error | warning | info
description: >
  What this rule enforces.
---
```

### 4. Structure the Rule Content

```markdown
# Rule Name

## Always
- Things that MUST be done

## Never
- Things that MUST NOT be done

## Examples
### ✅ Correct
```code
// Good pattern
```

### ❌ Incorrect
```code
// Bad pattern
```
```

---

## Enhancing a Phase

When adding ECC integration to an existing phase:

### 1. Add the ECC Integration Section

Place this section **after** the last prompt and **before** the `📎 Related Phases` section:

```markdown
---

### 🤖 ECC Agent Harness Integration

This phase is directly supported by the ECC agent harness:

#### Recommended Agent Invocations

| Task | Agent Command | What It Does |
|---|---|---|
| ... | `/agent-name` | Description |

#### Automated Hooks Active in This Phase

| Hook | Trigger | Effect |
|---|---|---|
| `hook-name` | When | What happens |

**📚 ECC Skill References:**
- [`skill-name`](../skills/skill-name/SKILL.md) — Description
```

### 2. Add Skill Reference Tables

Link skills and agents using relative paths from the `phases/` directory.

---

## Pull Request Checklist

Before submitting a PR, verify:

- [ ] **YAML Frontmatter**: All new skills, agents, and rules have valid YAML frontmatter
- [ ] **Required Fields**: All required fields are populated (no empty strings)
- [ ] **Relative Links**: All cross-references use correct relative paths
- [ ] **No Broken Links**: All linked files exist in the repository
- [ ] **Consistent Naming**: File names use kebab-case
- [ ] **Security Review**: Hook files have been reviewed for command injection risks
- [ ] **Version Bumped**: If modifying an existing skill/agent, increment the version

## Code of Conduct

- Be respectful and constructive in reviews
- Focus on patterns, not preferences
- Back up suggestions with evidence (benchmarks, security advisories, docs)
- Test your contributions before submitting
