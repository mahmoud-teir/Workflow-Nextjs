---
name: testing
description: Testing standards and requirements.
origin: ECC
---

# Testing Rules

## Requirements

- **80% minimum coverage** — Unit + Integration + E2E combined
- **TDD workflow** — Tests BEFORE implementation (RED → GREEN → REFACTOR)
- **AAA pattern** — Arrange, Act, Assert in every test
- **No skipped tests** — Every `it()` must be active
- **Descriptive naming** — Test names describe behavior, not implementation

## Test Stack

| Type | Tool | Target |
|------|------|--------|
| Unit | Vitest + Testing Library | Functions, components |
| Integration | Vitest + MSW | API routes, Server Actions |
| E2E | Playwright | User flows |
| Accessibility | @axe-core/playwright | WCAG compliance |

## Test Naming Convention

```typescript
// ✅ Good — describes behavior
it('returns empty array when no products match the category', ...)
it('redirects unauthenticated users to login page', ...)

// ❌ Bad — describes implementation
it('calls findMany with category filter', ...)
it('checks session object', ...)
```

## Git Checkpoint Commits

```
test: add failing tests for [feature]     # RED
feat: implement [feature]                  # GREEN
refactor: clean up [feature]               # REFACTOR
```
