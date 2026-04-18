---
name: continuous-learning
description: Self-improving knowledge capture. Extracts patterns, gotchas, and improvements discovered during development into persistent knowledge.
origin: ECC
---

# Continuous Learning Skill

Captures development learnings and converts them into reusable knowledge.

## When to Activate

- After solving a difficult bug
- After discovering a useful pattern
- After finding a framework gotcha or limitation
- At the end of each development session
- After a significant refactor or migration

## What to Capture

### 1. Bug Patterns
When solving non-trivial bugs, record:
```markdown
## Bug: [Brief Description]
**Symptoms**: [What the user saw]
**Root Cause**: [Why it happened]
**Fix**: [What resolved it]
**Prevention**: [How to avoid in future]
**Files**: [Key files involved]
```

### 2. Architecture Patterns
When discovering or establishing patterns:
```markdown
## Pattern: [Name]
**Context**: [When to use this pattern]
**Implementation**: [Code example]
**Alternatives**: [What else was considered]
**Trade-offs**: [Pros and cons]
```

### 3. Framework Gotchas
When hitting framework-specific issues:
```markdown
## Gotcha: [Framework] — [Issue]
**Version**: [Framework version]
**Problem**: [What went wrong]
**Solution**: [How to work around it]
**Documentation**: [Link to relevant docs or issues]
```

### 4. Performance Learnings
When discovering optimization opportunities:
```markdown
## Optimization: [Area]
**Before**: [Metric before]
**After**: [Metric after]
**Change**: [What was done]
**Measurement**: [How measured]
```

## Learning Log Format

Maintain a `learnings.md` file in the project:

```markdown
# Project Learnings

## Next.js App Router

### 2024-01-15: Async Params in Next.js 15
Dynamic route params are now `Promise` objects. Must `await` them:
```typescript
// Next.js 15+ — params is a Promise
const { id } = await params
```

### 2024-01-16: Server Action Revalidation
`revalidatePath` only works with exact paths, not patterns.
Use `revalidateTag` for broader cache invalidation:
```typescript
revalidateTag('posts') // Invalidates all queries tagged 'posts'
```

## Prisma

### 2024-01-17: Unique Constraint Errors
Prisma throws `PrismaClientKnownRequestError` with code `P2002` for unique violations.
Handle gracefully:
```typescript
try {
  await db.users.create({ data })
} catch (error) {
  if (error instanceof Prisma.PrismaClientKnownRequestError && error.code === 'P2002') {
    return { error: 'Email already registered' }
  }
  throw error
}
```
```

## Session-End Extraction

At the end of each session, review work done and extract:
1. **New patterns** established during the session
2. **Bugs resolved** and their root causes
3. **Gotchas discovered** with workarounds
4. **Performance improvements** made and measured
5. **Decisions made** with reasoning

## Integration with Skills

When a learning becomes general enough, promote it:

1. **Specific learning → Project learnings.md** (immediate)
2. **Recurring pattern → Existing skill update** (after 3+ occurrences)
3. **New domain → New skill** (when knowledge warrants a standalone skill)

## Knowledge Hierarchy

```
├── Session notes (ephemeral, per-session)
│   └── Promoted to...
├── Project learnings (persistent, per-project)
│   └── Promoted to...
├── Skill updates (reusable, cross-project)
│   └── Promoted to...
└── Rules (mandatory, always loaded)
```

---

**Remember**: Every bug you solve twice is a learning you failed to capture. Write it down once, benefit forever.
