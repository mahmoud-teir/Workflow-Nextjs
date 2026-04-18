---
name: performance
description: Model selection strategy and context window management.
origin: ECC
---

# Performance & Model Selection

## Model Selection Strategy

| Task | Recommended Model | Why |
|------|-------------------|-----|
| Planning & Architecture | Opus | Deep reasoning, complex analysis |
| Security Review | Opus | Can't afford to miss vulnerabilities |
| Code Implementation | Sonnet | Best coding model, fast iteration |
| Code Review | Sonnet | Good code understanding, efficient |
| Build Error Resolution | Sonnet | Practical problem-solving |
| SEO Audit | Haiku | Lightweight, pattern-matching |
| Simple Refactors | Haiku | Deterministic, cost-effective |

## Context Window Management

### Keep CLAUDE.md Lean
- Under 500 lines
- Only essential rules and patterns
- Link to skills/docs for details

### Lazy Load Skills
- Don't load all skills at session start
- Load based on current task keywords
- Unload when switching phases

### Use Extended Thinking
- For complex architecture decisions
- For debugging multi-file issues
- For security analysis with many variables

### Compact Strategically
- At phase transitions
- After completing milestones
- Before context shifts
- Never mid-implementation

## Cost Awareness
- Flag workflows that escalate to higher-cost models without clear need
- Default to lower-cost tiers for deterministic refactors
- Use Haiku for boilerplate and pattern-matching tasks
