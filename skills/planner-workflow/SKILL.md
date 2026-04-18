---
name: planner-workflow
description: Use this skill when starting a new feature, planning a refactor, or designing architecture. Produces structured implementation plans with phases, risks, and testing strategy.
origin: ECC
---

# Planner Workflow Skill

Produces comprehensive, actionable implementation plans before any code is written.

## When to Activate

- Starting a new feature or project
- Planning a refactor or migration
- Designing system architecture
- Breaking down complex requirements
- When the task spans 3+ files

## Planning Process

### 1. Requirements Analysis
- Understand the feature request completely
- Identify success criteria
- List assumptions and constraints
- Flag ambiguities for user clarification

### 2. Search First
Before planning from scratch:
- **Search the codebase** — Look for existing patterns, similar implementations
- **Search documentation** — Check official docs, `llms.txt` endpoints
- **Search package registries** — Prefer battle-tested libraries over hand-rolled code
- **Search for skeleton projects** — Look for 80%+ solutions that can be adapted

### 3. Architecture Review
- Analyze existing codebase structure
- Identify affected components
- Review similar implementations in the project
- Consider reusable patterns from existing code

### 4. Step Breakdown
Create detailed steps with:
- Clear, specific actions
- Exact file paths and locations
- Dependencies between steps
- Estimated complexity (S/M/L)
- Potential risks (Low/Medium/High)

### 5. Implementation Order
- Prioritize by dependencies (database → API → UI)
- Group related changes
- Minimize context switching
- Enable incremental testing at each phase

## Plan Format

```markdown
# Implementation Plan: [Feature Name]

## Overview
[2-3 sentence summary of what will be built and why]

## Requirements
- [Requirement 1]
- [Requirement 2]

## Architecture Changes
- [New table/model]: [file path and description]
- [New API route]: [file path and description]
- [New component]: [file path and description]

## Implementation Steps

### Phase 1: [Phase Name] (X files)
1. **[Step Name]** (File: `path/to/file.ts`)
   - Action: Specific action to take
   - Why: Reason for this step
   - Dependencies: None / Requires step X
   - Risk: Low/Medium/High

### Phase 2: [Phase Name]
...

## Testing Strategy
- Unit tests: [specific files and functions to test]
- Integration tests: [API flows to test]
- E2E tests: [user journeys to test]

## Risks & Mitigations
- **Risk**: [Description]
  - Mitigation: [How to address]

## Success Criteria
- [ ] Criterion 1
- [ ] Criterion 2
- [ ] All tests pass with 80%+ coverage
```

## Sizing and Phasing

When the feature is large, break into independently deliverable phases:

- **Phase 1: Minimum Viable** — Smallest slice that provides value
- **Phase 2: Core Experience** — Complete happy path
- **Phase 3: Edge Cases** — Error handling, edge cases, polish
- **Phase 4: Optimization** — Performance, monitoring, analytics

Each phase should be mergeable independently. Avoid plans that require all phases before anything works.

## Red Flags to Check

- Large functions (>50 lines)
- Deep nesting (>4 levels)
- Plans with no testing strategy
- Steps without clear file paths
- Phases that cannot be delivered independently
- Missing error handling
- Hardcoded values
- No consideration of authentication/authorization
- Missing loading and error states

## Agent Support

- **planner** agent — Generates implementation plans (uses Opus for deep reasoning)
- Use read-only tools (Read, Grep, Glob) during planning — no writes

---

**Remember**: A great plan is specific, actionable, and considers both the happy path and edge cases. The best plans enable confident, incremental implementation.
